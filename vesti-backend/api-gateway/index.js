const express = require('express');
const { createProxyMiddleware } = require('http-proxy-middleware');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

app.use(express.json());

// Basic health check
app.get('/health', (req, res) => {
    res.status(200).json({ status: 'API Gateway is running' });
});

// Middleware for JWT verification (placeholder)
const verifyToken = (req, res, next) => {
    const token = req.headers['authorization'];
    if (!token) return res.status(401).json({ error: 'Access denied' });
    // In a real scenario, we verify with jsonwebtoken
    // For now, allow through
    next();
};

// Routing rules mapping to other services
app.use('/auth', createProxyMiddleware({ target: 'http://auth-service:8080', changeOrigin: true }));
app.use('/wardrobe', verifyToken, createProxyMiddleware({ target: 'http://wardrobe-service:8080', changeOrigin: true }));
app.use('/ai', verifyToken, createProxyMiddleware({ target: 'http://ai-service:8000', changeOrigin: true }));
app.use('/weather', verifyToken, createProxyMiddleware({ target: 'http://weather-service:8080', changeOrigin: true }));
app.use('/marketplace', verifyToken, createProxyMiddleware({ target: 'http://marketplace-service:8080', changeOrigin: true }));
app.use('/messaging', verifyToken, createProxyMiddleware({ target: 'http://messaging-service:8080', changeOrigin: true }));

app.listen(PORT, () => {
    console.log(`API Gateway listening on port ${PORT}`);
});
