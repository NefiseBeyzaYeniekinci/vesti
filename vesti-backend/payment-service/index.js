const express = require('express');
const cors = require('cors');
const { v4: uuidv4 } = require('uuid');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8084;

app.use(cors());
app.use(express.json());

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Payment service is running securely' });
});

// Güvenli Ödeme İşlemi Mock Endpoint
app.post('/api/payment/checkout', (req, res) => {
    const { itemId, amount, currency, paymentMethod } = req.body;

    // Basit doğrulama işlemleri
    if (!itemId || !amount || !paymentMethod) {
        return res.status(400).json({
            success: false,
            error: 'Eksik veya hatalı ödeme bilgisi.'
        });
    }

    // 1. Kredi Kartı Format Doğrulaması (Çok Basit Formatta: Sadece 16 hane mi?)
    const cardNumber = paymentMethod.cardNumber?.replace(/\s+/g, '');
    if (!cardNumber || cardNumber.length !== 16 || isNaN(Number(cardNumber))) {
        return res.status(400).json({
            success: false,
            error: 'Geçersiz kredi kartı numarası.'
        });
    }

    // Cvv Doğrulama
    const cvv = paymentMethod.cvv;
    if (!cvv || (cvv.length !== 3 && cvv.length !== 4)) {
        return res.status(400).json({
            success: false,
            error: 'Geçersiz CVV/CVC kodu.'
        });
    }

    // NOT: Gerçek bir sistemde burada Stripe/Iyzico API çağrılır. 
    // Asla kredi kartı bilgileri veritabanına loglanmaz veya kaydedilmez.

    // Mock başarı yanıtı %90 ihtimalle başarılı say
    const isSuccess = Math.random() < 0.9;

    if (isSuccess) {
        // Güvenli Token üretimi (Stripe Charge Id gibi simülasyon)
        const transactionId = `txn_${uuidv4().replace(/-/g, '').substring(0, 24)}`;

        return res.status(200).json({
            success: true,
            transactionId: transactionId,
            message: 'Ödemeniz başarıyla alındı.',
            receiptUrl: `https://vesti.app/receipts/${transactionId}`
        });
    } else {
        return res.status(400).json({
            success: false,
            error: 'Ödeme reddedildi. Lütfen kart limitinizi veya bakiyenizi kontrol edin.'
        });
    }
});

app.listen(PORT, () => {
    console.log(`Secure Payment service listening on port ${PORT}`);
});
