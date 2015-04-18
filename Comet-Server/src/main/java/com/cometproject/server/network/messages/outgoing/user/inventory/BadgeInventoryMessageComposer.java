package com.cometproject.server.network.messages.outgoing.user.inventory;

import com.cometproject.server.network.messages.composers.MessageComposer;
import com.cometproject.server.network.messages.headers.Composers;
import com.cometproject.server.network.messages.types.Composer;
import javolution.util.FastMap;

import java.util.Map;


public class BadgeInventoryMessageComposer extends MessageComposer {

    private final Map<String, Integer> badges;

    public BadgeInventoryMessageComposer(final Map<String, Integer> badges) {
        this.badges = badges;
    }

    @Override
    public short getId() {
        return Composers.LoadBadgesWidgetMessageComposer;
    }

    @Override
    public void compose(Composer msg) {
        Map<String, Integer> activeBadges = new FastMap<>();

        msg.writeInt(badges.size());

        for (Map.Entry<String, Integer> badge : badges.entrySet()) {
            if (badge.getValue() > 0) {
                activeBadges.put(badge.getKey(), badge.getValue());
            }

            msg.writeInt(0);
            msg.writeString(badge.getKey());
        }

        msg.writeInt(activeBadges.size());

        for (Map.Entry<String, Integer> badge : activeBadges.entrySet()) {
            msg.writeInt(badge.getValue());
            msg.writeString(badge.getKey());
        }

        activeBadges.clear();
    }
}