export default function handler(req, res) {
  // Extract the code from the query parameters
  const { code, state, error } = req.query;

  // Log the received parameters for debugging
  console.log("Callback received:", { code: code ? "present" : "missing", error: error || "none" });

  // Create the response HTML
  let htmlResponse;

  if (error) {
    // Handle error case
    htmlResponse = `
      <html>
        <head>
          <title>Spotify Authorization Error</title>
          <style>
            body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; text-align: center; }
            h1 { color: #e74c3c; }
            .error { background-color: #f8d7da; padding: 15px; border-radius: 4px; margin: 20px 0; }
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
    // Handle successful authorization with code
    htmlResponse = `
      <html>
        <head>
          <title>Spotify Authorization Successful</title>
          <style>
            body { font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; text-align: center; }
            h1 { color: #1DB954; }
            .code-box { 
              background-color: #f4f4f4; 
              padding: 10px; 
              border-radius: 4px; 
              font-family: monospace; 
              word-break: break-all; 
              margin: 20px 0; 
              text-align: left;
              border: 1px solid #ddd;
            }
            .copy-instruction {
              font-weight: bold;
              margin: 20px 0;
            }
            .command {
              background-color: #e9f7ef;
              padding: 12px;
              border-radius: 4px;
              font-family: monospace;
              margin: 10px 0;
              display: inline-block;
            }
          </style>
        </head>
        <body>
          <h1>Spotify Authorization Successful!</h1>
          <p>Please copy the authorization code below and return to Telegram:</p>
          
          <div class="code-box" onclick="this.select()">${code}</div>
          
          <p class="copy-instruction">Then send this command to the bot:</p>
          
          <div class="command">/setcode ${code}</div>
          
          <script>
            // Add function to copy code to clipboard
            function copyToClipboard(text) {
              const textArea = document.createElement('textarea');
              textArea.value = text;
              document.body.appendChild(textArea);
              textArea.select();
              document.execCommand('copy');
              document.body.removeChild(textArea);
              alert('Code copied to clipboard!');
            }
            
            // Add click event to code box
            document.querySelector('.code-box').addEventListener('click', function() {
              copyToClipboard('${code}');
            });
            
            // Add click event to command
            document.querySelector('.command').addEventListener('click', function() {
              copyToClipboard('/setcode ${code}');
            });
          </script>
        </body>
      </html>
    `;
  } else {
    // Handle case where no code or error was provided
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
}