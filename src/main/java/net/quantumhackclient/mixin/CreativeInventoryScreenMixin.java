/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen.CreativeScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.quantumhackclient.QuantumHackClient;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin
	extends AbstractInventoryScreen<CreativeScreenHandler>
{
	private CreativeInventoryScreenMixin(QuantumHackClient wurst,
                                         CreativeScreenHandler screenHandler, PlayerInventory inventory,
                                         Text title)
	{
		super(screenHandler, inventory, title);
	}
	
	@Inject(at = @At("HEAD"),
		method = "shouldShowOperatorTab(Lnet/minecraft/entity/player/PlayerEntity;)Z",
		cancellable = true)
	private void onShouldShowOperatorTab(PlayerEntity player,
		CallbackInfoReturnable<Boolean> cir)
	{
		if(QuantumHackClient.INSTANCE.isEnabled())
			cir.setReturnValue(true);
	}
}
