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

const jwt = require('jsonwebtoken');
const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_vesti_key_123';

// Middleware for JWT verification
const verifyToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    if (!authHeader) return res.status(401).json({ error: 'Access denied. No token provided.' });

    // Expecting 'Bearer <token>'
    const token = authHeader.split(' ')[1];

    if (!token) return res.status(401).json({ error: 'Access denied. Invalid token format.' });

    try {
        const verified = jwt.verify(token, JWT_SECRET);
        req.user = verified;
        next();
    } catch (error) {
        res.status(401).json({ error: 'Invalid token.' });
    }
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
