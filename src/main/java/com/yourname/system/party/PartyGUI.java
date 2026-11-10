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

    // メインGUI (自分がリーダーかメンバーかで表示を変える)
    public static void openMainGUI(Player player, PartyManager manager) {
        partyManager = manager;
        boolean inParty = partyManager.isInParty(player.getUniqueId());
        boolean isLeader = inParty && partyManager.isLeader(player.getUniqueId());

        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティーメニュー");

        // 1. メンバー一覧アイテム (スロット11)
        int memberCount = 0;
        if (inParty) {
            UUID leader = partyManager.getPartyLeader(player.getUniqueId());
            memberCount = partyManager.getPartyMembers(leader).size();
        }

        ItemStack memberItem = new ItemStack(Material.NETHER_STAR);
        ItemMeta mMeta = memberItem.getItemMeta();
        mMeta.setDisplayName("§a§lパーティーメンバー");
        List<String> mLore = new ArrayList<>();
        mLore.add("§7現在のパーティーの人数: §e[" + memberCount + "]");
        mLore.add("§7クリックしてメンバーを確認・管理します。");
        mMeta.setLore(mLore);
        memberItem.setItemMeta(mMeta);
        inv.setItem(11, memberItem);

        // 2. 右側のアイテム (パーティに入っているかどうか、オーナーかどうかで分岐)
        if (!inParty) {
            // パーティー未所属：届いている招待一覧を表示
            ItemStack inviteItem = new ItemStack(Material.PAPER);
            ItemMeta iMeta = inviteItem.getItemMeta();
            iMeta.setDisplayName("§e§l届いているパーティー招待");
            List<String> iLore = new ArrayList<>();
            iLore.add("§7クリックして届いた招待を確認します。");
            iMeta.setLore(iLore);
            inviteItem.setItemMeta(iMeta);
            inv.setItem(15, inviteItem);
        } else if (isLeader) {
            // リーダーの場合：解散ボタン (スロット15: 赤色の羊毛やバリアなど)
            ItemStack dissolveItem = new ItemStack(Material.BARRIER);
            ItemMeta dMeta = dissolveItem.getItemMeta();
            dMeta.setDisplayName("§c§lパーティーを解散する");
            List<String> dLore = new ArrayList<>();
            dLore.add("§7パーティーを解散し、メンバーを全員外します。");
            dMeta.setLore(dLore);
            dissolveItem.setItemMeta(dMeta);
            inv.setItem(15, dissolveItem);
        } else {
            // 一般メンバーの場合：脱退ボタン (スロット15: 門扉やドアなど)
            ItemStack leaveItem = new ItemStack(Material.OAK_DOOR);
            ItemMeta lMeta = leaveItem.getItemMeta();
            lMeta.setDisplayName("§c§lパーティーから退出する");
            List<String> lLore = new ArrayList<>();
            lLore.add("§7パーティーから抜け出します。");
            lMeta.setLore(lLore);
            leaveItem.setItemMeta(lMeta);
            inv.setItem(15, leaveItem);
        }

        player.openInventory(inv);
    }

    // メンバー一覧GUI
    public static void openMembersGUI(Player player) {
        if (!partyManager.isInParty(player.getUniqueId())) {
            player.sendMessage("§cあなたはパーティーに入っていません。");
            return;
        }

        UUID leader = partyManager.getPartyLeader(player.getUniqueId());
        List<UUID> members = partyManager.getPartyMembers(leader);

        Inventory inv = Bukkit.createInventory(null, 54, "§8パーティーメンバー一覧");

        for (UUID uuid : members) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            meta.setOwningPlayer(target);

            boolean isTargetLeader = uuid.equals(leader);
            meta.setDisplayName((isTargetLeader ? "§e[Leader] " : "§a[Member] ") + (target.getName() != null ? target.getName() : "Unknown"));

            List<String> lore = new ArrayList<>();
            lore.add(target.isOnline() ? "§a● オンライン" : "§c● オフライン");
            if (partyManager.isLeader(player.getUniqueId()) && !uuid.equals(player.getUniqueId())) {
                lore.add("§eクリックして管理（譲渡・キック）");
            }
            meta.setLore(lore);

            head.setItemMeta(meta);
            inv.addItem(head);
        }

        player.openInventory(inv);
    }

    // リーダー用：メンバー個別管理GUI (譲渡 or キック)
    public static void openMemberManageGUI(Player player, OfflinePlayer target) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8メンバー管理: " + target.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(target);
        meta.setDisplayName("§e" + target.getName());
        head.setItemMeta(meta);
        inv.setItem(4, head);

        // リーダー譲渡ボタン (スロット11: 金のブロック)
        ItemStack transferBtn = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta tMeta = transferBtn.getItemMeta();
        tMeta.setDisplayName("§6§lオーナー（リーダー）を譲渡");
        List<String> tLore = new ArrayList<>();
        tLore.add("§7このプレイヤーにパーティーのリーダーを渡します。");
        tMeta.setLore(tLore);
        transferBtn.setItemMeta(tMeta);
        inv.setItem(11, transferBtn);

        // キックボタン (スロット15: レッドストーンブロック)
        ItemStack kickBtn = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta kMeta = kickBtn.getItemMeta();
        kMeta.setDisplayName("§c§lパーティーからキックする");
        List<String> kLore = new ArrayList<>();
        kLore.add("§7このプレイヤーをパーティーから強制退出させます。");
        kMeta.setLore(kLore);
        kickBtn.setItemMeta(kMeta);
        inv.setItem(15, kickBtn);

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

    public static void openPartyRequestActionGUI(Player player, OfflinePlayer leader) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8パーティ招待管理: " + leader.getName());

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(leader);
        meta.setDisplayName("§e" + leader.getName() + " のパーティー");
        head.setItemMeta(meta);
        inv.setItem(4, head);

        ItemStack acceptBtn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta aMeta = acceptBtn.getItemMeta();
        aMeta.setDisplayName("§a§l招待を承認する");
        acceptBtn.setItemMeta(aMeta);
        inv.setItem(11, acceptBtn);

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
                if (!partyManager.isInParty(player.getUniqueId())) {
                    // 未所属なら招待一覧へ
                    openRequestsGUI(player);
                } else if (partyManager.isLeader(player.getUniqueId())) {
                    // リーダーなら解散
                    UUID leader = player.getUniqueId();
                    for (UUID mUUID : partyManager.getPartyMembers(leader)) {
                        Player m = Bukkit.getPlayer(mUUID);
                        if (m != null) {
                            m.sendMessage("§c§l[Party] §cパーティーが解散されました。");
                        }
                    }
                    partyManager.dissolveParty(leader);
                    player.closeInventory();
                    player.sendMessage("§c§l[Party] パーティーを解散しました。");
                } else {
                    // メンバーなら退出
                    partyManager.leaveParty(player.getUniqueId());
                    player.closeInventory();
                    player.sendMessage("§c§l[Party] パーティーから退出しました。");
                    UUID leader = partyManager.getPartyLeader(player.getUniqueId());
                    // 残りのメンバーに通知
                    if (leader != null) {
                        for (UUID mUUID : partyManager.getPartyMembers(leader)) {
                            Player m = Bukkit.getPlayer(mUUID);
                            if (m != null) {
                                m.sendMessage("§c§l[Party] §e" + player.getName() + " がパーティーから退出しました。");
                            }
                        }
                    }
                }
            }
        } 
        else if (title.equals("§8パーティーメンバー一覧")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer target = meta.getOwningPlayer();

            // 自分がリーダーで、かつ自分自身以外のメンバーをクリックした場合のみ管理GUIを開く
            if (partyManager.isLeader(player.getUniqueId()) && !target.getUniqueId().equals(player.getUniqueId())) {
                openMemberManageGUI(player, target);
            }
        } 
        else if (title.startsWith("§8メンバー管理: ")) {
            event.setCancelled(true);
            String targetName = title.replace("§8メンバー管理: ", "");
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

            UUID leader = player.getUniqueId();

            if (event.getRawSlot() == 11) {
                // オーナー譲渡
                if (target.getUniqueId() != null) {
                    partyManager.transferOwnership(leader, target.getUniqueId());
                    player.sendMessage("§a§l[Party] §a" + target.getName() + " にオーナー権限を譲渡しました！");
                    if (target.isOnline() && target.getPlayer() != null) {
                        target.getPlayer().sendMessage("§a§l[Party] §aあなたがパーティーの新しいオーナーになりました！");
                    }
                }
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                // キック
                if (target.getUniqueId() != null) {
                    partyManager.kickMember(leader, target.getUniqueId());
                    player.sendMessage("§c§l[Party] §c" + target.getName() + " をパーティーからキックしました。");
                    if (target.isOnline() && target.getPlayer() != null) {
                        target.getPlayer().sendMessage("§c§l[Party] §cあなたはパーティーからキックされました。");
                    }
                }
                player.closeInventory();
            }
        }
        else if (title.equals("§8パーティー招待一覧")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clicked.getItemMeta();
            if (meta == null || meta.getOwningPlayer() == null) return;

            OfflinePlayer leader = meta.getOwningPlayer();
            openPartyRequestActionGUI(player, leader);
        }
        else if (title.startsWith("§8パーティ招待管理: ")) {
            event.setCancelled(true);
            String leaderName = title.replace("§8パーティ招待管理: ", "");
            OfflinePlayer leader = Bukkit.getOfflinePlayer(leaderName);

            if (event.getRawSlot() == 11) {
                player.performCommand("p accept " + leader.getName());
                player.closeInventory();
            } else if (event.getRawSlot() == 15) {
                player.performCommand("p deny " + leader.getName());
                player.closeInventory();
            }
        }
    }
}
