/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.other_features;

import net.quantumhackclient.DontBlock;
import net.quantumhackclient.other_feature.OtherFeature;

@DontBlock
public final class ReconnectOtf extends OtherFeature
{
	public ReconnectOtf()
	{
		super("Reconnect",
			"Whenever you get kicked from a server, Wurst gives you a \"Reconnect\" button that lets you instantly join again.");
	}
}
