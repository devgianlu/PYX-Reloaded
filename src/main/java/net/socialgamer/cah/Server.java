package net.socialgamer.cah;

import fi.iki.elonen.NanoHTTPD;
import net.socialgamer.cah.servlets.App;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException {
        new App(1000).start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
}
