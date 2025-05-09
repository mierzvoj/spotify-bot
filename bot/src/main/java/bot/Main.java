package bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import bot.server.OAuthCallbackServer;
import bot.services.SpotifyService;
import bot.shared.TokenRefreshScheduler;
import bot.telegramBot.SpotifyTelegramBot;

public class Main {
    public static void main(String[] args) {
        try {
            // Initialize Spotify service
            SpotifyService spotifyService = new SpotifyService();

            // Start OAuth callback server
            OAuthCallbackServer callbackServer = new OAuthCallbackServer(spotifyService, 8888);
            callbackServer.start();

            // Start token refresh scheduler
            TokenRefreshScheduler tokenRefresher = new TokenRefreshScheduler(spotifyService);
            tokenRefresher.startScheduler();

            // Register Telegram bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new SpotifyTelegramBot(spotifyService));

            System.out.println("Spotify Telegram Bot started successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}