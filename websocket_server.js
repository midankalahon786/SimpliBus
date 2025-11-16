/*
 * REAL-TIME BUS TRACKING WEBSOCKET SERVER (Node.js)
 * CORRECTED VERSION: Calculates ETA, Distance, and Next Stop
 * UPDATED: Now includes Route 1 Road Path for Google Maps
 */

const http = require('http');
const { WebSocketServer } = require('ws');

// --- ROUTE PATHS (Co-ordinates for drawing the road line) ---
// These are the coordinates you provided, converted to {lat, lng} format
const route1Path = [
    { lat: 26.191723256896395, lng: 91.75091364604606 }, // High Court Area
    { lat: 26.187856859134016, lng: 91.74210188225084 },
    { lat: 26.18004583399204, lng: 91.73553201473135 },
    { lat: 26.17034960036014, lng: 91.72382333336549 },
    { lat: 26.16939127942156, lng: 91.72264880599897 },
    { lat: 26.162297996440103, lng: 91.71301454976145 },
    { lat: 26.158974808415493, lng: 91.69660569704462 },
    { lat: 26.157321564999094, lng: 91.66951573321228 },
    { lat: 26.15632697131649, lng: 91.66626620117711 },
    { lat: 26.154979792758283, lng: 91.66277545333338 }, // Main Gate Area
    { lat: 26.152249880471572, lng: 91.65576203341601 },
    { lat: 26.146166336017217, lng: 91.64604649753977 },
    { lat: 26.141016635215337, lng: 91.63969030262365 },
    { lat: 26.137339875043992, lng: 91.62803634220342 }  // Dharapur Area
];

// --- Schedule Data ---
const busSchedule = {
    // --- ROUTE 1: HIGH COURT <-> DHARAPUR ---
    route1: {
        name: "Route 1: High Court - Dharapur",
        
        // --- NEW: Added the path here so Android can read it ---
        path: route1Path, 
        // -----------------------------------------------------

        stops: [
            { name: "High Court", lat: 26.1885, lng: 91.7535 },
            { name: "Panbazar", lat: 26.1834, lng: 91.7475 },
            { name: "Fancy Bazar", lat: 26.1805, lng: 91.7405 },
            { name: "Bharalumukh", lat: 26.1705, lng: 91.7305 },
            { name: "Santipur", lat: 26.1655, lng: 91.7255 },
            { name: "Kamakhya Gate", lat: 26.1605, lng: 91.7105 },
            { name: "Maligaon Chariali", lat: 26.1575, lng: 91.7005 },
            { name: "Guest House", lat: 26.1565, lng: 91.6685 },
            { name: "NAB", lat: 26.1558, lng: 91.6655 },
            { name: "Main Gate", lat: 26.1553, lng: 91.6627 },
            { name: "Satmile", lat: 26.1505, lng: 91.6555 },
            { name: "Forest School Gate", lat: 26.1455, lng: 91.6505 },
            { name: "Lankeshwar", lat: 26.1425, lng: 91.6485 },
            { name: "Dharapur", lat: 26.1385, lng: 91.6405 }
        ],
        stopsReturn: [
            { name: "Dharapur", lat: 26.1385, lng: 91.6405 },
            { name: "Lankeshwar", lat: 26.1425, lng: 91.6485 },
            { name: "Forest School Gate", lat: 26.1455, lng: 91.6505 },
            { name: "Satmile", lat: 26.1505, lng: 91.6555 },
            { name: "Main Gate", lat: 26.1553, lng: 91.6627 },
            { name: "NAB", lat: 26.1558, lng: 91.6655 },
            { name: "Guest House", lat: 26.1565, lng: 91.6685 },
            { name: "Maligaon Chariali", lat: 26.1575, lng: 91.7005 },
            { name: "Santipur", lat: 26.1655, lng: 91.7255 },
            { name: "Bharalumukh", lat: 26.1705, lng: 91.7305 },
            { name: "Fancy Bazar", lat: 26.1805, lng: 91.7405 },
            { name: "Panbazar", lat: 26.1834, lng: 91.7475 },
            { name: "High Court", lat: 26.1885, lng: 91.7535 }
        ]
    },
    // --- ROUTE 2: BASISTHA <-> AT-7 BOYS HALL ---
    route2: {
        name: "Route 2: Basistha Chariali - AT-7 Boys Hall",
        stops: [
            { name: "Basistha Chariali", lat: 26.1085, lng: 91.7885 },
            { name: "Lokhora", lat: 26.1125, lng: 91.7505 },
            { name: "ISBT", lat:26.1155, lng: 91.7205 },
            { name: "Garchuk", lat:26.1165, lng: 91.7005 },
            { name: "Boragaon", lat:26.1205, lng: 91.6905 },
            { name: "Tetelia", lat:26.1355, lng: 91.6705 },
            { name: "GST House", lat:26.1505, lng: 91.6655 },
            { name: "NAB", lat:26.1558, lng: 91.6655 },
            { name: "GU Main Gate", lat:26.1553, lng: 91.6627 },
            { name: "AT-7 Boys Hall", lat:26.1545, lng: 91.6605 }
        ],
        stopsReturn: [
            { name: "AT-7 Boys Hall", lat: 26.1545, lng: 91.6605 },
            { name: "GU Main Gate", lat: 26.1553, lng: 91.6627 },
            { name: "NAB", lat: 26.1558, lng: 91.6655 },
            { name: "GST House", lat: 26.1505, lng: 91.6655 },
            { name: "Tetelia", lat: 26.1355, lng: 91.6705 },
            { name: "ISBT", lat: 26.1155, lng: 91.7205 },
            { name: "Lokhora", lat: 26.1125, lng: 91.7505 },
            { name: "Basistha Chariali", lat: 26.1085, lng: 91.7885 }
        ]
    }
};

