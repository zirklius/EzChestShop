package me.deadlight.ezchestshop.listeners;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.utils.Utils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ShopExplosionListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        removeExplodedShops(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        removeExplodedShops(event.blockList());
    }

    private void removeExplodedShops(List<Block> blocks) {
        Set<Location> explodedShops = new HashSet<>();
        for (Block block : blocks) {
            Location shopLocation = Utils.resolveShopLocation(block);
            if (shopLocation != null) {
                explodedShops.add(shopLocation);
            }
        }
        for (Location shopLocation : explodedShops) {
            ShopContainer.deleteShop(shopLocation);
        }
    }
}
