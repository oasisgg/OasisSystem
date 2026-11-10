package com.yourname.system.party;

import java.util.*;

public class PartyManager {
    // プレイヤーUUID -> パーティーのリーダーUUID
    private final Map<UUID, UUID> playerPartyMap = new HashMap<>();
    // リーダーUUID -> メンバーUUIDのリスト（リーダー自身も含む）
    private final Map<UUID, List<UUID>> partyMembersMap = new HashMap<>();
    // 招待される人UUID -> 招待を送ったリーダーUUIDのリスト
    private final Map<UUID, List<UUID>> partyInvitesMap = new HashMap<>();

    public UUID getPartyLeader(UUID player) {
        return playerPartyMap.get(player);
    }

    public List<UUID> getPartyMembers(UUID leader) {
        return partyMembersMap.getOrDefault(leader, new ArrayList<>());
    }

    public boolean isInParty(UUID player) {
        return playerPartyMap.containsKey(player);
    }

    public boolean isLeader(UUID player) {
        UUID leader = playerPartyMap.get(player);
        return leader != null && leader.equals(player);
    }

    // 招待を送る
    public void sendInvite(UUID leader, UUID target) {
        List<UUID> invites = partyInvitesMap.computeIfAbsent(target, k -> new ArrayList<>());
        if (!invites.contains(leader)) {
            invites.add(leader);
        }
    }

    public List<UUID> getInvites(UUID target) {
        return partyInvitesMap.getOrDefault(target, new ArrayList<>());
    }

    // 招待を承認
    public boolean acceptInvite(UUID player, UUID leader) {
        List<UUID> invites = partyInvitesMap.get(player);
        if (invites == null || !invites.contains(leader)) {
            return false;
        }

        if (!partyMembersMap.containsKey(leader)) {
            List<UUID> members = new ArrayList<>();
            members.add(leader);
            partyMembersMap.put(leader, members);
            playerPartyMap.put(leader, leader);
        }

        List<UUID> members = partyMembersMap.get(leader);
        if (!members.contains(player)) {
            members.add(player);
        }
        playerPartyMap.put(player, leader);

        invites.remove(leader);
        return true;
    }

    public boolean denyInvite(UUID player, UUID leader) {
        List<UUID> invites = partyInvitesMap.get(player);
        if (invites != null && invites.contains(leader)) {
            invites.remove(leader);
            return true;
        }
        return false;
    }

    // パーティーを脱退 (メンバー用)
    public void leaveParty(UUID player) {
        UUID leader = playerPartyMap.remove(player);
        if (leader != null) {
            List<UUID> members = partyMembersMap.get(leader);
            if (members != null) {
                members.remove(player);
                // もしメンバーが0人になったらマップから消去
                if (members.isEmpty()) {
                    partyMembersMap.remove(leader);
                }
            }
        }
    }

    // パーティー解散 (リーダー用)
    public void dissolveParty(UUID leader) {
        List<UUID> members = partyMembersMap.remove(leader);
        if (members != null) {
            for (UUID m : members) {
                playerPartyMap.remove(m);
            }
        }
    }

    // メンバーをキック
    public void kickMember(UUID leader, UUID target) {
        List<UUID> members = partyMembersMap.get(leader);
        if (members != null) {
            members.remove(target);
            playerPartyMap.remove(target);
        }
    }

    // オーナー（リーダー）を譲渡
    public void transferOwnership(UUID oldLeader, UUID newLeader) {
        List<UUID> members = partyMembersMap.remove(oldLeader);
        if (members != null) {
            // リストの先頭を新しいリーダーに
            partyMembersMap.put(newLeader, members);
            for (UUID m : members) {
                playerPartyMap.put(m, newLeader);
            }
        }
    }
}
