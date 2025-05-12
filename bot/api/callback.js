export default function handler(req, res) {
  // Extract code and state from query parameters
  const { code, state, error } = req.query;

  // Build response HTML
  let htmlResponse;

  if (error) {
    htmlResponse = `
      <html>
        <head>
          <title>Spotify Authorization Error</title>
          <style>
            body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; text-align: center; }
            h1 { color: #e74c3c; }
            .error { background-color: #f8d7da; padding: 15px; border-radius: 4px; margin: 20px 0; }
            .code { background-color: #f4f4f4; padding: 15px; border-radius: 4px; font-family: monospace; word-break: break-all; margin: 20px 0; }
          </style>
        </head>
        <body>
          <h1>Spotify Authorization Error</h1>
          <div class="error">Error: ${error}</div>
          <p>Please return to the Telegram bot and try again.</p>
        </body>
      </html>
    `;
  } else if (code) {
    htmlResponse = `
      <html>
        <head>
          <title>Spotify Authorization Successful</title>
          <style>
            body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; text-align: center; }
            h1 { color: #1DB954; }
            .code { background-color: #f4f4f4; padding: 15px; border-radius: 4px; font-family: monospace; word-break: break-all; margin: 20px 0; }
            .instructions { text-align: left; }
          </style>
        </head>
        <body>
          <h1>Spotify Authorization Successful</h1>
          <p>Please copy the code below and paste it in your Telegram bot chat:</p>
          <div class="code">${code}</div>
          <div class="instructions">
            <p><strong>How to complete authorization:</strong></p>
            <ol>
              <li>Copy the code above</li>
              <li>Return to your Telegram chat with the bot</li>
              <li>Send the command: /setcode ${code}</li>
            </ol>
          </div>
        </body>
      </html>
    `;
  } else {
    htmlResponse = `
      <html>
        <head>
          <title>Spotify Authorization</title>
          <style>
            body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; text-align: center; }
            h1 { color: #3498db; }
          </style>
        </head>
        <body>
          <h1>Spotify Authorization</h1>
          <p>No authorization code was received. Please try again.</p>
          <p>Query parameters: ${JSON.stringify(req.query)}</p>
        </body>
      </html>
    `;
  }

  // Send the response
  res.status(200).send(htmlResponse);

  // Optional: Log the request for debugging
  console.log(`Callback received: ${new Date().toISOString()} - Code: ${code ? 'present' : 'missing'}, Error: ${error || 'none'}`);
}