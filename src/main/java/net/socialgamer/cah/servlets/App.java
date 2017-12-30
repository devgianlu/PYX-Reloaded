package net.socialgamer.cah.servlets;

import fi.iki.elonen.router.RouterNanoHTTPD;

public class App extends RouterNanoHTTPD {

    public App(int port) {
        super(port);
    }

    @Override
    public void addMappings() {
        super.addMappings();
        addRoute("/AjaxServlet", AjaxResponder.class);
        addRoute("/LongPollServlet", );
        addRoute("/Schema", );

        // TODO: JavascriptConfigServlet
    }
}
