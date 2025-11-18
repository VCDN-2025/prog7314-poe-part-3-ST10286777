import express from "express";
const router = express.Router();
import { QuizResultController } from '../controllers/quizResultController.js';
import authMiddleware from '../middleware/auth.js';

// Apply auth middleware to all routes
router.use(authMiddleware);
const quizResultController = new QuizResultController();

// Quiz result routes
router.post('/', quizResultController.saveQuizResult);
router.get('/history', quizResultController.getQuizHistory);
router.get('/category/:category', quizResultController.getQuizResultsByCategory);

export default router;
