/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks.autofish;

import net.minecraft.entity.projectile.FishingBobberEntity;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.settings.CheckboxSetting;
import net.quantumhackclient.util.ChatUtils;

public class ShallowWaterWarningCheckbox extends CheckboxSetting
{
	private boolean hasAlreadyWarned;
	
	public ShallowWaterWarningCheckbox()
	{
		super("Shallow water warning",
			"Displays a warning message in chat when you are fishing in shallow"
				+ " water.",
			true);
	}
	
	public void reset()
	{
		hasAlreadyWarned = false;
	}
	
	public void checkWaterType()
	{
		FishingBobberEntity bobber = QuantumHackClient.MC.player.fishHook;
		if(bobber.isOpenOrWaterAround(bobber.getBlockPos()))
		{
			hasAlreadyWarned = false;
			return;
		}
		
		if(isChecked() && !hasAlreadyWarned)
		{
			ChatUtils.warning("You are currently fishing in shallow water.");
			ChatUtils.message(
				"You can't get any treasure items while fishing like this.");
			
			if(!QuantumHackClient.INSTANCE.getHax().openWaterEspHack.isEnabled())
				ChatUtils.message("Use OpenWaterESP to find open water.");
			
			hasAlreadyWarned = true;
		}
	}
}
