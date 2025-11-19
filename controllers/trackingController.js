const { busSchedule, availableBuses } = require('../data/busData');
const { getHaversineDistance } = require('../utils/geometry');
const { broadcastUpdate } = require('../services/websocket');

// Runtime State
const busStates = {};
const AVERAGE_BUS_SPEED_KPH = 20;
const AVERAGE_BUS_SPEED_MPS = AVERAGE_BUS_SPEED_KPH * 1000 / 3600;
const ARRIVAL_THRESHOLD_METERS = 50;

const updateLocation = (req, res) => {
    try {
        const data = req.body;
        console.log('Raw Update Received:', data.busId);

        // 1. Initialize State if needed
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
                nextStopIndex: 0,
                seatStatus: "Seats Available"
            };
            console.log(`Initialized state for ${data.busId}`);
        }

        const state = busStates[data.busId];

        // 2. Update Seat Status
        if (data.seatStatus) {
            state.seatStatus = data.seatStatus;
        }

        // 3. Calculate Logic
        if (state.nextStopIndex >= state.routeStops.length) state.nextStopIndex = 0;

        let nextStop = state.routeStops[state.nextStopIndex];
        let distanceToNextStop = getHaversineDistance(data.lat, data.lng, nextStop.lat, nextStop.lng);

        if (distanceToNextStop < ARRIVAL_THRESHOLD_METERS) {
            console.log(`Bus ${data.busId} arrived at ${nextStop.name}`);
            state.nextStopIndex++;
            if (state.nextStopIndex < state.routeStops.length) {
                nextStop = state.routeStops[state.nextStopIndex];
                distanceToNextStop = getHaversineDistance(data.lat, data.lng, nextStop.lat, nextStop.lng);
            }
        }

        const etaMinutes = Math.round((distanceToNextStop / AVERAGE_BUS_SPEED_MPS) / 60);
        const formattedDistance = distanceToNextStop < 1000 ? 
            `${Math.round(distanceToNextStop)}m` : 
            `${(distanceToNextStop / 1000).toFixed(1)}km`;

        const enhancedPayload = {
            ...data,
            route: busSchedule[state.routeKey].name,
            nextStop: nextStop.name,
            nextStopIndex: state.nextStopIndex,
            distance: formattedDistance,
            eta: etaMinutes,
            seatStatus: state.seatStatus
        };

        // 4. Broadcast via WebSocket Service
        console.log("Broadcasting Payload:", JSON.stringify(enhancedPayload));
        broadcastUpdate(enhancedPayload);

        res.status(200).json({ status: 'success', received: enhancedPayload });

    } catch (e) {
        console.error('Error in updateLocation:', e.message);
        res.status(400).json({ error: e.message });
    }
};

const getSchedule = (req, res) => {
    res.status(200).json(busSchedule);
};

const getBuses = (req, res) => {
    res.status(200).json(availableBuses);
};

module.exports = { updateLocation, getSchedule, getBuses };