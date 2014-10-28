package com.cometproject.server.game.rooms.types.components;

import com.cometproject.server.boot.Comet;
import com.cometproject.server.game.rooms.objects.items.RoomItem;
import com.cometproject.server.game.rooms.objects.items.RoomItemFloor;
import com.cometproject.server.game.rooms.objects.items.RoomItemWall;
import com.cometproject.server.game.rooms.objects.items.queue.RoomItemEventQueue;
import com.cometproject.server.game.rooms.objects.items.types.floor.RollerFloorItem;
import com.cometproject.server.game.rooms.types.Room;
import com.cometproject.server.logging.sentry.SentryDispatcher;
import com.cometproject.server.tasks.CometTask;
import com.cometproject.server.tasks.CometThreadManagement;
import com.cometproject.server.utilities.TimeSpan;
import javolution.util.FastMap;
import org.apache.log4j.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ItemProcessComponent implements CometTask {
    private final int INTERVAL = Integer.parseInt(Comet.getServer().getConfig().get("comet.system.item_process.interval"));
    private final int FLAG = Integer.parseInt(Comet.getServer().getConfig().get("comet.system.item_process.flag"));

    private Room room;
    private Logger log;

    private ScheduledFuture myFuture;
    private CometThreadManagement mgr;

    private boolean active = false;

    private final RoomItemEventQueue eventQueue = new RoomItemEventQueue();

    public ItemProcessComponent(CometThreadManagement mgr, Room room) {
        this.mgr = mgr;
        this.room = room;

        log = Logger.getLogger("GenericRoomItem Process [" + room.getData().getName() + "]");
    }

    public RoomItemEventQueue getEventQueue() {
        return this.eventQueue;
    }

    public void start() {
        if (Room.useCycleForItems) {
            this.active = true;
            return;
        }

        if (this.myFuture != null && this.active) {
            stop();
        }

        this.active = true;
        this.myFuture = this.mgr.executePeriodic(this, 0, INTERVAL, TimeUnit.MILLISECONDS);

        log.debug("Processing started");
    }

    public void stop() {
        if (Room.useCycleForItems) {
            this.active = false;
            return;
        }

        if (this.myFuture != null) {
            this.active = false;
            this.myFuture.cancel(false);

            log.debug("Processing stopped");
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public void tick() {
        if (!this.active) {
            return;
        }

        if (this.getRoom().getEntities().realPlayerCount() == 0) {
            this.stop();
            return;
        }

        long timeStart = System.currentTimeMillis();

        for (RoomItemFloor item : this.getRoom().getItems().getFloorItems()) {
            try {
                if (item.requiresTick() || item instanceof RollerFloorItem) {
                    item.tick();
                }
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                this.handleException(item, e);
            }
        }

        for (RoomItemWall item : this.getRoom().getItems().getWallItems()) {
            try {
                if (item.requiresTick()) {
                    item.tick();
                }
            } catch (NullPointerException | IndexOutOfBoundsException e) {
                this.handleException(item, e);
            }
        }


        // Now lets process any queued events last
//        try {
//            this.eventQueue.cycle();
//        } catch (NullPointerException | IndexOutOfBoundsException e) {
//            this.handleSupressedExceptions(e);
//        }

        TimeSpan span = new TimeSpan(timeStart, System.currentTimeMillis());

        if (span.toMilliseconds() > FLAG && Comet.isDebugging) {
            log.warn("ItemProcessComponent process took: " + span.toMilliseconds() + "ms to execute.");
        }
    }

    @Override
    public void run() {
        this.tick();
    }

    protected void handleException(RoomItem item, Exception e) {
        SentryDispatcher.getInstance().dispatchException("itemProcessError", "Exception while processing items", e, net.kencochrane.raven.event.Event.Level.ERROR, new FastMap<String, Object>() {{
            put("Item ID", item.getId());
            put("Item Class", item.getClass().getSimpleName());
        }});
    }

    public Room getRoom() {
        return this.room;
    }
}