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

    public static void openFriendListGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8フレンド一覧 (オンライン: §a緑§8 / オフライン: §c赤§8)");
        List<UUID> friends = friendManager.getFriends(player.getUniqueId());

        for (UUID uuid : friends) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            boolean isOnline = target.isOnline();

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            // オンラインなら§a、オフラインなら§cで名前をつける
            String statusColor = isOnline ? "§a" : "§c";
            meta.setDisplayName(statusColor + target.getName());

            List<String> lore = new ArrayList<>();
            lore.add(isOnline ? "§a● オンライン" : "§c● オフライン");
            lore.add("§7クリックしてフレンド解除");
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
                player.sendMessage("§e[Friend] 申請一覧機能は現在準備中やで！");
            }
        } else if (title.startsWith("§8フレンド一覧")) {
            event.setCancelled(true);
        }
    }
}
