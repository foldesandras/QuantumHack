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
import net.quantumhackclient.settings.Setting;
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.util.CmdUtils;
import net.quantumhackclient.util.MathUtils;

@DontBlock
public final class SetSliderCmd extends Command
{
	public SetSliderCmd()
	{
		super("setslider",
			"Changes a slider setting of a feature. Allows you to\n"
				+ "move sliders through keybinds.",
			".setslider <feature> <setting> <value>",
			".setslider <feature> <setting> (more|less)");
	}
	
	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length != 3)
			throw new CmdSyntaxError();
		
		Feature feature = CmdUtils.findFeature(args[0]);
		Setting setting = CmdUtils.findSetting(feature, args[1]);
		SliderSetting slider = getAsSlider(feature, setting);
		setValue(args[2], slider);
	}
	
	private SliderSetting getAsSlider(Feature feature, Setting setting)
		throws CmdError
	{
		if(!(setting instanceof SliderSetting))
			throw new CmdError(feature.getName() + " " + setting.getName()
				+ " is not a slider setting.");
		
		return (SliderSetting)setting;
	}
	
	private void setValue(String value, SliderSetting slider)
		throws CmdSyntaxError
	{
		switch(value.toLowerCase())
		{
			case "more":
			slider.increaseValue();
			break;
			
			case "less":
			slider.decreaseValue();
			break;
			
			default:
			if(!MathUtils.isDouble(value))
				throw new CmdSyntaxError("Value must be a number.");
			slider.setValue(Double.parseDouble(value));
			break;
		}
	}
}
