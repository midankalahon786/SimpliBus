const bcrypt = require('bcrypt');
const Driver = require('../models/driver');

const loginDriver = async (req, res) => {
    try {
        const { driverId, password } = req.body;
        const driver = await Driver.findOne({ driverId: driverId.toUpperCase() });
        
        if (!driver) return res.status(401).json({ message: "Invalid ID or password" });
        
        const isMatch = await bcrypt.compare(password, driver.passwordHash);
        if (!isMatch) return res.status(401).json({ message: "Invalid ID or password" });

        res.status(200).json({
            message: "Login successful",
            name: driver.name,
            busId: driver.busId
        });
    } catch (e) {
        console.error("Login failed:", e.message);
        res.status(500).json({ message: "Server error during login" });
    }
};

module.exports = { loginDriver };