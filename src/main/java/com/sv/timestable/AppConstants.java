package com.sv.timestable;

import com.sv.core.Constants;
import com.sv.swingui.SwingUtils;
import com.sv.swingui.UIConstants;

import javax.swing.*;
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
    public static final int DEFAULT_TABLE_ROWS = 8;
    public static final String SCORE_DATA_SEP = Constants.PIPE;
    public static final String SCORE_DATA_SEP_FOR_SPLIT = Constants.SLASH + SCORE_DATA_SEP;
    public static final String SCORE_SEP = Constants.SEMI_COLON;
    public static final String PROP_SCORES_SUFFIX = "-scores";
    public static final String PRM_NAME_ACTION = "action";
    public static final String PRM_VAL_NOPWD = "no-pwd";
    public static final String PRM_NOPWD_ACTION = "no-pwd";
    public static final String PRM_VAL_DELUSER = "delete-user";

    public static final Color GAME_BTN_COLOR = Color.white;
    public static final Color GAME_BTN_CLICK_COLOR = new Color(255, 228, 181);
}
