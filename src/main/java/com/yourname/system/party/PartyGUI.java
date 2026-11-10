package com.yourname.system.party;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyGUI implements Listener {
    private static PartyManager partyManager;

    public PartyGUI(PartyManager manager) {
        partyManager = manager;
    }

    public static void openMainGUI(Player player, PartyManager manager) {
        partyManager = manager;
        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティーメニュー");

        // パーティーメンバー一覧 (スロット11)
        ItemStack memberItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta mMeta = memberItem.getItemMeta();
        mMeta.setDisplayName("§a§lパーティーメンバー一覧");
        List<String> mLore = new ArrayList<>();
        mLore.add("§7現在のパーティーメンバーを確認します。");
        mMeta.setLore(mLore);
        memberItem.setItemMeta(mMeta);
        inv.setItem(11, memberItem);

        // 届いている招待一覧 (スロット15)
        ItemStack inviteItem = new ItemStack(Material.PAPER);
        ItemMeta iMeta = inviteItem.getItemMeta();
        iMeta.setDisplayName("§e§l届いているパーティー招待");
        List<String> iLore = new ArrayList<>();
        iLore.add("§7クリックして届いた招待を確認します。");
        iMeta.setLore(iLore);
        inviteItem.setItemMeta(iMeta);
        inv.setItem(15, inviteItem);

        player.openInventory(inv);
    }

    public static void openMembersGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティーメンバー一覧");
        
        if (!partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cあなたはパーティーに入っていません。");
            return;
        }

        UUID leader = partyManager.getPartyLeader(player.getUniqueId());
        List<UUID> members = partyManager.getPartyMembers(leader);

        for (UUID uuid : members) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            boolean isLeader = uuid.equals(leader);
            meta.setDisplayName((isLeader ? "§e[Leader] " : "§a[Member] ") + (target.getName() != null ? target.getName() : "Unknown"));

            List<String> lore = new ArrayList<>();
            lore.add(target.isOnline() ? "§a● オンライン" : "§c● オフライン");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    public static void openRequestsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティー招待一覧");
        List<UUID> invites = partyManager.getInvites(player.getUniqueId());

        if (invites.isEmpty()) {
            player.sendMessage("§c届いているパーティー招待はありません。");
            return;
        }

        for (UUID leaderUUID : invites) {
            OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderUUID);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(leader);
            meta.setDisplayName("§a" + (leader.getName() != null ? leader.getName() : "Unknown") + " のパーティー");

            List<String> lore = new ArrayList<>();
            lore.add("§7クリックして承認・拒否を選ぶ");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    // 統合版対応：個別のパーティー招待管理GUI
    public static void openPartyRequestActionGUI(Player player, OfflinePlayer leader) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティ招待管理: " + leader.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(leader);
        meta.setDisplayName("§e" + leader.getName() + " のパーティー");
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // 承認ボタン (スロット11: エメラルドブロック)
        ItemStack acceptBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta aMeta = acceptBtn.getItemMeta();
        aMeta.setDisplayName("§a§l招待を承認する");
        acceptBtn.setItemMeta(aMeta);
        inv.setItem(11, acceptBtn);

        // 拒否ボタン (スロット15: レッドストーンブロック)
        ItemStack denyBtn = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta dMeta = denyBtn.getItemMeta();
        dMeta.setDisplayName("§c§l招待を拒否する");
        denyBtn.setItemMeta(dMeta);
        inv.setItem(15, denyBtn);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§8パーティーメニュー")) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) {
                openMembersGUI(player);
            } else if (event.getRawSlot() == 15) {
                openRequestsGUI(player);
            }
        } 
        else if (title.equals("§8パーティーメンバー一覧")) {
            event.setCancelled(true);
        } 
        else if (title.equals("§8パーティー招待一覧")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer leader = meta.getOwningPlayer();
            // 個別の管理画面を開く
            openPartyRequestActionGUI(player, leader);
        }
        else if (title.startsWith("§8パーティ招待管理: ")) {
            event.setCancelled(true);
            String leaderName = title.replace("§8パーティ招待管理: ", "");
            OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderName);

            if (event.getRawSlot() == 11) {
                // 承認
                player.performCommand("p accept " + leader.getName());
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                // 拒否
                player.performCommand("p deny " + leader.getName());
                player.closeInventory();
            }
        }
    }
}
