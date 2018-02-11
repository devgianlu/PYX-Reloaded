package com.gianlu.pyxreloaded;

import com.gianlu.pyxreloaded.data.Game;

public class EventWrapper extends JsonWrapper {

    public EventWrapper(Game game, Consts.Event event) {
        this(event);
        add(Consts.GeneralKeys.GAME_ID, game.getId());
    }

    public EventWrapper(Consts.Event event) {
        add(Consts.GeneralKeys.EVENT, event.toString());
    }
}
