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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkData;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ServerMetadataS2CPacket;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.event.EventManager;
import net.quantumhackclient.events.PacketOutputListener.PacketOutputEvent;
import net.quantumhackclient.util.ChatUtils;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin
	implements ClientPlayPacketListener
{
	@Shadow
	@Final
	private MinecraftClient client;
	
	@WrapOperation(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;)V"),
		method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V")
	private void wrapSendPacket(ClientConnection connection, Packet<?> packet,
		Operation<Void> original)
	{
		PacketOutputEvent event = new PacketOutputEvent(packet);
		EventManager.fire(event);
		
		if(!event.isCancelled())
			original.call(connection, event.getPacket());
	}
	
	@Inject(at = @At("TAIL"),
		method = "onServerMetadata(Lnet/minecraft/network/packet/s2c/play/ServerMetadataS2CPacket;)V")
	public void onOnServerMetadata(ServerMetadataS2CPacket packet,
		CallbackInfo ci)
	{
		QuantumHackClient wurst = QuantumHackClient.INSTANCE;
		if(!wurst.isEnabled())
			return;
		
		// Remove Mojang's dishonest warning toast on safe servers
		if(!packet.isSecureChatEnforced())
		{
			client.getToastManager().toastQueue.removeIf(toast -> toast
				.getType() == SystemToast.Type.UNSECURE_SERVER_WARNING);
			return;
		}
		
		// Add an honest warning toast on unsafe servers
		MutableText title = Text.literal(ChatUtils.QUANTUM_HACK_PREFIX
			+ wurst.translate("toast.wurst.nochatreports.unsafe_server.title"));
		MutableText message = Text.literal(
			wurst.translate("toast.wurst.nochatreports.unsafe_server.message"));
		
		SystemToast systemToast = SystemToast.create(client,
			SystemToast.Type.UNSECURE_SERVER_WARNING, title, message);
		client.getToastManager().add(systemToast);
	}
	
	@Inject(at = @At("TAIL"),
		method = "loadChunk(IILnet/minecraft/network/packet/s2c/play/ChunkData;)V")
	private void onLoadChunk(int x, int z, ChunkData chunkData, CallbackInfo ci)
	{
		QuantumHackClient.INSTANCE.getHax().newChunksHack.afterLoadChunk(x, z);
	}
	
	@Inject(at = @At("TAIL"),
		method = "onBlockUpdate(Lnet/minecraft/network/packet/s2c/play/BlockUpdateS2CPacket;)V")
	private void onOnBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci)
	{
		QuantumHackClient.INSTANCE.getHax().newChunksHack
			.afterUpdateBlock(packet.getPos());
	}
	
	@Inject(at = @At("TAIL"),
		method = "onChunkDeltaUpdate(Lnet/minecraft/network/packet/s2c/play/ChunkDeltaUpdateS2CPacket;)V")
	private void onOnChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet,
		CallbackInfo ci)
	{
		packet.visitUpdates(
			(pos, state) -> QuantumHackClient.INSTANCE.getHax().newChunksHack
				.afterUpdateBlock(pos));
	}
}
