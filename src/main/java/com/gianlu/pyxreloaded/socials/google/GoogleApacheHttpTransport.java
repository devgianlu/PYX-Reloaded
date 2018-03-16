package com.gianlu.pyxreloaded.socials.google;

import com.google.api.client.http.HttpMethods;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GoogleApacheHttpTransport extends HttpTransport {
    private static final Logger logger = Logger.getLogger(GoogleApacheHttpTransport.class.getSimpleName());
    private final HttpClient httpClient;
    private final PoolingHttpClientConnectionManager connectionManager;
    private final IdleConnectionMonitorThread idleConnectionMonitorThread;

    public GoogleApacheHttpTransport() {
        connectionManager = new PoolingHttpClientConnectionManager();
        idleConnectionMonitorThread = new IdleConnectionMonitorThread();
        httpClient = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(1, true))
                .setKeepAliveStrategy(new ShortKeepAliveStrategy())
                .setConnectionManager(connectionManager)
                .build();

        idleConnectionMonitorThread.start();
    }

    @Override
    public boolean supportsMethod(String method) {
        return true;
    }

    @Override
    protected GoogleApacheHttpRequest buildRequest(String method, String url) {
        HttpRequestBase requestBase;
        switch (method) {
            case HttpMethods.DELETE:
                requestBase = new HttpDelete(url);
                break;
            case HttpMethods.GET:
                requestBase = new HttpGet(url);
                break;
            case HttpMethods.HEAD:
                requestBase = new HttpHead(url);
                break;
            case HttpMethods.POST:
                requestBase = new HttpPost(url);
                break;
            case HttpMethods.PUT:
                requestBase = new HttpPut(url);
                break;
            case HttpMethods.TRACE:
                requestBase = new HttpTrace(url);
                break;
            case HttpMethods.OPTIONS:
                requestBase = new HttpOptions(url);
                break;
            default:
                requestBase = new UnknownMethodRequest(method, url);
                break;
        }

        return new GoogleApacheHttpRequest(httpClient, requestBase);
    }

    @Override
    public void shutdown() {
        idleConnectionMonitorThread.shutdown();
        connectionManager.shutdown();
    }

    private static final class ShortKeepAliveStrategy implements ConnectionKeepAliveStrategy {

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            return 5000; // 5 seconds
        }
    }

    private static final class UnknownMethodRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        UnknownMethodRequest(String method, String uri) {
            this.method = Preconditions.checkNotNull(method);
            setURI(URI.create(uri));
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private final class IdleConnectionMonitorThread extends Thread {
        private volatile boolean shutdown;

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(5000);
                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, "", ex);
            }
        }

        void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
}

