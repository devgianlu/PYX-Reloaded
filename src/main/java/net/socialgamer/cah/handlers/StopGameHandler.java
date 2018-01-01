package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;
import org.apache.log4j.Logger;

public class StopGameHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.STOP_GAME.toString();
    protected final Logger logger = Logger.getLogger(GameWithPlayerHandler.class);

    public StopGameHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (game.getHost() != user) {
            throw new CahResponder.CahException(ErrorCode.NOT_GAME_HOST);
        } else if (game.getState() == GameState.LOBBY) {
            throw new CahResponder.CahException(ErrorCode.ALREADY_STOPPED);
        } else {
            logger.info(String.format("Game %d stopped by host %s. Players: %s", game.getId(), user, game.getPlayers()));
            game.resetState(false);
            return new JsonObject();
        }
    }
}
