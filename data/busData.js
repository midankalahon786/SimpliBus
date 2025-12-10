// Existing Route 1 Path (High Court - Dharapur)
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

// NEW: Route 2 Path (Basistha - AT-7/University)
// Converted from your GeoJSON input
const route2Path = [
    { lat: 26.11205485150343, lng: 91.79773107257887 },  // Basistha Chariali
    { lat: 26.111469144653626, lng: 91.7495224375283 },  // Lokhora
    { lat: 26.115502000355363, lng: 91.71961162304586 }, // ISBT
    { lat: 26.12264518598184, lng: 91.6856471675498 },   // Boragaon
    { lat: 26.131860030886898, lng: 91.6738097434523 },  // Tetelia
    { lat: 26.15731598395476, lng: 91.66941229420814 },  // Jalukbari/Uni Approach
    { lat: 26.15636657640306, lng: 91.66639549144122 },
    { lat: 26.15492313442553, lng: 91.6628466168699 },
    { lat: 26.14921660741878, lng: 91.65451999108274 }
];

const busSchedule = {
    route1: {
        name: "Route 1: High Court - Dharapur",
        path: route1Path,
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
    route2: {
        name: "Route 2: Basistha Chariali - AT-7 Boys Hall",
        path: route2Path, // <--- UPDATED HERE
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

module.exports = { busSchedule, availableBuses };