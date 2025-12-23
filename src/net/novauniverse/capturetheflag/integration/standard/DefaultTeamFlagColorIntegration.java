package net.novauniverse.capturetheflag.integration.standard;

import org.bukkit.DyeColor;
import net.md_5.bungee.api.ChatColor;
import net.novauniverse.capturetheflag.integration.TeamFlagColorProvider;
import net.zeeraa.novacore.spigot.teams.Team;

public class DefaultTeamFlagColorIntegration implements TeamFlagColorProvider {
	@Override
	public DyeColor getTeamFlagColor(Team team) {
		if (team.getTeamColor() == ChatColor.BLACK) {
			return DyeColor.BLACK;
		} else if (team.getTeamColor() == ChatColor.DARK_BLUE || team.getTeamColor() == ChatColor.BLUE) {
			return DyeColor.BLUE;
		} else if (team.getTeamColor() == ChatColor.GOLD) {
			return DyeColor.ORANGE;
		} else if (team.getTeamColor() == ChatColor.AQUA) {
			return DyeColor.LIGHT_BLUE;
		} else if (team.getTeamColor() == ChatColor.DARK_AQUA) {
			return DyeColor.CYAN;
		} else if (team.getTeamColor() == ChatColor.DARK_GRAY) {
			return DyeColor.GRAY;
		} else if (team.getTeamColor() == ChatColor.DARK_GREEN) {
			return DyeColor.GREEN;
		} else if (team.getTeamColor() == ChatColor.DARK_PURPLE) {
			return DyeColor.PURPLE;
		} else if (team.getTeamColor() == ChatColor.DARK_RED || team.getTeamColor() == ChatColor.RED) {
			return DyeColor.RED;
		} else if (team.getTeamColor() == ChatColor.GRAY) {
			return DyeColor.SILVER;
		} else if (team.getTeamColor() == ChatColor.GREEN) {
			return DyeColor.LIME;
		} else if (team.getTeamColor() == ChatColor.LIGHT_PURPLE) {
			return DyeColor.MAGENTA;
		} else if (team.getTeamColor() == ChatColor.WHITE) {
			return DyeColor.WHITE;
		} else if (team.getTeamColor() == ChatColor.YELLOW) {
			return DyeColor.YELLOW;
		} else {
			return DyeColor.WHITE;
		}
	}
}