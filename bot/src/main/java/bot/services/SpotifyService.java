package bot.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import bot.auth.UserSessionManager;
import bot.auth.UserSessionManager.UserSession;

import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlaying;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import se.michaelthelin.spotify.requests.data.player.GetInformationAboutUsersCurrentPlaybackRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class SpotifyService {
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    URI redirectUri = URI.create("https://spotbot3-muhf813tp-mierzvojs-projects.vercel.app/api/callback");

    private final SpotifyApi spotifyApi;

    public SpotifyService() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(redirectUri)
                .build();
        System.out.println("SpotifyService initialized with redirect URI: " + redirectUri);
    }

    public String getAuthorizationUrl() {
        try {
            AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                    .scope("user-read-playback-state user-modify-playback-state user-read-currently-playing user-read-private")
                    .show_dialog(true) // Force consent screen to show
                    .state(String.valueOf(System.currentTimeMillis())) // Prevent caching
                    .build();

            URI uri = authorizationCodeUriRequest.execute();
            System.out.println("Generated authorization URL: " + uri);
            return uri.toString();
        } catch (Exception e) {
            System.err.println("Error generating authorization URL: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void setAuthorizationCode(String code) {
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            final AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            // Set access and refresh tokens
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            System.out.println("Expires in: " + credentials.getExpiresIn());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Search for tracks on Spotify
     * 
     * @param query The search query
     * @return List of tracks found
     */
    public List<Track> searchTracks(String query) {
        try {
            SearchTracksRequest searchRequest = spotifyApi.searchTracks(query)
                    .limit(5) // Limit to 5 results for simplicity
                    .build();

            Paging<Track> results = searchRequest.execute();
            return Arrays.asList(results.getItems());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error searching tracks: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Play a track on the user's active device
     * 
     * @param trackUri The Spotify URI of the track
     * @return True if successful, false otherwise
     */
    // In SpotifyService.java
public boolean playTrack(long userId, String trackUri) {
    // Get user session from UserSessionManager
    UserSession session = UserSessionManager.getInstance().getSession(userId);
    
    if (session == null || !session.isAuthenticated()) {
        System.err.println("User " + userId + " not authenticated");
        return false;
    }
    
    try {
        // Check if token is expired and refresh if needed
        if (session.isTokenExpired()) {
            boolean refreshed = refreshAccessToken(userId);
            if (!refreshed) {
                System.err.println("Failed to refresh token for user " + userId);
                return false;
            }
        }
        
        // Set the user's access token for this request
        spotifyApi.setAccessToken(session.getAccessToken());
        
        // Make the API call
        JsonArray urisArray = JsonParser.parseString("[\"" + trackUri + "\"]").getAsJsonArray();
        spotifyApi.startResumeUsersPlayback()
                .uris(urisArray)
                .build()
                .execute();
                
        return true;
    } catch (Exception e) {
        System.err.println("Error playing track for user " + userId + ": " + e.getMessage());
        return false;
    }
}

// Refresh token for a specific user
public boolean refreshAccessToken(long userId) {
    UserSession session = UserSessionManager.getInstance().getSession(userId);
    
    if (session == null || session.getRefreshToken() == null) {
        System.err.println("No refresh token available for user " + userId);
        return false;
    }
    
    try {
        // Set the refresh token for this request
        spotifyApi.setRefreshToken(session.getRefreshToken());
        
        // Refresh the token
        AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh()
                .build();
                
        AuthorizationCodeCredentials credentials = refreshRequest.execute();
        
        // Update the session with new token
        session.setAccessToken(credentials.getAccessToken());
        
        // Update refresh token if provided
        if (credentials.getRefreshToken() != null) {
            session.setRefreshToken(credentials.getRefreshToken());
        }
        
        System.out.println("Refreshed token for user " + userId);
        return true;
    } catch (Exception e) {
        System.err.println("Error refreshing token for user " + userId + ": " + e.getMessage());
        return false;
    }
}
    /**
     * Pause the user's playback
     * 
     * @return True if successful, false otherwise
     */
    public boolean pausePlayback() {
        try {
            spotifyApi.pauseUsersPlayback()
                    .build()
                    .execute();
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error pausing playback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Resume the user's playback
     * 
     * @return True if successful, false otherwise
     */
    public boolean resumePlayback() {
        try {
            spotifyApi.startResumeUsersPlayback()
                    .build()
                    .execute();
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error resuming playback: " + e.getMessage());
            return false;
        }
    }

    /**
     * Skip to the next track
     * 
     * @return True if successful, false otherwise
     */
    public boolean skipToNextTrack() {
        try {
            spotifyApi.skipUsersPlaybackToNextTrack()
                    .build()
                    .execute();
            return true;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error skipping to next track: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get information about the currently playing track
     * 
     * @return The currently playing track or null if nothing is playing
     */
    public CurrentlyPlaying getCurrentlyPlaying() {
        try {
            return spotifyApi.getUsersCurrentlyPlayingTrack()
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error getting currently playing track: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Refreshes the Spotify access token using the stored refresh token
     * 
     * @return True if the token was successfully refreshed, false otherwise
     */
    public boolean refreshAccessToken() {
        try {
            // Get the current refresh token from the API instance
            String refreshToken = spotifyApi.getRefreshToken();

            if (refreshToken == null || refreshToken.isEmpty()) {
                System.err.println("Cannot refresh token: No refresh token available");
                return false;
            }

            // Create the refresh request
            AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh()
                    .build();

            // Execute the refresh request
            AuthorizationCodeCredentials credentials = refreshRequest.execute();

            // Update the access token in the API instance
            spotifyApi.setAccessToken(credentials.getAccessToken());

            // Update the refresh token if a new one was provided
            if (credentials.getRefreshToken() != null && !credentials.getRefreshToken().isEmpty()) {
                spotifyApi.setRefreshToken(credentials.getRefreshToken());
            }

            // Log token refresh (server-side only)
            System.out.println("Access token refreshed. New token expires in: " +
                    credentials.getExpiresIn() + " seconds");

            return true;
        } catch (IOException e) {
            System.err.println("Network error refreshing access token: " + e.getMessage());
            return false;
        } catch (SpotifyWebApiException e) {
            System.err.println("Spotify API error refreshing access token: " + e.getMessage());
            return false;
        } catch (ParseException e) {
            System.err.println("Parse error refreshing access token: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error refreshing access token: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Refreshes the access token for a specific user using their stored refresh
     * token
     * 
     * @param userRefreshToken The user's refresh token
     * @return The new access token if successful, null otherwise
     */
    public String refreshAccessToken(String userRefreshToken) {
        try {
            // Create a temporary SpotifyApi instance with just the client credentials and
            // refresh token
            SpotifyApi tempApi = new SpotifyApi.Builder()
                    .setClientId(spotifyApi.getClientId())
                    .setClientSecret(spotifyApi.getClientSecret())
                    .setRefreshToken(userRefreshToken)
                    .build();

            // Create the refresh request
            AuthorizationCodeRefreshRequest refreshRequest = tempApi.authorizationCodeRefresh()
                    .build();

            // Execute the refresh request
            AuthorizationCodeCredentials credentials = refreshRequest.execute();

            // Return the new access token
            return credentials.getAccessToken();
        } catch (Exception e) {
            System.err.println("Error refreshing user token: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Exchanges the authorization code from Spotify for access and refresh tokens
     * 
     * @param code The authorization code from Spotify's OAuth redirect
     * @return AuthorizationCodeCredentials containing access and refresh tokens
     * @throws IOException            If there's a network error
     * @throws SpotifyWebApiException If there's an error from the Spotify API
     * @throws ParseException         If there's an error parsing the response
     */
    public AuthorizationCodeCredentials exchangeAuthorizationCode(String code)
            throws IOException, SpotifyWebApiException, ParseException {
        try {
            // Create the authorization code request
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code)
                    .build();

            // Execute the request to exchange the code for tokens
            AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            // Update the API instance with the new tokens
            spotifyApi.setAccessToken(credentials.getAccessToken());
            spotifyApi.setRefreshToken(credentials.getRefreshToken());

            // Log token expiration (server-side only)
            System.out.println("Access token expires in: " + credentials.getExpiresIn() + " seconds");

            return credentials;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.err.println("Error exchanging authorization code: " + e.getMessage());
            throw e; // Rethrow to handle in the calling method
        } catch (Exception e) {
            System.err.println("Unexpected error exchanging authorization code: " + e.getMessage());
            throw new IOException("Unexpected error: " + e.getMessage(), e);
        }
    }
}