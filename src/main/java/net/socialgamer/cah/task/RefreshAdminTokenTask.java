package net.socialgamer.cah.task;

import net.socialgamer.cah.servlets.AdminToken;

import java.util.logging.Logger;

public class RefreshAdminTokenTask extends SafeTimerTask {
    private static final Logger logger = Logger.getLogger(RefreshAdminTokenTask.class.getSimpleName());

    @Override
    public void process() {
        String token = AdminToken.refresh();
        logger.info("Current admin token: " + token);
    }
}
