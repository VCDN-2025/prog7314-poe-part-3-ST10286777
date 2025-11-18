import cron from 'node-cron';
import NotificationService from './services/notificationService.js';

const notificationService = new NotificationService();

/**
 * Schedule daily reminders at 9:00 AM every day
 */
function startDailyReminderScheduler() {
    // Schedule: 0 9 * * * = Every day at 9:00 AM
    cron.schedule('0 9 * * *', async () => {
        console.log('🕘 Running daily reminder scheduler...');
        
        try {
            const result = await notificationService.sendDailyRemindersToAllUsers();
            
            if (result.success) {
                console.log(`Daily reminders sent to ${result.sent} users`);
            } else {
                console.error('Failed to send daily reminders:', result.error);
            }
        } catch (error) {
            console.error('Error in daily reminder scheduler:', error);
        }
    }, {
        timezone: "Africa/Johannesburg" 
    });

}

export { startDailyReminderScheduler };