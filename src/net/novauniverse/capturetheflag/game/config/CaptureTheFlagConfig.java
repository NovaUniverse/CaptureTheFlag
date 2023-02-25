package net.novauniverse.capturetheflag.game.config;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;
import net.zeeraa.novacore.spigot.utils.LocationData;
import net.zeeraa.novacore.spigot.utils.VectorArea;

public class CaptureTheFlagConfig extends MapModule {
	private List<CTFConfiguredTeam> configuredTeams;
	
	private int flagTpBackY;
	private int respawnTime;

	private boolean debug;
	private boolean useActionBar;
	
	public CaptureTheFlagConfig(JSONObject json) {
		super(json);

		configuredTeams = new ArrayList<>();

		flagTpBackY = json.getInt("flag_tp_back_y");
		respawnTime = json.getInt("respawn_time");
		useActionBar = json.optBoolean("use_action_bar", true);
		debug = json.optBoolean("debug", false);

		JSONArray teams = json.getJSONArray("teams");
		for (int i = 0; i < teams.length(); i++) {
			JSONObject team = teams.getJSONObject(i);

			VectorArea flagRoom = VectorArea.fromJSON(team.getJSONObject("flag_room"));
			LocationData flagLocation = LocationData.fromJSON(team.getJSONObject("flag_location"));
			LocationData spawnLocation = LocationData.fromJSON(team.getJSONObject("spawn_location"));

			configuredTeams.add(new CTFConfiguredTeam(flagRoom, flagLocation, spawnLocation));
		}
	}

	public List<CTFConfiguredTeam> getConfiguredTeams() {
		return configuredTeams;
	}

	public int getFlagTpBackY() {
		return flagTpBackY;
	}

	public int getRespawnTime() {
		return respawnTime;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public boolean isUseActionBar() {
		return useActionBar;
	}
}