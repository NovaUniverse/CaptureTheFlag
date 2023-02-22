package net.novauniverse.capturetheflag.game.objects.flag;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;

public class CTFRespawnTimer {
	private static final DecimalFormat df = new DecimalFormat("0.0");
	
	private final UUID uuid;
	private final Consumer<Player> onDone;
	private int ticksLeft;

	public CTFRespawnTimer(UUID uuid, int ticksLeft, Consumer<Player> onDone) {
		this.uuid = uuid;
		this.ticksLeft = ticksLeft;
		this.onDone = onDone;
	}

	public UUID getUuid() {
		return uuid;
	}

	public int getTicksLeft() {
		return ticksLeft;
	}

	public void tick() {
		if (ticksLeft > 0) {
			ticksLeft--;
			PlayerUtils.ifOnline(uuid, player -> {
				double time = ((double) ticksLeft) / 20D;
				VersionIndependentUtils.get().sendTitle(player, ChatColor.RED + "Respawning in " + ChatColor.AQUA + df.format(time), "", 0, 5, 0);
			});
			if (ticksLeft == 0) {
				PlayerUtils.ifOnline(uuid, onDone);
			}
		}
	}

	public boolean shouldRemove() {
		return ticksLeft <= 0;
	}
}