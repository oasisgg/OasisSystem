package com.yourname.system;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.Plugin;

public class MaintenanceListener implements Listener {

    private final Plugin plugin;

    public MaintenanceListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // メンテナンスが有効かどうかチェック
        boolean isMaintenance = plugin.getConfig().getBoolean("maintenance.enabled", false);
        if (!isMaintenance) return;

        Player player = event.getPlayer();

        // 管理者権限（duel.admin）を持っている場合は参加を許可
        if (player.hasPermission("duel.admin")) {
            return;
        }

        // 一般プレイヤーの場合はログインを拒否し、切断画面にメッセージを表示
        String rawMessage = plugin.getConfig().getString("maintenance.message", "§cこのサーバーはメンテナンス中です。");
        String kickMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);
        
        // ログイン段階で切断してメッセージを表示する
        event.disallow(PlayerLoginEvent.Result.KICK_OTHER, kickMessage);
    }
}
