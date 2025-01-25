/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.other_features;

import net.quantumhackclient.DontBlock;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.options.KeybindManagerScreen;
import net.quantumhackclient.other_feature.OtherFeature;

@SearchTags({"KeybindManager", "keybind manager", "KeybindsManager",
	"keybinds manager"})
@DontBlock
public final class KeybindManagerOtf extends OtherFeature
{
	public KeybindManagerOtf()
	{
		super("Keybinds",
			"This is just a shortcut to let you open the Keybind Manager from within the GUI. Normally you would go to Wurst Options > Keybinds.");
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Open Keybind Manager";
	}
	
	@Override
	public void doPrimaryAction()
	{
		MC.setScreen(new KeybindManagerScreen(MC.currentScreen));
	}
}
