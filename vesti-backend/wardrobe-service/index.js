const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');
const { v4: uuidv4 } = require('uuid');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8081;

app.use(cors());
app.use(express.json());
// Serves the uploads directory as static HTTP resources
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// In-memory mock database for wardrobe items
let wardrobeItems = [];

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

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Wardrobe service is running' });
});

// GET /api/wardrobe/items
app.get('/api/wardrobe/items', (req, res) => {
    // In a real app we'd filter by userId parsed from JWT
    res.status(200).json(wardrobeItems);
});

// POST /api/wardrobe/upload
app.post('/api/wardrobe/upload', upload.single('image'), (req, res) => {
    try {
        if (!req.file) {
            return res.status(400).json({ error: 'No image file provided' });
        }

        const { category, color, brand, size } = req.body;
        const imageUrl = `/uploads/${req.file.filename}`;

        const newItem = {
            id: uuidv4(),
            imageUrl,
            category: category || 'Uncategorized',
            color: color || '',
            brand: brand || '',
            size: size || '',
            createdAt: new Date().toISOString()
        };

        wardrobeItems.push(newItem);

        res.status(201).json({
            message: 'Image uploaded and item created successfully',
            item: newItem
        });
    } catch (error) {
        console.error("Upload error:", error);
        res.status(500).json({ error: 'Internal server error during upload' });
    }
});

// DELETE /api/wardrobe/items/:id
app.delete('/api/wardrobe/items/:id', (req, res) => {
    const { id } = req.params;
    const itemIndex = wardrobeItems.findIndex(i => i.id === id);

    if (itemIndex === -1) {
        return res.status(404).json({ error: 'Item not found' });
    }

    // Optionally delete from filesystem too
    const item = wardrobeItems[itemIndex];
    const filePath = path.join(__dirname, item.imageUrl);
    if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
    }

    wardrobeItems.splice(itemIndex, 1);
    res.status(200).json({ message: 'Item deleted successfully' });
});

app.listen(PORT, () => {
    console.log(`Wardrobe service listening on port ${PORT}`);
});
