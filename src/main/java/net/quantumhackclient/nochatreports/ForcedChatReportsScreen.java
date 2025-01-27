/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.nochatreports;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.other_features.NoChatReportsOtf;
import net.quantumhackclient.util.ChatUtils;
import net.quantumhackclient.util.LastServerRememberer;

public final class ForcedChatReportsScreen extends Screen
{
	private static final List<String> TRANSLATABLE_DISCONNECT_REASONS =
		Arrays.asList("multiplayer.disconnect.missing_public_key",
			"multiplayer.disconnect.invalid_public_key_signature",
			"multiplayer.disconnect.invalid_public_key",
			"multiplayer.disconnect.unsigned_chat");
	
	private static final List<String> LITERAL_DISCONNECT_REASONS =
		Arrays.asList("An internal error occurred in your connection.",
			"A secure profile is required to join this server.",
			"Secure profile expired.", "Secure profile invalid.");
	
	private final Screen prevScreen;
	private final Text reason;
	private MultilineText reasonFormatted = MultilineText.EMPTY;
	private int reasonHeight;
	
	private ButtonWidget signatureButton;
	private final Supplier<String> sigButtonMsg;
	
	public ForcedChatReportsScreen(Screen prevScreen)
	{
		super(Text.literal(ChatUtils.QUANTUM_HACK_PREFIX)
			.append(Text.literal(QuantumHackClient.INSTANCE
				.translate("gui.wurst.nochatreports.unsafe_server.title"))));
		this.prevScreen = prevScreen;
		
		reason = Text.literal(QuantumHackClient.INSTANCE
			.translate("gui.wurst.nochatreports.unsafe_server.message"));
		
		NoChatReportsOtf ncr =
			QuantumHackClient.INSTANCE.getOtfs().noChatReportsOtf;
		sigButtonMsg = () -> QuantumHackClient.INSTANCE
			.translate("button.wurst.nochatreports.signatures_status")
			+ blockedOrAllowed(ncr.isEnabled());
	}
	
	private String blockedOrAllowed(boolean blocked)
	{
		return QuantumHackClient.INSTANCE.translate(
			"gui.wurst.generic.allcaps_" + (blocked ? "blocked" : "allowed"));
	}
	
	@Override
	protected void init()
	{
		reasonFormatted =
			MultilineText.create(textRenderer, reason, width - 50);
		reasonHeight = reasonFormatted.count() * textRenderer.fontHeight;
		
		int buttonX = width / 2 - 100;
		int belowReasonY =
			(height - 78) / 2 + reasonHeight / 2 + textRenderer.fontHeight * 2;
		int signaturesY = Math.min(belowReasonY, height - 68);
		int reconnectY = signaturesY + 24;
		int backButtonY = reconnectY + 24;
		
		addDrawableChild(signatureButton = ButtonWidget
			.builder(Text.literal(sigButtonMsg.get()), b -> toggleSignatures())
			.dimensions(buttonX, signaturesY, 200, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.literal("Reconnect"),
				b -> LastServerRememberer.reconnect(prevScreen))
			.dimensions(buttonX, reconnectY, 200, 20).build());
		
		addDrawableChild(ButtonWidget
			.builder(Text.translatable("gui.toMenu"),
				b -> client.setScreen(prevScreen))
			.dimensions(buttonX, backButtonY, 200, 20).build());
	}
	
	private void toggleSignatures()
	{
		QuantumHackClient.INSTANCE.getOtfs().noChatReportsOtf.doPrimaryAction();
		signatureButton.setMessage(Text.literal(sigButtonMsg.get()));
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta)
	{
		renderBackground(context);
		
		int centerX = width / 2;
		int reasonY = (height - 68) / 2 - reasonHeight / 2;
		int titleY = reasonY - textRenderer.fontHeight * 2;
		
		context.drawCenteredTextWithShadow(textRenderer, title, centerX, titleY,
			0xAAAAAA);
		reasonFormatted.drawCenterWithShadow(context, centerX, reasonY);
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	public static boolean isCausedByNoChatReports(Text disconnectReason)
	{
		if(!QuantumHackClient.INSTANCE.getOtfs().noChatReportsOtf.isActive())
			return false;
		
		if(disconnectReason.getContent() instanceof TranslatableTextContent tr
			&& TRANSLATABLE_DISCONNECT_REASONS.contains(tr.getKey()))
			return true;
		
		if(disconnectReason.getContent() instanceof LiteralTextContent lt
			&& LITERAL_DISCONNECT_REASONS.contains(lt.string()))
			return true;
		
		return false;
	}
}
