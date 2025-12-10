const axios = require('axios');

// --- CONFIGURATION ---
// Ensure this matches your running server URL & Port
const BACKEND_URL = 'http://localhost:5008/update';
const BUS_ID = 'R1-Bus1 (Route 1)';
const MOVING_INTERVAL_MS = 1000; // Speed when moving
const STOP_WAIT_TIME_MS = 5000;  // Time to wait at a bus stop (5 seconds)

// --- KEY STOPS ---
const keyStops = [
    { name: "High Court", lat: 26.191723, lng: 91.750913 },
    { name: "Panbazar", lat: 26.187856, lng: 91.742101 },
    { name: "Fancy Bazar", lat: 26.180045, lng: 91.735532 },
    { name: "Bharalumukh", lat: 26.170349, lng: 91.723823 },
    { name: "Kamakhya Gate", lat: 26.162297, lng: 91.713014 },
    { name: "Maligaon", lat: 26.157321, lng: 91.669515 },
    { name: "Main Gate", lat: 26.154979, lng: 91.662775 },
    { name: "Dharapur", lat: 26.137339, lng: 91.628036 }
];

// --- HELPER: Haversine Distance (Meters) ---
function getDistanceMeters(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // Earth radius in meters
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
}

// --- 1. FETCH ROAD PATH (OSRM) ---
const fetchRoadPath = async (stops) => {
    console.log("🗺️  Fetching real road geometry from OSRM...");
    const coordinatesString = stops.map(stop => `${stop.lng},${stop.lat}`).join(';');
    const url = `http://router.project-osrm.org/route/v1/driving/${coordinatesString}?overview=full&geometries=geojson`;

    try {
        const response = await axios.get(url);
        if (!response.data.routes || response.data.routes.length === 0) throw new Error("No route found");

        const rawCoordinates = response.data.routes[0].geometry.coordinates;
        // Swap [Lng, Lat] to [Lat, Lng]
        return rawCoordinates.map(coord => ({ lat: coord[1], lng: coord[0] }));
    } catch (error) {
        console.error("❌ Failed to fetch road path:", error.message);
        return stops;
    }
};

// --- 2. MAIN SIMULATION LOOP ---
const startSimulation = async () => {
    const fullRoadPath = await fetchRoadPath(keyStops);
    console.log(`✅ Path generated: ${fullRoadPath.length} points.`);

    let pathIndex = 0;
    let nextStopTargetIndex = 0; // We only look forward to this specific stop

    const sendUpdate = async () => {
        // Reset if we reached the end
        if (pathIndex >= fullRoadPath.length) {
            console.log("🏁 Route Completed! Restarting...");
            pathIndex = 0;
            nextStopTargetIndex = 0;
        }

        const currentPoint = fullRoadPath[pathIndex];
        let isAtStop = false;
        let stopName = "";

        // CHECK: Are we near the *Next Scheduled Stop*?
        // We iterate through keyStops to check proximity, but only for the one we expect next.
        if (nextStopTargetIndex < keyStops.length) {
            const targetStop = keyStops[nextStopTargetIndex];
            const distance = getDistanceMeters(currentPoint.lat, currentPoint.lng, targetStop.lat, targetStop.lng);

            // If within 40 meters of the stop, trigger the wait
            if (distance < 40) {
                isAtStop = true;
                stopName = targetStop.name;
                nextStopTargetIndex++; // Advance to the NEXT stop so we don't wait here again
            }
        }

        // SEND TO BACKEND
        try {
            await axios.post(BACKEND_URL, {
                busId: BUS_ID,
                lat: currentPoint.lat,
                lng: currentPoint.lng,
                speed: isAtStop ? 0 : 40, // Speed is 0 if waiting
                seatStatus: "Seats Available"
            });

            if (isAtStop) {
                console.log(`🛑 ARRIVED: ${stopName}. Waiting ${STOP_WAIT_TIME_MS/1000}s...`);
            } else if (pathIndex % 5 === 0) {
                // Log strictly moving updates less frequently
                console.log(`🚌 Moving... (${currentPoint.lat.toFixed(5)}, ${currentPoint.lng.toFixed(5)})`);
            }

        } catch (error) {
            console.error(`❌ Connection Error: ${error.message}`);
        }

        // NEXT STEP LOGIC
        pathIndex++;

        if (isAtStop) {
            // If at a stop, wait longer
            setTimeout(sendUpdate, STOP_WAIT_TIME_MS);
        } else {
            // If moving, wait standard interval
            setTimeout(sendUpdate, MOVING_INTERVAL_MS);
        }
    };

    await sendUpdate();
};

startSimulation().then(r => {});