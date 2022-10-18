package com.sv.timestable.task;

import java.util.TimerTask;

public class GameCompletedTask extends TimerTask {

    private final com.sv.matchpair.MathPractise mp;

    public GameCompletedTask(com.sv.matchpair.MathPractise mp) {
        this.mp = mp;
    }

    @Override
    public void run() {
        mp.gameCompletedActions();
    }
}
