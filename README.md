# Spotify Telegram Bot

A Java-based Telegram bot that allows users to control their Spotify playback through Telegram commands.

## Features

- Authenticate with Spotify using OAuth 2.0
- Play, pause, and skip tracks
- Search for songs and playlists
- Control volume and playback
- View currently playing track information
- Create and manage playlists

## Technologies Used

- Java 17+
- Telegram Bot API (TelegramBots library)
- Spotify Web API Java
- Vercel (for OAuth callback hosting)
- Maven/Gradle for dependency management

## Prerequisites

- JDK 17 or higher
- Telegram Bot token (from BotFather)
- Spotify Developer account and application credentials
- Maven or Gradle

## Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/spotify-telegram-bot.git
cd spotify-telegram-bot
```

### 2. Spotify Developer Setup

1. Create a new application in the [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Set a Redirect URI: `https://your-vercel-app.vercel.app/api/callback`
3. Note your Client ID and Client Secret

### 3. Telegram Bot Setup

1. Talk to [@BotFather](https://t.me/botfather) on Telegram
2. Use the `/newbot` command to create a new bot
3. Note the API token provided

### 4. OAuth Callback Setup

1. Deploy the callback handler to Vercel:
   ```bash
   cd bot/api
   vercel
   ```
2. Add the deployed URL + `/api/callback` as a Redirect URI in your Spotify Dashboard

### 5. Configure Environment Variables

Create a file named `assets/token.env` in your project with the following content:

```
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
SPOTIFY_REDIRECT_URI=https://your-vercel-app.vercel.app/api/callback
```

### 6. Build and Run

For Maven:
```bash
mvn clean package
java -jar target/spotify-telegram-bot-1.0.jar
```

For Gradle:
```bash
gradle build
java -jar build/libs/spotify-telegram-bot-1.0.jar
```

## Usage

### Bot Commands

- `/start` - Initialize the bot and get welcome message
- `/spotify_login` - Connect your Spotify account
- `/setcode [code]` - Manually set Spotify authorization code
- `/play [track name]` - Search and play a track
- `/pause` - Pause playback
- `/resume` - Resume playback
- `/skip` - Skip to next track
- `/previous` - Go to previous track
- `/current` - Show current playing track
- `/volume [0-100]` - Set volume
- `/search [query]` - Search for tracks
- `/help` - Show available commands

## Project Structure

```
spotify-telegram-bot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── spotify/
│   │   │           └── telegram/
│   │   │               └── bot/
│   │   │                   ├── Main.java
│   │   │                   ├── SpotifyTelegramBot.java
│   │   │                   ├── SpotifyService.java
│   │   │                   ├── UserSessionManager.java
│   │   │                   └── Config.java
│   │   └── resources/
│   │       └── assets/
│   │           └── token.env
│   └── test/
│       └── java/
│           └── com/
│               └── spotify/
│                   └── telegram/
│                       └── bot/
│                           └── SpotifyTelegramBotTest.java
├── bot/
│   └── api/
│       ├── callback.js
│       └── vercel.json
├── pom.xml (or build.gradle)
└── README.md
```

## Authentication Flow

1. User sends `/spotify_login` to the bot
2. Bot generates an authorization URL and sends it to the user
3. User clicks the URL and authorizes the application on Spotify
4. Spotify redirects to the callback URL with an authorization code
5. User copies the code from the callback page
6. User sends `/setcode [code]` to the bot
7. Bot exchanges the code for access and refresh tokens
8. User can now use Spotify commands

## Extending the Bot

To add new commands:

1. Add a new case in the `handleCommand` method in `SpotifyTelegramBot.java`
2. Implement the corresponding method in `SpotifyService.java`
3. Add the command to the help message

## Error Handling

The bot implements robust error handling for:
- Spotify API errors (with token refresh)
- Network issues
- Invalid user input
- Authentication failures

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/new-feature`
3. Commit your changes: `git commit -am 'Add new feature'`
4. Push to the branch: `git push origin feature/new-feature`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgements

- [Telegram Bot Java Library](https://github.com/rubenlagus/TelegramBots)
- [Spotify Web API Java](https://github.com/spotify-web-api-java/spotify-web-api-java)
- [Vercel](https://vercel.com) for hosting the OAuth callback
