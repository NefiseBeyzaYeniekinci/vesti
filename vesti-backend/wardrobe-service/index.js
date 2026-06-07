const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
const { PrismaClient } = require('@prisma/client');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8081;
const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_vesti_key_123';
const prisma = new PrismaClient();

app.use(cors());
app.use(express.json());
// Serves the uploads directory as static HTTP resources
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Ensure uploads directory exists
const uploadDir = path.join(__dirname, 'uploads');
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
}

// Configure Multer for file storage
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, 'uploads/');
    },
    filename: function (req, file, cb) {
        const uniqueSuffix = uuidv4() + path.extname(file.originalname);
        cb(null, file.fieldname + '-' + uniqueSuffix);
    }
});

const upload = multer({
    storage: storage,
    limits: { fileSize: 10 * 1024 * 1024 } // 10 MB limit
});

// Middleware to extract user from JWT
const authenticateToken = (req, res, next) => {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1];

    if (!token) return res.status(401).json({ error: 'No token provided' });

    jwt.verify(token, JWT_SECRET, (err, user) => {
        if (err) return res.status(403).json({ error: 'Invalid token' });
        req.user = user;
        next();
    });
};

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Wardrobe service is running' });
});

// GET /api/wardrobe/items
app.get('/api/wardrobe/items', authenticateToken, async (req, res) => {
    try {
        const items = await prisma.wardrobeItem.findMany({
            where: { userId: req.user.id },
            orderBy: { createdAt: 'desc' }
        });
        res.status(200).json(items);
    } catch (error) {
        console.error("Fetch wardrobe error:", error);
        res.status(500).json({ error: 'Internal server error fetching wardrobe' });
    }
});

// POST /api/wardrobe/upload
app.post('/api/wardrobe/upload', authenticateToken, upload.single('image'), async (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No image file provided' });
        }

        const { category, color, brand, size } = req.body;
        const imageUrl = `/uploads/${req.file.filename}`;

        const newItem = await prisma.wardrobeItem.create({
            data: {
                id: uuidv4(),
                userId: req.user.id,
                name: category || 'Uncategorized',
                category: category || 'Uncategorized',
                color: color || '',
                brand: brand || '',
                size: size || '',
                imageUrl: imageUrl,
                tags: [],
                updatedAt: new Date()
            }
        });

        res.status(201).json({
            message: 'Image uploaded and item created successfully',
            item: newItem
        });
    } catch (error) {
        console.error("Upload error:", error);
        res.status(500).json({ error: 'Internal server error during upload' });
    }
});

// PUT /api/wardrobe/items/:id
app.put('/api/wardrobe/items/:id', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const { category, color, brand, size } = req.body;
        const item = await prisma.wardrobeItem.findUnique({
            where: { id: id }
        });

        if (!item) {
            return res.status(404).json({ error: 'Item not found' });
        }

        if (item.userId !== req.user.id) {
            return res.status(403).json({ error: 'Forbidden: You do not own this item' });
        }

        const updatedItem = await prisma.wardrobeItem.update({
            where: { id: id },
            data: {
                category: category !== undefined ? category : item.category,
                name: category !== undefined ? category : item.name,
                color: color !== undefined ? color : item.color,
                brand: brand !== undefined ? brand : item.brand,
                size: size !== undefined ? size : item.size,
                updatedAt: new Date()
            }
        });

        res.status(200).json(updatedItem);
    } catch (error) {
        console.error("Update item error:", error);
        res.status(500).json({ error: 'Internal server error during update' });
    }
});

// DELETE /api/wardrobe/items/:id
app.delete('/api/wardrobe/items/:id', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const item = await prisma.wardrobeItem.findUnique({
            where: { id: id }
        });

        if (!item) {
            return res.status(404).json({ error: 'Item not found' });
        }

        if (item.userId !== req.user.id) {
            return res.status(403).json({ error: 'Forbidden: You do not own this item' });
        }

        // Delete from filesystem too
        if (item.imageUrl) {
            const filePath = path.join(__dirname, item.imageUrl);
            if (fs.existsSync(filePath)) {
                fs.unlinkSync(filePath);
            }
        }

        await prisma.wardrobeItem.delete({
            where: { id: id }
        });

        res.status(200).json({ message: 'Item deleted successfully' });
    } catch (error) {
        console.error("Delete item error:", error);
        res.status(500).json({ error: 'Internal server error during delete' });
    }
});

// Graceful shutdown handling
process.on('SIGINT', async () => {
    console.log("Shutting down wardrobe-service...");
    await prisma.$disconnect();
    process.exit(0);
});

app.listen(PORT, () => {
    console.log(`Wardrobe service listening on port ${PORT}`);
});

