import { db } from '../config/firebase-admin.js';
import QuizResult from '../models/QuizResult.js';
import User from '../models/User.js';

export class QuizResultController {
    // Save quiz results and update user stats
    async saveQuizResult(req, res) {
        try {
            const {
                category,
                difficulty,
                score,
                totalQuestions,
                correctAnswers,
                timeSpent,
                deviceId
            } = req.body;

            const userId = req.user.uid;
            
            // 1. Create and validate quiz result
            const quizResultRef = db.collection('users').doc(userId)
                .collection('quizResults').doc();
                
            const quizResult = new QuizResult(
                quizResultRef.id,
                userId,
                category,
                difficulty,
                score,
                totalQuestions,
                correctAnswers,
                timeSpent,
                new Date(),
                deviceId
            );

            // Validate quiz result
            const validationErrors = quizResult.validate();
            if (validationErrors.length > 0) {
                return res.status(400).json({ 
                    success: false,
                    error: 'Validation failed', 
                    message: validationErrors.join(', ')
                });
            }

            // 2. Get or create user
            const userRef = db.collection('users').doc(userId);
            const userDoc = await userRef.get();
            
            let user;
            if (!userDoc.exists) {
                // Create user if doesn't exist
                user = new User(
                    userId,
                    req.user.email,
                    req.user.name || '',
                    0, 0, 0, 0, 0, '', new Date(), new Date()
                );
                await userRef.set(user.toFirestore());
            } else {
                user = User.fromFirestore(userDoc);
            }

            // 3. Update user statistics
            user.updateStats(score, totalQuestions, correctAnswers, category);

            // 4. Save both quiz result and updated user in a batch (atomic operation)
            const batch = db.batch();
            batch.set(quizResultRef, quizResult.toFirestore());
            batch.set(userRef, user.toFirestore(), { merge: true });
            await batch.commit();

            res.json({
                success: true,
                message: "Quiz results saved successfully",
                data: { 
                    quizId: quizResultRef.id,
                    userStats: {
                        totalScore: user.totalScore,
                        totalQuizzes: user.totalQuizzes,
                        correctAnswers: user.correctAnswers,
                        totalQuestions: user.totalQuestions,
                        averageScore: user.averageScore,
                        bestCategory: user.bestCategory
                    }
                }
            });
            
        } catch (error) {
            console.error('Error saving quiz result:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to save quiz results'
            });
        }
    }

    // Get user's quiz history
    async getQuizHistory(req, res) {
        try {
            const userId = req.user.uid;
            const { limit = 10 } = req.query;
            
            const quizResultsSnapshot = await db.collection('users').doc(userId)
                .collection('quizResults')
                .orderBy('date', 'desc')
                .limit(parseInt(limit))
                .get();
            
            if (quizResultsSnapshot.empty) {
                return res.json({
                    success: true,
                    data: [],
                    message: 'No quiz results found',
                    count: 0
                });
            }
            
            const quizResults = quizResultsSnapshot.docs.map(doc => 
                QuizResult.fromFirestore(doc)
            );
            
            res.json({
                success: true,
                data: quizResults,
                count: quizResults.length,
                message: `Found ${quizResults.length} quiz results`
            });
            
        } catch (error) {
            console.error('Error fetching quiz history:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to fetch quiz history'
            });
        }
    }

    // Get quiz results by category
    async getQuizResultsByCategory(req, res) {
        try {
            const userId = req.user.uid;
            const { category } = req.params;
            
            const quizResultsSnapshot = await db.collection('users').doc(userId)
                .collection('quizResults')
                .where('category', '==', category)
                .orderBy('date', 'desc')
                .get();
            
            if (quizResultsSnapshot.empty) {
                return res.json({
                    success: true,
                    data: [],
                    message: `No quiz results found for category: ${category}`,
                    count: 0
                });
            }
            
            const quizResults = quizResultsSnapshot.docs.map(doc => 
                QuizResult.fromFirestore(doc)
            );
            
            res.json({
                success: true,
                data: quizResults,
                count: quizResults.length,
                message: `Found ${quizResults.length} quiz results in ${category} category`
            });
            
        } catch (error) {
            console.error('Error fetching quiz results by category:', error);
            res.status(500).json({ 
                success: false,
                error: 'Internal server error',
                message: 'Failed to fetch quiz results by category'
            });
        }
    }
}

