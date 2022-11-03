package com.sv.timestable;

public final class QuesAns {

    private final int idx, num1, num2, correctAns;
    private String timeTaken;
    public static final int VAL_TO_IGNORE = -1;
    private int userAns = VAL_TO_IGNORE;
    private final String opr = "x";

    private String status = AnsStatus.notAnswered.val;

    public enum AnsStatus {
        correct("correct"),
        wrong("wrong"),
        notAnswered("not answered");

        String val;

        AnsStatus(String val) {
            this.val = val;
        }
    }

    public QuesAns(int idx, int num1, int num2) {
        this.idx = idx;
        this.num1 = num1;
        this.num2 = num2;
        this.correctAns = num1 * num2;
    }

    public int getIdx() {
        return idx;
    }

    public int getNum1() {
        return num1;
    }

    public int getNum2() {
        return num2;
    }

    public int getUserAns() {
        return userAns;
    }

    public void setUserAns(int userAns) {
        this.userAns = userAns;
        if (userAns != VAL_TO_IGNORE) {
            status = correctAns == userAns ? AnsStatus.correct.val : AnsStatus.wrong.val;
        }
    }

    public void setTimeTaken(String timeTaken) {
        this.timeTaken = timeTaken;
    }

    public String getTimeTaken() {
        return timeTaken;
    }

    public String getOpr() {
        return opr;
    }

    public String getStatus() {
        return status;
    }

    public boolean isCorrectAns() {
        return status.equals(AnsStatus.correct.val);
    }

    public boolean isQNotAnswered() {
        return status.equals(AnsStatus.notAnswered.val);
    }

    @Override
    public String toString() {
        return "QuesAns{" +
                "idx=" + idx +
                ", num1=" + num1 +
                ", num2=" + num2 +
                ", correctAns=" + correctAns +
                ", timeTaken=" + timeTaken +
                ", userAns=" + userAns +
                ", opr='" + opr + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
