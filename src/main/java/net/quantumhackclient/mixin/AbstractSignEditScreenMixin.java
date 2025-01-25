/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.text.Text;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.hacks.AutoSignHack;

@Mixin(AbstractSignEditScreen.class)
public abstract class AbstractSignEditScreenMixin extends Screen
{
	@Shadow
	@Final
	private String[] messages;
	
	private AbstractSignEditScreenMixin(QuantumHackClient wurst, Text title)
	{
		super(title);
	}
	
	@Inject(at = @At("HEAD"), method = "init()V")
	private void onInit(CallbackInfo ci)
	{
		AutoSignHack autoSignHack = QuantumHackClient.INSTANCE.getHax().autoSignHack;
		
		String[] autoSignText = autoSignHack.getSignText();
		if(autoSignText == null)
			return;
		
		for(int i = 0; i < 4; i++)
			messages[i] = autoSignText[i];
		
		finishEditing();
	}
	
	@Inject(at = @At("HEAD"), method = "finishEditing()V")
	private void onFinishEditing(CallbackInfo ci)
	{
		QuantumHackClient.INSTANCE.getHax().autoSignHack.setSignText(messages);
	}
	
	@Shadow
	private void finishEditing()
	{
		
	}
}
