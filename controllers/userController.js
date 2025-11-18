import { db } from '../config/firebase-admin.js';
import User from '../models/User.js';

export class UserController {
    // Get or create user profile
    async getOrCreateUser(req, res) {
        try {
            const userId = req.user.uid;
            const userEmail = req.user.email;
            
            const userRef = db.collection('users').doc(userId);
            const userDoc = await userRef.get();
            
            let user;
            
            if (!userDoc.exists) {
                // Create new user
                user = new User(
                    userId,
                    userEmail,
                    req.user.name || '',
                    0, 0, 0, 0, 0, '', new Date(), new Date()
                );
                
                // Validate user
                const validationErrors = user.validate();
                if (validationErrors.length > 0) {
                    return res.status(400).json({ 
                        success: false,
                        error: 'Validation failed', 
                        message: validationErrors.join(', ')
                    });
                }
                
                // Save to Firestore
                await userRef.set(user.toFirestore());
                
                res.json({
                    success: true,
                    message: 'User profile created successfully',
                    data: { user: user },
                    count: 1
                });
            } else {
                // Return existing user
                user = User.fromFirestore(userDoc);
                
                res.json({
                    success: true,
                    message: 'User profile retrieved successfully',
                    data: { user: user },
                    count: 1
                });
            }
            
        } catch (error) {
            console.error('Error in getOrCreateUser:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to get or create user profile'
            });
        }
    }

    // Get user statistics
    async getUserStats(req, res) {
        try {
            const userId = req.user.uid;
            
            const userRef = db.collection('users').doc(userId);
            const userDoc = await userRef.get();
            
            if (!userDoc.exists) {
                return res.status(404).json({
                    success: false,
                    error: 'User not found',
                    message: 'User profile does not exist'
                });
            }
            
            const user = User.fromFirestore(userDoc);
            
            res.json({
                success: true,
                data: {
                    totalScore: user.totalScore,
                    totalQuizzes: user.totalQuizzes,
                    correctAnswers: user.correctAnswers,
                    totalQuestions: user.totalQuestions,
                    averageScore: user.averageScore,
                    bestCategory: user.bestCategory
                },
                message: 'User statistics retrieved successfully'
            });
            
        } catch (error) {
            console.error('Error fetching user stats:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to fetch user statistics'
            });
        }
    }

    // Update user display name
    async updateDisplayName(req, res) {
        try {
            const { displayName } = req.body;
            const userId = req.user.uid;
            
            if (!displayName || displayName.trim() === '') {
                return res.status(400).json({
                    success: false,
                    error: 'Validation failed',
                    message: 'Display name is required'
                });
            }
            
            const userRef = db.collection('users').doc(userId);
            await userRef.update({
                displayName: displayName.trim(),
                updatedAt: new Date()
            });
            
            // Get updated user
            const updatedDoc = await userRef.get();
            const updatedUser = User.fromFirestore(updatedDoc);
            
            res.json({
                success: true,
                message: 'Display name updated successfully',
                data: { user: updatedUser },
                count: 1
            });
            
        } catch (error) {
            console.error('Error updating display name:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to update display name'
            });
        }
    }
    
     async updateFCMToken(req, res) {
        try {
            const userId = req.user.uid;
            const { fcmToken } = req.body;

            if (!fcmToken) {
                return res.status(400).json({
                    success: false,
                    message: 'FCM token is required'
                });
            }

            const userRef = db.collection('users').doc(userId);
            await userRef.update({
                fcmToken: fcmToken,
                updatedAt: new Date()
            });

            res.json({
                success: true,
                message: 'FCM token updated successfully'
            });

        } catch (error) {
            console.error('Error updating FCM token:', error);
            res.status(500).json({
                success: false,
                error: 'Internal server error',
                message: 'Failed to update FCM token'
            });
        }
    }

      async removeFCMToken(req, res) {
        try {
            const userId = req.user.uid;

            const userRef = db.collection('users').doc(userId);
            await userRef.update({
                fcmToken: '',
                updatedAt: new Date()
            });

            res.json({
                success: true,
                message: 'FCM token removed successfully'
            });

        } catch (error) {
            console.error('Error removing FCM token:', error);
            res.status(500).json({
                success: false,
                error: 'Internal server error',
                message: 'Failed to remove FCM token'
            });
        }
    }
}

