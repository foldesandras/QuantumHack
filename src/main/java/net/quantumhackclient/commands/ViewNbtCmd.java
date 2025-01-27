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
import net.minecraft.nbt.NbtCompound;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.command.CmdError;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.util.ChatUtils;

@SearchTags({"view nbt", "NBTViewer", "nbt viewer"})
public final class ViewNbtCmd extends Command
{
	public ViewNbtCmd()
	{
		super("viewnbt", "Shows you the NBT data of an item.", ".viewnbt",
			"Copy to clipboard: .viewnbt copy");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		ClientPlayerEntity player = MC.player;
		ItemStack stack = player.getInventory().getMainHandStack();
		if(stack.isEmpty())
			throw new CmdError("You must hold an item in your main hand.");
		
		NbtCompound tag = stack.getNbt();
		String nbt = tag == null ? "" : tag.asString();
		
		switch(String.join(" ", args).toLowerCase())
		{
			case "":
			ChatUtils.message("NBT: " + nbt);
			break;
			
			case "copy":
			MC.keyboard.setClipboard(nbt);
			ChatUtils.message("NBT data copied to clipboard.");
			break;
			
			default:
			throw new CmdSyntaxError();
		}
	}
}
