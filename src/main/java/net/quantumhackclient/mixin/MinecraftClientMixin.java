/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.mixin;

import java.io.File;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.ProfileKeys;
import net.minecraft.client.util.Session;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.thread.ReentrantThreadExecutor;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.event.EventManager;
import net.quantumhackclient.events.HandleBlockBreakingListener.HandleBlockBreakingEvent;
import net.quantumhackclient.events.HandleInputListener.HandleInputEvent;
import net.quantumhackclient.events.LeftClickListener.LeftClickEvent;
import net.quantumhackclient.events.RightClickListener.RightClickEvent;
import net.quantumhackclient.mixinterface.IClientPlayerEntity;
import net.quantumhackclient.mixinterface.IClientPlayerInteractionManager;
import net.quantumhackclient.mixinterface.IMinecraftClient;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin
	extends ReentrantThreadExecutor<Runnable>
	implements WindowEventHandler, IMinecraftClient
{
	@Shadow
	@Final
	public File runDirectory;
	@Shadow
	public ClientPlayerInteractionManager interactionManager;
	@Shadow
	public ClientPlayerEntity player;
	@Shadow
	@Final
	private Session session;
	@Shadow
	@Final
	private YggdrasilAuthenticationService authenticationService;
	
	private Session wurstSession;
	private ProfileKeys wurstProfileKeys;
	
	private MinecraftClientMixin(QuantumHackClient wurst, String name)
	{
		super(name);
	}
	
	/**
	 * Runs just before {@link MinecraftClient#handleInputEvents()}, bypassing
	 * the <code>overlay == null && currentScreen == null</code> check in
	 * {@link MinecraftClient#tick()}.
	 */
	@Inject(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;",
		ordinal = 0), method = "tick()V")
	private void onHandleInputEvents(CallbackInfo ci)
	{
		// Make sure this event is not fired outside of gameplay
		if(player == null)
			return;
		
		EventManager.fire(HandleInputEvent.INSTANCE);
	}
	
	@Inject(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;",
		ordinal = 0), method = "doAttack()Z", cancellable = true)
	private void onDoAttack(CallbackInfoReturnable<Boolean> cir)
	{
		LeftClickEvent event = new LeftClickEvent();
		EventManager.fire(event);
		
		if(event.isCancelled())
			cir.setReturnValue(false);
	}
	
	@Inject(
		at = @At(value = "FIELD",
			target = "Lnet/minecraft/client/MinecraftClient;itemUseCooldown:I",
			ordinal = 0),
		method = "doItemUse()V",
		cancellable = true)
	private void onDoItemUse(CallbackInfo ci)
	{
		RightClickEvent event = new RightClickEvent();
		EventManager.fire(event);
		
		if(event.isCancelled())
			ci.cancel();
	}
	
	@Inject(at = @At("HEAD"), method = "doItemPick()V")
	private void onDoItemPick(CallbackInfo ci)
	{
		if(!QuantumHackClient.INSTANCE.isEnabled())
			return;
		
		HitResult hitResult = QuantumHackClient.MC.crosshairTarget;
		if(!(hitResult instanceof EntityHitResult eHitResult))
			return;
		
		QuantumHackClient.INSTANCE.getFriends()
			.middleClick(eHitResult.getEntity());
	}
	
	/**
	 * Allows hacks to cancel vanilla block breaking and replace it with their
	 * own. Useful for Nuker-like hacks.
	 */
	@Inject(at = @At("HEAD"),
		method = "handleBlockBreaking(Z)V",
		cancellable = true)
	private void onHandleBlockBreaking(boolean breaking, CallbackInfo ci)
	{
		HandleBlockBreakingEvent event = new HandleBlockBreakingEvent();
		EventManager.fire(event);
		
		if(event.isCancelled())
			ci.cancel();
	}
	
	@Inject(at = @At("HEAD"),
		method = "getSession()Lnet/minecraft/client/util/Session;",
		cancellable = true)
	private void onGetSession(CallbackInfoReturnable<Session> cir)
	{
		if(wurstSession != null)
			cir.setReturnValue(wurstSession);
	}
	
	@Redirect(at = @At(value = "FIELD",
		target = "Lnet/minecraft/client/MinecraftClient;session:Lnet/minecraft/client/util/Session;",
		opcode = Opcodes.GETFIELD,
		ordinal = 0),
		method = "getSessionProperties()Lcom/mojang/authlib/properties/PropertyMap;")
	private Session getSessionForSessionProperties(MinecraftClient mc)
	{
		return wurstSession != null ? wurstSession : session;
	}
	
	@Inject(at = @At("HEAD"),
		method = "getProfileKeys()Lnet/minecraft/client/util/ProfileKeys;",
		cancellable = true)
	private void onGetProfileKeys(CallbackInfoReturnable<ProfileKeys> cir)
	{
		if(QuantumHackClient.INSTANCE.getOtfs().noChatReportsOtf.isActive())
			cir.setReturnValue(ProfileKeys.MISSING);
		
		if(wurstProfileKeys == null)
			return;
		
		cir.setReturnValue(wurstProfileKeys);
	}
	
	@Inject(at = @At("HEAD"),
		method = "isTelemetryEnabledByApi()Z",
		cancellable = true)
	private void onIsTelemetryEnabledByApi(CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(
			!QuantumHackClient.INSTANCE.getOtfs().noTelemetryOtf.isEnabled());
	}
	
	@Inject(at = @At("HEAD"),
		method = "isOptionalTelemetryEnabledByApi()Z",
		cancellable = true)
	private void onIsOptionalTelemetryEnabledByApi(
		CallbackInfoReturnable<Boolean> cir)
	{
		cir.setReturnValue(
			!QuantumHackClient.INSTANCE.getOtfs().noTelemetryOtf.isEnabled());
	}
	
	@Override
	public IClientPlayerEntity getPlayer()
	{
		return (IClientPlayerEntity)player;
	}
	
	@Override
	public IClientPlayerInteractionManager getInteractionManager()
	{
		return (IClientPlayerInteractionManager)interactionManager;
	}
	
	@Override
	public void setSession(Session session)
	{
		wurstSession = session;
		
		UserApiService userApiService =
			session.getAccountType() == Session.AccountType.MSA
				? wurst_createUserApiService(session.getAccessToken())
				: UserApiService.OFFLINE;
		wurstProfileKeys =
			ProfileKeys.create(userApiService, session, runDirectory.toPath());
	}
	
	private UserApiService wurst_createUserApiService(String accessToken)
	{
		try
		{
			return authenticationService.createUserApiService(accessToken);
			
		}catch(AuthenticationException e)
		{
			e.printStackTrace();
			return UserApiService.OFFLINE;
		}
	}
}
