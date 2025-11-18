import { db } from '../config/firebase-admin.js';

class User {
    constructor(userId, email, displayName = '', totalScore = 0, totalQuizzes = 0, 
                correctAnswers = 0, totalQuestions = 0, averageScore = 0, 
                bestCategory = '', createdAt = new Date(), updatedAt = new Date(), fcmToken = '') {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.totalScore = totalScore;
        this.totalQuizzes = totalQuizzes;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.averageScore = averageScore;
        this.bestCategory = bestCategory;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.fcmToken = fcmToken; 
    }

    // Static method to create user from Firestore data
    static fromFirestore(doc) {
        const data = doc.data();
        return new User(
            doc.id,
            data.email,
            data.displayName,
            data.totalScore,
            data.totalQuizzes,
            data.correctAnswers,
            data.totalQuestions,
            data.averageScore,
            data.bestCategory,
            data.createdAt?.toDate(),
            data.updatedAt?.toDate(), 
             data.fcmToken || '' 
        );
    }

    // Convert to plain object for Firestore
    toFirestore() {
        return {
            userId: this.userId,
            email: this.email,
            displayName: this.displayName,
            totalScore: this.totalScore,
            totalQuizzes: this.totalQuizzes,
            correctAnswers: this.correctAnswers,
            totalQuestions: this.totalQuestions,
            averageScore: this.averageScore,
            bestCategory: this.bestCategory,
            createdAt: this.createdAt,
            updatedAt: this.updatedAt,
            fcmToken: this.fcmToken
        };
    }

    // Validation method
    validate() {
        const errors = [];
        
        if (!this.email || this.email.trim() === '') {
            errors.push('Email is required');
        }
        
        if (this.totalScore < 0) {
            errors.push('Total score cannot be negative');
        }
        
        if (this.totalQuizzes < 0) {
            errors.push('Total quizzes cannot be negative');
        }
        
        if (this.correctAnswers < 0) {
            errors.push('Correct answers cannot be negative');
        }
        
        if (this.totalQuestions < 0) {
            errors.push('Total questions cannot be negative');
        }
        
        if (this.averageScore < 0 || this.averageScore > 100) {
            errors.push('Average score must be between 0 and 100');
        }
        
        if (this.correctAnswers > this.totalQuestions) {
            errors.push('Correct answers cannot exceed total questions');
        }
        
        return errors;
    }

    // Method to update stats after a quiz
    updateStats(score, totalQuestions, correctAnswers, category) {
        this.totalScore += score;
        this.totalQuizzes += 1;
        this.correctAnswers += correctAnswers;
        this.totalQuestions += totalQuestions;
        this.averageScore = (this.correctAnswers / this.totalQuestions) * 100;
        this.updatedAt = new Date();

        // Simple best category logic - you can enhance this
        const categoryScoreRatio = score / totalQuestions;
        if (categoryScoreRatio > 0.8) { // If score is 80% or higher in this category
            this.bestCategory = category;
        }
    }
}

export default User;