package com.signbridge.service;

import com.signbridge.db.dao.AchievementDAO;
import com.signbridge.db.dao.GameDAO;
import com.signbridge.db.dao.SignDAO;
import com.signbridge.model.GameScore;
import com.signbridge.model.SignItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameService {
    private final GameDAO gameDAO;
    private final SignDAO signDAO;
    private final AchievementDAO achievementDAO;

    public GameService() {
        this.gameDAO = new GameDAO();
        this.signDAO = new SignDAO();
        this.achievementDAO = new AchievementDAO();
    }

    // Memory Match Game Card Model
    public static class MemoryCard {
        public final int id;
        public final SignItem sign;
        public final boolean isGesture; // true: shows gesture visual; false: shows text symbol
        public boolean isMatched = false;
        public boolean isRevealed = false;

        public MemoryCard(int id, SignItem sign, boolean isGesture) {
            this.id = id;
            this.sign = sign;
            this.isGesture = isGesture;
        }
    }

    public List<MemoryCard> setupMemoryGame(int pairCount) {
        List<SignItem> pool = signDAO.getAllSigns();
        Collections.shuffle(pool);

        int count = Math.min(pairCount, pool.size());
        List<MemoryCard> cards = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            SignItem item = pool.get(i);
            cards.add(new MemoryCard(i, item, true));  // Card 1: Gesture
            cards.add(new MemoryCard(i, item, false)); // Card 2: Text/Meaning
        }

        Collections.shuffle(cards);
        return cards;
    }

    public int finishMemoryGame(int userId, int moves, int timeSeconds) {
        // High score calculation: base 1000 minus penalties for moves and time
        int score = Math.max(100, 1000 - (moves * 20) - (timeSeconds * 5));
        gameDAO.saveScore(userId, "Memory Match", score, moves, timeSeconds);

        int xpGained = score / 5;
        AuthService.getInstance().awardXp(xpGained);

        if (moves <= 16) {
            achievementDAO.unlockBadge(userId, "MEMORY_PRO");
        }

        return score;
    }

    public int finishSpeedGame(int userId, int score, int correctAnswers, int timeSeconds) {
        gameDAO.saveScore(userId, "Speed Blitz", score, correctAnswers, timeSeconds);

        int xpGained = score / 3;
        AuthService.getInstance().awardXp(xpGained);

        if (score >= 100) {
            achievementDAO.unlockBadge(userId, "SPEED_RUNNER");
        }

        return score;
    }

    public List<GameScore> getLeaderboard(String gameName, int limit) {
        return gameDAO.getTopScores(gameName, limit);
    }
}
