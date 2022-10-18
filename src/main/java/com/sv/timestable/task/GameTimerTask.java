package com.sv.timestable.task;

import com.sv.core.Utils;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {

    private final com.sv.matchpair.MathPractise mp;

    public GameTimerTask(com.sv.matchpair.MathPractise mp) {
        this.mp = mp;
    }

    @Override
    public void run() {
        if (mp.isGameStart()) {
            mp.updateGameTime();
        }
        if (mp.isGamePaused()) {
            mp.performPauseAction();
        }
    }
}
