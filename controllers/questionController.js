import { db } from '../config/firebase-admin.js';
import Question from '../models/Question';

class QuestionController {
  // Create a new question
  async createQuestion(req, res) {
    try {
      const { category, questionText, choices, answer, difficulty } = req.body;
      
      // Generate a new question ID
      const questionRef = db.collection('Questions').doc();
      const questionId = questionRef.id;
      
      // Create question instance
      const question = new Question(
        questionId,
        category,
        questionText,
        choices,
        answer,
        difficulty
      );
      
      // Validate question
      const validationErrors = question.validate();
      if (validationErrors.length > 0) {
        return res.status(400).json({ 
          success: false,
          error: 'Validation failed', 
          message: validationErrors.join(', ')
        });
      }
      
      // Save to Firestore
      await questionRef.set(question.toFirestore());
      
      res.status(201).json({
        success: true,
        message: 'Question created successfully',
        data: question,
        count: 1
      });
      
    } catch (error) {
      console.error('Error creating question:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to create question'
      });
    }
  }

  // Get all questions 
  async getQuestions(req, res) {
    try {
      const questionsSnapshot = await db.collection('questions').get();
      
      if (questionsSnapshot.empty) {
        return res.json({
          success: true,
          data: [],
          message: 'No questions found',
          count: 0
        });
      }
      
      const questions = questionsSnapshot.docs.map(doc => 
        Question.fromFirestore(doc)
      );
      
      res.json({
        success: true,
        data: questions,
        count: questions.length,
        message: `Found ${questions.length} questions`
      });
      
    } catch (error) {
      console.error('Error fetching questions:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch questions'
      });
    }
  }

  // Get questions by category
  async getQuestionsByCategory(req, res) {
    try {
      const { category } = req.params;
      
      const questionsSnapshot = await db.collection('questions')
        .where('category', '==', category)
        .get();
      
      if (questionsSnapshot.empty) {
        return res.json({
          success: true,
          data: [],
          message: `No questions found for category: ${category}`,
          count: 0
        });
      }
      
      const questions = questionsSnapshot.docs.map(doc => 
        Question.fromFirestore(doc)
      );
      
      res.json({
        success: true,
        data: questions,
        count: questions.length,
        message: `Found ${questions.length} questions in ${category} category`
      });
      
    } catch (error) {
      console.error('Error fetching questions by category:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch questions by category'
      });
    }
  }

  // Get all unique categories
  async getCategories(req, res) {
    try {
      const questionsSnapshot = await db.collection('questions')
        .select('category')
        .get();
      
      if (questionsSnapshot.empty) {
        return res.json({
          success: true,
          data: [],
          message: 'No categories found',
          count: 0
        });
      }

      const categoriesSet = new Set();
      
      questionsSnapshot.forEach(doc => {
        const category = doc.data().category;
        if (category && category.trim() !== '') {
          categoriesSet.add(category.trim());
        }
      });

      const categories = Array.from(categoriesSet).sort();

      res.json({
        success: true,
        data: categories,
        count: categories.length,
        message: `Found ${categories.length} categories`
      });

    } catch (error) {
      console.error('Error fetching categories:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch categories'
      });
    }
  }
  
  // Get question by ID
  async getQuestionById(req, res) {
    try {
      const { questionId } = req.params;
      
      const questionDoc = await db.collection('questions')
        .where('questionId', '==', questionId)
        .limit(1)
        .get();
      
      if (questionDoc.empty) {
        return res.status(404).json({
          success: false,
          error: 'Question not found',
          message: `Question with ID ${questionId} not found`
        });
      }
      
      const question = Question.fromFirestore(questionDoc.docs[0]);
      
      res.json({
        success: true,
        data: question,
        message: 'Question retrieved successfully',
        count: 1
      });
      
    } catch (error) {
      console.error('Error fetching question:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch question'
      });
    }
  }

  // Update question
  async updateQuestion(req, res) {
    try {
      const { questionId } = req.params;
      const updateData = req.body;
      
      // Find the question document
      const questionDoc = await db.collection('questions')
        .where('questionId', '==', questionId)
        .limit(1)
        .get();
      
      if (questionDoc.empty) {
        return res.status(404).json({
          success: false,
          error: 'Question not found',
          message: `Question with ID ${questionId} not found`
        });
      }
      
      const docRef = questionDoc.docs[0].ref;
      await docRef.update(updateData);
      
      // Get updated question
      const updatedDoc = await docRef.get();
      const updatedQuestion = Question.fromFirestore(updatedDoc);
      
      res.json({
        success: true,
        message: 'Question updated successfully',
        data: updatedQuestion,
        count: 1
      });
      
    } catch (error) {
      console.error('Error updating question:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to update question'
      });
    }
  }

  // Delete question
  async deleteQuestion(req, res) {
    try {
      const { questionId } = req.params;
      
      const questionDoc = await db.collection('questions')
        .where('questionId', '==', questionId)
        .limit(1)
        .get();
      
      if (questionDoc.empty) {
        return res.status(404).json({
          success: false,
          error: 'Question not found',
          message: `Question with ID ${questionId} not found`
        });
      }
      
      await questionDoc.docs[0].ref.delete();
      
      res.json({
        success: true,
        message: 'Question deleted successfully',
        data: null,
        count: 0
      });
      
    } catch (error) {
      console.error('Error deleting question:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to delete question'
      });
    }
  }
 // Get a single random question
  async getRandomQuestion(req, res) {
    try {
      const questionsSnapshot = await db.collection('questions').get();
      
      if (questionsSnapshot.empty) {
        return res.json({
          success: true,
          data: null,
          message: 'No questions found in database',
          count: 0
        });
      }
      
      const questions = questionsSnapshot.docs.map(doc => 
        Question.fromFirestore(doc)
      );
      
      // Get a random question
      const randomIndex = Math.floor(Math.random() * questions.length);
      const randomQuestion = questions[randomIndex];
      
      res.json({
        success: true,
        data: randomQuestion,
        message: 'Random question retrieved successfully',
        count: 1
      });
      
    } catch (error) {
      console.error('Error fetching random question:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch random question'
      });
    }
  }

  // Get multiple random questions
  async getRandomQuestions(req, res) {
    try {
      const count = parseInt(req.params.count) || 5;
      
      // Validate count
      if (count < 1 || count > 50) {
        return res.status(400).json({
          success: false,
          error: 'Invalid count',
          message: 'Count must be between 1 and 50'
        });
      }
      
      const questionsSnapshot = await db.collection('questions').get();
      
      if (questionsSnapshot.empty) {
        return res.json({
          success: true,
          data: [],
          message: 'No questions found in database',
          count: 0
        });
      }
      
      const questions = questionsSnapshot.docs.map(doc => 
        Question.fromFirestore(doc)
      );

      // Shuffle array using Fisher-Yates algorithm
      const shuffledQuestions = [...questions];
      for (let i = shuffledQuestions.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffledQuestions[i], shuffledQuestions[j]] = [shuffledQuestions[j], shuffledQuestions[i]];
      }
      
      // Get the first N questions
      const randomQuestions = shuffledQuestions.slice(0, count);
      
      res.json({
        success: true,
        data: randomQuestions,
        count: randomQuestions.length,
        message: `Retrieved ${randomQuestions.length} random questions`
      });
      
    } catch (error) {
      console.error('Error fetching random questions:', error);
      res.status(500).json({ 
        success: false,
        error: 'Internal server error',
        message: 'Failed to fetch random questions'
      });
    }
  }

}

export default new QuestionController();