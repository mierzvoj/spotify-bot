package bot;


import bot.shared.UserSessionManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import bot.server.OAuthCallbackServer;
import bot.services.SpotifyService;
import bot.shared.TokenRefreshScheduler;
import bot.telegramBot.SpotifyTelegramBot;

public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("Starting Spotify Telegram Bot...");

            // Initialize the UserSessionManager first
            UserSessionManager sessionManager = UserSessionManager.getInstance();
            System.out.println("UserSessionManager initialized: " + sessionManager);

            // Initialize Spotify service
            SpotifyService spotifyService = new SpotifyService();
            System.out.println("SpotifyService initialized");

            // Start token refresh scheduler
            TokenRefreshScheduler tokenRefresher = new TokenRefreshScheduler(spotifyService);
            tokenRefresher.startScheduler();
            System.out.println("TokenRefreshScheduler started");

            // Register Telegram bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            SpotifyTelegramBot bot = new SpotifyTelegramBot(spotifyService);
            botsApi.registerBot(bot);

            System.out.println("Spotify Telegram Bot started successfully!");
        } catch (Exception e) {
            System.err.println("Error starting bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}