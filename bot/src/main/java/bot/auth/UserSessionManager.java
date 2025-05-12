package bot.auth;

import java.util.HashMap;
import java.util.Map;

public class UserSessionManager {
    // Store sessions by chat ID
    private final Map<Long, UserSession> sessions = new HashMap<>();

    // Singleton pattern (optional)
    private static UserSessionManager instance;

    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    // Get an existing session
    public UserSession getSession(long chatId) {
        return sessions.get(chatId);
    }

    // Get or create a session
    public UserSession getOrCreateSession(long chatId) {
        if (!sessions.containsKey(chatId)) {
            System.out.println("Creating new session for user: " + chatId);
            sessions.put(chatId, new UserSession());
        } else {
            System.out.println("Using existing session for user: " + chatId);
        }
        return sessions.get(chatId);
    }

    // Check if a user is authenticated
    public boolean isUserAuthenticated(long chatId) {
        UserSession session = sessions.get(chatId);
        boolean isAuth = session != null && session.isAuthenticated();
        System.out.println("User " + chatId + " authentication status: " + isAuth);
        return isAuth;
    }

    // Log out a user
    public void logoutUser(long chatId) {
        sessions.remove(chatId);
        System.out.println("Logged out user: " + chatId);
    }

    // User session class
    public static class UserSession {
        private String accessToken;
        private String refreshToken;
        private boolean authenticated = false;
        private long tokenExpiry = 0; // Unix timestamp when token expires

        // Getters and setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            this.authenticated = true;
            // Set expiry time to 1 hour from now (Spotify default)
            this.tokenExpiry = System.currentTimeMillis() + 3600000;
            System.out.println("Access token set, expires at: " + new java.util.Date(tokenExpiry));
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public boolean isAuthenticated() {
            // Check if authenticated and token not expired
            boolean validToken = authenticated && System.currentTimeMillis() < tokenExpiry;
            if (authenticated && !validToken) {
                System.out.println("Token has expired for user");
            }
            return validToken;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
            if (!authenticated) {
                this.tokenExpiry = 0;
            }
        }

        public boolean isTokenExpired() {
            return System.currentTimeMillis() >= tokenExpiry;
        }
    }
}