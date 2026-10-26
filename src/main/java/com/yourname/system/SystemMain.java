package com.yourname.system;

import com.yourname.system.friend.FriendCommand;
import com.yourname.system.friend.FriendGUI;
import com.yourname.system.friend.FriendManager;
import com.yourname.system.party.PartyCommand;
import com.yourname.system.party.PartyGUI;
import com.yourname.system.party.PartyManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SystemMain extends JavaPlugin implements CommandExecutor, Listener {

    private static SystemMain instance;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 1. フレンド機能の登録
        FriendManager friendManager = new FriendManager(this);
        FriendCommand friendCmd = new FriendCommand(friendManager);
        if (getCommand("f") != null) {
            getCommand("f").setExecutor(friendCmd);
        }
        Bukkit.getPluginManager().registerEvents(new FriendGUI(friendManager), this);

        // 2. パーティー機能の登録
        PartyManager partyManager = new PartyManager();
        PartyCommand partyCmd = new PartyCommand(partyManager);
        if (getCommand("p") != null) {
            getCommand("p").setExecutor(partyCmd);
        }
        Bukkit.getPluginManager().registerEvents(new PartyGUI(partyManager), this);

        // 3. システムコマンドの登録
        if (getCommand("hub") != null) getCommand("hub").setExecutor(this);
        if (getCommand("leave") != null) getCommand("leave").setExecutor(this);
        if (getCommand("sethub") != null) getCommand("sethub").setExecutor(this);

        // 4. イベントの登録
        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("SystemPlugin が正常に有効化されました！");
    }

    @Override
    public void onDisable() {
        getLogger().info("SystemPlugin が停止しました。");
    }

    public static SystemMain getInstance() {
        return instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("プレイヤーのみ実行できます。");
            return true;
        }

        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("sethub")) {
            if (!player.hasPermission("system.admin")) {
                player.sendMessage("§c権限がありません。");
                return true;
            }
            saveHubLocation(player.getLocation());
            player.sendMessage("§a§l[System] §aロビーのスポーン地点を設定しました！");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;
        }

        if (command.getName().equalsIgnoreCase("hub") || command.getName().equalsIgnoreCase("leave")) {
            teleportToHub(player);
            return true;
        }

        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double voidY = getConfig().getDouble("void-y", 0.0);

        if (player.getLocation().getY() < voidY) {
            teleportToHub(player);
        }
    }

    public void teleportToHub(Player player) {
        Location hubLoc = getHubLocation();
        if (hubLoc == null) {
            player.sendMessage("§cロビーの位置が設定されていません！ /sethub で設定してください。");
            return;
        }

        player.teleport(hubLoc);
        player.setFallDistance(0);
        player.sendMessage("§a§l[System] §aロビーへ移動しました！");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private void saveHubLocation(Location loc) {
        getConfig().set("hub.world", loc.getWorld().getName());
        getConfig().set("hub.x", loc.getX());
        getConfig().set("hub.y", loc.getY());
        getConfig().set("hub.z", loc.getZ());
        getConfig().set("hub.yaw", loc.getYaw());
        getConfig().set("hub.pitch", loc.getPitch());
        saveConfig();
    }

    private Location getHubLocation() {
        if (!getConfig().contains("hub.world")) return null;
        String worldName = getConfig().getString("hub.world");
        double x = getConfig().getDouble("hub.x");
        double y = getConfig().getDouble("hub.y");
        double z = getConfig().getDouble("hub.z");
        float yaw = (float) getConfig().getDouble("hub.yaw");
        float pitch = (float) getConfig().getDouble("hub.pitch");

        if (Bukkit.getWorld(worldName) == null) return null;
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
