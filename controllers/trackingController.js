const { busSchedule, availableBuses } = require('../data/busData');
const { getHaversineDistance } = require('../utils/geometry');
const { broadcastUpdate } = require('../services/websocket');

const busStates = {};
const AVERAGE_BUS_SPEED_KPH = 25;
const AVERAGE_BUS_SPEED_MPS = AVERAGE_BUS_SPEED_KPH * 1000 / 3600;
const ARRIVAL_THRESHOLD_METERS = 100;

const findCorrectStopIndex = (currentLat, currentLng, routeStops) => {
    let closestIndex = 0;
    let minDistance = Infinity;

    for (let i = 0; i < routeStops.length; i++) {
        const stop = routeStops[i];
        const dist = getHaversineDistance(currentLat, currentLng, stop.lat, stop.lng);
        if (dist < minDistance) {
            minDistance = dist;
            closestIndex = i;
        }
    }
    if (minDistance < 50 && closestIndex < routeStops.length - 1) {
        return closestIndex + 1;
    }

    return closestIndex;
};

const updateLocation = (req, res) => {
    try {
        const data = req.body;
        if (!busStates[data.busId]) {
            const now = new Date();
            const isAfternoon = now.getHours() >= 12;
            const direction = isAfternoon ? 'stopsReturn' : 'stops';

            let routeKey = 'route1';
            if (data.busId.toLowerCase().includes('r2')) {
                routeKey = 'route2';
            }
            const routeStops = busSchedule[routeKey][direction];
            const startIndex = findCorrectStopIndex(data.lat, data.lng, routeStops);

            busStates[data.busId] = {
                routeKey: routeKey,
                routeStops: routeStops,
                nextStopIndex: startIndex,
                seatStatus: "Seats Available"
            };
            console.log(`Initialized ${data.busId} at Index ${startIndex} (${routeStops[startIndex].name})`);
        }

        const state = busStates[data.busId];
        if (data.seatStatus) state.seatStatus = data.seatStatus;
        let currentTarget = state.routeStops[state.nextStopIndex];
        let distToCurrent = getHaversineDistance(data.lat, data.lng, currentTarget.lat, currentTarget.lng);

        if (distToCurrent > 2000) {
            console.log(`[${data.busId}] Drift detected (Target: ${currentTarget.name} is ${Math.round(distToCurrent)}m away). Recalibrating...`);
            state.nextStopIndex = findCorrectStopIndex(data.lat, data.lng, state.routeStops);
            currentTarget = state.routeStops[state.nextStopIndex];
            distToCurrent = getHaversineDistance(data.lat, data.lng, currentTarget.lat, currentTarget.lng);
        }
        if (state.nextStopIndex < state.routeStops.length - 1) {
            const nextTarget = state.routeStops[state.nextStopIndex + 1];
            const distToNext = getHaversineDistance(data.lat, data.lng, nextTarget.lat, nextTarget.lng);

            if (distToCurrent < ARRIVAL_THRESHOLD_METERS) {
                console.log(`[${data.busId}] ✅ Arrived at ${currentTarget.name}`);
                state.nextStopIndex++;
                currentTarget = nextTarget;
                distToCurrent = distToNext;
            } else if (distToNext < ARRIVAL_THRESHOLD_METERS) {
                console.log(`[${data.busId}] ⏩ Skipped to ${nextTarget.name}`);
                state.nextStopIndex++;
                currentTarget = nextTarget;
                distToCurrent = distToNext;
            }
        } else {
            if (distToCurrent < ARRIVAL_THRESHOLD_METERS) {
                console.log(`[${data.busId}] 🏁 Route Completed. Resetting.`);
                state.nextStopIndex = 0;
                currentTarget = state.routeStops[0];
                distToCurrent = getHaversineDistance(data.lat, data.lng, currentTarget.lat, currentTarget.lng);
            }
        }
        const etaMinutes = Math.round((distToCurrent / AVERAGE_BUS_SPEED_MPS) / 60);
        const formattedDistance = distToCurrent < 1000 ?
            `${Math.round(distToCurrent)}m` :
            `${(distToCurrent / 1000).toFixed(1)}km`;

        const enhancedPayload = {
            ...data,
            route: busSchedule[state.routeKey].name,
            nextStop: currentTarget.name,
            nextStopIndex: state.nextStopIndex,
            distance: formattedDistance,
            eta: etaMinutes,
            seatStatus: state.seatStatus
        };

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