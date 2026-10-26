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
        iLore.add("§7クリックして届いた招待を承認・拒否します。");
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
            meta.setDisplayName((isLeader ? "§e[Leader] " : "§a[Member] ") + target.getName());

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
            meta.setDisplayName("§a" + leader.getName() + " のパーティー");

            List<String> lore = new ArrayList<>();
            lore.add("§a[左クリック] 承認する");
            lore.add("§c[右クリック] 拒否する");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

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
        } else if (title.equals("§8パーティーメンバー一覧")) {
            event.setCancelled(true);
        } else if (title.equals("§8パーティー招待一覧")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta.getOwningPlayer() == null) return;

            OfflinePlayer leader = meta.getOwningPlayer();

            if (event.isLeftClick()) {
                // 左クリックで承認
                player.performCommand("p accept " + leader.getName());
                player.closeInventory();
            } else if (event.isRightClick()) {
                // 右クリックで拒否
                player.performCommand("p deny " + leader.getName());
                player.closeInventory();
            }
        }
    }
}
