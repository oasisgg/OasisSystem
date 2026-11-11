package com.yourname.system;

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
        if (sender instanceof Player && !sender.hasPermission("duel.admin")) {
            sender.sendMessage("§cあなたにはこのコマンドを実行する権限がありません。");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§c使用方法: /maintenance <on|off>");
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String rawMessage = plugin.getConfig().getString("maintenance.message", "§cこのサーバーはメンテナンス中です。");
        String kickMessage = ChatColor.translateAlternateColorCodes('&', rawMessage);

        if (subCommand.equals("on")) {
            plugin.getConfig().set("maintenance.enabled", true);
            plugin.saveConfig();

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.hasPermission("duel.admin")) {
                    player.kickPlayer(kickMessage);
                }
            }

            Bukkit.broadcastMessage("§c[メンテナンス] サーバーがメンテナンスモードになりました。");
            sender.sendMessage("§aメンテナンスモードを有効にしました。");
            return true;

        } else if (subCommand.equals("off")) {
            plugin.getConfig().set("maintenance.enabled", false);
            plugin.saveConfig();

            Bukkit.broadcastMessage("§a[メンテナンス] メンテナンスが終了しました。通常通り参加できます！");
            sender.sendMessage("§aメンテナンスモードを解除しました。");
            return true;
        } else {
            sender.sendMessage("§c使用方法: /maintenance <on|off>");
            return true;
        }
    }
}
