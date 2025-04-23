const express = require('express');
const cors = require('cors');
const dotenv = require('dotenv');

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;

// Middleware
app.use(cors());
app.use(express.json());

// Mock verification endpoint
app.post('/verify-turnstile', (req, res) => {
  const { token } = req.body;
  
  // In a real implementation, you would verify the token with Cloudflare
  // https://developers.cloudflare.com/turnstile/get-started/server-side-validation/
  
  // For mock purposes, we'll consider any token valid except empty ones
  if (!token) {
    return res.status(400).json({
      success: false,
      error: 'Missing token'
    });
  }
  
  // Simulate a successful verification (with 90% success rate)
  const isSuccess = Math.random() < 0.9;
  
  if (isSuccess) {
    return res.json({
      success: true,
      hostname: 'localhost'
    });
  } else {
    return res.status(400).json({
      success: false,
      error: 'Invalid token'
    });
  }
});

app.listen(PORT, () => {
  console.log(`Mock server running on port ${PORT}`);
}); 