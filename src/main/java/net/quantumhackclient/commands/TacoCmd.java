/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.quantumhackclient.Category;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.events.GUIRenderListener;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.util.RenderUtils;

public final class TacoCmd extends Command
	implements GUIRenderListener, UpdateListener
{
	private final Identifier[] tacos =
		{new Identifier("quantumhack", "dancingtaco1.png"),
			new Identifier("quantumhack", "dancingtaco2.png"),
			new Identifier("quantumhack", "dancingtaco3.png"),
			new Identifier("quantumhack", "dancingtaco4.png")};
	
	private boolean enabled;
	private int ticks = 0;
	
	public TacoCmd()
	{
		super("taco", "Spawns a dancing taco on your hotbar.\n"
			+ "\"I love that little guy. So cute!\" -WiZARD");
		setCategory(Category.FUN);
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError("Tacos don't need arguments!");
		
		enabled = !enabled;
		
		if(enabled)
		{
			EVENTS.add(GUIRenderListener.class, this);
			EVENTS.add(UpdateListener.class, this);
			
		}else
		{
			EVENTS.remove(GUIRenderListener.class, this);
			EVENTS.remove(UpdateListener.class, this);
		}
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Be a BOSS!";
	}
	
	@Override
	public void doPrimaryAction()
	{
		QUANTUM_HACK.getCmdProcessor().process("taco");
	}
	
	@Override
	public void onUpdate()
	{
		if(ticks >= 31)
			ticks = 0;
		else
			ticks++;
	}
	
	@Override
	public void onRenderGUI(DrawContext context, float partialTicks)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		if(QUANTUM_HACK.getHax().rainbowUiHack.isEnabled())
			RenderUtils.setShaderColor(QUANTUM_HACK.getGui().getAcColor(), 1);
		else
			RenderSystem.setShaderColor(1, 1, 1, 1);
		
		Window sr = MC.getWindow();
		int x = sr.getScaledWidth() / 2 - 32 + 76;
		int y = sr.getScaledHeight() - 32 - 19;
		int w = 64;
		int h = 32;
		context.drawTexture(tacos[ticks / 8], x, y, 0, 0, w, h, w, h);
		
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_BLEND);
	}
}
