/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.other_features;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.quantumhackclient.DontBlock;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.ConnectionPacketOutputListener;
import net.quantumhackclient.mixin.CustomPayloadC2SPacketAccessor;
import net.quantumhackclient.other_feature.OtherFeature;
import net.quantumhackclient.settings.CheckboxSetting;

@DontBlock
@SearchTags({"vanilla spoof", "AntiFabric", "anti fabric", "LibHatesMods",
	"HackedServer"})
public final class VanillaSpoofOtf extends OtherFeature
	implements ConnectionPacketOutputListener
{
	private final CheckboxSetting spoof =
		new CheckboxSetting("Spoof Vanilla", false);
	
	public VanillaSpoofOtf()
	{
		super("VanillaSpoof",
			"Bypasses anti-Fabric plugins by pretending to be a vanilla client.");
		addSetting(spoof);
		
		EVENTS.add(ConnectionPacketOutputListener.class, this);
	}
	
	@Override
	public void onSentConnectionPacket(ConnectionPacketOutputEvent event)
	{
		if(!spoof.isChecked())
			return;
		
		if(!(event.getPacket() instanceof CustomPayloadC2SPacketAccessor))
			return;
		
		CustomPayloadC2SPacketAccessor packet =
			(CustomPayloadC2SPacketAccessor)event.getPacket();
		
		if(packet.getChannel().getNamespace().equals("minecraft")
			&& packet.getChannel().getPath().equals("register"))
			event.cancel();
		
		if(packet.getChannel().getNamespace().equals("minecraft")
			&& packet.getChannel().getPath().equals("brand"))
			event.setPacket(new CustomPayloadC2SPacket(
				CustomPayloadC2SPacket.BRAND,
				new PacketByteBuf(Unpooled.buffer()).writeString("vanilla")));
		
		if(packet.getChannel().getNamespace().equals("fabric"))
			event.cancel();
	}
	
	@Override
	public boolean isEnabled()
	{
		return spoof.isChecked();
	}
	
	@Override
	public String getPrimaryAction()
	{
		return isEnabled() ? "Disable" : "Enable";
	}
	
	@Override
	public void doPrimaryAction()
	{
		spoof.setChecked(!spoof.isChecked());
	}
}
