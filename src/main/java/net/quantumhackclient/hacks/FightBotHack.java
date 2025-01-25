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

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
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
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;
import net.quantumhackclient.settings.SwingHandSetting;
import net.quantumhackclient.settings.SwingHandSetting.SwingHand;
import net.quantumhackclient.settings.filterlists.EntityFilterList;
import net.quantumhackclient.util.EntityUtils;

@SearchTags({"fight bot"})
@DontSaveState
public final class FightBotHack extends Hack
	implements UpdateListener, RenderListener
{
	private final SliderSetting range = new SliderSetting("Range",
		"Attack range (like Killaura)", 4.25, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final AttackSpeedSliderSetting speed =
		new AttackSpeedSliderSetting();
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericCombatDescription(this), SwingHand.CLIENT);
	
	private final SliderSetting distance = new SliderSetting("Distance",
		"How closely to follow the target.\n"
			+ "This should be set to a lower value than Range.",
		3, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final CheckboxSetting useAi =
		new CheckboxSetting("Use AI (experimental)", false);
	
	private final PauseAttackOnContainersSetting pauseOnContainers =
		new PauseAttackOnContainersSetting(true);
	
	private final EntityFilterList entityFilters =
		EntityFilterList.genericCombat();
	
	private EntityPathFinder pathFinder;
	private PathProcessor processor;
	private int ticksProcessing;
	
	public FightBotHack()
	{
		super("FightBot");
		
		setCategory(Category.COMBAT);
		addSetting(range);
		addSetting(speed);
		addSetting(swingHand);
		addSetting(distance);
		addSetting(useAi);
		addSetting(pauseOnContainers);
		
		entityFilters.forEach(this::addSetting);
	}
	
	@Override
	protected void onEnable()
	{
		// disable other killauras
		QUANTUM_HACK.getHax().aimAssistHack.setEnabled(false);
		QUANTUM_HACK.getHax().clickAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().crystalAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraLegitHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraHack.setEnabled(false);
		QUANTUM_HACK.getHax().multiAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().protectHack.setEnabled(false);
		QUANTUM_HACK.getHax().triggerBotHack.setEnabled(false);
		QUANTUM_HACK.getHax().tpAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().tunnellerHack.setEnabled(false);
		
		pathFinder = new EntityPathFinder(MC.player);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		// remove listener
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		pathFinder = null;
		processor = null;
		ticksProcessing = 0;
		PathProcessor.releaseControls();
	}
	
	@Override
	public void onUpdate()
	{
		speed.updateTimer();
		
		if(pauseOnContainers.shouldPause())
			return;
		
		// set entity
		Stream<Entity> stream = EntityUtils.getAttackableEntities();
		stream = entityFilters.applyTo(stream);
		
		Entity entity = stream
			.min(
				Comparator.comparingDouble(e -> MC.player.squaredDistanceTo(e)))
			.orElse(null);
		if(entity == null)
			return;
		
		QUANTUM_HACK.getHax().autoSwordHack.setSlot(entity);
		
		if(useAi.isChecked())
		{
			// reset pathfinder
			if((processor == null || processor.isDone() || ticksProcessing >= 10
				|| !pathFinder.isPathStillValid(processor.getIndex()))
				&& (pathFinder.isDone() || pathFinder.isFailed()))
			{
				pathFinder = new EntityPathFinder(entity);
				processor = null;
				ticksProcessing = 0;
			}
			
			// find path
			if(!pathFinder.isDone() && !pathFinder.isFailed())
			{
				PathProcessor.lockControls();
				QUANTUM_HACK.getRotationFaker()
					.faceVectorClient(entity.getBoundingBox().getCenter());
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
			if(MC.player.isTouchingWater() && MC.player.getY() < entity.getY())
				MC.player.addVelocity(0, 0.04, 0);
			
			// control height if flying
			if(!MC.player.isOnGround()
				&& (MC.player.getAbilities().flying
					|| QUANTUM_HACK.getHax().flightHack.isEnabled())
				&& MC.player.squaredDistanceTo(entity.getX(), MC.player.getY(),
					entity.getZ()) <= MC.player.squaredDistanceTo(
						MC.player.getX(), entity.getY(), MC.player.getZ()))
			{
				if(MC.player.getY() > entity.getY() + 1D)
					MC.options.sneakKey.setPressed(true);
				else if(MC.player.getY() < entity.getY() - 1D)
					MC.options.jumpKey.setPressed(true);
			}else
			{
				MC.options.sneakKey.setPressed(false);
				MC.options.jumpKey.setPressed(false);
			}
			
			// follow entity
			MC.options.forwardKey.setPressed(
				MC.player.distanceTo(entity) > distance.getValueF());
			QUANTUM_HACK.getRotationFaker()
				.faceVectorClient(entity.getBoundingBox().getCenter());
		}
		
		// check cooldown
		if(!speed.isTimeToAttack())
			return;
		
		// check range
		if(MC.player.squaredDistanceTo(entity) > Math.pow(range.getValue(), 2))
			return;
		
		// attack entity
		MC.interactionManager.attackEntity(MC.player, entity);
		swingHand.swing(Hand.MAIN_HAND);
		speed.resetTimer();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		PathCmd pathCmd = QUANTUM_HACK.getCmds().pathCmd;
		RenderSystem.setShader(GameRenderer::getPositionProgram);
		pathFinder.renderPath(matrixStack, pathCmd.isDebugMode(),
			pathCmd.isDepthTest());
	}
	
	private class EntityPathFinder extends PathFinder
	{
		private final Entity entity;
		
		public EntityPathFinder(Entity entity)
		{
			super(BlockPos.ofFloored(entity.getPos()));
			this.entity = entity;
			setThinkTime(1);
		}
		
		@Override
		protected boolean checkDone()
		{
			return done =
				entity.squaredDistanceTo(Vec3d.ofCenter(current)) <= Math
					.pow(distance.getValue(), 2);
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
