class QuizResult {
    constructor(quizId, userId, category, difficulty, score, totalQuestions, 
                correctAnswers, timeSpent, date = new Date(), deviceId = '') {
        this.quizId = quizId;
        this.userId = userId;
        this.category = category;
        this.difficulty = difficulty;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.timeSpent = timeSpent; // in seconds
        this.date = date;
        this.deviceId = deviceId;
    }

    // Static method to create quiz result from Firestore data
    static fromFirestore(doc) {
        const data = doc.data();
        return new QuizResult(
            doc.id,
            data.userId,
            data.category,
            data.difficulty,
            data.score,
            data.totalQuestions,
            data.correctAnswers,
            data.timeSpent,
            data.date?.toDate(),
            data.deviceId
        );
    }

    // Convert to plain object for Firestore
    toFirestore() {
        return {
            userId: this.userId,
            category: this.category,
            difficulty: this.difficulty,
            score: this.score,
            totalQuestions: this.totalQuestions,
            correctAnswers: this.correctAnswers,
            timeSpent: this.timeSpent,
            date: this.date,
            deviceId: this.deviceId
        };
    }

    // Validation method
    validate() {
        const errors = [];
        
        if (!this.userId || this.userId.trim() === '') {
            errors.push('User ID is required');
        }
        
        if (!this.category || this.category.trim() === '') {
            errors.push('Category is required');
        }
        
        if (!this.difficulty || !['Easy', 'Medium', 'Hard'].includes(this.difficulty)) {
            errors.push('Difficulty must be one of: easy, medium, hard');
        }
        
        if (this.score < 0 || this.score > this.totalQuestions) {
            errors.push('Score must be between 0 and total questions');
        }
        
        if (this.totalQuestions <= 0) {
            errors.push('Total questions must be greater than 0');
        }
        
        if (this.correctAnswers < 0 || this.correctAnswers > this.totalQuestions) {
            errors.push('Correct answers must be between 0 and total questions');
        }
        
        if (this.timeSpent < 0) {
            errors.push('Time spent cannot be negative');
        }
        
        return errors;
    }
}

export default QuizResult;