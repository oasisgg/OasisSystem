package com.yourname.system.friend;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FriendCommand implements CommandExecutor {
    private final FriendManager friendManager;

    public FriendCommand(FriendManager friendManager) {
        this.friendManager = friendManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("プレイヤーのみ実行可能です。");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // 引数なしならGUIを開く
            FriendGUI.openMainGUI(player, friendManager);
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equalsIgnoreCase("invite") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§c指定したプレイヤーが見つからないか、オフラインです。");
                return true;
            }
            if (target.equals(player)) {
                player.sendMessage("§c自分自身にフレンド申請は送れません。");
                return true;
            }

            friendManager.sendRequest(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§a§l[Friend] §a" + target.getName() + " にフレンド申請を送りました！");
            target.sendMessage("§a§l[Friend] §a" + player.getName() + " からフレンド申請が届きました！ /f で確認できます。");
            target.playSound(target.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f);
            return true;
        }

        player.sendMessage("§c使い方: /f invite <プレイヤー名>");
        return true;
    }
}
