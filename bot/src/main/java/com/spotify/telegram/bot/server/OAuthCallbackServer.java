package com.spotify.telegram.bot.server;

import com.spotify.telegram.bot.services.SpotifyService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class OAuthCallbackServer {
    private final SpotifyService spotifyService;
    private final int port;
    private HttpServer server;

    public OAuthCallbackServer(SpotifyService spotifyService, int port) {
        this.spotifyService = spotifyService;
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/callback", new CallbackHandler());
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
        System.out.println("Callback server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private class CallbackHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = parseQuery(query);

            String response;
            if (params.containsKey("code")) {
                String code = params.get("code");
                spotifyService.setAuthorizationCode(code);
                response = "Authorization successful! You can now return to the Telegram bot.";
            } else {
                response = "Authorization failed!";
            }

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private Map<String, String> parseQuery(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
            return params;
        }
    }
}