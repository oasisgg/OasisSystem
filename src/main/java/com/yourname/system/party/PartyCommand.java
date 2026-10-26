package com.yourname.system.party;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommand implements CommandExecutor {
    private final PartyManager partyManager;

    public PartyCommand(PartyManager partyManager) {
        this.partyManager = partyManager;
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
            PartyGUI.openMainGUI(player, partyManager);
            return true;
        }

        String sub = args[0].toLowerCase();

        // /p invite <プレイヤー名>
        if (sub.equalsIgnoreCase("invite") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                player.sendMessage("§c指定したプレイヤーが見つからないか、オフラインです。");
                return true;
            }
            if (target.equals(player)) {
                player.sendMessage("§c自分自身をパーティーに招待することはできません。");
                return true;
            }

            // 自分がパーティーに入っている場合はそのリーダーを取得、入っていなければ自分自身をリーダー（新規パーティー）とする
            UUID leader = partyManager.isInParty(player.getUniqueId()) ? partyManager.getPartyLeader(player.getUniqueId()) : player.getUniqueId();

            partyManager.sendInvite(leader, target.getUniqueId());
            player.sendMessage("§a§l[Party] §a" + target.getName() + " にパーティー招待を送りました！");
            target.sendMessage("§a§l[Party] §a" + player.getName() + " からパーティー招待が届きました！");
            target.sendMessage("§e/p accept " + player.getName() + " §7で参加、§c/p deny " + player.getName() + " §7で拒否できます。");
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
            return true;
        }

        // /p accept <プレイヤー名>
        if (sub.equalsIgnoreCase("accept") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cプレイヤーが見つかりません。");
                return true;
            }

            UUID leader = partyManager.isInParty(target.getUniqueId()) ? partyManager.getPartyLeader(target.getUniqueId()) : target.getUniqueId();
            
            if (partyManager.acceptInvite(player.getUniqueId(), leader)) {
                player.sendMessage("§a§l[Party] §a" + target.getName() + " のパーティーに参加しました！");
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1f, 1f);

                // パーティーメンバー全員に通知
                for (UUID memberUUID : partyManager.getPartyMembers(leader)) {
                    Player p = Bukkit.getPlayer(memberUUID);
                    if (p != null && !p.equals(player)) {
                        p.sendMessage("§a§l[Party] §a" + player.getName() + " がパーティーに参加しました！");
                    }
                }
            } else {
                player.sendMessage("§cそのプレイヤーからのパーティー招待は届いていません。");
            }
            return true;
        }

        // /p deny <プレイヤー名>
        if (sub.equalsIgnoreCase("deny") && args.length >= 2) {
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                player.sendMessage("§cプレイヤーが見つかりません。");
                return true;
            }

            UUID leader = partyManager.isInParty(target.getUniqueId()) ? partyManager.getPartyLeader(target.getUniqueId()) : target.getUniqueId();

            if (partyManager.denyInvite(player.getUniqueId(), leader)) {
                player.sendMessage("§c§l[Party] §c" + target.getName() + " からのパーティー招待を拒否しました。");
            } else {
                player.sendMessage("§cそのプレイヤーからのパーティー招待は届いていません。");
            }
            return true;
        }

        player.sendMessage("§c使い方: /p [invite|accept|deny] <プレイヤー名>");
        return true;
    }
}
