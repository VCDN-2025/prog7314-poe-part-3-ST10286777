import { admin } from '../config/firebase-admin.js';

const verifyToken = async (req, res, next) => {
  const token = req.headers.authorization?.split("Bearer ")[1];
  
  if (!token) {
    return res.status(401).json({ 
      success: false,
      error: "No token provided" 
    });
  }

  try {
    const decodedToken = await admin.auth().verifyIdToken(token);
    req.user = decodedToken;
    next();
  } catch (error) {
    res.status(401).json({ 
      success: false,
      error: "Invalid token" 
    });
  }
};

export default verifyToken;