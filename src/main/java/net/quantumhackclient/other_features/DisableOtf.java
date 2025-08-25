/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.other_features;

import net.quantumhackclient.Branding;
import net.quantumhackclient.DontBlock;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.other_feature.OtherFeature;
import net.quantumhackclient.settings.CheckboxSetting;

@SearchTags({"turn off", "hide wurst logo", "ghost mode", "stealth mode",
	"vanilla Minecraft"})
@DontBlock
public final class DisableOtf extends OtherFeature
{
	private final CheckboxSetting hideEnableButton =
		new CheckboxSetting("Hide enable button",
			"Removes the \"Enable " + Branding.BRANDING_NAME
				+ "\" button as soon as you close the Statistics screen."
				+ " You will have to restart the game to re-enable "
				+ Branding.BRANDING_NAME + ".",
			false);
	
	public DisableOtf()
	{
		super("Disable " + Branding.BRANDING_NAME,
			"To disable " + Branding.BRANDING_NAME
				+ ", go to the Statistics screen and press the \"Disable "
				+ Branding.BRANDING_NAME + "\" button.\n"
				+ "It will turn into an \"Enable " + Branding.BRANDING_NAME
				+ "\" button once pressed.");
		addSetting(hideEnableButton);
	}
	
	public boolean shouldHideEnableButton()
	{
		return !QUANTUM_HACK.isEnabled() && hideEnableButton.isChecked();
	}
}
