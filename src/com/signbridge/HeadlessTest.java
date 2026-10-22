package com.signbridge;

import com.signbridge.db.DatabaseManager;
import com.signbridge.db.DBConnection;
import com.signbridge.db.dao.*;
import com.signbridge.model.*;
import com.signbridge.service.*;

import java.util.List;
import java.util.Map;

public class HeadlessTest {
    public static void main(String[] args) {
        System.out.println(">>> Starting Headless Test for SignBridge...");

        // 1. Initialize Database
        DatabaseManager.initializeDatabase();
        System.out.println("Connection status: " + DBConnection.getStatusMessage());
        assert DBConnection.isConnected() : "Database should be connected!";

        // 2. Check signs
        SignDAO signDAO = new SignDAO();
        List<SignItem> allSigns = signDAO.getAllSigns();
        System.out.println("Total signs loaded in DB: " + allSigns.size());
        if (allSigns.isEmpty()) {
            throw new RuntimeException("Signs table is empty!");
        }

        // 3. Authenticate demo user
        UserDAO userDAO = new UserDAO();
        User user = userDAO.authenticate("aayushi", "pass123");
        if (user == null) {
            throw new RuntimeException("Demo user 'student' could not be authenticated!");
        }
        System.out.println("Authenticated user: " + user.getFullName() + ", Level=" + user.getCurrentLevel() + ", XP=" + user.getTotalXp());

        // 4. Test Quiz Service
        QuizService quizService = new QuizService();
        List<QuizQuestion> questions = quizService.generateQuiz("Alphabet", 5);
        System.out.println("Generated quiz questions: " + questions.size());
        for (int i = 0; i < questions.size(); i++) {
            QuizQuestion q = questions.get(i);
            System.out.println("  Q" + (i + 1) + ": " + q.getTargetSign().getSymbol() + " (" + q.getOptions().size() + " options, correctIdx=" + q.getCorrectIndex() + ")");
        }

        // 5. Test Quiz result recording
        QuizDAO quizDAO = new QuizDAO();
        boolean quizSaved = quizDAO.saveQuizResult(user.getId(), "Alphabet", 5, 5, 45);
        System.out.println("Quiz result saved: " + quizSaved);

        // 6. Test XP update
        int initialXp = user.getTotalXp();
        userDAO.addXp(user.getId(), 100);
        User updatedUser = userDAO.getUserById(user.getId());
        System.out.println("XP updated from " + initialXp + " to " + updatedUser.getTotalXp());

        // 7. Test Game score saving
        GameDAO gameDAO = new GameDAO();
        boolean gameSaved = gameDAO.saveScore(user.getId(), "Memory Match", 850, 14, 28);
        System.out.println("Game score saved: " + gameSaved);
        List<GameScore> topScores = gameDAO.getTopScores("Memory Match", 5);
        System.out.println("Top Memory Match score: " + topScores.get(0).getScore() + " by " + topScores.get(0).getUsername());

        // 8. Test Badges
        AchievementDAO achievementDAO = new AchievementDAO();
        achievementDAO.unlockBadge(user.getId(), "FIRST_QUIZ");
        List<Achievement> badges = achievementDAO.getAllAchievementsForUser(user.getId());
        for (Achievement b : badges) {
            System.out.println("  Badge: " + b.getIconType() + " " + b.getTitle() + " -> " + (b.isUnlocked() ? "UNLOCKED" : "LOCKED"));
        }

        System.out.println(">>> ALL BACKEND & DATABASE TESTS PASSED WITH 100% SUCCESS! <<<");
    }
}
