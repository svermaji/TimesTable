package com.sv.timestable;

public final class Question {

    private final int idx, num1, num2, correctAns;
    private final String opr = "x";
    private String status = AnsStatus.notAnswered.val;

    private QuesAns ans;

    public enum AnsStatus {
        correct("correct"),
        wrong("wrong"),
        notAnswered("not answered");

        String val;

        AnsStatus(String val) {
            this.val = val;
        }
    }

    public Question(int idx, int num1, int num2) {
        this.idx = idx;
        this.num1 = num1;
        this.num2 = num2;
        correctAns = num1 * num2;
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

    public int getCorrectAns() {
        return correctAns;
    }

    public String getOpr() {
        return opr;
    }

    public String getStatus() {
        return status;
    }

    /**
     * Can set only once
     * @param userAns of type int
     */
    public void setUserAns(int userAns) {
        if (ans == null) {
            ans = new QuesAns(userAns);
            status = correctAns == userAns ? AnsStatus.correct.val : AnsStatus.wrong.val;
        }
    }

    public int getAns() {
        return ans.getUserAns();
    }

    @Override
    public String toString() {
        return "Question{" +
                "num1=" + num1 +
                ", num2=" + num2 +
                '}';
    }

    public String detail() {
        return "Question{" +
                "idx=" + idx +
                ", num1=" + num1 +
                ", num2=" + num2 +
                ", correctAns=" + correctAns +
                ", opr='" + opr + '\'' +
                ", status='" + status + '\'' +
                ", ans=" + ans.getUserAns() +
                '}';
    }
}
