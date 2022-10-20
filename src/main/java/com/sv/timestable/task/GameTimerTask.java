package com.sv.timestable.task;

import com.sv.timestable.TimesTable;

import java.util.TimerTask;

public class GameTimerTask extends TimerTask {

    private final TimesTable tt;

    public GameTimerTask(TimesTable tt) {
        this.tt = tt;
    }

    @Override
    public void run() {
        if (tt.isGameStart()) {
            tt.updateGameTime();
        }
    }
}
