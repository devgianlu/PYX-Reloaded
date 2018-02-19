package com.gianlu.pyxreloaded.task;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.EventWrapper;
import com.gianlu.pyxreloaded.data.QueuedMessage.MessageType;
import com.gianlu.pyxreloaded.singletons.ConnectedUsers;

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
            users.broadcastToAll(MessageType.GAME_EVENT, new EventWrapper(Consts.Event.GAME_LIST_REFRESH));
            needsUpdate = false;
        }
    }
}
