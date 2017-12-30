package net.socialgamer.cah.task;

import net.socialgamer.cah.Constants.LongPollEvent;
import net.socialgamer.cah.Constants.LongPollResponse;
import net.socialgamer.cah.Utils;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.QueuedMessage.MessageType;

// TODO: Schedule this
public class BroadcastGameListUpdateTask extends SafeTimerTask {

    private final ConnectedUsers users;
    private volatile boolean needsUpdate = false;

    public BroadcastGameListUpdateTask(final ConnectedUsers users) {
        this.users = users;
    }

    public void needsUpdate() {
        needsUpdate = true;
    }

    @Override
    public void process() {
        if (needsUpdate) {
            users.broadcastToAll(MessageType.GAME_EVENT, Utils.singletonJsonObject(LongPollResponse.EVENT.toString(), LongPollEvent.GAME_LIST_REFRESH.toString()));
            needsUpdate = false;
        }
    }
}
