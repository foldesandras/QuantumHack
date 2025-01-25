/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.hacks;

import java.util.Random;

import net.quantumhackclient.Category;
import net.quantumhackclient.SearchTags;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.util.Rotation;

@SearchTags({"Retarded"})
public final class DerpHack extends Hack implements UpdateListener
{
	private final Random random = new Random();
	
	public DerpHack()
	{
		super("Derp");
		setCategory(Category.FUN);
	}
	
	@Override
	protected void onEnable()
	{
		// disable incompatible derps
		QUANTUM_HACK.getHax().headRollHack.setEnabled(false);
		QUANTUM_HACK.getHax().tiredHack.setEnabled(false);
		
		EVENTS.add(UpdateListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		float yaw = MC.player.getYaw() + random.nextFloat() * 360F - 180F;
		float pitch = random.nextFloat() * 180F - 90F;
		
		new Rotation(yaw, pitch).sendPlayerLookPacket();
	}
}
