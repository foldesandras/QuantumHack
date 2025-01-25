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
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"range"})
public final class ReachHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 6, 1, 10, 0.05, ValueDisplay.DECIMAL);
	
	public ReachHack()
	{
		super("Reach");
		setCategory(Category.OTHER);
		addSetting(range);
	}
	
	public float getReachDistance()
	{
		return range.getValueF();
	}
	
	// See ClientPlayerInteractionManagerMixin.onGetReachDistance() and
	// ClientPlayerInteractionManagerMixin.hasExtendedReach()
}
