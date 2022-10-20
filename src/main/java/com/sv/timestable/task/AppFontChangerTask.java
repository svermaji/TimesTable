package com.sv.timestable.task;

import com.sv.timestable.TimesTable;

import java.util.TimerTask;

public class AppFontChangerTask extends TimerTask {

    private final TimesTable tt;

    public AppFontChangerTask(TimesTable tt) {
        this.tt = tt;
    }

    @Override
    public void run() {
        tt.changeAppFont();
    }
}
