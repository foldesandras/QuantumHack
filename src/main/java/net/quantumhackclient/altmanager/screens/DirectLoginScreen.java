/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.altmanager.screens;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.quantumhackclient.altmanager.LoginException;
import net.quantumhackclient.altmanager.LoginManager;
import net.quantumhackclient.altmanager.MicrosoftLoginManager;

public final class DirectLoginScreen extends AltEditorScreen
{
	public DirectLoginScreen(Screen prevScreen)
	{
		super(prevScreen, Text.literal("Direct Login"));
	}
	
	@Override
	protected String getDoneButtonText()
	{
		return getPassword().isEmpty() ? "Change Cracked Name"
			: "Login with Password";
	}
	
	@Override
	protected void pressDoneButton()
	{
		String nameOrEmail = getNameOrEmail();
		String password = getPassword();
		
		if(password.isEmpty())
			LoginManager.changeCrackedName(nameOrEmail);
		else
			try
			{
				MicrosoftLoginManager.login(nameOrEmail, password);
				
			}catch(LoginException e)
			{
				try
				{
					LoginManager.login(nameOrEmail, password);
					
				}catch(LoginException e2)
				{
					message = "\u00a7c\u00a7lMicrosoft:\u00a7c "
						+ e.getMessage() + "\n\u00a7c\u00a7lMojang:\u00a7c "
						+ e2.getMessage();
					
					doErrorEffect();
				}
				return;
			}
		
		message = "";
		client.setScreen(new TitleScreen());
	}
}
