import express from "express";
const router = express.Router();
import questionController from '../controllers/questionController.js';
import authMiddleware from'../middleware/auth.js';

// Applied auth middleware to all routes
router.use(authMiddleware);

// Question routes
router.post('/', questionController.createQuestion);
router.get('/category/:category', questionController.getQuestionsByCategory);
router.get('/categories', questionController.getCategories); 
router.put('/:questionId', questionController.updateQuestion);
router.delete('/:questionId', questionController.deleteQuestion);
router.get('/random', questionController.getRandomQuestion); // Single random question
router.get('/random/:count', questionController.getRandomQuestions); // Multiple random questions
router.get('/:questionId', questionController.getQuestionById);


export default router;