package bot.auth;

import java.util.HashMap;
import java.util.Map;

public class UserSessionManager {
    private Map<Long, SpotifyUserSession> sessions = new HashMap<>();

    public SpotifyUserSession getOrCreateSession(long chatId) {
        if (!sessions.containsKey(chatId)) {
            sessions.put(chatId, new SpotifyUserSession());
        }
        return sessions.get(chatId);
    }

    public static class SpotifyUserSession {
        private String accessToken;
        private String refreshToken;
        private boolean isAuthenticated = false;

        // Getters and setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            this.isAuthenticated = true;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public boolean isAuthenticated() {
            return isAuthenticated;
        }
    }
}
