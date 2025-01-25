/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.commands;

import net.quantumhackclient.DontBlock;
import net.quantumhackclient.Feature;
import net.quantumhackclient.command.CmdError;
import net.quantumhackclient.command.CmdException;
import net.quantumhackclient.command.CmdSyntaxError;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.settings.ColorSetting;
import net.quantumhackclient.settings.Setting;
import net.quantumhackclient.util.CmdUtils;
import net.quantumhackclient.util.ColorUtils;
import net.quantumhackclient.util.json.JsonException;

@DontBlock
public final class SetColorCmd extends Command
{
	public SetColorCmd()
	{
		super("setcolor",
			"Changes a color setting of a feature. Allows you\n"
				+ "to set RGB values through keybinds.",
			".setcolor <feature> <setting> <RGB>",
			"Example: .setcolor ClickGUI AC #FF0000");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		ColorSetting colorSetting = getAsColor(feature, setting);
		setColor(colorSetting, args[2]);
	}
	
	private ColorSetting getAsColor(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof ColorSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " is not a color setting.");
		
		return (ColorSetting)setting;
	}
	
	private void setColor(ColorSetting setting, String value)
		throws CmdSyntaxError
	{
		try
		{
			setting.setColor(ColorUtils.parseHex(value));
			
		}catch(JsonException e)
		{
			throw new CmdSyntaxError("Invalid color: " + value);
		}
	}
}
