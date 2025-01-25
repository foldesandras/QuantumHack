/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient.other_features;

import java.nio.file.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.quantumhackclient.Category;
import net.quantumhackclient.Feature;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.keybinds.Keybind;
import net.quantumhackclient.keybinds.KeybindList;
import net.quantumhackclient.other_feature.OtherFeature;
import net.quantumhackclient.settings.Setting;
import net.quantumhackclient.util.ChatUtils;
import net.quantumhackclient.util.json.JsonUtils;

public final class WikiDataExportOtf extends OtherFeature
{
	public WikiDataExportOtf()
	{
		super("WikiDataExport",
			"Creates a JSON file full of technical details about all the"
				+ " different features and settings in this version of Wurst."
				+ " Primarily used to update the Wurst Wiki.");
	}
	
	@Override
	public String getPrimaryAction()
	{
		return "Export Data";
	}
	
	@Override
	public void doPrimaryAction()
	{
		try
		{
			Path exportFile = QUANTUM_HACK.getWurstFolder().resolve("wiki-data.json");
			
			JsonObject json = new JsonObject();
			for(Hack hack : QUANTUM_HACK.getHax().getAllHax())
				json.add(hack.getName(), hackToJson(hack));
			
			JsonUtils.toJson(json, exportFile);
			
		}catch(Exception e)
		{
			ChatUtils.error("Failed to export data: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private JsonObject hackToJson(Hack hack)
	{
		JsonObject json = new JsonObject();
		json.addProperty("name", hack.getName());
		json.addProperty("descriptionKey", hack.getDescriptionKey());
		json.addProperty("type", "Hack");
		Category category = hack.getCategory();
		if(category != null)
			json.addProperty("category", category.getName());
		json.addProperty("keybind", getDefaultKeybind(hack));
		json.addProperty("stateSaved", hack.isStateSaved());
		json.addProperty("class", hack.getClass().getName());
		
		JsonArray settings = new JsonArray();
		for(Setting setting : hack.getSettings().values())
			settings.add(setting.exportWikiData());
		json.add("settings", settings);
		return json;
	}
	
	private String getDefaultKeybind(Feature feature)
	{
		String name = feature.getName().toLowerCase().replace(" ", "_");
		if(name.startsWith("."))
			name = name.substring(1);
		
		for(Keybind keybind : KeybindList.DEFAULT_KEYBINDS)
			if(keybind.getCommands().toLowerCase().contains(name))
				return keybind.getKey();
			
		return null;
	}
}
