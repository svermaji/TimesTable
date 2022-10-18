package com.sv.timestable.task;

import java.util.TimerTask;

public class AppFontChangerTask extends TimerTask {

    private final com.sv.matchpair.MathPractise mp;

    public AppFontChangerTask(com.sv.matchpair.MathPractise sbf) {
        this.mp = sbf;
    }

    @Override
    public void run() {
        mp.changeAppFont();
    }
}
