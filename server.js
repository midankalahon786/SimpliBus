const http = require('http');
const express = require('express');
const cors = require('cors');
const connectDB = require('./config/db');
const apiRoutes = require('./routes/apiRoutes');
const { initWebSocket } = require('./services/websocket');
require('dotenv').config();

// 1. Connect to DB
connectDB();

// 2. Init Express
const app = express();
app.use(cors());
app.use(express.json()); // Parse JSON bodies

// 3. Mount Routes
// This maps routes like '/update' to the root. 
// If you wanted '/api/update', you'd change this to app.use('/api', apiRoutes);
app.use('/', apiRoutes); 

// 4. Create HTTP Server
const server = http.createServer(app);

// 5. Init WebSocket (Attach to server)
initWebSocket(server);

// 6. Start Server
const PORT = process.env.PORT || 5008;
server.listen(PORT, () => {
    console.log(`Server running on ${PORT}`);
});