package com.sv.timestable;

import com.sv.core.Utils;

import java.util.ArrayList;
import java.util.List;

public enum UIName {
    BTN_START("Start", 's', "Start/Stop the game"),
    BTN_PAUSE("Pause", 'p', "Pause/Resume the game"),
    LBL_SCORE("Score", 'l', "Score of the game"),
    LBL_TIME("Time", 'm', "Time remaining of the game"),
    LBL_QUESTIONS("Questions", 'q', "How many questions in a minute"),
    LBL_TBLRANGE("Tables", 's', "Tables range for questions."),
    SETTINGS_MENU("Settings", 'g', "Different system settings"),
    BTN_HISTORY("History", 'y', "Scores history"),
    MI_HELP("Help", 'h', "Help about game"),
    MI_HELP_BROWSER("Help in browser", 'w', "Open help in browser");

    final String name, tip;
    final char mnemonic;
    List<String> keys;

    UIName(String name, char mnemonic) {
        this(name, mnemonic, null);
    }

    UIName(String name, char mnemonic, String tip) {
        this.name = name;
        this.tip = tip;
        this.mnemonic = mnemonic;
    }

    UIName(String name, char mnemonic, String addlKey, String tip) {
        this(name, mnemonic, tip);
        keys = new ArrayList<>();
        keys.add(mnemonic + "");
        if (Utils.hasValue(addlKey)) {
            keys.add(addlKey);
        }
    }
}
