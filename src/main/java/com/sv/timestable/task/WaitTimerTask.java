package com.sv.timestable.task;

import java.util.TimerTask;

public class WaitTimerTask extends TimerTask {

    private final com.sv.matchpair.MathPractise mp;

    public WaitTimerTask(com.sv.matchpair.MathPractise mp) {
        this.mp = mp;
    }

    @Override
    public void run() {
        mp.updateWaitTime();
    }
}
