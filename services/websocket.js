const { WebSocketServer } = require('ws');

let wss;

const initWebSocket = (server) => {
    wss = new WebSocketServer({ noServer: true });

    wss.on('connection', (ws) => {
        console.log('Client (Tracker) connected');
        ws.send(JSON.stringify({ type: 'status', message: 'Connected to Bus Tracking Server' }));

        ws.on('close', () => {
            console.log('Client (Tracker) disconnected');
        });
    });

    server.on('upgrade', (request, socket, head) => {
        wss.handleUpgrade(request, socket, head, (ws) => {
            wss.emit('connection', ws, request);
        });
    });

    console.log("WebSocket Server Initialized");
};

const broadcastUpdate = (data) => {
    if (!wss) return;
    const message = JSON.stringify({ type: 'location_update', payload: data });
    
    wss.clients.forEach(client => {
        if (client.readyState === 1) {
            client.send(message);
        }
    });
};



module.exports = { initWebSocket, broadcastUpdate };