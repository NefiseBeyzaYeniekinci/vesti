const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
const { PrismaClient } = require('@prisma/client');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8083;
const JWT_SECRET = process.env.JWT_SECRET || 'super_secret_vesti_key_123';
const prisma = new PrismaClient();

app.use(cors());
app.use(express.json());

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
    res.status(200).json({ status: 'Marketplace service is running' });
});

// Helper function to map Prisma Listing to MarketplaceItemDto
const mapListingToDto = (listing) => {
    return {
        id: listing.id,
        sellerId: listing.userId,
        title: listing.title,
        description: listing.description,
        price: listing.price,
        currency: "TRY",
        imageUrl: listing.images[0] || "",
        category: listing.category,
        size: listing.size || "Standart",
        condition: listing.condition,
        createdAt: listing.createdAt.toISOString()
    };
};

// Tüm pazar yeri ürünlerini getir (Feed)
app.get('/api/marketplace/items', authenticateToken, async (req, res) => {
    try {
        const listings = await prisma.listing.findMany({
            where: { status: 'active' },
            orderBy: { createdAt: 'desc' }
        });
        res.status(200).json(listings.map(mapListingToDto));
    } catch (error) {
        console.error("Fetch marketplace items error:", error);
        res.status(500).json({ error: 'Internal server error fetching feed' });
    }
});

// Belirtilen ürünü getir
app.get('/api/marketplace/items/:id', authenticateToken, async (req, res) => {
    try {
        const listing = await prisma.listing.findUnique({
            where: { id: req.params.id }
        });
        if (!listing) {
            return res.status(404).json({ error: 'Item not found' });
        }
        res.status(200).json(mapListingToDto(listing));
    } catch (error) {
        console.error("Fetch listing details error:", error);
        res.status(500).json({ error: 'Internal server error fetching listing details' });
    }
});

// Yeni ürün oluştur
app.post('/api/marketplace/items', authenticateToken, async (req, res) => {
    try {
        const { title, description, price, imageUrl, category, size, condition } = req.body;

        if (!title || price == null) {
            return res.status(400).json({ error: 'Title and price are required' });
        }

        const newListing = await prisma.listing.create({
            data: {
                id: uuidv4(),
                userId: req.user.id,
                title: title,
                description: description || "",
                price: Number(price),
                category: category || "Other",
                size: size || "Standart",
                condition: condition || "Kullanılmış",
                images: imageUrl ? [imageUrl] : [],
                status: "active",
                updatedAt: new Date()
            }
        });

        res.status(201).json(mapListingToDto(newListing));
    } catch (error) {
        console.error("Create listing error:", error);
        res.status(500).json({ error: 'Internal server error creating listing' });
    }
});

// Ürün sil (Sadece kendi ürününü)
app.delete('/api/marketplace/items/:id', authenticateToken, async (req, res) => {
    try {
        const { id } = req.params;
        const listing = await prisma.listing.findUnique({
            where: { id: id }
        });

        if (!listing) {
            return res.status(404).json({ error: 'Item not found' });
        }

        if (listing.userId !== req.user.id) {
            return res.status(403).json({ error: 'Forbidden: You do not own this item' });
        }

        await prisma.listing.delete({
            where: { id: id }
        });

        res.status(200).json({ message: 'Listing deleted successfully' });
    } catch (error) {
        console.error("Delete listing error:", error);
        res.status(500).json({ error: 'Internal server error deleting listing' });
    }
});

// Graceful shutdown handling
process.on('SIGINT', async () => {
    console.log("Shutting down marketplace-service...");
    await prisma.$disconnect();
    process.exit(0);
});

app.listen(PORT, () => {
    console.log(`Marketplace service listening on port ${PORT}`);
});

