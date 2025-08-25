/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.quantumhackclient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.quantumhackclient.altmanager.AltManager;
import net.quantumhackclient.altmanager.Encryption;
import net.quantumhackclient.clickgui.ClickGui;
import net.quantumhackclient.command.CmdList;
import net.quantumhackclient.command.CmdProcessor;
import net.quantumhackclient.command.Command;
import net.quantumhackclient.event.EventManager;
import net.quantumhackclient.events.ChatOutputListener;
import net.quantumhackclient.events.GUIRenderListener;
import net.quantumhackclient.events.GUIRenderListener.GUIRenderEvent;
import net.quantumhackclient.events.KeyPressListener;
import net.quantumhackclient.events.PostMotionListener;
import net.quantumhackclient.events.PreMotionListener;
import net.quantumhackclient.events.UpdateListener;
import net.quantumhackclient.hack.Hack;
import net.quantumhackclient.hack.HackList;
import net.quantumhackclient.hud.IngameHUD;
import net.quantumhackclient.keybinds.KeybindList;
import net.quantumhackclient.keybinds.KeybindProcessor;
import net.quantumhackclient.mixinterface.IMinecraftClient;
import net.quantumhackclient.navigator.Navigator;
import net.quantumhackclient.other_feature.OtfList;
import net.quantumhackclient.other_feature.OtherFeature;
import net.quantumhackclient.settings.SettingsFile;
import net.quantumhackclient.update.ProblematicResourcePackDetector;
import net.quantumhackclient.update.QuantumHackUpdater;
import net.quantumhackclient.util.json.JsonException;

public enum QuantumHackClient
{
	INSTANCE;
	
	public static MinecraftClient MC;
	public static IMinecraftClient IMC;
	
	public static final String VERSION = "2.0.0";
	public static final String MC_VERSION = "1.20.1";
	private EventManager eventManager;
	private AltManager altManager;
	private HackList hax;
	private CmdList cmds;
	private OtfList otfs;
	private SettingsFile settingsFile;
	private Path settingsProfileFolder;
	private KeybindList keybinds;
	private ClickGui gui;
	private Navigator navigator;
	private CmdProcessor cmdProcessor;
	private IngameHUD hud;
	private RotationFaker rotationFaker;
	private FriendsList friends;
	private QuantumHackTranslator translator;
	
	private boolean enabled = true;
	private static boolean guiInitialized;
	private QuantumHackUpdater updater;
	private ProblematicResourcePackDetector problematicPackDetector;
	private Path wurstFolder;
	
	public void initialize()
	{
		System.out.println("Starting " + Branding.BRANDING_NAME + "...");
		
		MC = MinecraftClient.getInstance();
		IMC = (IMinecraftClient)MC;
		wurstFolder = createWurstFolder();
		
		String trackingID = "UA-52838431-5";
		String hostname = "client.wurstclient.net";
		Path analyticsFile = wurstFolder.resolve("analytics.json");
		
		eventManager = new EventManager(this);
		
		Path enabledHacksFile = wurstFolder.resolve("enabled-hacks.json");
		hax = new HackList(enabledHacksFile);
		
		cmds = new CmdList();
		
		otfs = new OtfList();
		
		Path settingsFile = wurstFolder.resolve("settings.json");
		settingsProfileFolder = wurstFolder.resolve("settings");
		this.settingsFile = new SettingsFile(settingsFile, hax, cmds, otfs);
		this.settingsFile.load();
		hax.tooManyHaxHack.loadBlockedHacksFile();
		
		Path keybindsFile = wurstFolder.resolve("keybinds.json");
		keybinds = new KeybindList(keybindsFile);
		
		Path guiFile = wurstFolder.resolve("windows.json");
		gui = new ClickGui(guiFile);
		
		Path preferencesFile = wurstFolder.resolve("preferences.json");
		navigator = new Navigator(preferencesFile, hax, cmds, otfs);
		
		Path friendsFile = wurstFolder.resolve("friends.json");
		friends = new FriendsList(friendsFile);
		friends.load();
		
		translator = new QuantumHackTranslator();
		
		cmdProcessor = new CmdProcessor(cmds);
		eventManager.add(ChatOutputListener.class, cmdProcessor);
		
		KeybindProcessor keybindProcessor =
			new KeybindProcessor(hax, keybinds, cmdProcessor);
		eventManager.add(KeyPressListener.class, keybindProcessor);
		
		hud = new IngameHUD();
		eventManager.add(GUIRenderListener.class, hud);
		
		rotationFaker = new RotationFaker();
		eventManager.add(PreMotionListener.class, rotationFaker);
		eventManager.add(PostMotionListener.class, rotationFaker);
		
		updater = new QuantumHackUpdater();
		eventManager.add(UpdateListener.class, updater);
		
		problematicPackDetector = new ProblematicResourcePackDetector();
		problematicPackDetector.start();
		
		Path altsFile = wurstFolder.resolve("alts.encrypted_json");
		Path encFolder = Encryption.chooseEncryptionFolder();
		altManager = new AltManager(altsFile, encFolder);
		
		if(FabricLoader.getInstance().isModLoaded("connectormod"))
		{
			System.out.println("[Wurst] Applying fixes for Sinytra Connector");
			HudRenderCallback.EVENT.register(this::onHudRender);
		}
	}
	
