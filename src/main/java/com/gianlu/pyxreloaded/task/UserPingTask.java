package com.gianlu.pyxreloaded.task;

import com.gianlu.pyxreloaded.data.ConnectedUsers;

import java.util.concurrent.ScheduledThreadPoolExecutor;


public class UserPingTask extends SafeTimerTask {
    private final ConnectedUsers users;
    private final ScheduledThreadPoolExecutor globalTimer;

    public UserPingTask(ConnectedUsers users, ScheduledThreadPoolExecutor globalTimer) {
        this.users = users;
        this.globalTimer = globalTimer;
    }

    @Override
    public void process() {
        users.checkForPingAndIdleTimeouts();
        globalTimer.purge();
    }
}
