/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.events;

import java.util.ArrayList;

import net.minecraft.util.shape.VoxelShape;
import net.quantumhackclient.event.Event;
import net.quantumhackclient.event.Listener;

public interface CactusCollisionShapeListener extends Listener
{
	public void onCactusCollisionShape(CactusCollisionShapeEvent event);
	
	public static class CactusCollisionShapeEvent
		extends Event<CactusCollisionShapeListener>
	{
		private VoxelShape collisionShape;
		
		public VoxelShape getCollisionShape()
		{
			return collisionShape;
		}
		
		public void setCollisionShape(VoxelShape collisionShape)
		{
			this.collisionShape = collisionShape;
		}
		
		@Override
		public void fire(ArrayList<CactusCollisionShapeListener> listeners)
		{
			for(CactusCollisionShapeListener listener : listeners)
				listener.onCactusCollisionShape(this);
		}
		
		@Override
		public Class<CactusCollisionShapeListener> getListenerType()
		{
			return CactusCollisionShapeListener.class;
		}
	}
}
