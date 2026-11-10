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
        rLore.add("§7クリックして申請を承認・拒否します。");
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
            lore.add("§a左クリック: §2申請を承認する");
            lore.add("§c右クリック: §4申請を拒否する");
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

            if (event.isLeftClick()) {
                // 承認
                friendManager.acceptRequest(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§a[Friend] " + target.getName() + " からのフレンド申請を承認しました！");
                if (target.isOnline() && target.getPlayer() != null) {
                    target.getPlayer().sendMessage("§a[Friend] " + player.getName() + " がフレンド申請を承認しました！");
                }
                player.closeInventory();
            } else if (event.isRightClick()) {
                // 拒否
                friendManager.removeRequest(player.getUniqueId(), target.getUniqueId());
                player.sendMessage("§c[Friend] " + target.getName() + " からのフレンド申請を拒否しました。");
                player.closeInventory();
            }
        }
    }
}
