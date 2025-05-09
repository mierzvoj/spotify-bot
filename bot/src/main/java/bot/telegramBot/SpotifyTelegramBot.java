package bot.telegramBot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import bot.auth.UserSessionManager;
import bot.services.SpotifyService;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;

public class SpotifyTelegramBot extends TelegramLongPollingBot {

    private final SpotifyService spotifyService = new SpotifyService();
    private final UserSessionManager sessionManager = new UserSessionManager();

    public SpotifyTelegramBot(SpotifyService spotifyService) {
    }

    @Override
    public String getBotUsername() {
        return "mierzvoj_bot";
    }

    @Override
    public String getBotToken() {
        return "";
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

    private void handleCommand(String command, long chatId) {
        switch (command) {
            case "/start":
                sendTextMessage(chatId, "Welcome to Spotify Bot! Use /login to connect your Spotify account.");
                break;
            case "/login":
                sendLoginMessage(chatId);
                break;
            case "/play":
                // Handle play command
                break;
            case "/pause":
                // Handle pause command
                break;
            case "/search":
                // Handle search command
                break;
            // Add more commands as needed
            default:
                sendTextMessage(chatId, "Unknown command. Use /help to see available commands.");
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

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // Handle commands
            if (messageText.startsWith("/")) {
                handleCommand(messageText, chatId);
            }
        }

        // Handle callbacks from inline keyboards
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callbackData.startsWith("play_")) {
                String trackUri = callbackData.substring(5);
                spotifyService.playTrack(trackUri);
                sendTextMessage(chatId, "Playing track...");
            }
        }
    }

    private void handleSearchCommand(String messageText, long chatId) {
        String query = messageText.substring("/search ".length());
        List<Track> tracks = spotifyService.searchTracks(query);

        if (tracks.isEmpty()) {
            sendTextMessage(chatId, "No tracks found for your query.");
            return;
        }

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (int i = 0; i < Math.min(5, tracks.size()); i++) {
            Track track = tracks.get(i);
            List<InlineKeyboardButton> row = new ArrayList<>();

            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(track.getName() + " - " + track.getArtists()[0].getName());
            button.setCallbackData("play_" + track.getUri());

            row.add(button);
            rowsInline.add(row);
        }

        markupInline.setKeyboard(rowsInline);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Search results for: " + query);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}