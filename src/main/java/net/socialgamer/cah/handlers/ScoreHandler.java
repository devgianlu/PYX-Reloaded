package net.socialgamer.cah.handlers;

import io.undertow.server.HttpServerExchange;
import net.socialgamer.cah.Constants.AjaxOperation;
import net.socialgamer.cah.Constants.AjaxRequest;
import net.socialgamer.cah.Constants.AjaxResponse;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.JsonWrapper;
import net.socialgamer.cah.data.ConnectedUsers;
import net.socialgamer.cah.data.Game;
import net.socialgamer.cah.data.Player;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.Annotations;
import net.socialgamer.cah.servlets.BaseCahHandler;
import net.socialgamer.cah.servlets.BaseJsonHandler;
import net.socialgamer.cah.servlets.Parameters;

public class ScoreHandler extends BaseHandler {
    public static final String OP = AjaxOperation.SCORE.toString();
    private final ConnectedUsers connectedUsers;

    public ScoreHandler(@Annotations.ConnectedUsers ConnectedUsers connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    @Override
    public JsonWrapper handle(User user, Parameters params, HttpServerExchange exchange) throws BaseJsonHandler.StatusException {
        if (!user.isAdmin()) throw new BaseCahHandler.CahException(ErrorCode.NOT_ADMIN);

        String argsStr = params.get(AjaxRequest.MESSAGE);
        String[] args = (argsStr == null || argsStr.isEmpty()) ? new String[0] : argsStr.trim().split(" ");
        if (args.length != 2) throw new BaseCahHandler.CahException(ErrorCode.BAD_REQUEST);

        User target = connectedUsers.getUser(args[0]);
        if (target == null) throw new BaseCahHandler.CahException(ErrorCode.NO_SUCH_USER);

        Game game = target.getGame();
        if (game == null) throw new BaseCahHandler.CahException(ErrorCode.INVALID_GAME);

        Player player = game.getPlayerForUser(target);
        if (player == null) throw new BaseCahHandler.CahException(ErrorCode.INVALID_GAME);

        try {
            int offset = Integer.parseInt(args[1]);
            player.increaseScore(offset);
            game.notifyPlayerInfoChange(player);
        } catch (final NumberFormatException ex) {
            throw new BaseCahHandler.CahException(ErrorCode.BAD_REQUEST, ex);
        }

        JsonWrapper obj = new JsonWrapper();
        obj.add(AjaxResponse.PLAYER_INFO, game.getPlayerInfoJson(player));
        return obj;
    }
}
