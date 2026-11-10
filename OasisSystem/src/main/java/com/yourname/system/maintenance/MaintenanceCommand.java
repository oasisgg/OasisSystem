package com.yourname.duel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MaintenanceCommand implements CommandExecutor {

    private final Plugin plugin;

    public MaintenanceCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // --- 1. 権限チェック (管理者以外は絶対に実行させない) ---
        if (!sender.hasPermission("duel.admin")) {
            sender.sendMessage("§cコマンドを実行する権限がありません。");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§c使用方法: /maintenance <on|off>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        if (subCommand.equals("on")) {
            plugin.getConfig().set("maintenance.enabled", true);
            plugin.saveConfig();

            // メンテナンスONにした瞬間、管理者以外のオンラインプレイヤーを全員Kickする
            String rawMessage = plugin.getConfig().getString("maintenance.message", "§cこのサーバーはメンテナンス中です。");
            String kickMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);

            for (Player player : Bukkit.getOnlinePlayers()) {
                // 管理者権限持ちはKickされないように除外
                if (!player.hasPermission("duel.admin")) {
                    player.kickPlayer(kickMessage);
                }
            }

            Bukkit.broadcastMessage("§c《メンテナンス》|サーバーがメンテナンスモードになりました。メンバーはkickされます。");
            return true;

        } else if (subCommand.equals("off")) {
            plugin.getConfig().set("maintenance.enabled", false);
            plugin.saveConfig();

            Bukkit.broadcastMessage("§a《メンテナンス》|メンテナンスが終了しました。通常通り参加できます。");
            return true;
        } else {
            sender.sendMessage("§c使用方法: /maintenance <on|off>");
            return true;
        }
    }
}
