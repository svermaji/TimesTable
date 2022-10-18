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
    public static final Border inBorder = new GameButtonBorder(2);
    public static final Border GAMEBTN_BORDER =
            BorderFactory.createCompoundBorder(outBorder, inBorder);
    public static final int BTNS_WIDTH = 300;
    public static final int BTNS_HEIGHT = 300;
    public static final int GRAPH_POINTS_TO_DRAW_LIMIT = 10;
    public static final int WRONG_PAIR_MSG_TIME = 300; // milliseconds
    public static final int GAME_WAIT_TIME_SEC = 3;
    public static final int ALARM_TIME_SEC = 5;
    public static final int GAME_TIME_SEC = 80;
    public static final int GRAPH_POINT_WIDTH = 12;
    public static final int GRAPH_LINE_WIDTH = 6;
    public static final int DEFAULT_TABLE_ROWS = 8;
    public static final int GAME_SEQ_LIMIT_MAX = 8;
    public static final int GAME_SEQ_LIMIT_MIN = 2;
    public static final int PAIRS_COUNT = 3;
    public static final String SCORE_DATA_SEP = Constants.PIPE;
    public static final String SCORE_DATA_SEP_FOR_SPLIT = Constants.SLASH + SCORE_DATA_SEP;
    public static final String SCORE_SEP = Constants.SEMI_COLON;
    public static final String PROP_SCORES_SUFFIX = "-scores";
    public static final String PRM_NAME_ACTION = "action";
    public static final String PRM_VAL_NOPWD = "no-pwd";
    public static final String PRM_NOPWD_ACTION = "no-pwd";
    public static final String PRM_VAL_DELUSER = "delete-user";

    // 40 in count
    public static final String[] GAME_SMILEYS = {
            "10084", "129317", "128521", "128525", "129336",
            "129312", "129313", "129317", "128520", "128064",
            "128128", "9760", "128123", "128125", "128169",
            "128578", "128571", "128105", "128142", "128118",
            "128170", "128130", "127877", "128120", "128107",
            "128112", "128131", "128400", "128077", "128139",
            "128013", "127801", "127760", "128081", "128087",
            "128591"
    };

    // 35 in count
    public static final String[] GAME_SYMBOLS = {
            "9786", "9787", "9788", "9789", "9790",
            "9791", "9792", "9793", "9794", "9795",
            "9796", "9797", "9798", "9799", "9800",
            "9801", "9802", "9803", "9804", "9805",
            "9806", "9807", "9808", "9809", "9810",
            "9811", "9812", "9813", "9814", "9815",
            "9816", "9817", "9818", "9819", "9820"
    };

    // 35 in count
    public static final String[] GAME_CHARS = {
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
            "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z", "1", "2", "3", "4", "5", "6", "7", "8", "9"
    };

    public static final Color GAME_BTN_COLOR = Color.white;
    public static final Color GAME_BTN_CLICK_COLOR = new Color(255, 228, 181);
}
