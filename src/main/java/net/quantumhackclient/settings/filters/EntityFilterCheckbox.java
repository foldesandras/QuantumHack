/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.settings.filters;

import net.quantumhackclient.settings.CheckboxSetting;
import net.quantumhackclient.settings.Setting;
import net.quantumhackclient.settings.filterlists.EntityFilterList.EntityFilter;

public abstract class EntityFilterCheckbox extends CheckboxSetting
	implements EntityFilter
{
	public EntityFilterCheckbox(String name, String description,
		boolean checked)
	{
		super(name, description, checked);
	}
	
	@Override
	public final boolean isFilterEnabled()
	{
		return isChecked();
	}
	
	@Override
	public final Setting getSetting()
	{
		return this;
	}
}
