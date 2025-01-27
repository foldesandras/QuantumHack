/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.quantumhackclient.Category;
import net.quantumhackclient.hack.DontSaveState;
import net.quantumhackclient.hack.Hack;

@DontSaveState
public final class LsdHack extends Hack
{
	public LsdHack()
	{
		super("LSD");
		setCategory(Category.FUN);
	}
	
	@Override
	protected void onEnable()
	{
		if(!(MC.getCameraEntity() instanceof PlayerEntity))
		{
			setEnabled(false);
			return;
		}
		
		if(MC.gameRenderer.getPostProcessor() != null)
			MC.gameRenderer.disablePostProcessor();
		
		MC.gameRenderer
			.loadPostProcessor(new Identifier("shaders/post/wobble.json"));
	}
	
	@Override
	protected void onDisable()
	{
		if(MC.gameRenderer.getPostProcessor() != null)
			MC.gameRenderer.disablePostProcessor();
	}
}
