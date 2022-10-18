package com.sv.timestable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GameScores {

    private final String username;
    private final List<GameScore> scores;
    private List<GameScore> topScores;

    public GameScores(String username, List<GameScore> scores) {
        this.username = username;
        this.scores = scores == null ? new ArrayList<>() : scores;
        updateTopScores();
    }

    public String getUsername() {
        return username;
    }

    private List<GameScore> updateTopScores() {
        topScores = new ArrayList<>(scores);
        topScores.sort(Comparator.comparing(GameScore::getScoreAsInt).reversed());
        return topScores;
    }

    public List<GameScore> getTopScores() {
        return topScores;
    }

    public Integer getTopScore() {
        return getTopScores().size() > 0 ? getTopScores().get(0).getScoreAsInt() : 0;
    }

    public List<GameScore> getRecentScores() {
        return scores;
    }

    public void addScore(GameScore gs) {
        scores.add(0, gs);
        updateTopScores();
    }

    @Override
    public String toString() {
        return "GameScores{" +
                "username='" + username + '\'' +
                ", scores=" + scores +
                '}';
    }
}
