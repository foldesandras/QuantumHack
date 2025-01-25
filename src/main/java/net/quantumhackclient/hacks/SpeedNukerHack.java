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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.LeftClickListener;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.DontSaveState;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.hacks.nukers.CommonNukerSettings;
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;
import net.quantumhackclient.settings.SwingHandSetting;
import net.quantumhackclient.settings.SwingHandSetting.SwingHand;
import net.quantumhackclient.util.BlockBreaker;
import net.quantumhackclient.util.BlockUtils;
import net.quantumhackclient.util.RotationUtils;

@SearchTags({"speed nuker", "FastNuker", "fast nuker"})
@DontSaveState
public final class SpeedNukerHack extends Hack implements UpdateListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	
	private final CommonNukerSettings commonSettings =
		new CommonNukerSettings();
	
	private final SwingHandSetting swingHand = new SwingHandSetting(
		SwingHandSetting.genericMiningDescription(this), SwingHand.OFF);
	
	public SpeedNukerHack()
	{
		super("SpeedNuker");
		setCategory(Category.BLOCKS);
		addSetting(range);
		commonSettings.getSettings().forEach(this::addSetting);
		addSetting(swingHand);
	}
	
	@Override
	public String getRenderName()
	{
		return getName() + commonSettings.getRenderNameSuffix();
	}
	
	@Override
	protected void onEnable()
	{
		QUANTUM_HACK.getHax().autoMineHack.setEnabled(false);
		QUANTUM_HACK.getHax().excavatorHack.setEnabled(false);
		QUANTUM_HACK.getHax().nukerHack.setEnabled(false);
		QUANTUM_HACK.getHax().nukerLegitHack.setEnabled(false);
		QUANTUM_HACK.getHax().tunnellerHack.setEnabled(false);
		QUANTUM_HACK.getHax().veinMinerHack.setEnabled(false);
		
		EVENTS.add(LeftClickListener.class, commonSettings);
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(LeftClickListener.class, commonSettings);
		EVENTS.remove(UpdateListener.class, this);
		
		commonSettings.reset();
	}
	
	@Override
	public void onUpdate()
	{
		if(commonSettings.isIdModeWithAir())
			return;
		
		Vec3d eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.ofFloored(eyesVec);
		double rangeSq = range.getValueSq();
		int blockRange = range.getValueCeil();
		
		Stream<BlockPos> stream =
			BlockUtils.getAllInBoxStream(eyesBlock, blockRange)
				.filter(BlockUtils::canBeClicked)
				.filter(commonSettings::shouldBreakBlock);
		
		if(commonSettings.isSphereShape())
			stream = stream
				.filter(pos -> pos.getSquaredDistance(eyesVec) <= rangeSq);
		
		ArrayList<BlockPos> blocks = stream
			.sorted(Comparator
				.comparingDouble(pos -> pos.getSquaredDistance(eyesVec)))
			.collect(Collectors.toCollection(ArrayList::new));
		
		if(blocks.isEmpty())
			return;
		
		QUANTUM_HACK.getHax().autoToolHack.equipIfEnabled(blocks.get(0));
		BlockBreaker.breakBlocksWithPacketSpam(blocks);
		swingHand.swing(Hand.MAIN_HAND);
	}
}
