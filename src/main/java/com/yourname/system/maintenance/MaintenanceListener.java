package com.yourname.duel;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

public class MaintenanceListener implements Listener {

    private final Plugin plugin;

    public MaintenanceListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // メンテナンスが有効かどうかチェック
        boolean isMaintenance = plugin.getConfig().getBoolean("maintenance.enabled", false);
        if (!isMaintenance) return;

        // 管理者権限を持っている場合は参加を許可
        if (player.hasPermission("duel.admin")) {
            player.sendMessage("§6§l»§e現在サーバーはメンテナンス中です。§7権限があるため参加が許可されています。");
            return;
        }

        // 一般プレイヤーの場合はKickする
        String rawMessage = plugin.getConfig().getString("maintenance.message", "§cこのサーバーはメンテナンス中です。");
        String kickMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);
        
        player.kickPlayer(kickMessage);
    }
}
