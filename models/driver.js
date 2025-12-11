const mongoose = require('mongoose');

const driverSchema = new mongoose.Schema({
    driverId: {
        type: String,
        required: true,
        unique: true,
        uppercase: true,
        trim: true
    },
    name: {
        type: String,
        required: true
    },
    busId: {
        type: String,
        required: true
    },
    passwordHash: {
        type: String,
        required: true
    },
    resetPasswordToken: {
        type: String,
        default: null
    },
    resetPasswordExpires: {
        type: Date,
        default: null
    }
});

const Driver = mongoose.model('Driver', driverSchema);

module.exports = Driver;