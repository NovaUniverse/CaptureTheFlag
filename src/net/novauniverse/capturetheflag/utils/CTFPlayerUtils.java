package net.novauniverse.capturetheflag.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class CTFPlayerUtils {
	public static boolean isCloseToGround(Player player) {
		return player.getLocation().clone().add(0D, -0.3D, 0D).getBlock().getType() != Material.AIR;
	}
}