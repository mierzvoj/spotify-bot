package bot.telegramBot;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import bot.shared.UserSessionManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import bot.services.SpotifyService;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class SpotifyTelegramBot extends TelegramLongPollingBot {

    private final SpotifyService spotifyService;
    private final UserSessionManager userSessionManager;

    public SpotifyTelegramBot(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        // Always get the singleton instance
        this.userSessionManager = UserSessionManager.getInstance();
        System.out.println("SpotifyTelegramBot initialized with UserSessionManager: " + this.userSessionManager);


        if (this.userSessionManager == null) {
            System.err.println("CRITICAL ERROR: Failed to initialize UserSessionManager in bot constructor!");
        } else {
            System.out.println("SpotifyTelegramBot initialized with UserSessionManager: " + this.userSessionManager);
        }
    }

    @Override
    public String getBotUsername() {
        return "mierzvoj_bot";
    }

    @Override
    public String getBotToken() {
        return ":";
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendLoginMessage(long chatId) {
        String authUrl = spotifyService.getAuthorizationUrl();

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(createInlineButton("Login with Spotify", authUrl));
        rowsInline.add(rowInline);
        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Please login to your Spotify account:");
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardButton createInlineButton(String text, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setUrl(url);
        return button;
    }

    // Add to your onUpdateReceived method in SpotifyTelegramBot.java
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.startsWith("/")) {
                handleCommand(messageText, chatId);
            } else {
                // Handle non-command messages if needed
            }
        } else if (update.hasCallbackQuery()) {
            // Handle callback queries (for inline keyboards)
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleCommand(String command, long chatId) {
        // Split command into the command name and arguments
        String[] parts = command.split("\\s+", 2);
        String cmd = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1].trim() : "";

        // Debug log
        System.out.println("Handling command: " + cmd + " from user: " + chatId);

        // Check authentication for commands that require it
        boolean isAuthenticated = userSessionManager.isUserAuthenticated(chatId);
        System.out.println("User authenticated: " + isAuthenticated);

        switch (cmd) {
            case "/start":
                sendTextMessage(chatId, "Welcome to Spotify Telegram Bot! Use /help to see available commands.");
                break;

            case "/help":
                sendHelpMessage(chatId);
                break;

            case "/login":
                sendLoginMessage(chatId);
                break;

            case "/setcode":
                if (!argument.isEmpty()) {
                    handleAuthorizationCode(chatId, argument);
                } else {
                    sendTextMessage(chatId, "Please provide the authorization code: /setcode YOUR_CODE_HERE");
                }
                break;

            case "/logout":
                userSessionManager.logoutUser(chatId);
                sendTextMessage(chatId, "You have been logged out of Spotify.");
                break;

            case "/play":
                if (!isAuthenticated) {
                    sendNotAuthenticatedMessage(chatId);
                    return;
                }

                // Add this case to your handleCommand method
            case "/status":
                boolean auth = userSessionManager.isUserAuthenticated(chatId);
                UserSessionManager.UserSession session = userSessionManager.getSession(chatId);

                StringBuilder status = new StringBuilder();
                status.append("🔍 Bot Status:\n\n");
                status.append("Authenticated: ").append(auth ? "✅ Yes" : "❌ No").append("\n");

                if (session != null) {
                    status.append("Access Token: ").append(session.getAccessToken() != null ? "✅ Present" : "❌ Missing")
                            .append("\n");
                    status.append("Refresh Token: ")
                            .append(session.getRefreshToken() != null ? "✅ Present" : "❌ Missing").append("\n");

                    if (session.isTokenExpired()) {
                        status.append("Token Status: ⚠️ Expired\n");
                    } else if (session.isAuthenticated()) {
                        status.append("Token Status: ✅ Valid\n");
                    }
                } else {
                    status.append("No session found for your account.\n");
                }

                status.append("\nUse /spotify_login to connect your Spotify account.");

                sendTextMessage(chatId, status.toString());
                break;

            // Other commands that require authentication
            case "/pause":
            case "/resume":
            case "/skip":
            case "/current":
                if (!isAuthenticated) {
                    sendNotAuthenticatedMessage(chatId);
                    return;
                }

                // Handle the specific command...
                if (cmd.equals("/pause"))
                    handlePauseCommand(chatId);
                else if (cmd.equals("/resume"))
                    handleResumeCommand(chatId);
                else if (cmd.equals("/skip"))
                    handleSkipCommand(chatId);
                else if (cmd.equals("/current"))
                    handleCurrentTrackCommand(chatId);
                break;
            case "/sessions":
                if (chatId == 7143567186l) { // Your user ID for admin commands
                    String diagnostics = userSessionManager.getDiagnostics();
                    sendTextMessage(chatId, diagnostics);
                } else {
                    sendTextMessage(chatId, "This command is restricted to administrators.");
                }
                break;

            case "/reset_session":
                // Force create a new session
                userSessionManager.getOrCreateSession(chatId);
                sendTextMessage(chatId, "Your session has been reset. Use /spotify_login to authenticate.");
                break;

            // Add these cases to your handleCommand method
            case "/auth_debug":
                StringBuilder debug = new StringBuilder();
                debug.append("🔍 Authentication Debug:\n\n");

                // Check if userSessionManager exists
                debug.append("UserSessionManager instance: ")
                        .append(userSessionManager != null ? "✅ Exists" : "❌ NULL!")
                        .append("\n");

                // If it exists, check if this user has a session
                if (userSessionManager != null) {
                    UserSessionManager.UserSession session_1 = userSessionManager.getSession(chatId);
                    debug.append("User session: ")
                            .append(session_1 != null ? "✅ Exists" : "❌ Not found")
                            .append("\n");

                    // If session exists, check its state
                    if (session_1 != null) {
                        debug.append("Access token: ")
                                .append(session_1.getAccessToken() != null ? "✅ Present" : "❌ Missing")
                                .append("\n");

                        debug.append("Refresh token: ")
                                .append(session_1.getRefreshToken() != null ? "✅ Present" : "❌ Missing")
                                .append("\n");

                        // Check if the session thinks it's authenticated
                        debug.append("Session.isAuthenticated(): ")
                                .append(session_1.isAuthenticated() ? "✅ True" : "❌ False")
                                .append("\n");

                        // Check if manager thinks user is authenticated
                        debug.append("UserSessionManager.isUserAuthenticated(): ")
                                .append(userSessionManager.isUserAuthenticated(chatId) ? "✅ True" : "❌ False")
                                .append("\n");
                    }
                }

                sendTextMessage(chatId, debug.toString());
                break;

            case "/print_sessions":
                // Print all sessions (admin only)
                if (chatId == 7143567186l) { // Your user ID
                    StringBuilder sessions = new StringBuilder("📋 All Sessions:\n\n");

                    // Get all sessions if possible
                    if (userSessionManager != null) {
                        // Implement a method to get all sessions
                        String sessionsInfo = userSessionManager.getDiagnostics();
                        sessions.append(sessionsInfo);
                    } else {
                        sessions.append("❌ UserSessionManager is null!");
                    }

                    sendTextMessage(chatId, sessions.toString());
                } else {
                    sendTextMessage(chatId, "This command is restricted to administrators.");
                }
                break;

            default:
                sendTextMessage(chatId, "Unknown command. Use /help to see available commands.");
                break;
        }
    }

    private void sendNotAuthenticatedMessage(long chatId) {
        sendTextMessage(chatId, "⚠️ You need to connect your Spotify account first.\n\n" +
                "Use /spotify_login to authorize this bot with your Spotify account.");
    }

    private void sendHelpMessage(long chatId) {
        String helpText = "Available commands:\n\n" +
                "/spotify_login - Connect your Spotify account\n" +
                "/setcode [code] - Manually set Spotify authorization code\n" +
                "/play [track name] - Search and play a track\n" +
                "/pause - Pause playback\n" +
                "/resume - Resume playback\n" +
                "/skip or /next - Skip to next track\n" +
                "/current - Show current playing track\n" +
                "/search [query] - Search for tracks\n" +
                "/help - Show this help message";

        sendTextMessage(chatId, helpText);
    }
    // Add these methods to your SpotifyTelegramBot.java file

    private void handlePlayCommand(long chatId, String query) {
        // First, search for tracks
        List<Track> tracks = spotifyService.searchTracks(query);

        if (tracks.isEmpty()) {
            sendTextMessage(chatId, "No tracks found for your query: " + query);
            return;
        }

        // Create an inline keyboard with track options
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Track track : tracks) {
            String artists = track.getArtists()[0].getName();
            String trackInfo = String.format("%s - %s", track.getName(), artists);
            String callbackData = "play:" + track.getUri();

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(trackInfo);
            button.setCallbackData(callbackData);
            rowInline.add(button);
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Select a track to play:");
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handlePauseCommand(long chatId) {
        boolean success = spotifyService.pausePlayback();

        if (success) {
            sendTextMessage(chatId, "Playback paused.");
        } else {
            sendTextMessage(chatId, "Failed to pause playback. Make sure Spotify is running on an active device.");
        }
    }

    private void handleResumeCommand(long chatId) {
        boolean success = spotifyService.resumePlayback();

        if (success) {
            sendTextMessage(chatId, "Playback resumed.");
        } else {
            sendTextMessage(chatId, "Failed to resume playback. Make sure Spotify is running on an active device.");
        }
    }

    private void handleSkipCommand(long chatId) {
        boolean success = spotifyService.skipToNextTrack();

        if (success) {
            sendTextMessage(chatId, "Skipped to next track.");
        } else {
            sendTextMessage(chatId, "Failed to skip track. Make sure Spotify is running on an active device.");
        }
    }

    private void handleCurrentTrackCommand(long chatId) {
        CurrentlyPlaying currentlyPlaying = spotifyService.getCurrentlyPlaying();

        if (currentlyPlaying != null && currentlyPlaying.getIs_playing()) {
            Track track = (Track) currentlyPlaying.getItem();
            String artists = track.getArtists()[0].getName();

            String message = String.format("Currently playing: %s - %s", track.getName(), artists);
            sendTextMessage(chatId, message);
        } else {
            sendTextMessage(chatId, "Nothing is currently playing on Spotify.");
        }
    }

    private void handleSearchCommand(long chatId, String query) {
        // Similar to play command, but just displays results without playing
        List<Track> tracks = spotifyService.searchTracks(query);

        if (tracks.isEmpty()) {
            sendTextMessage(chatId, "No tracks found for your query: " + query);
            return;
        }

        StringBuilder response = new StringBuilder("Search results for: " + query + "\n\n");

        for (Track track : tracks) {
            String artists = track.getArtists()[0].getName();
            response.append("• ").append(track.getName()).append(" - ").append(artists).append("\n");
        }

        sendTextMessage(chatId, response.toString());
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callbackData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();

        if (callbackData.startsWith("play:")) {
            String trackUri = callbackData.substring("play:".length());
            boolean success = spotifyService.playTrack(chatId, trackUri);

            if (success) {
                sendTextMessage(chatId, "Playing track now.");
            } else {
                sendTextMessage(chatId, "Failed to play track. Make sure Spotify is running on an active device.");
            }
        }

        // Answer callback query to remove loading state
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(callbackQuery.getId());

        try {
            execute(answer);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Spotify authorization code provided by the user
     * This method exchanges the authorization code for access and refresh tokens
     * and stores them in the user's session
     *
     * @param chatId The Telegram chat ID of the user
     * @param code   The authorization code from Spotify
     * @throws ParseException
     */

    // In SpotifyTelegramBot.java
    private void handleAuthorizationCode(long chatId, String code) {
        try {
            sendTextMessage(chatId, "🔄 Processing your authorization code...");
            System.out.println("Processing authorization code for user: " + chatId);

            // Get UserSessionManager instance
            UserSessionManager sessionManager = UserSessionManager.getInstance();
            System.out.println("Got UserSessionManager instance: " + sessionManager);

            // Exchange code for tokens
            AuthorizationCodeCredentials credentials = spotifyService.exchangeAuthorizationCode(code);

            if (credentials != null) {
                System.out.println("Received credentials for user " + chatId);

                // Get or create a session
                UserSessionManager.UserSession session = sessionManager.getOrCreateSession(chatId);
                System.out.println("Got session for user " + chatId + ": " + session);

                if (session != null) {
                    // Store the credentials
                    session.setAccessToken(credentials.getAccessToken());
                    session.setRefreshToken(credentials.getRefreshToken());
                    session.setAuthenticated(true);

                    System.out.println("Stored credentials for user " + chatId +
                            " - Authentication status: " + sessionManager.isUserAuthenticated(chatId));

                    sendTextMessage(chatId, "✅ Authentication successful! You can now use Spotify commands.");
                } else {
                    System.err.println("Failed to get session for user " + chatId);
                    sendTextMessage(chatId, "❌ Error: Failed to store authentication data.");
                }
            } else {
                System.err.println("No credentials received for user " + chatId);
                sendTextMessage(chatId, "❌ Authentication failed: No credentials received.");
            }
        } catch (Exception e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            sendTextMessage(chatId, "❌ Error: " + e.getMessage());
        }
    }
}