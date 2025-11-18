
import express from "express";
import NotificationService from '../services/notificationService.js';
import authMiddleware from '../middleware/auth.js';

const router = express.Router();
const notificationService = new NotificationService();

// Apply auth middleware to all routes
router.use(authMiddleware);

/**
 * Send daily reminders to all users
 */
router.post('/send-daily-reminders', async (req, res) => {
    try {
        
        const result = await notificationService.sendDailyRemindersToAllUsers();
        
        if (result.success) {
            res.json({
                success: true,
                message: `Daily reminders sent successfully to ${result.sent} users`,
                data: result
            });
        } else {
            res.status(500).json({
                success: false,
                error: result.error,
                message: 'Failed to send daily reminders'
            });
        }

    } catch (error) {
        console.error('Error in send-daily-reminders:', error);
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: 'Failed to send daily reminders'
        });
    }
});

/**
 * Send test notification to current user
 */
router.post('/send-test', async (req, res) => {
    try {
        const userId = req.user.uid;
        const { title = 'Test Notification', message = 'This is a test notification' } = req.body;

        const result = await notificationService.sendToUser(
            userId, 
            title, 
            message, 
            { type: 'test' }
        );

        if (result.success) {
            res.json({
                success: true,
                message: 'Test notification sent successfully',
                data: result
            });
        } else {
            res.status(400).json({
                success: false,
                error: result.error,
                message: 'Failed to send test notification'
            });
        }

    } catch (error) {
        console.error('Error sending test notification:', error);
        res.status(500).json({
            success: false,
            error: 'Internal server error',
            message: 'Failed to send test notification'
        });
    }
});

export default router;