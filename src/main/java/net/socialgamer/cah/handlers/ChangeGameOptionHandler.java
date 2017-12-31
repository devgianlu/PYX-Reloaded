package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.GameOptions;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public class ChangeGameOptionHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CHANGE_GAME_OPTIONS.toString();

    public ChangeGameOptionHandler(@Annotations.GameManager GameManager gameManager) {
        super(gameManager);
    }

    @Override
    public JsonElement handleWithUserInGame(User user, Game game, Parameters params, NanoHTTPD.IHTTPSession session) throws CahResponder.CahException {
        if (game.getHost() != user) throw new CahResponder.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new CahResponder.CahException(ErrorCode.ALREADY_STARTED);

        try {
            String value = params.get(AjaxRequest.GAME_OPTIONS);
            GameOptions options = GameOptions.deserialize(value);
            String oldPassword = game.getPassword();
            game.updateGameSettings(options);

            // only broadcast an update if the password state has changed, because it needs to change
            // the text on the join button and the sort order
            if (!game.getPassword().equals(oldPassword)) gameManager.broadcastGameListRefresh();
        } catch (Exception ex) {
            throw new CahResponder.CahException(ErrorCode.BAD_REQUEST, ex);
        }

        return new JsonObject();
    }
}
