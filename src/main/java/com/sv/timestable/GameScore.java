package com.sv.timestable;

import com.sv.core.Utils;

import java.util.List;

public final class GameScore {

    private final String score, date, accuracy;
    private final List<QuesAns> questions;

    public GameScore(int score, String date, int accuracy, List<QuesAns> questions) {
        this.score = score + "";
        this.date = date;
        this.accuracy = accuracy + "";
        this.questions = questions;
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

    public List<QuesAns> getQuestions() {
        return questions;
    }

    @Override
    public String toString() {
        return "GameScore{" +
                "score='" + score + '\'' +
                ", date='" + date + '\'' +
                ", accuracy='" + accuracy + '\'' +
                '}';
    }
}
