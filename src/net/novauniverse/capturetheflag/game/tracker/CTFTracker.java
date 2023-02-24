package net.novauniverse.capturetheflag.game.tracker;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.novauniverse.capturetheflag.game.CaptureTheFlag;
import net.novauniverse.capturetheflag.game.config.CTFTeam;
import net.novauniverse.capturetheflag.game.objects.flag.FlagState;
import net.zeeraa.novacore.spigot.gameengine.compass.trackers.ClosestEnemyPlayerTracker;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTarget;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTrackerTarget;

public class CTFTracker implements CompassTrackerTarget {
	private final CaptureTheFlag ctf;

	private ClosestEnemyPlayerTracker fallbackTracker;

	public CTFTracker(CaptureTheFlag ctf) {
		this.ctf = ctf;
		this.fallbackTracker = new ClosestEnemyPlayerTracker();
	}

	public ClosestEnemyPlayerTracker getFallbackTracker() {
		return fallbackTracker;
	}

	public void setFallbackTracker(ClosestEnemyPlayerTracker fallbackTracker) {
		this.fallbackTracker = fallbackTracker;
	}

	@Override
	public CompassTarget getCompassTarget(Player player) {
		CTFTeam team = ctf.getTeams().stream().filter(t -> t.isMember(player)).findFirst().orElse(null);
		if (team != null) {
			FlagState state = team.getFlagState();
			if (state == FlagState.CARRIED) {
				if (team.getFlag().isCarrierEnemy()) {
					return new CompassTarget(team.getFlag().getCarrier().getLocation(), ChatColor.RED + "Tracking enemy " + ChatColor.AQUA + team.getFlag().getCarrier().getName() + ChatColor.RED + " that is carrying your teams flag");
				} else {
					return new CompassTarget(team.getSpawnLocation(), ChatColor.GREEN + "Tracking home base");
				}
			} else if (state == FlagState.ON_GROUND) {
				return new CompassTarget(team.getFlag().getStand().getLocation(), ChatColor.GREEN + "Tracking your flag");
			}
		}
		return fallbackTracker.getCompassTarget(player);
	}
}