// services/otpService.js

// 1. Generate a 6-digit number
const generateOTP = () => {
    return Math.floor(100000 + Math.random() * 900000).toString();
};

// 2. Send the OTP (Currently mocks it, but this is where you add Email/SMS logic)
const sendOTP = async (recipientName, recipientId, otp) => {
    // In the future, you will replace this block with Nodemailer or Twilio code
    console.log(`\n========================================`);
    console.log(`[OTP SERVICE] Sending OTP...`);
    console.log(`To: ${recipientName} (ID: ${recipientId})`);
    console.log(`CODE: ${otp}`);
    console.log(`========================================\n`);

    // Return true if sent successfully
    return true;
};

module.exports = { generateOTP, sendOTP };