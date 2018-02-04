package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Consts;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public abstract class GameWithPlayerHandler extends GameHandler {

    public GameWithPlayerHandler(GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public final JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (user.getGame() != game) throw new BaseCahHandler.CahException(Consts.ErrorCode.NOT_IN_THAT_GAME);
        else return handleWithUserInGame(user, game, params, exchange);
    }

    public abstract JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException;
}
