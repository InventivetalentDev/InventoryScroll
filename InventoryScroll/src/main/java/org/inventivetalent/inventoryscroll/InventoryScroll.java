package org.inventivetalent.inventoryscroll;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.pluginannotations.PluginAnnotations;
import org.inventivetalent.pluginannotations.config.ConfigValue;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class InventoryScroll extends JavaPlugin implements Listener {

	Set<UUID> scrollTimeout = new HashSet<>();

	@ConfigValue(path = "skipEmpty") boolean skipEmpty;
	@ConfigValue(path = "scrollAll") boolean scrollAll;

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		saveDefaultConfig();
		PluginAnnotations.CONFIG.loadValues(this, this);
	}

	@EventHandler
	public void on(final PlayerItemHeldEvent event) {
		if (scrollTimeout.contains(event.getPlayer().getUniqueId())) {
			scrollTimeout.remove(event.getPlayer().getUniqueId());
			return;
		}
		if (event.isCancelled()) {
			return;
		}
		if (!event.getPlayer().hasPermission("inventoryscroll.use")) {
			return;
		}

		final Player player = event.getPlayer();
		if (player.isSneaking()) {
			int from = event.getPreviousSlot();
			int to = event.getNewSlot();
			if (from == 8 && to == 0) { to = 9; }
			if (from == 0 && to == 8) { to = -1; }

			PlayerInventory inventory = player.getInventory();

			if (scrollAll) {
				for (int i = 0; i < 9; i++) {
					scrollItems(inventory, i, to > from);
				}
			} else {
				if (skipEmpty) {
					for (int i = 0; i < 4; i++) {
						scrollItems(inventory, from, to > from);
						if (inventory.getItem(from) != null && inventory.getItem(from).getAmount() > 0 && inventory.getItem(from).getType() != Material.AIR) {
							break;
						}
					}
				} else {
					scrollItems(inventory, from, to > from);
				}
			}

			//Cancelling the event would reset the item and mess up our modification, so only reset the selected slot
			//But setting the slot will call the event again...
			scrollTimeout.add(player.getUniqueId());
			inventory.setHeldItemSlot(from);
		}
	}

	void scrollItems(Inventory inventory, int slot, boolean direction) {
		int firstRow = slot + 9;//9-17
		int secondRow = slot + 18;//18-26
		int thirdRow = slot + 27;//27-35
		int fourthRow = slot;//Hotbar | 0-8

		ItemStack firstRowItem = inventory.getItem(firstRow);
		ItemStack secondRowItem = inventory.getItem(secondRow);
		ItemStack thirdRowItem = inventory.getItem(thirdRow);
		ItemStack fourthRowItem = inventory.getItem(fourthRow);

		if (direction) {
			inventory.setItem(firstRow, fourthRowItem);
			inventory.setItem(secondRow, firstRowItem);
			inventory.setItem(thirdRow, secondRowItem);
			inventory.setItem(fourthRow, thirdRowItem);
		} else {
			inventory.setItem(fourthRow, firstRowItem);
			inventory.setItem(thirdRow, fourthRowItem);
			inventory.setItem(secondRow, thirdRowItem);
			inventory.setItem(firstRow, secondRowItem);
		}
	}

}
