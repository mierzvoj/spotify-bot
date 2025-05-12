package bot.shared;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class UserSessionManager {
    // Initialize the map in the declaration to ensure it's never null
    private final Map<Long, UserSession> sessions = new HashMap<>();

    // Singleton pattern implementation
    private static UserSessionManager instance;

    // Private constructor for singleton
    private UserSessionManager() {
        System.out.println("UserSessionManager: New instance created with empty sessions map");
    }

    // Static method to get the singleton instance
    public static synchronized UserSessionManager getInstance() {
        if (instance == null) {
            instance = new UserSessionManager();
        }
        return instance;
    }

    // Get a session for a chat ID
    public UserSession getSession(long chatId) {
        UserSession session = sessions.get(chatId);
        System.out.println("Getting session for user " + chatId + ": " + (session != null ? "found" : "not found"));
        return session;
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

    // Log out a user (ADDED THIS MISSING METHOD)
    public void logoutUser(long chatId) {
        if (sessions.containsKey(chatId)) {
            System.out.println("Logging out user: " + chatId);
            sessions.remove(chatId);
        } else {
            System.out.println("Logout requested for non-existent session: " + chatId);
        }
    }

    // Dump the state of all sessions for debugging
    public String getDiagnostics() {
        StringBuilder sb = new StringBuilder();
        sb.append("UserSessionManager Diagnostics:\n");
        sb.append("Total sessions: ").append(sessions.size()).append("\n\n");

        if (sessions.isEmpty()) {
            sb.append("No active sessions found.");
        } else {
            sb.append("Active sessions:\n");
            for (Map.Entry<Long, UserSession> entry : sessions.entrySet()) {
                long userId = entry.getKey();
                UserSession session = entry.getValue();

                sb.append("User ID: ").append(userId).append("\n");
                sb.append("  Has access token: ").append(session.getAccessToken() != null).append("\n");
                sb.append("  Has refresh token: ").append(session.getRefreshToken() != null).append("\n");
                sb.append("  Authenticated: ").append(session.isAuthenticated()).append("\n");
                sb.append("  Token expires: ").append(new Date(session.getTokenExpiry())).append("\n");
                sb.append("  Token expired: ").append(session.isTokenExpired()).append("\n\n");
            }
        }

        return sb.toString();
    }

    // User session class
    public static class UserSession {
        private String accessToken;
        private String refreshToken;
        private boolean authenticated = false;
        private long tokenExpiry = 0;

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
            if (accessToken != null && !accessToken.isEmpty()) {
                this.authenticated = true;
                // Set token expiry to 50 minutes from now (Spotify tokens last 60 mins)
                this.tokenExpiry = System.currentTimeMillis() + (50 * 60 * 1000);
                System.out.println("Access token set - authenticated=true, expires at: " +
                        new Date(tokenExpiry));
            }
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public boolean isAuthenticated() {
            boolean hasToken = accessToken != null && !accessToken.isEmpty();
            boolean notExpired = System.currentTimeMillis() < tokenExpiry;

            // Log the check for debugging
            System.out.println("isAuthenticated check: hasToken=" + hasToken +
                    ", notExpired=" + notExpired + ", flag=" + authenticated);

            return authenticated && hasToken && notExpired;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
            System.out.println("Explicitly setting authenticated=" + authenticated);
        }

        // ADDED THIS MISSING METHOD
        public boolean isTokenExpired() {
            boolean expired = tokenExpiry > 0 && System.currentTimeMillis() >= tokenExpiry;
            System.out.println("isTokenExpired check: expired=" + expired +
                    ", current time=" + new Date(System.currentTimeMillis()) +
                    ", expiry time=" + new Date(tokenExpiry));
            return expired;
        }

        // Getter for tokenExpiry (for diagnostics)
        public long getTokenExpiry() {
            return tokenExpiry;
        }
    }
}