const availableBuses = [
    "R1-Bus1 (Route 1)",
    "R1-Bus2 (Route 1)",
    "R2-Bus1 (Route 2)",
    "R2-Bus2 (Route 2)"
];

// --- Haversine distance function ---
function getHaversineDistance(lat1, lng1, lat2, lng2) {
    const R = 6371e3; // Earth's radius in meters
    const phi1 = lat1 * Math.PI / 180;
    const phi2 = lat2 * Math.PI / 180;
    const deltaPhi = (lat2 - lat1) * Math.PI / 180;
    const deltaLambda = (lng2 - lng1) * Math.PI / 180;

    const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
              Math.cos(phi1) * Math.cos(phi2) *
              Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; 
}

// --- Bus state management ---
const busStates = {}; 
const AVERAGE_BUS_SPEED_KPH = 20; 
const AVERAGE_BUS_SPEED_MPS = AVERAGE_BUS_SPEED_KPH * 1000 / 3600; 
const ARRIVAL_THRESHOLD_METERS = 50; 

// --- WebSocket Server Setup ---
const wss = new WebSocketServer({ noServer: true });
const clients = new Set();

wss.on('connection', (ws) => {
    console.log('Client (Tracker) connected');
    clients.add(ws);
    ws.send(JSON.stringify({ type: 'status', message: 'Connected to Bus Tracking Server' }));

    ws.on('close', () => {
        console.log('Client (Tracker) disconnected');
        clients.delete(ws);
    });
});

// --- HTTP Server Setup ---
const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*');
    res.setHeader('Access-Control-Allow-Methods', 'POST, GET, OPTIONS');
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

    if (req.method === 'OPTIONS') {
        res.writeHead(204); res.end(); return;
    }

    if (req.url === '/update' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => { body += chunk.toString(); });
        req.on('end', () => {
            try {
                const data = JSON.parse(body);
                console.log('Raw Update Received:', data.busId); // LOG 1

                // 1. Find or initialize bus state
                if (!busStates[data.busId]) {
                    const now = new Date();
                    const isAfternoon = now.getHours() >= 12;
                    const direction = isAfternoon ? 'stopsReturn' : 'stops';
                    
                    let routeKey = 'route1'; 
                    if (data.busId.toLowerCase().includes('r2')) {
                        routeKey = 'route2';
                    }
                    
                    busStates[data.busId] = {
                        routeKey: routeKey,
                        routeStops: busSchedule[routeKey][direction],
                        nextStopIndex: 0 
                    };
                    console.log(`Initialized state for ${data.busId}`); // LOG 2
                }
                
                const state = busStates[data.busId];
                
                // 2. Reset if finished
                if (state.nextStopIndex >= state.routeStops.length) state.nextStopIndex = 0; 

                // 3. Calculate stats
                let nextStop = state.routeStops[state.nextStopIndex];
                let distanceToNextStop = getHaversineDistance(data.lat, data.lng, nextStop.lat, nextStop.lng);

                if (distanceToNextStop < ARRIVAL_THRESHOLD_METERS) {
                    console.log(`Bus ${data.busId} arrived at ${nextStop.name}`); // LOG 3
                    state.nextStopIndex++;
                    if (state.nextStopIndex < state.routeStops.length) {
                        nextStop = state.routeStops[state.nextStopIndex];
                        distanceToNextStop = getHaversineDistance(data.lat, data.lng, nextStop.lat, nextStop.lng);
                    }
                }

                const etaMinutes = Math.round((distanceToNextStop / AVERAGE_BUS_SPEED_MPS) / 60); 
                const formattedDistance = distanceToNextStop < 1000 ? `${Math.round(distanceToNextStop)}m` : `${(distanceToNextStop / 1000).toFixed(1)}km`;

                const enhancedPayload = {
                    ...data,
                    route: busSchedule[state.routeKey].name,
                    nextStop: nextStop.name,
                    nextStopIndex: state.nextStopIndex,
                    distance: formattedDistance, 
                    eta: etaMinutes
                };

                console.log("Broadcasting Payload:", JSON.stringify(enhancedPayload));

                // Broadcast
                const message = JSON.stringify({ type: 'location_update', payload: enhancedPayload });
                clients.forEach(client => {
                    if (client.readyState === 1) client.send(message);
                });

                res.writeHead(200, { 'Content-Type': 'application/json' });
                res.end(JSON.stringify({ status: 'success', received: enhancedPayload }));
            } catch (e) {
                console.error('Error:', e.message);
                res.writeHead(400); res.end();
            }
        });
    } else if (req.url === '/schedule' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(busSchedule)); 
    } else if (req.url === '/buses' && req.method === 'GET') {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(availableBuses));
    } else {
        res.writeHead(404); res.end();
    }
});

server.on('upgrade', (request, socket, head) => {
    wss.handleUpgrade(request, socket, head, (ws) => { wss.emit('connection', ws, request); });
});

server.listen(8080, () => {
    console.log(`Server running on 8080. Send POST to /update.`);
});