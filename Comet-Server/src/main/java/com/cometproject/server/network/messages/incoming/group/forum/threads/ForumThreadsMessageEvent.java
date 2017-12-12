package com.cometproject.server.network.messages.incoming.group.forum.threads;

import com.cometproject.server.composers.group.forums.GroupForumThreadsMessageComposer;
import com.cometproject.server.game.groups.GroupManager;
import com.cometproject.server.game.groups.types.Group;
import com.cometproject.api.game.groups.types.components.forum.ForumPermission;
import com.cometproject.server.network.messages.incoming.Event;
import com.cometproject.server.protocol.messages.MessageEvent;
import com.cometproject.server.network.sessions.Session;

public class ForumThreadsMessageEvent implements Event {
    @Override
    public void handle(Session client, MessageEvent msg) throws Exception {
        int groupId = msg.readInt();

        int start = msg.readInt();

        Group group = GroupManager.getInstance().get(groupId);

        if(group == null) {
            return;
        }

        if(group.getForum().getForumSettings().getReadPermission() == ForumPermission.MEMBERS) {
            if(!group.getMembers().getAll().containsKey(client.getPlayer().getId())) {
                return;
            }
        } else if(group.getForum().getForumSettings().getReadPermission() == ForumPermission.ADMINISTRATORS) {
            if(!group.getMembers().getAdministrators().contains(client.getPlayer().getId())) {
                return;
            }
        }

        client.send(new GroupForumThreadsMessageComposer(group.getId(), group.getForum().getForumThreads(start), start));
    }
}
