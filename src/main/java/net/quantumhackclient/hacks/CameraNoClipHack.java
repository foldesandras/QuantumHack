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

@SearchTags({"camera noclip", "camera no clip"})
public final class CameraNoClipHack extends Hack
{
	public CameraNoClipHack()
	{
		super("CameraNoClip");
		setCategory(Category.RENDER);
	}
	
	// See CameraMixin.onClipToSpace()
}
