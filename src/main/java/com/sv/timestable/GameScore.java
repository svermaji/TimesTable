package com.sv.timestable;

import com.sv.core.Utils;

public final class GameScore {

    private final String score, date, accuracy, level, type;

    public GameScore(int score, String date, int accuracy, int level, String type) {
        this(score + "", date, accuracy + "", level + "", type);
    }

    public GameScore(String score, String date, String accuracy, String level, String type) {
        this.score = score;
        this.date = date;
        this.accuracy = accuracy;
        this.level = level;
        this.type = type;
    }

    public String getScore() {
        return score;
    }

    // used for sorting
    public int getScoreAsInt() {
        return Utils.convertToInt(score);
    }

    public String getDate() {
        return date;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public String getLevel() {
        return level;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "GameScore{" +
                "score='" + score + '\'' +
                ", date='" + date + '\'' +
                ", accuracy='" + accuracy + '\'' +
                ", level='" + level + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public String shortString() {
        return "score='" + score + '\'' +
                        ", date='" + date + '\'' +
                        ", accuracy='" + accuracy + '\'' +
                        ", level='" + level + '\'' +
                        ", type='" + type + '\'';
    }
}
