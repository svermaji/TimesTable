package com.sv.timestable;

import com.sv.core.Constants;

import javax.swing.border.*;
import java.awt.*;

public final class AppConstants {

    private AppConstants() {
    }

    public static final Border outBorder =
            new EmptyBorder(new Insets(1, 1, 1, 1));
    public static final int BTNS_WIDTH = 300;
    public static final int BTNS_HEIGHT = 300;
    public static final int GAME_WAIT_TIME_SEC = 3;
    public static final int ALARM_TIME_SEC = 5;
    public static final int GAME_TIME_SEC = 60;
    public static final int DEFAULT_TABLE_ROWS = 30;
    public static final String GAME_DATA_SEP = Constants.PIPE;
    public static final String GAME_DATA_SEP_FOR_SPLIT = Constants.SLASH + GAME_DATA_SEP;
    public static final String QA_DATA_SEP = Constants.COMMA;
    public static final String QA_SEP = Constants.SEMI_COLON;
    public static final String QA_SEP_FOR_SPLIT = Constants.SLASH + QA_SEP;
    public static final String QA_DATA_SEP_FOR_SPLIT = Constants.SLASH + QA_DATA_SEP;
}
