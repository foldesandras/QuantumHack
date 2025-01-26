/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hud;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.other_features.QuantumHackLogoOtf;

public final class QuantumHackLogo
{
	public void render(DrawContext context)
	{
		MatrixStack matrixStack = context.getMatrices();
		QuantumHackLogoOtf otf =
			QuantumHackClient.INSTANCE.getOtfs().wurstLogoOtf;
		if(!otf.isVisible())
			return;
		
		String display = "QuantumHack " + getVersionString();
		TextRenderer tr = QuantumHackClient.MC.textRenderer;
		
		// draw version background
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		float[] color;
		if(QuantumHackClient.INSTANCE.getHax().rainbowUiHack.isEnabled())
			color = QuantumHackClient.INSTANCE.getGui().getAcColor();
		else
			color = otf.getBackgroundColor();
		
		drawQuads(matrixStack, 0, 0, tr.getWidth(display) + 76, 11, color[0],
			color[1], color[2], 0.5F);
		
		// draw version string
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		context.drawText(tr, display, 0, 2, otf.getTextColor(), false);
		
		// draw Wurst logo
		/*
		 * RenderSystem.setShaderColor(1, 1, 1, 1);
		 * GL11.glEnable(GL11.GL_BLEND);
		 * context.drawTexture(texture, 0, 3, 0, 0, 72, 18, 72, 18);
		 */
	}
	
	private String getVersionString()
	{
		String version = "v" + QuantumHackClient.VERSION;
		version += " MC" + QuantumHackClient.MC_VERSION;
		
		if(QuantumHackClient.INSTANCE.getUpdater().isOutdated())
			version += " (outdated)";
		
		return version;
	}
	
	private void drawQuads(MatrixStack matrices, int x1, int y1, int x2, int y2,
		float r, float g, float b, float a)
	{
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
			VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).next();
		tessellator.draw();
	}
}
