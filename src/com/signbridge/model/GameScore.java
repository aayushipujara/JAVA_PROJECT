package com.signbridge.model;

import java.sql.Timestamp;

public class GameScore {
    private int id;
    private int userId;
    private String username;
    private String gameName;
    private int score;
    private int moves;
    private int timeTakenSeconds;
    private Timestamp playedAt;

    public GameScore() {}

    public GameScore(int id, int userId, String username, String gameName, int score, int moves, int timeTakenSeconds, Timestamp playedAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.gameName = gameName;
        this.score = score;
        this.moves = moves;
        this.timeTakenSeconds = timeTakenSeconds;
        this.playedAt = playedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMoves() { return moves; }
    public void setMoves(int moves) { this.moves = moves; }

    public int getTimeTakenSeconds() { return timeTakenSeconds; }
    public void setTimeTakenSeconds(int timeTakenSeconds) { this.timeTakenSeconds = timeTakenSeconds; }

    public Timestamp getPlayedAt() { return playedAt; }
    public void setPlayedAt(Timestamp playedAt) { this.playedAt = playedAt; }
}
