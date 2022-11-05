package com.sv.timestable.action;

import com.sv.swingui.component.table.AppTable;
import com.sv.timestable.TimesTable;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class GameHistoryEnterAction extends AbstractAction {

    private final AppTable table;
    private final TimesTable tt;

    public GameHistoryEnterAction(AppTable table, TimesTable tt) {
        this.table = table;
        this.tt = tt;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tt.handleDblClickOnRow(table, null);
    }
}
