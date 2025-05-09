package com.spotify.telegram.bot.shared;

import com.spotify.telegram.bot.services.SpotifyService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenRefreshScheduler {
    private final SpotifyService spotifyService;
    private final ScheduledExecutorService executorService;

    public TokenRefreshScheduler(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    public void startScheduler() {
        // Refresh token every 50 minutes (Spotify tokens expire after 60 minutes)
        executorService.scheduleAtFixedRate(
                this::refreshToken,
                50,
                50,
                TimeUnit.MINUTES
        );
    }

    private void refreshToken() {
        try {
            spotifyService.refreshAccessToken();
            System.out.println("Access token refreshed successfully.");
        } catch (Exception e) {
            System.out.println("Failed to refresh access token: " + e.getMessage());
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}