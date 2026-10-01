package me.deadlight.ezchestshop.utils;

import java.util.Map;
import java.util.WeakHashMap;

import dev.triumphteam.gui.guis.BaseGui;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public final class ShopGuiTracker {

    private static final Map<BaseGui, Location> GUIS = new WeakHashMap<>();

    private ShopGuiTracker() {}

    public static void track(BaseGui gui, Block containerBlock) {
        if (gui == null || containerBlock == null) {
            return;
        }
        Location shopLocation = Utils.resolveShopLocation(containerBlock);
        GUIS.put(gui, shopLocation != null ? shopLocation : containerBlock.getLocation());
    }

    public static void closeAll(Location shopLocation) {
        if (shopLocation == null) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder(false);
            if (holder instanceof BaseGui gui && Utils.isSameBlock(GUIS.get(gui), shopLocation)) {
                player.closeInventory();
            }
        }
        GUIS.values().removeIf(location -> Utils.isSameBlock(location, shopLocation));
    }
}
