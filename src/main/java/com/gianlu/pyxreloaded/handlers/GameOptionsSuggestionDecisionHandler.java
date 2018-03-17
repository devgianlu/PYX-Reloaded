package com.gianlu.pyxreloaded.handlers;

import com.gianlu.pyxreloaded.Consts;
import com.gianlu.pyxreloaded.data.JsonWrapper;
import com.gianlu.pyxreloaded.data.User;
import com.gianlu.pyxreloaded.game.Game;
import com.gianlu.pyxreloaded.server.Annotations;
import com.gianlu.pyxreloaded.server.BaseCahHandler;
import com.gianlu.pyxreloaded.server.Parameters;
import com.gianlu.pyxreloaded.singletons.GamesManager;
import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;

public class GameOptionsSuggestionDecisionHandler extends GameWithPlayerHandler {
    public final static String OP = Consts.Operation.GAME_OPTIONS_SUGGESTION_DECISION.toString();

    public GameOptionsSuggestionDecisionHandler(@Annotations.GameManager GamesManager gamesManager) {
        super(gamesManager);
    }

    @NotNull
    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_GAME_HOST);

        String suggestedId = params.getStringNotNull(Consts.GameSuggestedOptionsData.ID);
        if (suggestedId.isEmpty()) throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        if (!params.has(Consts.GameSuggestedOptionsData.DECISION))
            throw new BaseCahHandler.CahException(Consts.ErrorCode.BAD_REQUEST);

        if (params.getBoolean(Consts.GameSuggestedOptionsData.DECISION, false))
            game.applySuggestedOptions(suggestedId);
        else
            game.declineSuggestedOptions(suggestedId);

        return JsonWrapper.EMPTY;
    }
}
