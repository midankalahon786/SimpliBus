const express = require('express');
const router = express.Router();
const { loginDriver } = require('../controllers/authController');
const { updateLocation, getSchedule, getBuses } = require('../controllers/trackingController');

// Driver Routes
router.post('/driver/login', loginDriver);

// Tracking Routes
router.post('/update', updateLocation);

// Passenger Data Routes
router.get('/schedule', getSchedule);
router.get('/buses', getBuses);

module.exports = router;