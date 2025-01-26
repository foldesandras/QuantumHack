/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.text.Text;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.settings.EnumSetting;
import net.quantumhackclient.util.ChatUtils;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;

@SearchTags({"instakill sword potion", "InstaKillSword"})
public final class InstaKillSwordHack extends Hack
{

	public InstaKillSwordHack()
	{
		super("InstaKillSword");
		setCategory(Category.ITEMS);
	}
	
	@Override
	protected void onEnable()
	{
		// check gamemode
		if(!MC.player.getAbilities().creativeMode)
		{
			ChatUtils.error("Creative mode only.");
			setEnabled(false);
			return;
		}
		
		// generate sword
		ItemStack stack = new ItemStack(Registries.ITEM.get(new Identifier("minecraft:diamond_sword")));
		stack.addEnchantment(Enchantments.SHARPNESS, 127);

		// give sword
		if(placeStackInHotbar(stack))
			ChatUtils.message("Insta killing sword created.");
		else
			ChatUtils.error("Please clear a slot in your hotbar.");
		
		setEnabled(false);
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
