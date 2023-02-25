package net.novauniverse.capturetheflag.game.mapmodules;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.JSONObject;

import net.novauniverse.capturetheflag.NovaCaptureTheFlag;
import net.novauniverse.capturetheflag.game.CaptureTheFlag;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.map.mapmodule.MapModule;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class GiveWoolSlow extends MapModule {
	public static final int WOOL_SLOT = 2;

	private final SimpleTask task;
	private final int maxItems;
	private final int tickBetweenItem;
	private final CaptureTheFlag ctf;

	public GiveWoolSlow(JSONObject json) {
		super(json);

		tickBetweenItem = json.optInt("tick_between_give", 20);
		maxItems = json.optInt("max_items", 16);

		ctf = NovaCaptureTheFlag.getInstance().getGame();

		task = new SimpleTask(NovaCaptureTheFlag.getInstance(), () -> {
			Bukkit.getServer().getOnlinePlayers().stream().filter(this::shouldCheck).forEach(player -> {
				ItemStack item = player.getInventory().getItem(WOOL_SLOT);
				if (item == null) {
					player.getInventory().setItem(WOOL_SLOT, ctf.getWoolItemStack(player));
				} else {
					if (item.getAmount() < maxItems) {
						item.setAmount(item.getAmount() + 1);
					}
				}
			});
		}, tickBetweenItem, tickBetweenItem);

		ctf.addPlayerTpToTeamCallback((player) -> {
			player.getInventory().setItem(GiveWoolSlow.WOOL_SLOT, new ItemBuilder(ctf.getWoolItemStack(player)).setAmount(16).build());
		});
	}

	private boolean shouldCheck(Player player) {
		if (!ctf.isPlayerInGame(player)) {
			return false;
		}

		if (player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}

		if (player.getGameMode() == GameMode.CREATIVE) {
			return false;
		}

		return true;
	}

	@Override
	public void onGameBegin(Game game) {
		Task.tryStartTask(task);
	}

	@Override
	public void onGameEnd(Game game) {
		Task.tryStopTask(task);
	}
}