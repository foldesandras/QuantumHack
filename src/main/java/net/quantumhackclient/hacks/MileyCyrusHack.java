/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import net.minecraft.client.option.KeyBinding;
import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.mixinterface.IKeyBinding;
import net.quantumhackclient.settings.SliderSetting;
import net.quantumhackclient.settings.SliderSetting.ValueDisplay;

@SearchTags({"miley cyrus", "twerk", "wrecking ball"})
public final class MileyCyrusHack extends Hack implements UpdateListener
{
	private final SliderSetting twerkSpeed = new SliderSetting("Twerk speed",
		"I came in like a wreeecking baaall...", 5, 1, 10, 1,
		ValueDisplay.INTEGER);
	
	private int timer;
	
	public MileyCyrusHack()
	{
		super("MileyCyrus");
		setCategory(Category.FUN);
		addSetting(twerkSpeed);
	}
	
	@Override
	protected void onEnable()
	{
		timer = 0;
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		IKeyBinding.get(MC.options.sneakKey).resetPressedState();
	}
	
	@Override
	public void onUpdate()
	{
		timer++;
		if(timer < 10 - twerkSpeed.getValueI())
			return;
		
		KeyBinding sneakKey = MC.options.sneakKey;
		sneakKey.setPressed(!sneakKey.isPressed());
		timer = -1;
	}
}
