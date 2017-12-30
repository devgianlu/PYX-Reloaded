package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.Constants;
import net.socialgamer.cah.Constants.ErrorCode;
import net.socialgamer.cah.data.User;
import net.socialgamer.cah.servlets.BaseUriResponder;
import net.socialgamer.cah.servlets.CahResponder;
import net.socialgamer.cah.servlets.Parameters;

public abstract class AdminHandler extends BaseHandler {

    @Override
    public JsonElement handle(User user, Parameters params, NanoHTTPD.IHTTPSession session) throws BaseUriResponder.StatusException {
        if (!Constants.ADMIN_IP_ADDRESSES.contains(session.getRemoteIpAddress())) // TODO: Can be done better
            throw new CahResponder.CahException(ErrorCode.ACCESS_DENIED);

        return handleAsAdmin(user, params);
    }

    public abstract JsonElement handleAsAdmin(User user, Parameters params) throws CahResponder.CahException;
}
