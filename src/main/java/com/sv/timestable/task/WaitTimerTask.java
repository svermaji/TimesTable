package com.sv.timestable.task;

import com.sv.timestable.TimesTable;

import java.util.TimerTask;

public class WaitTimerTask extends TimerTask {

    private final TimesTable tt;

    public WaitTimerTask(TimesTable tt) {
        this.tt = tt;
    }

    @Override
    public void run() {
        tt.updateWaitTime();
    }
}
