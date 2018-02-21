package com.gianlu.pyxreloaded.data;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.game.Game;

public class EventWrapper extends JsonWrapper {

    public EventWrapper(Game game, Consts.Event event) {
        this(event);
        add(Consts.GeneralKeys.GAME_ID, game.getId());
    }

    public EventWrapper(Consts.Event event) {
        add(Consts.GeneralKeys.EVENT, event.toString());
    }
}
