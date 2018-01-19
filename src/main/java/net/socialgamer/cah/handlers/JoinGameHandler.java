package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.Game.TooManyPlayersException;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public class JoinGameHandler extends GameHandler {
    public static final String OP = AjaxOperation.JOIN_GAME.toString();

    public JoinGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonWrapper handle(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (!game.isPasswordCorrect(params.get(AjaxRequest.PASSWORD)))
            throw new BaseCahHandler.CahException(ErrorCode.WRONG_PASSWORD);

        try {
            game.addPlayer(user);
        } catch (IllegalStateException ex) {
            throw new BaseCahHandler.CahException(ErrorCode.CANNOT_JOIN_ANOTHER_GAME, ex);
        } catch (TooManyPlayersException ex) {
            throw new BaseCahHandler.CahException(ErrorCode.GAME_FULL, ex);
        }

        return JsonWrapper.EMPTY;
    }
}
