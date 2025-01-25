/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import net.quantumhackclient.command.CmdError;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;

public final class JumpCmd extends Command
{
	public JumpCmd()
	{
		super("jump", "Makes you jump.");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 0)
			throw new CmdSyntaxError();
		
		if(!MC.player.isOnGround() && !QUANTUM_HACK.getHax().jetpackHack.isEnabled())
			throw new CmdError("Can't jump in mid-air.");
		
		MC.player.jump();
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Jump";
	}
	
	@Override
	public void doPrimaryAction()
	{
		QUANTUM_HACK.getCmdProcessor().process("jump");
	}
}
