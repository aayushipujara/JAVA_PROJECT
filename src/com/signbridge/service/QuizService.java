package com.signbridge.service;

import com.signbridge.db.dao.AchievementDAO;
import com.signbridge.db.dao.QuizDAO;
import com.signbridge.db.dao.SignDAO;
import com.signbridge.model.QuizQuestion;
import com.signbridge.model.SignItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizService {
    private final SignDAO signDAO;
    private final QuizDAO quizDAO;
    private final AchievementDAO achievementDAO;

    public QuizService() {
        this.signDAO = new SignDAO();
        this.quizDAO = new QuizDAO();
        this.achievementDAO = new AchievementDAO();
    }

    public List<QuizQuestion> generateQuiz(String category, int questionCount) {
        List<SignItem> allPool = signDAO.getAllSigns();
        List<SignItem> targetPool;

        if (category == null || category.equalsIgnoreCase("All") || category.equalsIgnoreCase("Mixed")) {
            targetPool = allPool;
        } else {
            targetPool = signDAO.getSignsByCategory(category);
        }

        if (targetPool.isEmpty()) {
            targetPool = allPool;
        }

        List<SignItem> shuffled = new ArrayList<>(targetPool);
        Collections.shuffle(shuffled);

        int count = Math.min(questionCount, shuffled.size());
        List<QuizQuestion> questions = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            SignItem correctSign = shuffled.get(i);
            boolean isSignToWord = (i % 2 == 0); // Alternate modes

            // Generate 3 random wrong options
            List<String> options = new ArrayList<>();
            options.add(correctSign.getSymbol());

            List<SignItem> distractorPool = new ArrayList<>(allPool);
            distractorPool.remove(correctSign);
            Collections.shuffle(distractorPool);

            for (SignItem distractor : distractorPool) {
                if (!options.contains(distractor.getSymbol())) {
                    options.add(distractor.getSymbol());
                    if (options.size() == 4) break;
                }
            }

            // Shuffle options
            Collections.shuffle(options);
            int correctIndex = options.indexOf(correctSign.getSymbol());

            String prompt = isSignToWord
                    ? "Identify the correct symbol or meaning for this hand gesture:"
                    : "Which gesture corresponds to the symbol '" + correctSign.getSymbol() + "'?";

            String explanation = "Symbol '" + correctSign.getSymbol() + "': " + correctSign.getDescription();

            questions.add(new QuizQuestion(prompt, correctSign, options, correctIndex, isSignToWord, explanation));
        }

        return questions;
    }

    public int finishQuiz(int userId, String category, int score, int totalQuestions, int timeSeconds) {
        // 1. Save quiz attempt to DB
        quizDAO.saveQuizResult(userId, category, score, totalQuestions, timeSeconds);

        // 2. Compute XP: 25 XP per correct answer + time bonus (up to 50 XP)
        int baseXP = score * 25;
        int timeBonus = Math.max(0, (totalQuestions * 15 - timeSeconds) / 2);
        int totalXP = baseXP + timeBonus;

        // 3. Award XP to user
        AuthService.getInstance().awardXp(totalXP);

        // 4. Check & Unlock Achievements
        achievementDAO.unlockBadge(userId, "FIRST_QUIZ");
        if (score == totalQuestions && totalQuestions >= 5) {
            achievementDAO.unlockBadge(userId, "QUIZ_ACE");
        }
        if (AuthService.getInstance().getCurrentUser().getCurrentLevel() >= 5) {
            achievementDAO.unlockBadge(userId, "LEVEL_5");
        }

        return totalXP;
    }
}
