/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.hack.Hack;

@SearchTags({"RainbowGUI", "rainbow ui", "rainbow gui", "rgb"})
public final class RainbowUiHack extends Hack
{
	public RainbowUiHack()
	{
		super("RainbowUI");
		setCategory(Category.FUN);
	}
	
	// See ClickGui.updateColors()
}
