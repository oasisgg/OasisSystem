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
            lore.add("§c§l[!] クリックしてフレンド解除");
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
            lore.add("§7クリックして承認・拒否を選ぶ");
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    // 個別の申請管理GUIを開く（統合版対応：ボタン式）
    public static void openRequestActionGUI(Player player, OfflinePlayer target) {
        // インベントリのタイトルに相手の名前やUUIDを目印として持たせるか、あるいはシンプルに固定タイトルにする
        Inventory inv = Bukkit.createInventory(null, 27, "§8申請管理: " + target.getName());

        // 相手の頭 (スロット4)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName("§e" + target.getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // 承認ボタン (スロット11: エメラルドブロック)
        ItemStack acceptBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta aMeta = acceptBtn.getItemMeta();
        aMeta.setDisplayName("§a§l申請を承認する");
        acceptBtn.setItemMeta(aMeta);
        inv.setItem(11, acceptBtn);

        // 拒否ボタン (スロット15: レッドストーンブロック)
        ItemStack denyBtn = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta dMeta = denyBtn.getItemMeta();
        dMeta.setDisplayName("§c§l申請を拒否する");
        denyBtn.setItemMeta(dMeta);
        inv.setItem(15, denyBtn);

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

            // フレンド解除の実行
            friendManager.removeFriend(player.getUniqueId(), target.getUniqueId());
            player.sendMessage("§c[Friend] " + target.getName() + " とのフレンドを解除しました。");
            player.closeInventory();
        } 
        else if (title.equals("§8フレンド申請一覧")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer target = meta.getOwningPlayer();
            if (target.getUniqueId() == null) return;

            // 統合版でも安全に操作できる個別の承認・拒否メニューを開く
            openRequestActionGUI(player, target);
        }
        else if (title.startsWith("§8申請管理: ")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            // タイトルからターゲットの名前を抽出する
            String targetName = title.replace("§8申請管理: ", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            if (event.getRawSlot() == 11) {
                // 承認
                friendManager.acceptRequest(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§a[Friend] " + target.getName() + " からのフレンド申請を承認しました！");
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§a[Friend] " + player.getName() + " がフレンド申請を承認しました！");
                }
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                // 拒否
