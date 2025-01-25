/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.HandleBlockBreakingListener;
import net.quantumhackclient.events.LeftClickListener;
import net.quantumhackclient.events.RenderListener;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.hacks.nukers.CommonNukerSettings;
import net.quantumhackclient.mixinterface.IKeyBinding;
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;
import net.quantumhackclient.settings.SwingHandSetting;
import net.quantumhackclient.settings.SwingHandSetting.SwingHand;
import net.quantumhackclient.util.BlockBreaker;
import net.quantumhackclient.util.BlockBreaker.BlockBreakingParams;
import net.quantumhackclient.util.BlockUtils;
import net.quantumhackclient.util.OverlayRenderer;
import net.quantumhackclient.util.RotationUtils;

@SearchTags({"LegitNuker", "nuker legit", "legit nuker"})
public final class NukerLegitHack extends Hack
	implements UpdateListener, HandleBlockBreakingListener, RenderListener
{
	private final SliderSetting range =
		new SliderSetting("Range", 4.25, 1, 4.5, 0.05, ValueDisplay.DECIMAL);
	
	private final CommonNukerSettings commonSettings =
		new CommonNukerSettings();
	
	private final SwingHandSetting swingHand =
		SwingHandSetting.withoutOffOption(
			SwingHandSetting.genericMiningDescription(this), SwingHand.CLIENT);
	
	private final OverlayRenderer overlay = new OverlayRenderer();
	private BlockPos currentBlock;
	
	public NukerLegitHack()
	{
		super("NukerLegit");
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
		QUANTUM_HACK.getHax().speedNukerHack.setEnabled(false);
		QUANTUM_HACK.getHax().tunnellerHack.setEnabled(false);
		QUANTUM_HACK.getHax().veinMinerHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(LeftClickListener.class, commonSettings);
		EVENTS.add(HandleBlockBreakingListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(LeftClickListener.class, commonSettings);
		EVENTS.remove(HandleBlockBreakingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		// resets
		IKeyBinding.get(MC.options.attackKey).resetPressedState();
		MC.interactionManager.cancelBlockBreaking();
		overlay.resetProgress();
		currentBlock = null;
		commonSettings.reset();
	}
	
	@Override
	public void onUpdate()
	{
		currentBlock = null;
		
		if(commonSettings.isIdModeWithAir())
		{
			overlay.resetProgress();
			return;
		}
		
		// Ignore the attack cooldown because opening any screen
		// will set it to 10k ticks.
		
		if(MC.player.isRiding())
		{
			overlay.resetProgress();
			MC.interactionManager.cancelBlockBreaking();
			return;
		}
		
		Vec3d eyesVec = RotationUtils.getEyesPos();
		BlockPos eyesBlock = BlockPos.ofFloored(eyesVec);
		double maxRange = MC.interactionManager.getReachDistance() + 1;
		double rangeSq = commonSettings.isSphereShape() ? range.getValueSq()
			: maxRange * maxRange;
		int blockRange = range.getValueCeil();
		
		Stream<BlockBreakingParams> stream = BlockUtils
			.getAllInBoxStream(eyesBlock, blockRange)
			.filter(commonSettings::shouldBreakBlock)
			.map(BlockBreaker::getBlockBreakingParams).filter(Objects::nonNull)
			.filter(BlockBreakingParams::lineOfSight)
			.filter(params -> params.distanceSq() <= rangeSq).sorted(
				Comparator.comparingDouble(BlockBreakingParams::distanceSq));
		
		// Break the first valid block
		currentBlock = stream.filter(this::breakBlock)
			.map(BlockBreakingParams::pos).findFirst().orElse(null);
		
		// reset if no block was found
		if(currentBlock == null)
		{
			IKeyBinding.get(MC.options.attackKey).resetPressedState();
			overlay.resetProgress();
		}
		
		overlay.updateProgress();
	}
	
	private boolean breakBlock(BlockBreakingParams params)
	{
		ClientPlayerInteractionManager im = MC.interactionManager;
		
		QUANTUM_HACK.getRotationFaker().faceVectorClient(params.hitVec());
		HitResult hitResult = MC.crosshairTarget;
		if(hitResult == null || hitResult.getType() != HitResult.Type.BLOCK
			|| !(hitResult instanceof BlockHitResult bHitResult))
		{
			im.cancelBlockBreaking();
			return true;
		}
		
		BlockPos pos = bHitResult.getBlockPos();
		BlockState state = MC.world.getBlockState(pos);
		Direction side = bHitResult.getSide();
		if(state.isAir() || !params.pos().equals(pos)
			|| !params.side().equals(side))
		{
			im.cancelBlockBreaking();
			return true;
		}
		
		QUANTUM_HACK.getHax().autoToolHack.equipIfEnabled(params.pos());
		
		if(MC.player.isUsingItem())
			// This case doesn't cancel block breaking in vanilla Minecraft.
			return true;
		
		if(!im.isBreakingBlock())
			im.attackBlock(pos, side);
		
		if(im.updateBlockBreakingProgress(pos, side))
		{
			MC.particleManager.addBlockBreakingParticles(pos, side);
			swingHand.swing(Hand.MAIN_HAND);
			MC.options.attackKey.setPressed(true);
		}
		
		return true;
	}
	
	@Override
	public void onHandleBlockBreaking(HandleBlockBreakingEvent event)
	{
		// Cancel vanilla block breaking so we don't send the packets twice.
		if(currentBlock != null)
			event.cancel();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		overlay.render(matrixStack, partialTicks, currentBlock);
	}
}
