/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import java.util.Arrays;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.quantumhackclient.command.CmdError;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.util.ChatUtils;
import net.quantumhackclient.util.MathUtils;

public final class GiveCmd extends Command
{
	public GiveCmd()
	{
		super("give",
			"Gives you an item with custom NBT data.\n"
				+ "Requires creative mode.",
			".give <item> [<amount>] [<nbt>]", ".give <id> [<amount>] [<nbt>]");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		// validate input
		if(args.length < 1)
			throw new CmdSyntaxError();
		
		if(!MC.player.getAbilities().creativeMode)
			throw new CmdError("Creative mode only.");
		
		// id/name
		Item item = getItem(args[0]);
		
		if(item == Items.AIR && MathUtils.isInteger(args[0]))
			item = Item.byRawId(Integer.parseInt(args[0]));
		
		if(item == Items.AIR)
			throw new CmdError("Item \"" + args[0] + "\" could not be found.");
		
		// amount
		int amount = 1;
		if(args.length >= 2)
		{
			if(!MathUtils.isInteger(args[1]))
				throw new CmdSyntaxError("Not a number: " + args[1]);
			
			amount = Integer.parseInt(args[1]);
			
			if(amount < 1)
				throw new CmdError("Amount cannot be less than 1.");
			
			if(amount > 64)
				throw new CmdError("Amount cannot be more than 64.");
		}
		
		// nbt data
		String nbt = null;
		if(args.length >= 3)
			nbt = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
		
		// generate item
		ItemStack stack = new ItemStack(item, amount);
		if(nbt != null)
			try
			{
				NbtCompound tag = StringNbtReader.parse(nbt);
				stack.setNbt(tag);
				
			}catch(CommandSyntaxException e)
			{
				ChatUtils.message(e.getMessage());
				throw new CmdSyntaxError("NBT data is invalid.");
			}
		
		// give item
		if(!placeStackInHotbar(stack))
			throw new CmdError("Please clear a slot in your hotbar.");
		ChatUtils.message("Item" + (amount > 1 ? "s" : "") + " created.");
	}
	
	private Item getItem(String id) throws CmdSyntaxError
	{
		try
		{
			return Registries.ITEM.get(new Identifier(id));
			
		}catch(InvalidIdentifierException e)
		{
			throw new CmdSyntaxError("Invalid item: " + id);
		}
	}
	
	private boolean placeStackInHotbar(ItemStack stack)
	{
		for(int i = 0; i < 9; i++)
		{
			if(!MC.player.getInventory().getStack(i).isEmpty())
				continue;
			
			MC.player.networkHandler.sendPacket(
				new CreativeInventoryActionC2SPacket(36 + i, stack));
			return true;
		}
		
		return false;
	}
}
