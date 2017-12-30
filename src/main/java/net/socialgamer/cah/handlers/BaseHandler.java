package net.socialgamer.cah.handlers;

import com.google.gson.JsonElement;
import net.socialgamer.cah.data.User;


public abstract class BaseHandler {
    public abstract JsonElement handle(User user);
}
