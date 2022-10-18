package com.sv.timestable;

import com.sv.core.Utils;

import java.util.ArrayList;
import java.util.List;

public enum UIName {
    BTN_USER("Hi", 'u', "Username, click to change"),
    LBL_USER("", 'r', "Set Username. Hit enter to save & ESC to cancel"),
    BTN_START("Start", 's', "Start/Stop the game"),
    BTN_LEVEL("Lvl", 'v', "Present level/type of the game"),
    BTN_PAUSE("Pause", 'p', "Pause/Resume the game"),
    LBL_SCORE("Score", 'l', "Score of the game"),
    LBL_TIME("Time", 'm', "Time remaining of the game"),
    MENU("Settings", 'g', "Different system settings"),
    MI_PAIR_TYPES("Pair Types", 'i', "Choose pair types in game"),
    MI_PT_CHARS("Characters", 'c', "A to Z and 1 to 9 characters"),
    MI_PT_SYMBOLS("Symbols", 'o', "Mixed symbols"),
    MI_PT_SMILEYS("Smileys", 'm', "Smileys"),
    MI_SETUSER("Set user", 't', "Set user"),
    MI_DELUSER("Delete user", 'e', "Delete user - need password"),
    BTN_HISTORY("History", 'y', "Scores history"),
    MI_NOUSERPWD("No password for users", 'w', "No password is required for users"),
    MI_CHANGEPWD("Change password", 'd', "Change or set Admin password"),
    MI_CHANGEMYPWD("Change my password", 'g', "Change or set user password"),
    MI_HELP("Help", 'h', "Help about game"),
    MI_HELP_BROWSER("Help in browser", 'w', "Open help in browser");

    String name, tip;
    char mnemonic;
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
