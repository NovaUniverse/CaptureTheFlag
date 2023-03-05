package net.novauniverse.capturetheflag.integration.standard;

import org.bukkit.DyeColor;

import net.novauniverse.capturetheflag.integration.TeamFlagColorProvider;
import net.zeeraa.novacore.spigot.teams.Team;

public class DefaultTeamFlagColorIntegration implements TeamFlagColorProvider {
	@Override
	public DyeColor getTeamFlagColor(Team team) {
		switch (team.getTeamColor()) {
		case BLACK:
			return DyeColor.BLACK;

		case DARK_BLUE:
		case BLUE:
			return DyeColor.BLUE;

		case GOLD:
			return DyeColor.ORANGE;

		case AQUA:
			return DyeColor.LIGHT_BLUE;

		case DARK_AQUA:
			return DyeColor.CYAN;

		case DARK_GRAY:
			return DyeColor.GRAY;

		case DARK_GREEN:
			return DyeColor.GREEN;

		case DARK_PURPLE:
			return DyeColor.PURPLE;

		case DARK_RED:
		case RED:
			return DyeColor.RED;

		case GRAY:
			return DyeColor.SILVER;

		case GREEN:
			return DyeColor.LIME;

		case LIGHT_PURPLE:
			return DyeColor.MAGENTA;

		case WHITE:
			return DyeColor.WHITE;

		case YELLOW:
			return DyeColor.YELLOW;

		default:
			return DyeColor.WHITE;
		}
	}
}