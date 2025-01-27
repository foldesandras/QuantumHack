/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.quantumhackclient.Category;
import net.quantumhackclient.ai.PathFinder;
import net.quantumhackclient.ai.PathPos;
import net.quantumhackclient.ai.PathProcessor;
import net.quantumhackclient.commands.PathCmd;
import net.quantumhackclient.events.RenderListener;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.DontSaveState;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.settings.AttackSpeedSliderSetting;
import net.quantumhackclient.settings.CheckboxSetting;
import net.quantumhackclient.settings.PauseAttackOnContainersSetting;
import net.quantumhackclient.settings.SwingHandSetting;
import net.quantumhackclient.settings.SwingHandSetting.SwingHand;
import net.quantumhackclient.settings.filterlists.EntityFilterList;
import net.quantumhackclient.settings.filters.*;
import net.quantumhackclient.util.EntityUtils;
import net.quantumhackclient.util.FakePlayerEntity;

@DontSaveState
public final class ProtectHack extends Hack
	implements UpdateListener, RenderListener
{
	private final AttackSpeedSliderSetting speed =
		new AttackSpeedSliderSetting();
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericCombatDescription(this), SwingHand.CLIENT);
	
	private final CheckboxSetting useAi =
		new CheckboxSetting("Use AI (experimental)", false);
	
	private final PauseAttackOnContainersSetting pauseOnContainers =
		new PauseAttackOnContainersSetting(true);
	
	private final EntityFilterList entityFilters =
		new EntityFilterList(FilterPlayersSetting.genericCombat(false),
			FilterSleepingSetting.genericCombat(false),
			FilterFlyingSetting.genericCombat(0),
			FilterHostileSetting.genericCombat(false),
			FilterNeutralSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterPassiveSetting.genericCombat(false),
			FilterPassiveWaterSetting.genericCombat(false),
			FilterBabiesSetting.genericCombat(false),
			FilterBatsSetting.genericCombat(false),
			FilterSlimesSetting.genericCombat(false),
			FilterPetsSetting.genericCombat(false),
			FilterVillagersSetting.genericCombat(false),
			FilterZombieVillagersSetting.genericCombat(false),
			FilterGolemsSetting.genericCombat(false),
			FilterPiglinsSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterZombiePiglinsSetting
				.genericCombat(FilterZombiePiglinsSetting.Mode.OFF),
			FilterEndermenSetting
				.genericCombat(AttackDetectingEntityFilter.Mode.OFF),
			FilterShulkersSetting.genericCombat(false),
			FilterAllaysSetting.genericCombat(false),
			FilterInvisibleSetting.genericCombat(false),
			FilterNamedSetting.genericCombat(false),
			FilterShulkerBulletSetting.genericCombat(false),
			FilterArmorStandsSetting.genericCombat(false),
			FilterCrystalsSetting.genericCombat(true));
	
	private EntityPathFinder pathFinder;
	private PathProcessor processor;
	private int ticksProcessing;
	
	private Entity friend;
	private Entity enemy;
	
	private double distanceF = 2;
	private double distanceE = 3;
	
	public ProtectHack()
	{
		super("Protect");
		
		setCategory(Category.COMBAT);
		addSetting(speed);
		addSetting(swingHand);
		addSetting(useAi);
		addSetting(pauseOnContainers);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	public String getRenderName()
	{
		if(friend != null)
			return "Protecting " + friend.getName().getString();
		return "Protect";
	}
	
	@Override
	protected void onEnable()
	{
		QUANTUM_HACK.getHax().followHack.setEnabled(false);
		QUANTUM_HACK.getHax().tunnellerHack.setEnabled(false);
		
		// disable other killauras
		QUANTUM_HACK.getHax().aimAssistHack.setEnabled(false);
		QUANTUM_HACK.getHax().clickAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().crystalAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().fightBotHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraLegitHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraHack.setEnabled(false);
		QUANTUM_HACK.getHax().multiAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().triggerBotHack.setEnabled(false);
		QUANTUM_HACK.getHax().tpAuraHack.setEnabled(false);
		
		// set friend
		if(friend == null)
		{
			Stream<Entity> stream = StreamSupport
				.stream(MC.world.getEntities().spliterator(), true)
				.filter(LivingEntity.class::isInstance)
				.filter(
					e -> !e.isRemoved() && ((LivingEntity)e).getHealth() > 0)
				.filter(e -> e != MC.player)
				.filter(e -> !(e instanceof FakePlayerEntity));
			friend = stream
				.min(Comparator
					.comparingDouble(e -> MC.player.squaredDistanceTo(e)))
				.orElse(null);
		}
		
		pathFinder = new EntityPathFinder(friend, distanceF);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		pathFinder = null;
		processor = null;
		ticksProcessing = 0;
		PathProcessor.releaseControls();
		
		enemy = null;
		
		if(friend != null)
		{
			MC.options.forwardKey.setPressed(false);
			friend = null;
		}
	}
	
	@Override
	public void onUpdate()
	{
		speed.updateTimer();
		
		if(pauseOnContainers.shouldPause())
			return;
		
		// check if player died, friend died or disappeared
		if(friend == null || friend.isRemoved()
			|| !(friend instanceof LivingEntity)
			|| ((LivingEntity)friend).getHealth() <= 0
			|| MC.player.getHealth() <= 0)
		{
			friend = null;
			enemy = null;
			setEnabled(false);
			return;
		}
		
		// set enemy
		Stream<Entity> stream = EntityUtils.getAttackableEntities()
			.filter(e -> MC.player.squaredDistanceTo(e) <= 36)
			.filter(e -> e != friend);
		
		stream = entityFilters.applyTo(stream);
		
		enemy = stream
			.min(
				Comparator.comparingDouble(e -> MC.player.squaredDistanceTo(e)))
			.orElse(null);
		
		Entity target =
			enemy == null || MC.player.squaredDistanceTo(friend) >= 24 * 24
				? friend : enemy;
		
		double distance = target == enemy ? distanceE : distanceF;
		
		if(useAi.isChecked())
		{
			// reset pathfinder
			if((processor == null || processor.isDone() || ticksProcessing >= 10
				|| !pathFinder.isPathStillValid(processor.getIndex()))
				&& (pathFinder.isDone() || pathFinder.isFailed()))
			{
				pathFinder = new EntityPathFinder(target, distance);
				processor = null;
				ticksProcessing = 0;
			}
			
			// find path
			if(!pathFinder.isDone() && !pathFinder.isFailed())
			{
				PathProcessor.lockControls();
				QUANTUM_HACK.getRotationFaker()
					.faceVectorClient(target.getBoundingBox().getCenter());
				pathFinder.think();
				pathFinder.formatPath();
				processor = pathFinder.getProcessor();
			}
			
			// process path
			if(!processor.isDone())
			{
				processor.process();
				ticksProcessing++;
			}
		}else
		{
			// jump if necessary
			if(MC.player.horizontalCollision && MC.player.isOnGround())
				MC.player.jump();
			
			// swim up if necessary
			if(MC.player.isTouchingWater() && MC.player.getY() < target.getY())
				MC.player.addVelocity(0, 0.04, 0);
			
			// control height if flying
			if(!MC.player.isOnGround()
				&& (MC.player.getAbilities().flying
					|| QUANTUM_HACK.getHax().flightHack.isEnabled())
				&& MC.player.squaredDistanceTo(target.getX(), MC.player.getY(),
					target.getZ()) <= MC.player.squaredDistanceTo(
						MC.player.getX(), target.getY(), MC.player.getZ()))
			{
				if(MC.player.getY() > target.getY() + 1D)
					MC.options.sneakKey.setPressed(true);
				else if(MC.player.getY() < target.getY() - 1D)
					MC.options.jumpKey.setPressed(true);
			}else
			{
				MC.options.sneakKey.setPressed(false);
				MC.options.jumpKey.setPressed(false);
			}
			
			// follow target
			QUANTUM_HACK.getRotationFaker()
				.faceVectorClient(target.getBoundingBox().getCenter());
			MC.options.forwardKey.setPressed(MC.player.distanceTo(
				target) > (target == friend ? distanceF : distanceE));
		}
		
		if(target == enemy)
		{
			QUANTUM_HACK.getHax().autoSwordHack.setSlot(enemy);
			
			// check cooldown
			if(!speed.isTimeToAttack())
				return;
			
			// attack enemy
			MC.interactionManager.attackEntity(MC.player, enemy);
			swingHand.swing(Hand.MAIN_HAND);
			speed.resetTimer();
		}
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		if(!useAi.isChecked())
			return;
		
		PathCmd pathCmd = QUANTUM_HACK.getCmds().pathCmd;
		pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(),
			pathCmd.isDepthTest());
	}
	
	public void setFriend(Entity friend)
	{
		this.friend = friend;
	}
	
	private class EntityPathFinder extends PathFinder
	{
		private final Entity entity;
		private double distanceSq;
		
		public EntityPathFinder(Entity entity, double distance)
		{
			super(BlockPos.ofFloored(entity.getPos()));
			this.entity = entity;
			distanceSq = distance * distance;
			setThinkTime(1);
		}
		
		@Override
		protected boolean checkDone()
		{
			return done =
				entity.squaredDistanceTo(Vec3d.ofCenter(current)) <= distanceSq;
		}
		
		@Override
		public ArrayList<PathPos> formatPath()
		{
			if(!done)
				failed = true;
			
			return super.formatPath();
		}
	}
}