package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.Constants.GameState;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.Preferences;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.GameManager;
import net.socialgamer.cah.data.GameOptions;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.Parameters;

public class ChangeGameOptionHandler extends GameWithPlayerHandler {
    public static final String OP = AjaxOperation.CHANGE_GAME_OPTIONS.toString();
    private final Preferences preferences;

    public ChangeGameOptionHandler(@Annotations.GameManager GameManager gameManager, @Annotations.Preferences Preferences preferences) {
        super(gameManager);
        this.preferences = preferences;
    }

    @Override
    public JsonWrapper handleWithUserInGame(User user, Game game, Parameters params, HttpServerExchange exchange) throws BaseCahHandler.CahException {
        if (game.getHost() != user) throw new BaseCahHandler.CahException(ErrorCode.NOT_GAME_HOST);
        if (game.getState() != GameState.LOBBY) throw new BaseCahHandler.CahException(ErrorCode.ALREADY_STARTED);

        try {
            String value = params.get(AjaxRequest.GAME_OPTIONS);
            GameOptions options = GameOptions.deserialize(preferences, value);
            String oldPassword = game.getPassword();
            game.updateGameSettings(options);

            // only broadcast an update if the password state has changed, because it needs to change
            // the text on the join button and the sort order
            if (!game.getPassword().equals(oldPassword)) gameManager.broadcastGameListRefresh();
        } catch (Exception ex) {
            throw new BaseCahHandler.CahException(ErrorCode.BAD_REQUEST, ex);
        }

        return JsonWrapper.EMPTY;
    }
}