	// Alternative HUD rendering when using Sinytra Connector
	private void onHudRender(DrawContext context, float tickDelta)
	{
		if(MC.options.debugEnabled)
			return;
		
		RenderSystem.disableBlend();
		EventManager.fire(new GUIRenderEvent(context, tickDelta));
		RenderSystem.disableBlend();
	}
	
	private Path createWurstFolder()
	{
		Path dotMinecraftFolder = MC.runDirectory.toPath().normalize();
		Path wurstFolder = dotMinecraftFolder.resolve("quantumhack");
		
		try
		{
			Files.createDirectories(wurstFolder);
			
		}catch(IOException e)
		{
			throw new RuntimeException(
				"Couldn't create .minecraft/quantumhack folder.", e);
		}
		
		return wurstFolder;
	}
	
	public String translate(String key, Object... args)
	{
		return translator.translate(key, args);
	}
	
	public EventManager getEventManager()
	{
		return eventManager;
	}
	
	public void saveSettings()
	{
		settingsFile.save();
	}
	
	public ArrayList<Path> listSettingsProfiles()
	{
		if(!Files.isDirectory(settingsProfileFolder))
			return new ArrayList<>();
		
		try(Stream<Path> files = Files.list(settingsProfileFolder))
		{
			return files.filter(Files::isRegularFile)
				.collect(Collectors.toCollection(ArrayList::new));
			
		}catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public void loadSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.loadProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public void saveSettingsProfile(String fileName)
		throws IOException, JsonException
	{
		settingsFile.saveProfile(settingsProfileFolder.resolve(fileName));
	}
	
	public HackList getHax()
	{
		return hax;
	}
	
	public CmdList getCmds()
	{
		return cmds;
	}
	
	public OtfList getOtfs()
	{
		return otfs;
	}
	
	public Feature getFeatureByName(String name)
	{
		Hack hack = getHax().getHackByName(name);
		if(hack != null)
			return hack;
		
		Command cmd = getCmds().getCmdByName(name.substring(1));
		if(cmd != null)
			return cmd;
		
		OtherFeature otf = getOtfs().getOtfByName(name);
		return otf;
	}
	
	public KeybindList getKeybinds()
	{
		return keybinds;
	}
	
	public ClickGui getGui()
	{
		if(!guiInitialized)
		{
			guiInitialized = true;
			gui.init();
		}
		
		return gui;
	}
	
	public Navigator getNavigator()
	{
		return navigator;
	}
	
	public CmdProcessor getCmdProcessor()
	{
		return cmdProcessor;
	}
	
	public IngameHUD getHud()
	{
		return hud;
	}
	
	public RotationFaker getRotationFaker()
	{
		return rotationFaker;
	}
	
	public FriendsList getFriends()
	{
		return friends;
	}
	
	public QuantumHackTranslator getTranslator()
	{
		return translator;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
		
		if(!enabled)
		{
			hax.panicHack.setEnabled(true);
			hax.panicHack.onUpdate();
		}
	}
	
	public QuantumHackUpdater getUpdater()
	{
		return updater;
	}
	
	public ProblematicResourcePackDetector getProblematicPackDetector()
	{
		return problematicPackDetector;
	}
	
	public Path getWurstFolder()
	{
		return wurstFolder;
	}
	
	public AltManager getAltManager()
	{
		return altManager;
	}
}
