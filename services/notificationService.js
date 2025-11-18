
import { admin, db } from '../config/firebase-admin.js';

export class NotificationService {
   
    /**
     * Send a daily reminder to a specific user
     */
    async sendDailyReminder(fcmToken) {
        try {
            const message = {
                notification: {
                    title: 'Daily Trivia Challenge!',
                    body: 'Your daily quiz is ready! Test your knowledge and earn points.'
                },
                data: {
                    type: 'daily_reminder',
                    click_action: 'FLUTTER_NOTIFICATION_CLICK'
                },
                token: fcmToken
            };

            // Send message using admin.messaging()
            const response = await admin.messaging().send(message);
            console.log('Daily reminder sent successfully:', response);
            return { success: true, messageId: response };
            
        } catch (error) {
            console.error('Error sending daily reminder:', error);
            return { success: false, error: error.message };
        }
    }

    /**
     * Send daily reminders to all users with FCM tokens
     */
    async sendDailyRemindersToAllUsers() {
        try {
            // Get all users with FCM tokens
            const usersRef = db.collection('users');
            const snapshot = await usersRef.where('fcmToken', '!=', null).get();
            
            if (snapshot.empty) {
                console.log('No users with FCM tokens found');
                return { success: true, sent: 0, message: 'No users with FCM tokens' };
            }

            const promises = [];
            snapshot.forEach(doc => {
                const user = doc.data();
                if (user.fcmToken && user.fcmToken !== '') {
                    promises.push(this.sendDailyReminder(user.fcmToken));
                }
            });

            const results = await Promise.all(promises);
            const successful = results.filter(result => result.success).length;
            
            console.log(`Sent ${successful}/${results.length} daily reminders`);
            return { 
                success: true, 
                sent: successful, 
                total: results.length 
            };
            
        } catch (error) {
            console.error('Error sending bulk reminders:', error);
            return { success: false, error: error.message };
        }
    }

    /**
     * Send notification to specific user by userId
     */
    async sendToUser(userId, title, message, data = {}) {
        try {
            // Get user from database
            const userRef = db.collection('users').doc(userId);
            const userDoc = await userRef.get();
            
            if (!userDoc.exists) {
                return { success: false, error: 'User not found' };
            }

            const user = userDoc.data();
            if (!user.fcmToken || user.fcmToken === '') {
                return { success: false, error: 'User has no FCM token' };
            }

            const messagePayload = {
                notification: {
                    title: title,
                    body: message
                },
                data: {
                    type: data.type || 'general',
                    ...data
                },
                token: user.fcmToken
            };

            const response = await admin.messaging().send(messagePayload);
            console.log(`Notification sent to user ${userId}:`, response);
            return { success: true, messageId: response };

        } catch (error) {
            console.error('Error sending to user:', error);
            return { success: false, error: error.message };
        }
    }
}

export default NotificationService;