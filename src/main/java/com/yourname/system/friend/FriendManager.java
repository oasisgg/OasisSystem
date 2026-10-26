package com.yourname.system.friend;

import com.yourname.system.SystemMain;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FriendManager {
    private final SystemMain plugin;
    private File file;
    private FileConfiguration config;

    public FriendManager(SystemMain plugin) {
        this.plugin = plugin;
        loadFiles();
    }

    public void loadFiles() {
        file = new File(plugin.getDataFolder(), "friends.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveFiles() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // フレンドリストの取得 (UUIDベース)
    public List<UUID> getFriends(UUID uuid) {
        List<String> list = config.getStringList("friends." + uuid.toString());
        List<UUID> friends = new ArrayList<>();
        for (String s : list) {
            friends.add(UUID.fromString(s));
        }
        return friends;
    }

    // フレンド申請一覧の取得
    public List<UUID> getRequests(UUID uuid) {
        List<String> list = config.getStringList("requests." + uuid.toString());
        List<UUID> requests = new ArrayList<>();
        for (String s : list) {
            requests.add(UUID.fromString(s));
        }
        return requests;
    }

    // 申請を送る
    public void sendRequest(UUID sender, UUID target) {
        List<String> reqs = config.getStringList("requests." + target.toString());
        if (!reqs.contains(sender.toString())) {
            reqs.add(sender.toString());
            config.set("requests." + target.toString(), reqs);
            saveFiles();
        }
    }

    // 申請を承認
    public void acceptRequest(UUID player, UUID target) {
        // プレイヤー側
        List<String> pFriends = config.getStringList("friends." + player.toString());
        if (!pFriends.contains(target.toString())) pFriends.add(target.toString());
        config.set("friends." + player.toString(), pFriends);

        // 相手側
        List<String> tFriends = config.getStringList("friends." + target.toString());
        if (!tFriends.contains(player.toString())) tFriends.add(player.toString());
        config.set("friends." + target.toString(), tFriends);

        // 申請削除
        removeRequest(player, target);
        saveFiles();
    }

    // 申請拒否・削除
    public void removeRequest(UUID player, UUID target) {
        List<String> reqs = config.getStringList("requests." + player.toString());
        reqs.remove(target.toString());
        config.set("requests." + player.toString(), reqs);
        saveFiles();
    }

    // フレンド解除
    public void removeFriend(UUID player, UUID target) {
        List<String> pFriends = config.getStringList("friends." + player.toString());
        pFriends.remove(target.toString());
        config.set("friends." + player.toString(), pFriends);

        List<String> tFriends = config.getStringList("friends." + target.toString());
        tFriends.remove(player.toString());
        config.set("friends." + target.toString(), tFriends);

        saveFiles();
    }
}
