package com.cometproject.server.network.messages.incoming.group;

import com.cometproject.server.composers.group.GroupMembersMessageComposer;
import com.cometproject.server.game.groups.GroupManager;
import com.cometproject.server.game.groups.types.Group;
import com.cometproject.server.game.players.PlayerManager;
import com.cometproject.server.network.NetworkManager;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.network.sessions.Session;

import java.util.ArrayList;

public class DeclineMembershipMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        int groupId = msg.readInt();
        int playerId = msg.readInt();

        if (!client.getPlayer().getGroups().contains(groupId))
            return;

        Group group = GroupManager.getInstance().get(groupId);

        if (group == null || group.getData().getOwnerId() != client.getPlayer().getId())
            return;

        if (!group.getMembers().getMembershipRequests().contains(playerId))
            return;

        group.getMembers().removeRequest(playerId);

        client.send(new GroupMembersMessageComposer(group.getData(), 0,
                new ArrayList<>(group.getMembers().getMembershipRequests()), 2, "",
                true, PlayerManager.getInstance(), NetworkManager.getInstance().getSessions()));
    }
}
