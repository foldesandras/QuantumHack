/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.update;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.quantumhackclient.QuantumHackClient;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.util.ChatUtils;
import net.quantumhackclient.util.json.JsonException;
import net.quantumhackclient.util.json.JsonUtils;
import net.quantumhackclient.util.json.WsonArray;
import net.quantumhackclient.util.json.WsonObject;

public final class QuantumHackUpdater implements UpdateListener
{
	private Thread thread;
	private boolean outdated;
	private Text component;
	
	@Override
	public void onUpdate()
	{
		if(thread == null)
		{
			thread = new Thread(this::checkForUpdates, "QuantumHackUpdater");
			thread.start();
			return;
		}
		
		if(thread.isAlive())
			return;
		
		if(component != null)
			ChatUtils.component(component);
		
		QuantumHackClient.INSTANCE.getEventManager()
			.remove(UpdateListener.class, this);
	}
	
	public void checkForUpdates()
	{
		Version currentVersion = new Version(QuantumHackClient.VERSION);
		Version latestVersion = null;
		
		try
		{
			WsonArray wson = JsonUtils.parseURLToArray(
				"https://api.github.com/repos/foldesandras/QuantumHack/releases");
			
			for(WsonObject release : wson.getAllObjects())
			{
				if(!currentVersion.isPreRelease()
					&& release.getBoolean("prerelease"))
					continue;
				
				if(!containsCompatibleAsset(release.getArray("assets")))
					continue;
				
				String tagName = release.getString("tag_name");
				latestVersion = new Version(tagName.substring(1));
				break;
			}
			
			if(latestVersion == null)
				throw new NullPointerException("Latest version is missing!");
			
			System.out.println("[Updater] Current version: " + currentVersion);
			System.out.println("[Updater] Latest version: " + latestVersion);
			outdated = currentVersion.shouldUpdateTo(latestVersion);
			
		}catch(Exception e)
		{
			System.err.println("[Updater] An error occurred!");
			e.printStackTrace();
		}
		
		String currentVersionEncoded = URLEncoder.encode(
			"Wurst " + currentVersion + " MC" + QuantumHackClient.MC_VERSION,
			StandardCharsets.UTF_8);
		
		String baseUrl = "https://www.wurstclient.net/download/";
		String utmSource = "Wurst+Client";
		String utmMedium = "WurstUpdater+chat+message";
		
		if(latestVersion == null || latestVersion.isInvalid())
		{
			String text = "An error occurred while checking for updates."
				+ " Click \u00a7nhere\u00a7r to check manually.";
			String url = baseUrl + "?utm_source=" + utmSource + "&utm_medium="
				+ utmMedium + "&utm_content=" + currentVersionEncoded
				+ "+error+checking+updates+chat+message";
			showLink(text, url);
			return;
		}
		
		if(!outdated)
			return;
		
		String text = "Wurst " + latestVersion
			+ " is now available for Minecraft " + QuantumHackClient.MC_VERSION
			+ ". \u00a7nUpdate now\u00a7r to benefit from new features and/or bugfixes!";
		String utmContent = currentVersionEncoded + "+update+chat+message";
		
		String url = baseUrl + "?utm_source=" + utmSource + "&utm_medium="
			+ utmMedium + "&utm_content=" + utmContent;
		
		showLink(text, url);
	}
	
	private void showLink(String text, String url)
	{
		ClickEvent event = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
		component = Text.literal(text).styled(s -> s.withClickEvent(event));
	}
	
	private boolean containsCompatibleAsset(WsonArray wsonArray)
		throws JsonException
	{
		String compatibleSuffix = "MC" + QuantumHackClient.MC_VERSION + ".jar";
		
		for(WsonObject asset : wsonArray.getAllObjects())
		{
			String assetName = asset.getString("name");
			if(!assetName.endsWith(compatibleSuffix))
				continue;
			
			return true;
		}
		
		return false;
	}
	
	public boolean isOutdated()
	{
		return outdated;
	}
}
