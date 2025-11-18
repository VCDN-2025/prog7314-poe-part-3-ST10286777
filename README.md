<div align="center">

  <h1> 
    <img width="500" height="500" alt="Trivora_Logo" src="https://github.com/user-attachments/assets/090c1a83-6bc8-48bc-9a23-57ea6e66587f" />
  </h1>

  <h2> Trivora - Interactive Trivia Game Application</h2>
</div>

---

## 🧭 Overview

**Trivora** is a modern trivia game application designed to challenge users’ knowledge across multiple categories while providing an interactive and enjoyable experience.  
The app aims to make learning fun through quizzes, leaderboards, and gamification — allowing users to test their knowledge and compete with others globally.

This README serves as a **comprehensive project report**, detailing the purpose, design considerations, and the use of **GitHub** and **GitHub Actions** in ensuring smooth collaboration, testing, and continuous integration.

---

## 🎬 YouTube Demonstration

▶️ [Watch the Demo Video](https://youtu.be/kKGOq5szSEg)

---

## 👥 Team Members

- Lindokuhle Moyana  
- Christinah Chitombi  
- Keira Wilmot  
- Nqobani Moyane  

---

## Purpose of the Application

The purpose of Trivora is to create a **knowledge-driven entertainment platform** that helps users learn while having fun.  
It combines **education, competition, and personalization** by allowing users to:

---

##  Application Features

### 1. User Authentication and Registration
- Account creation using email and password.  
- Persistent login sessions.  
- Google Single Sign-On.  
- Biometric authentication.  

### 2. User Profile
- Update username, profile picture, and email.  

### 3. Settings
- Sound preferences (on/off).  
- Notification toggle.  
- Language switch (English + Afrikaans).  
- Select difficulty level.
- Biometric Login Toggle

### 4. Trivia Gameplay
- Multiple categories (History, Sports, Movies, Science).    
- Image-based questions (planned).  
- Randomized questions and one-question “speed quiz”.  
- Hint system with coins.

### 5. Score Tracking
- Users are able to check their score statistics
- Users can view their most played category
- Achievement badges for high scores and streaks.  
- Social sharing and monthly challenges.

### 6. Offline Mode
- Local data storage for offline quizzes.  
- Sync progress when online.

Offline Play 
1. Download quizzes when online → Store in Room DB
2. Take quizzes offline → Store results locally  
3. Sync when back online → Upload results to backend
   
### 7. API and Database
- REST API for authentication, quiz fetching, and score submission.  
- Tables: Users, Questions, Categories, Scores. 
- HTTPS-enabled communication for secure data transfer.

---

## 🧰 GitHub & GitHub Actions Usage

### Version Control with GitHub
GitHub was utilized for:
- **Source code management** and team collaboration.  
- **Branching strategy** for development, testing, and production.  
- **Pull Requests** for code reviews before merging to `main`.

Each feature was developed in its own branch, ensuring clean integration and reducing merge conflicts. 


### Continuous Integration with GitHub Actions
GitHub Actions was configured to:
- Automatically **build the project** and **run tests** when changes are pushed.  
- Detect build issues early across environments.  
- Generate APK builds and upload them as **artifacts** for easy testing.


## Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java Development Kit (JDK) 11 or higher**
- **Android Studio** (latest stable version)
- **Firebase CLI** (for Firebase management)
- **Git** (for version control)

## Quick Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd skhaftin-android
   ```

2. **Open in Android Studio**:
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it
   - Wait for Gradle sync to complete

3. **Firebase Setup**:
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app with package name `com.skhaftin`
   - Download `google-services.json` and place it in `app/` directory
   - Enable Authentication and Firestore in Firebase Console

4. **Run the app**:
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use `./gradlew installDebug`

## Detailed Setup Guide

For comprehensive setup instructions including Firebase CLI installation, emulator setup, and troubleshooting, see [ANDROID_EMULATOR_SETUP.md](ANDROID_EMULATOR_SETUP.md).

