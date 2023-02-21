package net.novauniverse.capturetheflag.game.config;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.World;

import net.novauniverse.capturetheflag.NovaCaptureTheFlag;
import net.novauniverse.capturetheflag.game.objects.flag.CTFFlag;
import net.novauniverse.capturetheflag.game.objects.flag.FlagState;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.utils.VectorArea;

public class CTFTeam {
	private VectorArea flagArea;
	private Location flagLocation;
	private Location spawnLocation;

	private final World world;
	private final Team team;
	private final CTFFlag flag;
	private final DyeColor flagColor;

	private boolean active;

	public CTFTeam(CTFConfiguredTeam data, Team team, World world) {
		this.flagArea = data.getFlagArea();
		this.flagLocation = data.getFlagLocation().toLocation(world);
		this.spawnLocation = data.getSpawnLocation().toLocation(world);
		this.flagColor = NovaCaptureTheFlag.getInstance().getTeamFlagColorProvider().getTeamFlagColor(team);

		this.world = world;

		this.flag = new CTFFlag(this);

		this.active = false;

		this.team = team;
	}
	
	public void tick() {
		flag.tick();
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public void deactivate() {
		active = false;
		flag.deactivate();
	}

	public DyeColor getFlagColor() {
		return flagColor;
	}

	public World getWorld() {
		return world;
	}

	public CTFFlag getFlag() {
		return flag;
	}

	public FlagState getFlagState() {
		return flag.getState();
	}

	public Team getTeam() {
		return team;
	}

	public boolean isInFlagArea(Location location) {
		return flagArea.isInsideBlock(location);
	}

	public VectorArea getFlagArea() {
		return flagArea;
	}

	public Location getFlagLocation() {
		return flagLocation;
	}

	public Location getSpawnLocation() {
		return spawnLocation;
	}
	
	public boolean hasFlag() {
		FlagState state = getFlagState();
		return state != FlagState.CAPTURED && state != FlagState.DEACTIVATED;
	}
}