package net.socialgamer.cah;

import net.socialgamer.cah.data.Game;

public class EventWrapper extends JsonWrapper {

    public EventWrapper(Game game, Constants.LongPollEvent event) {
        this(event);
        add(Constants.LongPollResponse.GAME_ID, game.getId());
    }

    public EventWrapper(Constants.LongPollEvent event) {
        add(Constants.LongPollResponse.EVENT, event.toString());
    }
}
