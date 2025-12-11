const bcrypt = require('bcrypt');
const crypto = require('crypto'); // Import Node.js crypto module
const Driver = require('../models/driver');
const {generateOTP, sendOTP} = require("../services/otpService");

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

const forgotPassword = async (req, res) => {
    try {
        const { driverId } = req.body;

        const driver = await Driver.findOne({ driverId: driverId.toUpperCase() });
        if (!driver) {
            return res.status(404).json({ message: "Driver ID not found" });
        }
        const otp = generateOTP();

        driver.resetPasswordToken = otp;
        driver.resetPasswordExpires = Date.now() + 10 * 60 * 1000;
        await driver.save();

        await sendOTP(driver.name, driver.driverId, otp);

        res.status(200).json({ message: "OTP sent successfully" });

    } catch (e) {
        console.error("OTP Error:", e.message);
        res.status(500).json({ message: "Server error generating OTP" });
    }
};

const resetPasswordWithOtp = async (req, res) => {
    try {
        const { driverId, otp, newPassword } = req.body;

        const driver = await Driver.findOne({
            driverId: driverId.toUpperCase(),
            resetPasswordToken: otp,
            resetPasswordExpires: { $gt: Date.now() }
        });

        if (!driver) {
            return res.status(400).json({ message: "Invalid or expired OTP" });
        }

        const salt = await bcrypt.genSalt(10);
        driver.passwordHash = await bcrypt.hash(newPassword, salt);

        driver.resetPasswordToken = null;
        driver.resetPasswordExpires = null;

        await driver.save();

        res.status(200).json({ message: "Password reset successful" });

    } catch (e) {
        console.error("Reset Error:", e.message);
        res.status(500).json({ message: "Server error resetting password" });
    }
};
module.exports = { loginDriver, forgotPassword,resetPasswordWithOtp};