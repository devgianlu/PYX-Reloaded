package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.Player;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;


public class ScoreHandler extends BaseHandler {
    public static final String OP = AjaxOperation.SCORE.toString();
    private final ConnectedUsers connectedUsers;

    public ScoreHandler(final ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException {
        String argsStr = params.get(AjaxRequest.MESSAGE);
        String[] args = (argsStr == null || argsStr.isEmpty()) ? new String[0] : argsStr.trim().split(" ");

        User target = (args.length > 0) ? connectedUsers.getUser(args[0]) : user;
        if (target == null) throw new CahResponder.CahException(ErrorCode.NO_SUCH_USER);

        Game game = target.getGame();
        if (game == null) throw new CahResponder.CahException(ErrorCode.INVALID_GAME);

        Player player = game.getPlayerForUser(target);
        if (player == null) throw new CahResponder.CahException(ErrorCode.INVALID_GAME);

        if (user.isAdmin() && args.length == 2) {
            // TODO: for now only admins can change scores. could possibly extend this to let the host do it,
            // provided it's for a player in the same game and it does a game-wide announcement.
            try {
                int offset = Integer.parseInt(args[1]);
                player.increaseScore(offset);
                game.notifyPlayerInfoChange(player);
            } catch (final NumberFormatException ex) {
                throw new CahResponder.CahException(ErrorCode.BAD_REQUEST, ex);
            }
        }

        JsonObject obj = new JsonObject();
        obj.add(AjaxResponse.PLAYER_INFO.toString(), game.getPlayerInfoJson(player));
        return obj;
    }
}
