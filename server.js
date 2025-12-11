const http = require('http');
const express = require('express');
const cors = require('cors');
const connectDB = require('./config/db');
const apiRoutes = require('./routes/apiRoutes');
const { initWebSocket } = require('./services/websocket');
require('dotenv').config();

connectDB().then(r => {});

const app = express();
app.use(cors());
app.use(express.json());
app.use('/', apiRoutes); 

const server = http.createServer(app);
initWebSocket(server);

const PORT = process.env.PORT || 5008;
server.listen(PORT, () => {
    console.log(`Server running on ${PORT}`);
});