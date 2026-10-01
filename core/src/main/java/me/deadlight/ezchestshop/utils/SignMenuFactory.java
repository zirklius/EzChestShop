package me.deadlight.ezchestshop.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class SignMenuFactory {

    public static final int ACTION_INDEX = 9;
    public static final int SIGN_LINES = 4;

    public static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    public static final String NBT_BLOCK_ID = "minecraft:sign";

    private static final Set<SignMenuFactory> ACTIVE_FACTORIES = ConcurrentHashMap.newKeySet();

    private final Plugin plugin;

    private final Map<UUID, Menu> inputs;


    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputs = new ConcurrentHashMap<>();
        this.listen();
    }

    public Menu newMenu(List<String> text) {
        return new Menu(text);
    }

    private void listen() {
        ACTIVE_FACTORIES.add(this);
        Utils.nmsHandle.signFactoryListen(this);
    }

    public void unregister() {
        ACTIVE_FACTORIES.remove(this);
        Utils.nmsHandle.removeSignMenuFactoryListen(this);
    }

    public static void cancelAll(UUID playerId) {
        for (SignMenuFactory factory : ACTIVE_FACTORIES) {
            if (factory.inputs.remove(playerId) != null && factory.inputs.isEmpty()) {
                factory.unregister();
            }
        }
    }

    public static void cancelForShop(Location shopLocation) {
        if (shopLocation == null) {
            return;
        }
        for (SignMenuFactory factory : ACTIVE_FACTORIES) {
            factory.inputs.entrySet().removeIf(entry -> {
                Menu menu = entry.getValue();
                if (!Utils.isSameBlock(menu.getShopLocation(), shopLocation)) {
                    return false;
                }
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    menu.forceClose = true;
                    player.closeInventory();
                    Location fakeSign = menu.getLocation();
                    if (fakeSign != null && fakeSign.getWorld() != null) {
                        player.sendBlockChange(fakeSign, fakeSign.getBlock().getBlockData());
                    }
                }
                return true;
            });
            if (factory.inputs.isEmpty()) {
                factory.unregister();
            }
        }
    }

    public final class Menu {

        private final List<String> text;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private Location location;
        private Location shopLocation;

        private Player owner;

        private volatile boolean forceClose;

        Menu(List<String> text) {
            this.text = text;
        }

        public Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public Menu shopLocation(Location shopLocation) {
            if (shopLocation == null) {
                this.shopLocation = null;
                return this;
            }
            Location resolved = Utils.resolveShopLocation(shopLocation.getBlock());
            this.shopLocation = resolved != null ? resolved.clone() : shopLocation.clone();
            return this;
        }

        public void open(Player player) {
            this.owner = player;
            Utils.nmsHandle.openMenu(this, player);
        }

        /**
         * closes the menu. if force is true, the menu will close and will ignore the reopen
         * functionality. false by default.
         *
         * @param player the player
         * @param force decides whether it will reopen if reopen is enabled
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        public BiPredicate<Player, String[]> getResponse() {
            return response;
        }

        public boolean isForceClose() {
            return forceClose;
        }

        public boolean isReopenIfFail() {
            return reopenIfFail;
        }

        public Location getLocation() {
            return location;
        }

        public Location getShopLocation() {
            return shopLocation;
        }

        public Player getOwner() {
            return owner;
        }

        public List<String> getText() {
            return text;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public void close(Player player) {
            close(player, false);
        }

        public String color(String input) {
            return ChatColor.translateAlternateColorCodes('&', input);
        }

        public SignMenuFactory getFactory() {
            return SignMenuFactory.this;
        }
    }

    public Map<UUID, Menu> getInputs() {
        return inputs;
    }
}
