/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.quantumhackclient.command.CmdError;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.util.ChatUtils;

public final class RepairCmd extends Command
{
	public RepairCmd()
	{
		super("repair", "Repairs the held item. Requires creative mode.",
			".repair");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 0)
			throw new CmdSyntaxError();
		
		ClientPlayerEntity player = MC.player;
		
		if(!player.getAbilities().creativeMode)
			throw new CmdError("Creative mode only.");
		
		ItemStack stack = getHeldStack(player);
		stack.setDamage(0);
		MC.player.networkHandler
			.sendPacket(new CreativeInventoryActionC2SPacket(
				36 + player.getInventory().selectedSlot, stack));
		
		ChatUtils.message("Item repaired.");
	}
	
	private ItemStack getHeldStack(ClientPlayerEntity player) throws CmdError
	{
		ItemStack stack = player.getInventory().getMainHandStack();
		
		if(stack.isEmpty())
			throw new CmdError("You need an item in your hand.");
		
		if(!stack.isDamageable())
			throw new CmdError("This item can't take damage.");
		
		if(!stack.isDamaged())
			throw new CmdError("This item is not damaged.");
		
		return stack;
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Repair Current Item";
	}
	
	@Override
	public void doPrimaryAction()
	{
		QUANTUM_HACK.getCmdProcessor().process("repair");
	}
}
