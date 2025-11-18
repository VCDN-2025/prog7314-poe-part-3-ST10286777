import express from "express";
const router = express.Router();
import { UserController } from '../controllers/userController.js';
import authMiddleware from '../middleware/auth.js';

const userController = new UserController();

// Apply auth middleware to all routes
router.use(authMiddleware);

// User routes
router.get('/profile', userController.getOrCreateUser.bind(userController));
router.get('/stats', userController.getUserStats.bind(userController));
router.put('/profile/display-name', userController.updateDisplayName.bind(userController));
router.put('/fcm-token', userController.updateFCMToken.bind(userController));
router.delete('/fcm-token', userController.removeFCMToken.bind(userController));

export default router;