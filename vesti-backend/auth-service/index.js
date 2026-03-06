const express = require('express');
require('dotenv').config();

const app = express();
const PORT = process.env.PORT || 8080;

app.use(express.json());

app.get('/health', (req, res) => {
    res.status(200).json({ status: 'Auth service is running' });
});

app.listen(PORT, () => {
    console.log(`Auth service listening on port ${PORT}`);
});
