import express from "express";
import cors from "cors";
import dotenv from "dotenv";

// Import routes and middleware
import verifyToken from "./middleware/auth.js";
import questionRoutes from "./routes/questions.js";
import userRoutes from "./routes/users.js";
import quizResultRoutes from "./routes/quizResults.js";
import seedRoutes from "./routes/seed.js";
import { startDailyReminderScheduler } from './dailyReminder.js';
import notificationRoutes from './routes/notificationRoutes.js';


dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// Protected Question Routes
app.use("/api/questions", verifyToken, questionRoutes);
app.use("/api/users", verifyToken, userRoutes);
app.use("/api/quiz-results", verifyToken, quizResultRoutes);
app.use('/api/notifications', notificationRoutes);

app.use('/api/seed', seedRoutes);

// Health check to determine if the API is running
app.get("/", (req, res) => {
  res.json({ 
    success: true,
    message: "Trivia API is running!",
    timestamp: new Date().toISOString()
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    success: false,
    error: 'Route not found',
    path: req.path // Show which path was requested
  });
});


// Start the server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server running on port ${PORT}`));
startDailyReminderScheduler();
