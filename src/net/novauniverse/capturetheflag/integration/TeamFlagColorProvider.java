package net.novauniverse.capturetheflag.integration;

import org.bukkit.DyeColor;

import net.zeeraa.novacore.spigot.teams.Team;

public interface TeamFlagColorProvider {
	DyeColor getTeamFlagColor(Team team);
}