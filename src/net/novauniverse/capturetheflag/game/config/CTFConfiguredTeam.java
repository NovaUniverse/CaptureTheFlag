package net.novauniverse.capturetheflag.game.config;

import net.zeeraa.novacore.spigot.utils.LocationData;
import net.zeeraa.novacore.spigot.utils.VectorArea;

public class CTFConfiguredTeam {
	private final VectorArea flagArea;
	private final LocationData flagLocation;
	private final LocationData spawnLocation;

	public CTFConfiguredTeam(VectorArea flagArea, LocationData flagLocation, LocationData spawnLocation) {
		this.flagArea = flagArea;
		this.flagLocation = flagLocation;
		this.spawnLocation = spawnLocation;
	}

	public VectorArea getFlagArea() {
		return flagArea;
	}

	public LocationData getFlagLocation() {
		return flagLocation;
	}

	public LocationData getSpawnLocation() {
		return spawnLocation;
	}
}