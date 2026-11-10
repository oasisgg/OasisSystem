package com.yourname.system.friend;

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

public class FriendGUI implements Listener {
    private static FriendManager friendManager;

    public FriendGUI(FriendManager manager) {
        friendManager = manager;
    }

    public static void openMainGUI(Player player, FriendManager manager) {
        friendManager = manager;
        Inventory inv = Bukkit.createInventory(null, 27, "§8フレンドメニュー");

        // フレンド一覧アイコン (スロット11)
        ItemStack friendHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta fMeta = friendHead.getItemMeta();
        fMeta.setDisplayName("§a§lフレンド一覧");
        List<String> fLore = new ArrayList<>();
        fLore.add("§7クリックしてフレンドを確認・管理します。");
        fMeta.setLore(fLore);
        friendHead.setItemMeta(fMeta);
        inv.setItem(11, friendHead);

        // 届いている申請アイコン (スロット15)
        ItemStack requestHead = new ItemStack(Material.BOOK);
        ItemMeta rMeta = requestHead.getItemMeta();
        rMeta.setDisplayName("§e§l届いているフレンド申請");
        List<String> rLore = new ArrayList<>();
        rLore.add("§7クリックして申請を確認します。");
        rMeta.setLore(rLore);
        requestHead.setItemMeta(rMeta);
        inv.setItem(15, requestHead);

        player.openInventory(inv);
    }

    // フレンド一覧GUIを開く
    public static void openFriendListGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8フレンド一覧");
        List<UUID> friends = friendManager.getFriends(player.getUniqueId());

        for (UUID uuid : friends) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            boolean isOnline = target.isOnline();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            String statusColor = isOnline ? "§a" : "§c";
            meta.setDisplayName(statusColor + (target.getName() != null ? target.getName() : "Unknown"));

            List<String> lore = new ArrayList<>();
            lore.add(isOnline ? "§a● オンライン" : "§c● オフライン");
            lore.add("§7クリックしてフレンドを管理");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    // 届いている申請一覧GUIを開く
    public static void openRequestsGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8フレンド申請一覧");
        List<UUID> requests = friendManager.getRequests(player.getUniqueId());

        for (UUID uuid : requests) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            meta.setDisplayName("§e" + (target.getName() != null ? target.getName() : "Unknown"));

            List<String> lore = new ArrayList<>();
            lore.add("§7クリックして管理");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    // 個別の申請管理GUI（承認・拒否）
    public static void openRequestActionGUI(Player player, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8申請管理: " + target.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName("§e" + target.getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        ItemStack acceptBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta aMeta = acceptBtn.getItemMeta();
        aMeta.setDisplayName("§a§l申請を承認する");
        acceptBtn.setItemMeta(aMeta);
        inv.setItem(11, acceptBtn);

        ItemStack denyBtn = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta dMeta = denyBtn.getItemMeta();
        dMeta.setDisplayName("§c§l申請を拒否する");
        denyBtn.setItemMeta(dMeta);
        inv.setItem(15, denyBtn);

        player.openInventory(inv);
    }

    // ① フレンド個別管理GUI（削除・戻る）
    public static void openFriendActionGUI(Player player, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8フレンド管理: " + target.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName("§e" + target.getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // 削除ボタン (スロット11)
        ItemStack removeBtn = new ItemStack(Material.BARRIER);
        ItemMeta rMeta = removeBtn.getItemMeta();
        rMeta.setDisplayName("§c§lフレンドを削除する");
        List<String> rLore = new ArrayList<>();
        rLore.add("§7フレンド削除できます。");
        rMeta.setLore(rLore);
        removeBtn.setItemMeta(rMeta);
        inv.setItem(11, removeBtn);

        // 戻るボタン (スロット15)
        ItemStack backBtn = new ItemStack(Material.ARROW);
        ItemMeta bMeta = backBtn.getItemMeta();
        bMeta.setDisplayName("§e§l戻る");
        backBtn.setItemMeta(bMeta);
        inv.setItem(15, backBtn);

        player.openInventory(inv);
    }

    // ② 本当に削除するか確認するGUI
    public static void openConfirmDeleteGUI(Player player, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§4本当に削除しますか?: " + target.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName("§c" + target.getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // はい（削除する） (スロット11)
        ItemStack yesBtn = new ItemStack(Material.RED_WOOL);
        ItemMeta yMeta = yesBtn.getItemMeta();
        yMeta.setDisplayName("§c§l削除");
        yesBtn.setItemMeta(yMeta);
        inv.setItem(11, yesBtn);

        // いいえ（戻る） (スロット15)
        ItemStack noBtn = new ItemStack(Material.LIME_WOOL);
        ItemMeta nMeta = noBtn.getItemMeta();
        nMeta.setDisplayName("§a§l戻る");
        noBtn.setItemMeta(nMeta);
        inv.setItem(15, noBtn);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§8フレンドメニュー")) {
            event.setCancelled(true);
            if (event.getRawSlot() == 11) {
                openFriendListGUI(player);
            } else if (event.getRawSlot() == 15) {
                openRequestsGUI(player);
            }
        } 
        else if (title.equals("§8フレンド一覧")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer target = meta.getOwningPlayer();
            if (target.getUniqueId() == null) return;

            // 即座に削除せず、フレンド個別管理GUIを開く
            openFriendActionGUI(player, target);
        } 
        else if (title.startsWith("§8フレンド管理: ")) {
            event.setCancelled(true);
            String targetName = title.replace("§8フレンド管理: ", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (event.getRawSlot() == 11) {
                // 削除確認画面へ進む
                openConfirmDeleteGUI(player, target);
            } else if (event.getRawSlot() == 15) {
                // フレンド一覧に戻る
                openFriendListGUI(player);
            }
        }
        else if (title.startsWith("§4本当に削除しますか?: ")) {
            event.setCancelled(true);
            String targetName = title.replace("§4本当に削除しますか?: ", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (event.getRawSlot() == 11) {
                // 「はい」：本当に削除を実行
                friendManager.removeFriend(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§c[Friend] " + target.getName() + " をフレンドから削除しました。");
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                // 「いいえ」：個別管理画面に戻る
                openFriendActionGUI(player, target);
            }
        }
        else if (title.equals("§8フレンド申請一覧")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer target = meta.getOwningPlayer();
            if (target.getUniqueId() == null) return;

            openRequestActionGUI(player, target);
        }
        else if (title.startsWith("§8申請管理: ")) {
            event.setCancelled(true);
            String targetName = title.replace("§8申請管理: ", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (event.getRawSlot() == 11) {
                friendManager.acceptRequest(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§a[Friend] " + target.getName() + " からのフレンド申請を承認しました！");
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§a[Friend] " + player.getName() + " がフレンド申請を承認しました！");
                }
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                friendManager.removeRequest(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§c[Friend] " + target.getName() + " からのフレンド申請を拒否しました。");
                player.closeInventory();
            }
        }
    }
}
