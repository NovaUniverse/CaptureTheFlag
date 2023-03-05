package net.novauniverse.capturetheflag.utils;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InventoryUtils {
	public static int ammountOfItem(Player toCheck, Material material) {
		List<Integer> slots = slotsWith(toCheck.getInventory(), material);
		int ammount = 0;
		for (int i : slots) {
			ammount += toCheck.getInventory().getItem(i).getAmount();
		}

		if (toCheck.getItemOnCursor().getType() == material) {
			ammount += toCheck.getItemOnCursor().getAmount();
		}

		return ammount;
	}

	public static List<Integer> slotsWith(Inventory inventory, Collection<Material> materials) {
		ArrayList<Integer> slots = new ArrayList<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (materials != null) {
				if (inventory.getItem(i) != null) {
					for (Material material : materials) {
						if (inventory.getItem(i).getType() == material) {
							slots.add(i);
						}
					}
				}

			} else {
				if (inventory.getItem(i) == null) {
					slots.add(i);
				}
			}
		}
		return slots;
	}

	public static List<Integer> slotsWith(Inventory inventory, Material... materials) {
		ArrayList<Integer> slots = new ArrayList<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (materials != null) {
				if (inventory.getItem(i) != null) {
					for (Material material : materials) {
						if (inventory.getItem(i).getType() == material) {
							slots.add(i);
						}
					}
				}

			} else {
				if (inventory.getItem(i) == null) {
					slots.add(i);
				}
			}
		}
		return slots;
	}

	public static List<Integer> slotsWith(Inventory inventory, Material material) {
		ArrayList<Integer> slots = new ArrayList<>();
		for (int i = 0; i < inventory.getSize(); i++) {
			if (material != null) {
				if (inventory.getItem(i) != null) {
					if (inventory.getItem(i).getType() == material) {
						slots.add(i);
					}
				}

			} else {
				if (inventory.getItem(i) == null) {
					slots.add(i);
				}
			}
		}
		return slots;
	}
}