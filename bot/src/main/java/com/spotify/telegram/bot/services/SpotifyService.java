package com.spotify.telegram.bot.services;

import com.google.gson.JsonParser;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
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
    private static final URI REDIRECT_URI = URI.create("");

    private final SpotifyApi spotifyApi;
    
    public SpotifyService() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .setRedirectUri(REDIRECT_URI)
                .build();
    }
    
    public String getAuthorizationUrl() {
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("user-read-playback-state user-modify-playback-state user-read-currently-playing")
                .build();
        
        final URI uri = authorizationCodeUriRequest.execute();
        return uri.toString();
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
    
    // Methods for playing, pausing, searching music, etc.
    public void playTrack(String trackUri) {
        try {
            spotifyApi.startResumeUsersPlayback()
                    .uris(JsonParser.parseString("[\"" + trackUri + "\"]").getAsJsonArray())
                    .build()
                    .execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public void pausePlayback() {
        try {
            spotifyApi.pauseUsersPlayback().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public List<Track> searchTracks(String query) {
        try {
            final SearchTracksRequest searchTracksRequest = spotifyApi.searchTracks(query).build();
            final Paging<Track> trackPaging = searchTracksRequest.execute();
            return Arrays.asList(trackPaging.getItems());
        } catch (IOException | SpotifyWebApiException e) {
            System.out.println("Error: " + e.getMessage());
            return new ArrayList<>();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void nextTrack() {
        try {
            spotifyApi.skipUsersPlaybackToNextTrack().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void previousTrack() {
        try {
            spotifyApi.skipUsersPlaybackToPreviousTrack().build().execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public CurrentlyPlayingContext getCurrentlyPlaying() {
        try {
            final GetInformationAboutUsersCurrentPlaybackRequest request =
                    spotifyApi.getInformationAboutUsersCurrentPlayback().build();
            return request.execute();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    public void refreshAccessToken() {
        try {
            // Create a request to refresh the access token
            final AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh()
                    .build();

            // Execute the refresh request
            final AuthorizationCodeCredentials credentials = refreshRequest.execute();

            // Update the access token
            spotifyApi.setAccessToken(credentials.getAccessToken());

            // Optionally update the refresh token if a new one is provided
            if (credentials.getRefreshToken() != null) {
                spotifyApi.setRefreshToken(credentials.getRefreshToken());
            }

            System.out.println("Access token refreshed. New token expires in: "
                    + credentials.getExpiresIn() + " seconds");

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error refreshing access token: " + e.getMessage());
        }
    }
}