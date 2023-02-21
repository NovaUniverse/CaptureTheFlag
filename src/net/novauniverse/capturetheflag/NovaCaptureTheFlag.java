package net.novauniverse.capturetheflag;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

import net.novauniverse.capturetheflag.game.CaptureTheFlag;
import net.novauniverse.capturetheflag.game.config.CaptureTheFlagConfig;
import net.novauniverse.capturetheflag.integration.TeamFlagColorProvider;
import net.novauniverse.capturetheflag.integration.standard.DefaultTeamFlagColorIntegration;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.utils.JSONFileUtils;
import net.zeeraa.novacore.spigot.gameengine.NovaCoreGameEngine;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModuleManager;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.mapselector.selectors.RandomMapSelector;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.mapselector.selectors.guivoteselector.GUIMapVote;
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.language.LanguageReader;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTracker;

public class NovaCaptureTheFlag extends JavaPlugin implements Listener {
	public static NovaCaptureTheFlag instance;
	private CaptureTheFlag game;
	private TeamFlagColorProvider teamFlagColorProvider;

	public static NovaCaptureTheFlag getInstance() {
		return instance;
	}

	public CaptureTheFlag getGame() {
		return game;
	}

	public TeamFlagColorProvider getTeamFlagColorProvider() {
		return teamFlagColorProvider;
	}

	public void setTeamFlagColorProvider(TeamFlagColorProvider teamFlagColorProvider) {
		this.teamFlagColorProvider = teamFlagColorProvider;
	}

	@Override
	public void onEnable() {
		NovaCaptureTheFlag.instance = this;

		this.teamFlagColorProvider = new DefaultTeamFlagColorIntegration();

		boolean combatTagging = getConfig().getBoolean("CombatTagging");
		GameManager.getInstance().setUseCombatTagging(combatTagging);
		Log.info("CaptureTheFlag", "Combat tagging " + (combatTagging ? "enabled" : "disabled"));

		File mapFolder = new File(this.getDataFolder().getPath() + File.separator + "Maps");
		File worldFolder = new File(this.getDataFolder().getPath() + File.separator + "Worlds");

		if (NovaCoreGameEngine.getInstance().getRequestedGameDataDirectory() != null) {
			mapFolder = new File(NovaCoreGameEngine.getInstance().getRequestedGameDataDirectory().getAbsolutePath() + File.separator + getName() + File.separator + "Maps");
			worldFolder = new File(NovaCoreGameEngine.getInstance().getRequestedGameDataDirectory().getAbsolutePath() + File.separator + getName() + File.separator + "Worlds");
		}

		File mapOverrides = new File(this.getDataFolder().getPath() + File.separator + "map_overrides.json");
		if (mapOverrides.exists()) {
			Log.info("Trying to read map overrides file");
			try {
				JSONObject mapFiles = JSONFileUtils.readJSONObjectFromFile(mapOverrides);

				boolean relative = mapFiles.getBoolean("relative");

				mapFolder = new File((relative ? this.getDataFolder().getPath() + File.separator : "") + mapFiles.getString("maps_folder"));
				worldFolder = new File((relative ? this.getDataFolder().getPath() + File.separator : "") + mapFiles.getString("worlds_folder"));

				Log.info("New paths:");
				Log.info("Map folder: " + mapFolder.getAbsolutePath());
				Log.info("World folder: " + worldFolder.getAbsolutePath());
			} catch (JSONException | IOException e) {
				e.printStackTrace();
				Log.error("Failed to read map overrides from file " + mapOverrides.getAbsolutePath());
			}
		}

		try {
			FileUtils.forceMkdir(getDataFolder());
			Log.info("NovaSurvivalGames", "Loading language files...");
			try {
				LanguageReader.readFromJar(this.getClass(), "/lang/en-us.json");
			} catch (Exception e) {
				e.printStackTrace();
			}
			FileUtils.forceMkdir(mapFolder);
			FileUtils.forceMkdir(worldFolder);
		} catch (IOException e1) {
			e1.printStackTrace();
			Log.fatal("Failed to setup data directory");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		boolean disableGameLobby = getConfig().getBoolean("DisableGameLobby");

		MapModuleManager.addMapModule("capturetheflag.config", CaptureTheFlagConfig.class);

		ModuleManager.require(CompassTracker.class);
		ModuleManager.require(GameManager.class);
		if (!disableGameLobby) {
			ModuleManager.require(GameLobby.class);
		}

		this.game = new CaptureTheFlag(instance);
		GameManager.getInstance().loadGame(game);

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		if (!disableGameLobby) {
			GUIMapVote mapSelector = new GUIMapVote();
			GameManager.getInstance().setMapSelector(mapSelector);
			Bukkit.getServer().getPluginManager().registerEvents(mapSelector, this);
		} else {
			GameManager.getInstance().setMapSelector(new RandomMapSelector());
		}

		Log.info(getName(), "Scheduled loading maps from " + mapFolder.getPath());
		GameManager.getInstance().readMapsFromFolderDelayed(mapFolder, worldFolder);
	}
}
