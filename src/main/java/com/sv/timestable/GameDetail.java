package com.sv.timestable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class GameDetail {

    private final int tableFrom, tableTo, totalQuestions;
    private final String date;

    private List<QuesAns> quesAns;

    public GameDetail(int tableFrom, int tableTo, int totalQuestions, String date) {
        this.tableFrom = tableFrom;
        this.tableTo = tableTo;
        this.totalQuestions = totalQuestions;
        this.date = date;
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

    public String getDate() {
        return date;
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
        return "Question{" +
                "tableFrom=" + tableFrom +
                ", tableTo=" + tableTo +
                ", totalQuestions=" + totalQuestions +
                ", date=" + date +
                '}';
    }

    public String detail() {
        return "Question{" +
                "tableFrom=" + tableFrom +
                ", tableTo=" + tableTo +
                ", totalQuestions=" + totalQuestions +
                ", date=" + date +
                ", quesAns=" + quesAns +
                '}';
    }

    public String forTable() {
        return "Score [" + getCorrectAnsCnt() +
                "/" + totalQuestions +
                "], Tables [" + tableFrom +
                " to " + tableTo +
                "]";
    }

    public String tooltip() {
        return "On " + date +
                ", double click for details";
    }
}
