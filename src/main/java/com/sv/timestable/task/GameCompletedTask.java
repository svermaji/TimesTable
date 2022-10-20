package com.sv.timestable.task;

import com.sv.timestable.TimesTable;

import java.util.TimerTask;

public class GameCompletedTask extends TimerTask {

    private final TimesTable tt;

    public GameCompletedTask(TimesTable tt) {
        this.tt = tt;
    }

    @Override
    public void run() {
        tt.gameCompletedActions();
    }
}
