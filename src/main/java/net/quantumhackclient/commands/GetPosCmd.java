/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import net.minecraft.util.math.BlockPos;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.util.ChatUtils;

public final class GetPosCmd extends Command
{
	public GetPosCmd()
	{
		super("getpos", "Shows your current position.", ".getpos",
			"Copy to clipboard: .getpos copy");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		BlockPos pos = BlockPos.ofFloored(MC.player.getPos());
		String posString = pos.getX() + " " + pos.getY() + " " + pos.getZ();
		
		switch(String.join(" ", args).toLowerCase())
		{
			case "":
			ChatUtils.message("Position: " + posString);
			break;
			
			case "copy":
			MC.keyboard.setClipboard(posString);
			ChatUtils.message("Position copied to clipboard.");
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Get Position";
	}
	
	@Override
	public void doPrimaryAction()
	{
		QUANTUM_HACK.getCmdProcessor().process("getpos");
	}
}
