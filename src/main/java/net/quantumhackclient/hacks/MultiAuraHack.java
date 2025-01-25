/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.settings.AttackSpeedSliderSetting;
import net.quantumhackclient.settings.PauseAttackOnContainersSetting;
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;
import net.quantumhackclient.settings.SwingHandSetting;
import net.quantumhackclient.settings.SwingHandSetting.SwingHand;
import net.quantumhackclient.settings.filterlists.EntityFilterList;
import net.quantumhackclient.util.EntityUtils;
import net.quantumhackclient.util.RotationUtils;

@SearchTags({"multi aura", "ForceField", "force field"})
public final class MultiAuraHack extends Hack implements UpdateListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final AttackSpeedSliderSetting speed =
		new AttackSpeedSliderSetting();
	
	private final SliderSetting fov =
		new SliderSetting("FOV", 360, 30, 360, 10, ValueDisplay.DEGREES);
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericCombatDescription(this), SwingHand.CLIENT);
	
	private final PauseAttackOnContainersSetting pauseOnContainers =
		new PauseAttackOnContainersSetting(false);
	
	private final EntityFilterList entityFilters =
		EntityFilterList.genericCombat();
	
	public MultiAuraHack()
	{
		super("MultiAura");
		setCategory(Category.COMBAT);
		
		addSetting(range);
		addSetting(speed);
		addSetting(fov);
		addSetting(swingHand);
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
		QUANTUM_HACK.getHax().fightBotHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraLegitHack.setEnabled(false);
		QUANTUM_HACK.getHax().killauraHack.setEnabled(false);
		QUANTUM_HACK.getHax().protectHack.setEnabled(false);
		QUANTUM_HACK.getHax().tpAuraHack.setEnabled(false);
		QUANTUM_HACK.getHax().triggerBotHack.setEnabled(false);
		
		speed.resetTimer();
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		speed.updateTimer();
		if(!speed.isTimeToAttack())
			return;
		
		if(pauseOnContainers.shouldPause())
			return;
		
		// get entities
		Stream<Entity> stream = EntityUtils.getAttackableEntities();
		double rangeSq = Math.pow(range.getValue(), 2);
		stream = stream.filter(e -> MC.player.squaredDistanceTo(e) <= rangeSq);
		
		if(fov.getValue() < 360.0)
			stream = stream.filter(e -> RotationUtils.getAngleToLookVec(
				e.getBoundingBox().getCenter()) <= fov.getValue() / 2.0);
		
		stream = entityFilters.applyTo(stream);
		
		ArrayList<Entity> entities =
			stream.collect(Collectors.toCollection(ArrayList::new));
		if(entities.isEmpty())
			return;
		
		QUANTUM_HACK.getHax().autoSwordHack.setSlot(entities.get(0));
		
		// attack entities
		for(Entity entity : entities)
		{
			RotationUtils
				.getNeededRotations(entity.getBoundingBox().getCenter())
				.sendPlayerLookPacket();
			
			MC.interactionManager.attackEntity(MC.player, entity);
		}
		
		swingHand.swing(Hand.MAIN_HAND);
		speed.resetTimer();
	}
}
