const express = require('express');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');
const { PrismaClient } = require('@prisma/client');
const Redis = require('ioredis');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;
const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_vesti_key_123';

const prisma = new PrismaClient();
const redis = new Redis(process.env.REDIS_URL || 'redis://localhost:6379/0');

app.use(cors());
app.use(express.json());

// Graceful shutdown handling
process.on('SIGINT', async () => {
    console.log("Shutting down auth-service...");
    await prisma.$disconnect();
    redis.quit();
    process.exit(0);
});

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Auth service is running' });
});

app.post('/register', async (req, res) => {
    try {
        const { name, email, password } = req.body;

        if (!name || !email || !password) {
            return res.status(400).json({ error: 'Name, email and password are required' });
        }

        const existingUser = await prisma.user.findUnique({ where: { email } });
        if (existingUser) {
            return res.status(400).json({ error: 'User already exists' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = await prisma.user.create({
            data: {
                name,
                email,
                password: hashedPassword
            }
        });

        res.status(201).json({
            message: 'User registered successfully',
            user: { id: newUser.id, name: newUser.name, email: newUser.email }
        });
    } catch (error) {
        console.error("Register error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ error: 'Email and password are required' });
        }

        const user = await prisma.user.findUnique({ where: { email } });
        if (!user) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(401).json({ error: 'Invalid email or password' });
        }

        const token = jwt.sign({ id: user.id, email: user.email }, JWT_SECRET, { expiresIn: '7d' });

        res.status(200).json({
            message: 'Login successful',
            token,
            user: { id: user.id, name: user.name, email: user.email }
        });
    } catch (error) {
        console.error("Login error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

// Middleware to verify token and check Redis blocklist
const authenticateToken = async (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) return res.status(401).json({ error: 'No token provided' });

    try {
        // Check if token is blocklisted in Redis
        const isBlocklisted = await redis.get(`blocklist:${token}`);
        if (isBlocklisted) {
            return res.status(403).json({ error: 'Token is invalid (logged out)' });
        }

        jwt.verify(token, JWT_SECRET, (err, user) => {
            if (err) return res.status(403).json({ error: 'Invalid token' });
            req.user = user;
            req.token = token; // Make token available for logout
            next();
        });
    } catch (err) {
        console.error("Auth middleware error:", err);
        return res.status(500).json({ error: 'Internal server error' });
    }
};

app.get('/me', authenticateToken, async (req, res) => {
    try {
        const user = await prisma.user.findUnique({ where: { id: req.user.id } });
        if (!user) return res.status(404).json({ error: 'User not found' });

        res.json({ user: { id: user.id, name: user.name, email: user.email } });
    } catch (error) {
        console.error("Me endpoint error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.post('/logout', authenticateToken, async (req, res) => {
    try {
        // Decode token to find exact expiration time to set TTL in Redis
        const decoded = jwt.decode(req.token);
        const expTime = decoded.exp - Math.floor(Date.now() / 1000);

        if (expTime > 0) {
            // Blocklist the token for its remaining lifespan
            await redis.set(`blocklist:${req.token}`, 'true', 'EX', expTime);
        }
        res.status(200).json({ message: 'Logout successful' });
    } catch (error) {
        console.error("Logout error:", error);
        res.status(500).json({ error: 'Internal server error' });
    }
});

app.listen(PORT, () => {
    console.log(`Auth service listening on port ${PORT}`);
});
