package com.sv.timestable;

import com.sv.core.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameDetail {

    private final int tableFrom, tableTo, totalQuestions;
    private final String gamePlayedOnAsLong;
    private String timeTakenForGame;

    private List<QuesAns> quesAns;

    public GameDetail(int tableFrom, int tableTo, int totalQuestions, String gamePlayedOnAsLong) {
        this.tableFrom = tableFrom;
        this.tableTo = tableTo;
        this.totalQuestions = totalQuestions;
        this.gamePlayedOnAsLong = gamePlayedOnAsLong;
        quesAns = new ArrayList<>();
    }

    public int getTableFrom() {
        return tableFrom;
    }

    public int getTableTo() {
        return tableTo;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public String getGamePlayedOn() {
        return Utils.getFormattedDate(Long.parseLong(gamePlayedOnAsLong));
    }

    public Long getGamePlayedOnAsLong() {
        return Long.parseLong(gamePlayedOnAsLong);
    }

    public List<QuesAns> getQuesAns() {
        return quesAns;
    }

    public QuesAns getQues(int idx) {
        return quesAns.get(idx);
    }

    public void addQues(QuesAns q) {
        quesAns.add(q);
    }

    public String getTimeTakenForGame() {
        return timeTakenForGame;
    }

    public void setTimeTakenForGame(String timeTakenForGame) {
        this.timeTakenForGame = timeTakenForGame;
    }

    private int getCorrectAnsCnt() {
        AtomicInteger c = new AtomicInteger();
        quesAns.forEach(q -> {
            if (q.isCorrectAns()) {
                c.getAndIncrement();
            }
        });
        return c.get();
    }

    @Override
    public String toString() {
        return "GameDetail{" +
                "tableFrom=" + tableFrom +
                ", tableTo=" + tableTo +
                ", totalQuestions=" + totalQuestions +
                ", gamePlayedOnAsLong='" + gamePlayedOnAsLong + '\'' +
                ", timeTakenForGame='" + timeTakenForGame + '\'' +
                '}';
    }

    public String detail() {
        return "GameDetail{" +
                "tableFrom=" + tableFrom +
                ", tableTo=" + tableTo +
                ", totalQuestions=" + totalQuestions +
                ", gamePlayedOnAsLong='" + gamePlayedOnAsLong + '\'' +
                ", timeTakenForGame='" + timeTakenForGame + '\'' +
                ", quesAns=" + quesAns +
                '}';
    }

    public String forTable() {
        return "Score: " + getCorrectAnsCnt() +
                "/" + totalQuestions +
                ", Tables: " + tableFrom +
                "-" + tableTo +
                " in " + timeTakenForGame;
    }

    public String tooltip() {
        return "On " + getGamePlayedOn() +
                ", double click for details";
    }
}
