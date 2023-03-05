package net.novauniverse.capturetheflag.game.mapmodules;

import net.novauniverse.capturetheflag.utils.InventoryUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
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
				if (InventoryUtils.ammountOfItem(player, Material.WOOL) < maxItems) {
					player.getInventory().addItem(ctf.getWoolItemStack(player));
				}
			});
		}, tickBetweenItem, tickBetweenItem);
	}

	private boolean shouldCheck(Player player) {
		if (!ctf.isPlayerInGame(player)) {
			return false;
		}

		if (player.getGameMode() == GameMode.SPECTATOR || player.getGameMode() == GameMode.CREATIVE) {
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