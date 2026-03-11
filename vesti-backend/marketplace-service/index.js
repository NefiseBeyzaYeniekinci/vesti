const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8083;

app.use(cors());
app.use(express.json());

// Mock Veritabanı
let marketItems = [
    {
        id: "item-001",
        sellerId: "user-abc",
        title: "Vintage Denim Ceket",
        description: "Az kullanılmış, orjinal vintage jean ceket. L bedendir.",
        price: 350.00,
        currency: "TRY",
        imageUrl: "https://images.unsplash.com/photo-1578932750294-f5075e85f44a?q=80&w=400&auto=format&fit=crop",
        category: "Ceket",
        size: "L",
        condition: "Çok İyi",
        createdAt: new Date().toISOString()
    },
    {
        id: "item-002",
        sellerId: "user-xyz",
        title: "Siyah Triko Kazak",
        description: "Yumuşacık triko, hiç giyilmedi, etiketi üzerinde.",
        price: 150.00,
        currency: "TRY",
        imageUrl: "https://images.unsplash.com/photo-1599839619722-39751411ea63?q=80&w=400&auto=format&fit=crop",
        category: "Kazak",
        size: "M",
        condition: "Yeni-Etiketli",
        createdAt: new Date(Date.now() - 86400000).toISOString()
    },
    {
        id: "item-003",
        sellerId: "user-123",
        title: "Converse Chuck 70",
        description: "Sadece 2 defa giyildi. Klasik siyah 41 numara.",
        price: 850.00,
        currency: "TRY",
        imageUrl: "https://images.unsplash.com/photo-1608231387042-66d1773070a5?q=80&w=400&auto=format&fit=crop",
        category: "Ayakkabı",
        size: "41",
        condition: "İyi",
        createdAt: new Date(Date.now() - 172800000).toISOString()
    }
];

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Marketplace service is running' });
});

// Tüm pazar yeri ürünlerini getir (Feed)
app.get('/api/marketplace/items', (req, res) => {
    // Gelecekte query ile kategori filtreleme, sayfalama eklenecek
    res.status(200).json(marketItems);
});

// Belirtilen ürünü getir
app.get('/api/marketplace/items/:id', (req, res) => {
    const item = marketItems.find(i => i.id === req.params.id);
    if (!item) {
        return res.status(404).json({ error: 'Item not found' });
    }
    res.status(200).json(item);
});

// Yeni ürün oluştur (Mock)
app.post('/api/marketplace/items', (req, res) => {
    const { title, description, price, imageUrl, category, size, condition } = req.body;

    if (!title || price == null) {
        return res.status(400).json({ error: 'Title and price are required' });
    }

    const newItem = {
        id: uuidv4(),
        sellerId: "current-user", // Gerçekte JWT'den alınacak
        title,
        description: description || "",
        price: Number(price),
        currency: "TRY", // Şimdilik hep TRY
        imageUrl: imageUrl || "",
        category: category || "Other",
        size: size || "Standart",
        condition: condition || "Kullanılmış",
        createdAt: new Date().toISOString()
    };

    marketItems.unshift(newItem); // En başa ekle (feed'de yeni görünsün)
    res.status(201).json(newItem);
});

app.listen(PORT, () => {
    console.log(`Marketplace service listening on port ${PORT}`);
});
