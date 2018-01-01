package net.socialgamer.cah.servlets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends RouterNanoHTTPD {

    public App(int port) {
        super(port);

        setAsyncRunner(new BoundRunner(Executors.newCachedThreadPool()));
        addMappings();
    }

    @Override
    public void addMappings() {
        super.addMappings();
        addRoute("/AjaxServlet", AjaxResponder.class);
        addRoute("/LongPollServlet", LongPollResponder.class);
    }

    private static class BoundRunner implements NanoHTTPD.AsyncRunner {
        private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList<ClientHandler>());
        private final ExecutorService executorService;

        BoundRunner(ExecutorService executorService) {
            this.executorService = executorService;
        }

        @Override
        public void closeAll() {
            for (NanoHTTPD.ClientHandler clientHandler : new ArrayList<>(running)) clientHandler.close();
        }

        @Override
        public void closed(NanoHTTPD.ClientHandler clientHandler) {
            running.remove(clientHandler);
        }

        @Override
        public void exec(NanoHTTPD.ClientHandler clientHandler) {
            executorService.submit(clientHandler);
            running.add(clientHandler);
        }
    }
}
