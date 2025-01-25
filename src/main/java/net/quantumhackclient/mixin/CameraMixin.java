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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.hacks.CameraDistanceHack;

@Mixin(Camera.class)
public abstract class CameraMixin
{
	@ModifyVariable(at = @At("HEAD"),
		method = "clipToSpace(D)D",
		argsOnly = true)
	private double changeClipToSpaceDistance(double desiredCameraDistance)
	{
		CameraDistanceHack cameraDistance =
			QuantumHackClient.INSTANCE.getHax().cameraDistanceHack;
		if(cameraDistance.isEnabled())
			return cameraDistance.getDistance();
		
		return desiredCameraDistance;
	}
	
	@Inject(at = @At("HEAD"), method = "clipToSpace(D)D", cancellable = true)
	private void onClipToSpace(double desiredCameraDistance,
		CallbackInfoReturnable<Double> cir)
	{
		if(QuantumHackClient.INSTANCE.getHax().cameraNoClipHack.isEnabled())
			cir.setReturnValue(desiredCameraDistance);
	}
	
	@Inject(at = @At("HEAD"),
		method = "getSubmersionType()Lnet/minecraft/client/render/CameraSubmersionType;",
		cancellable = true)
	private void onGetSubmersionType(
		CallbackInfoReturnable<CameraSubmersionType> cir)
	{
		if(QuantumHackClient.INSTANCE.getHax().noOverlayHack.isEnabled())
			cir.setReturnValue(CameraSubmersionType.NONE);
	}
}
