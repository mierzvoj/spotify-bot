// For ESM format
export default function handler(req, res) {
  const { code } = req.query;
  return res.status(200).send(`
    <html><body>
      <h1>Spotify Authorization Successful</h1>
      <p>You can now return to your application.</p>
    </body></html>
  `);
}

// OR for CommonJS format
module.exports = function handler(req, res) {
  const { code } = req.query;
  return res.status(200).send(`
    <html><body>
      <h1>Spotify Authorization Successful</h1>
      <p>You can now return to your application.</p>
    </body></html>
  `);
};
