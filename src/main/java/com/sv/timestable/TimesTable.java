package com.sv.timestable;

import com.sv.core.Utils;
import com.sv.core.config.DefaultConfigs;
import com.sv.swingui.KeyActionDetails;
import com.sv.swingui.SwingUtils;
import com.sv.swingui.UIConstants;
import com.sv.swingui.component.*;
import com.sv.swingui.component.table.AppTable;
import com.sv.swingui.component.table.AppTableHeaderToolTip;
import com.sv.swingui.component.table.CellRendererCenterAlign;
import com.sv.swingui.component.table.CellRendererLeftAlign;
import com.sv.timestable.task.AppFontChangerTask;
import com.sv.timestable.task.GameCompletedTask;
import com.sv.timestable.task.GameTimerTask;
import com.sv.timestable.task.WaitTimerTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sv.core.Constants.*;
import static com.sv.swingui.UIConstants.*;

/**
 * Java Game for Times Table practise
 */
public class TimesTable extends AppFrame {

    /**
     * This is config and program will search getter
     * of each enum to store in config file.
     * <p>
     * e.g. if enum is Xyz then when storing getXyz will be called
     */
    public enum Configs {
        AppFontSize, CNFIdx, TablesFrom, TablesTo, TotalQuestions
    }

    public enum Status {
        NOT_STARTED, START, STOP
    }

    private DefaultConfigs configs;
    private TitledBorder titledBorder;
    private JMenuBar menuBar;
    private AppMenu menu;
    private JTextPane tpHelp;

    public enum GameScreens {
        help, wait, game
    }

    private AppButton btnStart, btnExit;
    private AppTextField txtAnswer;
    private AppLabel lblTime, lblWaitTime, lblTblRange, lblDash, lblQuestions,
            lblIdx, lblNum1, lblNum2, lblMultiply, lblEqual, lblResult;
    private AppComboBox cbTblFrom, cbTblTo, cbQuestions;
    private AppTable tblRecentScore;
    private DefaultTableModel recentScoreModel;

    public enum AppPaths {
        correctAns("./icons/correct-ans-icon.png"),
        wrongAns("./icons/wrong-ans-icon.png"),
        scoresLoc("./src/main/resources/scores.config"),
        openHelpLoc("./src/main/resources/show-help.bat");

        final String val;

        AppPaths(String val) {
            this.val = val;
        }
    }

    private AppPanel topPanel, centerPanel, gamePanel,
            completeGamePanel, gameButtonsPanel, questionPanel,
            waitPanel, waitLabelsPanel, tblPanel, helpPanel, tblRngPanel, totalQPanel;
    private JScrollPane jspHelp;
    private JSplitPane splitPane;
    private JComponent[] componentsToColor, componentsToAddLine;
    private List<JComponent> commonScreens;

    private Status gameStatus = Status.NOT_STARTED;
    private String fontName;
    private int gameScore, gameAccuracy, cnfIdx = 0,
            totalCorrectPairs;

    private final List<Timer> TIMERS = new ArrayList<>();
    private final ColorsNFonts[] APP_COLORS = SwingUtils.getFilteredCnF(false);
    private final CellRendererLeftAlign LEFT_RENDERER = new CellRendererLeftAlign();
    private final String TITLE_HEADING = "Controls";
    private final int TIMES = 12, DEFAULT_TIMES = 4, ANS_MAX_LEN = 3,
            TABLE_FROM_MIN = 1, TABLE_FROM_MAX = 30, TABLE_FROM_DEFAULT = 2,
            TABLE_TO_MIN = 2, TABLE_TO_MAX = 30, TABLE_TO_DEFAULT = 12,
            TOTAL_Q_MIN = 2, TOTAL_Q_MAX = 60, TOTAL_Q_DEFAULT = 24,
            HISTORY_LIMIT = 30;

    private static int tableFrom, tableTo, totalQuestions;
    private static int qCtr = 0, gameTime = 0, gameWaitTime = 0;

    private GameDetail gameDetail;
    private Map<String, GameDetail> gameHistory;
    private QuesAns currentQues;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new TimesTable()::initComponents);
    }

    public TimesTable() {
        super("Times Table");
    }

    /**
     * This method initializes the form.
     */
    private void initComponents() {
        configs = new DefaultConfigs(logger, Utils.getConfigsAsArr(Configs.class));
        gameHistory = new TreeMap<>(Comparator.comparing(Integer::valueOf));
        loadConfigValues();
        loadGameHistory();
        logger.setSimpleClassName(true);
        setEchoChar('*');

        LEFT_RENDERER.setShowSameTipOnRow(true);

        List<WindowChecks> windowChecks = new ArrayList<>();
        /*windowChecks.add(WindowChecks.WINDOW_ACTIVE);
        applyWindowActiveCheck(windowChecks.toArray(new WindowChecks[0]));*/

        appFontSize = Utils.validateInt(configs.getIntConfig(Configs.AppFontSize.name()),
                DEFAULT_APPFONTSIZE, MIN_APPFONTSIZE, MAX_APPFONTSIZE);

        logger.info("appFontSize " + Utils.addBraces(appFontSize));

        btnExit = new AppExitButton();
        btnExit.addActionListener(evt -> exitForm());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                exitForm();
            }
        });

        Container parentContainer = getContentPane();
        parentContainer.setLayout(new BorderLayout());

        topPanel = new AppPanel(new BorderLayout());
        titledBorder = SwingUtils.createTitledBorder(TITLE_HEADING, fg);
        topPanel.setBorder(titledBorder);
        UIName uin = UIName.BTN_START;
        btnStart = new AppButton(uin.name, uin.mnemonic, uin.tip);
        btnStart.addActionListener(e -> startGame());
        uin = UIName.LBL_TIME;
        lblTime = new AppLabel(uin.name, uin.mnemonic, uin.tip);
        cbTblFrom = new AppComboBox(Utils.createIntArr(TABLE_FROM_MIN, TABLE_FROM_MAX),
                Integer.valueOf(tableFrom), "");
        cbTblFrom.addActionListener(a -> tableFrom = Utils.convertToInt(cbTblFrom.getSelectedItem().toString()));
        cbTblTo = new AppComboBox(Utils.createIntArr(TABLE_TO_MIN, TABLE_TO_MAX),
                Integer.valueOf(tableTo), "");
        cbTblTo.addActionListener(a -> tableTo = Utils.convertToInt(cbTblTo.getSelectedItem().toString()));
        cbQuestions = new AppComboBox(Utils.createIntArr(1, 60),
                Integer.valueOf(totalQuestions), "");
        cbQuestions.addActionListener(a -> totalQuestions = Utils.convertToInt(cbQuestions.getSelectedItem().toString()));

        uin = UIName.LBL_TBLRANGE;
        lblTblRange = new AppLabel(uin.name, uin.mnemonic, uin.tip);
        lblDash = new AppLabel(DASH);
        uin = UIName.LBL_QUESTIONS;
        lblQuestions = new AppLabel(uin.name, uin.mnemonic, uin.tip);

        menuBar = new JMenuBar();
        tblRngPanel = new AppPanel();
        tblRngPanel.setLayout(new FlowLayout());
        tblRngPanel.add(lblTblRange);
        tblRngPanel.add(cbTblFrom);
        tblRngPanel.add(lblDash);
        tblRngPanel.add(cbTblTo);
        totalQPanel = new AppPanel();
        totalQPanel.setLayout(new FlowLayout());
        totalQPanel.add(lblQuestions);
        totalQPanel.add(cbQuestions);
        AppToolBar tbControls = new AppToolBar();
        tbControls.add(btnStart);
        tbControls.add(lblTime);
        tbControls.add(tblRngPanel);
        tbControls.add(totalQPanel);
        tbControls.add(menuBar);
        tbControls.add(btnExit);
        tbControls.setLayout(new GridLayout(1, tbControls.getComponentCount()));
        tbControls.setMargin(new Insets(1, 3, 1, 3));
        topPanel.add(tbControls);
        topPanel.setSize(topPanel.getWidth(), 100);
        topPanel.setBorder(SwingUtils.createLineBorder(Color.BLUE));

        centerPanel = new AppPanel(new BorderLayout());
        gamePanel = new AppPanel(new BorderLayout());

        helpPanel = new AppPanel(new BorderLayout());
        helpPanel.setName("Help Panel");
        waitPanel = new AppPanel(new BorderLayout());
        waitPanel.setName("Wait Panel");
        gameButtonsPanel = new AppPanel(new BorderLayout());
        gameButtonsPanel.setName("Game Buttons Panel");
        completeGamePanel = new AppPanel(new BorderLayout());
        completeGamePanel.setName("Complete Game Panel");
        loadGameButtons();
        prepareWaitScreen();
        setupHelp();
        setAllTables();

        gamePanel.add(helpPanel);
        gamePanel.add(waitPanel);
        gamePanel.add(completeGamePanel);

        AppLabel[] lbls = {lblWaitTime, lblTime};
        Arrays.stream(lbls).forEach(l -> l.setHorizontalAlignment(SwingConstants.CENTER));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tblPanel, gamePanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.01);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerLocation(-1);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        parentContainer.add(topPanel, BorderLayout.NORTH);
        parentContainer.add(centerPanel, BorderLayout.CENTER);

        maximiseWin();
        enableControls();
        SwingUtils.getInFocus(btnStart);
        storeAndLoad();

        uin = UIName.MENU;
        menu = new AppMenu(uin.name, uin.mnemonic, uin.tip) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, menuBar.getWidth()); // set minimums
                d.height = Math.max(d.height, menu.getHeight());
                return d;
            }
        };

        menu.add(SwingUtils.getColorsMenu(true, true,
                true, true, false, this, logger));
        menu.add(SwingUtils.getAppFontMenu(this, this, appFontSize, logger));
        menu.addSeparator();
        uin = UIName.MI_HELP;
        AppMenuItem miHelp = new AppMenuItem(uin.name, uin.mnemonic, uin.tip);
        menu.add(miHelp);
        miHelp.addActionListener(e -> showHelp());
        uin = UIName.MI_HELP_BROWSER;
        AppMenuItem miHelpBrowser = new AppMenuItem(uin.name, uin.mnemonic, uin.tip);
        menu.add(miHelpBrowser);
        miHelpBrowser.addActionListener(e -> showHelpInBrowser());
        menuBar.add(menu);

        SwingUtils.updateUIFor(menuBar);

        componentsToAddLine = new JComponent[]{btnStart, lblTime,
                menuBar, menu, btnExit, tblRecentScore.getTableHeader(),
                tblRngPanel, totalQPanel
        };
        componentsToColor = new JComponent[]{btnStart, lblTime,
                menuBar, menu, btnExit, tblRecentScore.getTableHeader(),
                tblRngPanel, totalQPanel,
                lblTblRange, cbTblFrom,
                lblDash, cbTblTo, lblQuestions, cbQuestions
        };
        commonScreens = new ArrayList<>();
        commonScreens.add(helpPanel);
        commonScreens.add(waitPanel);
        commonScreens.add(completeGamePanel);
        colorChange(cnfIdx);
        setControlsToEnable();
        addBindings();
        showHelp();

        new Timer().schedule(new AppFontChangerTask(this), SEC_1);
    }

    private void loadGameButtons() {
        questionPanel = new AppPanel();
        questionPanel.setName("Question Panel");
        lblIdx = new AppLabel();
        lblNum1 = new AppLabel();
        lblNum2 = new AppLabel();
        lblMultiply = new AppLabel("X");
        lblResult = new AppLabel();
        lblEqual = new AppLabel(EQUAL);
        txtAnswer = new AppTextField(EMPTY, ANS_MAX_LEN * 2);
        txtAnswer.setToolTipText("Hit ENTER to submit answer, ESCAPE to skip the question");
        txtAnswer.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    submitAns();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    skipQuestion();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (txtAnswer.getText().trim().length() == ANS_MAX_LEN
                        ||
                        ((c < '0') || (c > '9'))) {
                    e.consume();  // if it's not a number, ignore the event
                }
            }
        });
        questionPanel.add(lblIdx);
        questionPanel.add(lblNum1);
        questionPanel.add(lblMultiply);
        questionPanel.add(lblNum2);
        questionPanel.add(lblEqual);
        questionPanel.add(txtAnswer);
        questionPanel.add(lblResult);

        int rows = 4;
        int cols = 3;
        gameButtonsPanel.setLayout(new GridLayout(rows, cols));
        String[][] btns = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"},
                {"<x]", "0", "ENTER"}
        };

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                AppButton b = new AppButton(btns[i][j], btns[i][j].charAt(0));
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isLeftMouseButton(e)) {
                            gameBtnPressed(b.getText());
                        }
                        SwingUtils.getInFocus(txtAnswer);
                    }
                });
                gameButtonsPanel.add(b);
            }
        }
        completeGamePanel.add(questionPanel, BorderLayout.NORTH);
        completeGamePanel.add(gameButtonsPanel, BorderLayout.CENTER);
    }

    private void gameBtnPressed(String text) {
        String txt = txtAnswer.getText();
        if (txt.length() < ANS_MAX_LEN) {
            char c = text.charAt(0);
            if (c == '<') {
                if (Utils.hasValue(txt)) {
                    txtAnswer.setText(txt.substring(0, txt.length() - 1));
                }
            } else if (c == 'E') {
                submitAns();
            } else {
                txtAnswer.setText(txt + c);
            }
        }
        SwingUtils.getInFocus(txtAnswer);
    }

    private void submitAns() {
        currentQues.setUserAns(Utils.convertToInt(txtAnswer.getText()));
        showNextQue();
    }

    private void skipQuestion() {
        showNextQue();
    }

    private void setupHelp() {
        File file = new File("./help.html");
        tpHelp = new JTextPane();
        tpHelp.setEditable(false);
        tpHelp.setContentType("text/html");
        try {
            tpHelp.setPage(file.toURI().toURL());
        } catch (IOException e) {
            logger.error("Unable to display help");
        }
        jspHelp = new JScrollPane(tpHelp);
        jspHelp.setBorder(EMPTY_BORDER);
        helpPanel.add(jspHelp);
    }

    // This method will decide which panel to show
    private void showScreen(GameScreens gs) {
        AppPanel toShow = null;
        String nm = gs.name();
        switch (nm) {
            case "help":
                toShow = helpPanel;
                break;
            case "game":
                toShow = completeGamePanel;
                break;
            case "wait":
                toShow = waitPanel;
                break;
        }
        AppPanel finalToShow = toShow;
        commonScreens.forEach(c -> {
            if (null != c) {
                if (finalToShow == null || !c.getName().equals(finalToShow.getName())) {
                    c.setVisible(false);
                    gamePanel.remove(c);
                }
            }
        });
        if (toShow != null) {
            toShow.setVisible(true);
            gamePanel.add(toShow, BorderLayout.CENTER);
        }
        SwingUtils.updateUIFor(gamePanel);
        logger.info("Showing screen for " + Utils.addBraces(nm));
    }

    private void showHelpInBrowser() {
        //new RunCommand(new String[]{AppPaths.openHelpLoc.val + SPACE + Utils.getCurrentDir()}, logger);
    }

    private void showHelp() {
        showScreen(GameScreens.help);
    }

    private void prepareWaitScreen() {
        lblWaitTime = new AppLabel();
        BoxLayout boxlayout = new BoxLayout(waitPanel, BoxLayout.PAGE_AXIS);
        waitPanel.setLayout(boxlayout);
        int r = 3, c = 3;
        waitLabelsPanel = new AppPanel(new GridLayout(r, c));
        waitLabelsPanel.setName("Wait Labels");
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                waitLabelsPanel.add((i == 1 && j == 1) ? lblWaitTime : new AppLabel());
            }
        }
        waitPanel.add(waitLabelsPanel);
        lblWaitTime.setBorder(EMPTY_BORDER);
        lblWaitTime.setHorizontalAlignment(SwingConstants.CENTER);
        lblWaitTime.setVerticalAlignment(SwingConstants.CENTER);
        lblWaitTime.setPreferredSize(waitPanel.getPreferredSize());
    }

    private void setAllTables() {
        String[] recentScoreCols = new String[]{"History"};

        recentScoreModel = SwingUtils.getTableModel(recentScoreCols);
        tblRecentScore = new AppTable(recentScoreModel);
        tblRecentScore.setTableHeader(new AppTableHeaderToolTip(tblRecentScore.getColumnModel(), recentScoreCols));

        // sets the popup menu for the table
        setTable(tblRecentScore, recentScoreModel);
        loadTableData();

        tblPanel = new AppPanel(new GridLayout(1, 1));
        tblPanel.add(new JScrollPane(tblRecentScore));
        tblPanel.setBorder(EMPTY_BORDER);
    }

    private void loadTableData() {
        if (gameHistory.size() > 0) {
            populateScoreTbl(gameHistory, recentScoreModel, tblRecentScore);
        }
    }

    private void populateScoreTbl(Map<String, GameDetail> games, DefaultTableModel model, AppTable tbl) {
        // empty table first
        model.setRowCount(0);
        tbl.emptyRowTooltips();
        AtomicInteger i = new AtomicInteger(0);
        games.forEach((k, v) -> {
            if (i.getAndIncrement() < AppConstants.DEFAULT_TABLE_ROWS - 1) {
                GameDetail gd = v;
                model.addRow(new String[]{k + ". " + gd.forTable()});
                tbl.addRowTooltip(new String[]{gd.tooltip()});
            }
        });
        int sz = games.size();
        if (AppConstants.DEFAULT_TABLE_ROWS > sz) {
            int n = AppConstants.DEFAULT_TABLE_ROWS - sz;
            SwingUtils.createEmptyRows(model.getColumnCount(), n, model);
        }
    }

    private void maximiseWin() {
        setToCenter();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void loadGameHistory() {
        Properties props = Utils.readPropertyFile(AppPaths.scoresLoc.val, logger);
        props.stringPropertyNames().forEach(k -> {
                    String v = props.getProperty(k);
                    gameHistory.put(k, extractGameDetail(v));
                }
        );
        logger.info("Total [" + gameHistory.size() + "] games loaded in history.");
    }

    private GameDetail extractGameDetail(String v) {
        GameDetail gd;
        if (Utils.hasValue(v)) {
            // for split from same char you need to escape
            String[] arr = v.split(AppConstants.GAME_DATA_SEP_FOR_SPLIT);
            gd = new GameDetail(Utils.convertToInt(arr[0]),
                    Utils.convertToInt(arr[1]), Utils.convertToInt(arr[2]), arr[3]);
            String[] qas = arr[4].split(AppConstants.QA_SEP_FOR_SPLIT);
            Arrays.stream(qas).forEach(s -> {
                String[] qaData = s.split(AppConstants.QA_DATA_SEP_FOR_SPLIT);
                QuesAns q = new QuesAns(Utils.convertToInt(qaData[0]),
                        Utils.convertToInt(qaData[1]),
                        Utils.convertToInt(qaData[2]));
                q.setUserAns(Utils.convertToInt(qaData[3]));
                gd.addQues(q);
            });
            return gd;
        }

        return null;
    }

    private void loadConfigValues() {
        cnfIdx = configs.getIntConfig(Configs.CNFIdx.name());
        appFontSize = configs.getIntConfig(Configs.AppFontSize.name());
        tableFrom = Utils.validateInt(configs.getIntConfig(Configs.TablesFrom.name()),
                TABLE_FROM_DEFAULT, TABLE_FROM_MIN, TABLE_FROM_MAX);
        tableTo = Utils.validateInt(configs.getIntConfig(Configs.TablesTo.name()),
                TABLE_TO_DEFAULT, TABLE_TO_MIN, TABLE_TO_MAX);
        totalQuestions = Utils.validateInt(configs.getIntConfig(Configs.TotalQuestions.name()),
                TOTAL_Q_DEFAULT, TOTAL_Q_MIN, TOTAL_Q_MAX);
        logger.info("Config loaded as tableFrom [" + tableFrom + "], tableTo [" +
                tableTo + "], totalQuestions " + Utils.addBraces(totalQuestions));
    }

    private void setTable(AppTable tbl, DefaultTableModel model) {
        tbl.setScrollProps();
        tbl.setRowHeight(appFontSize + 4);
        tbl.setBorder(EMPTY_BORDER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setCellRenderer(LEFT_RENDERER);
        }
    }

    public void changeAppFont() {
        SwingUtils.applyAppFont(this, appFontSize, this, logger);
        // might be as these are hidden at start
        JComponent[] arr = {waitPanel, gameButtonsPanel, questionPanel};
        Arrays.stream(arr).forEach(a ->
                SwingUtils.applyAppFont(a, appFontSize, this, logger));
        /*SwingUtils.applyAppFont(txtAnswer, appFontSize, this, logger);*/
    }

    // This will be called by reflection from SwingUI jar
    public void appFontChanged(Integer fs) {
        appFontSize = fs;
        logger.info("Application font changed to " + Utils.addBraces(fs));

        // calling to change tooltip font
        changeAppColor();
    }

    // This will be called by reflection from SwingUI jar
    public void colorChange(Integer x) {
        //if (isWindowActive())
        {
            cnfIdx = x;
            ColorsNFonts c = APP_COLORS[cnfIdx];
            bg = c.getBk();
            fg = c.getFg(); // foreground not working with highlighter //c.getFg();
            hbg = c.getSelbk();
            hfg = c.getSelfg();
            fontName = c.getFont();
            changeAppColor();
        }
    }

    private void changeAppColor() {
        // will set colors for pwd screens
        setAppColors(fg, bg, hfg, hbg);

        createBorders();
        titledBorder = SwingUtils.createTitledBorder(TITLE_HEADING, fg);
        TitledBorder[] toTitleColor = {titledBorder};
        Arrays.stream(toTitleColor).forEach(t -> {
            t.setTitleColor(fg);
            t.setTitleFont(SwingUtils.getNewFontSize(t.getTitleFont(), appFontSize));
        });
        topPanel.setBorder(titledBorder);

        Arrays.stream(componentsToAddLine).forEach(c -> c.setBorder(SwingUtils.createLineBorder(fg)));
        Arrays.stream(gameButtonsPanel.getComponents()).forEach(c ->
                SwingUtils.setComponentColor((JComponent) c, bg, fg, hbg, hfg));
        Arrays.stream(questionPanel.getComponents()).forEach(c ->
                SwingUtils.setComponentColor((JComponent) c, null, fg));
        SwingUtils.setComponentColor(componentsToColor, bg, fg, hbg, hfg);
        // to make exit button different
        SwingUtils.setComponentColor(btnExit, hbg, hfg, bg, fg);
        Arrays.stream(componentsToColor).forEach(c ->
                SwingUtils.applyTooltipColorNFont(c, bg, fg, SwingUtils.getNewFont(c.getFont(), fontName)));

        gamePanel.setBorder(SwingUtils.createLineBorder(hbg, 10));
        AppTable[] tbls = {tblRecentScore};
        Arrays.stream(tbls).forEach(t -> t.setRowHeight(appFontSize + 4));
        Arrays.stream(tbls).forEach(t ->
                SwingUtils.applyTooltipColorNFontAllChild(t, fg, bg,
                        SwingUtils.getNewFontSize(t.getFont(), appFontSize)));
        SwingUtils.applyTooltipColorNFontAllChild(totalQPanel, fg, bg,
                SwingUtils.getNewFontSize(totalQPanel.getFont(), appFontSize));
        JComponent[] arr = {waitPanel, gameButtonsPanel, questionPanel};
        Arrays.stream(arr).forEach(a ->
                SwingUtils.applyTooltipColorNFontAllChild(a, fg, bg,
                        SwingUtils.getNewFontSize(a.getFont(), appFontSize)));
    }

    private void startNewGame() {
        resetGame();
        showWaitScreenAndStart();
    }

    public void updateWaitTime() {
        if (gameWaitTime <= 0) {
            cancelTimers();
            setGameScreen();
        } else {
            lblWaitTime.setText(gameWaitTime + "");
            gameWaitTime--;
        }
    }

    private void showWaitScreenAndStart() {
        setWaitScreen();
        showScreen(GameScreens.wait);
        Timer t = new Timer();
        t.scheduleAtFixedRate(new WaitTimerTask(this), 0, SEC_1);
        TIMERS.add(t);
    }

    private void setWaitScreen() {
        SwingUtils.changeFont(lblWaitTime, appFontSize);
        Arrays.stream(waitLabelsPanel.getComponents()).forEach(c ->
                SwingUtils.setComponentColor((JComponent) c, UIConstants.ORIG_COLOR, fg));
    }

    private void setGameScreen() {
        showScreen(GameScreens.game);
        makeButtonsSquare();
        Timer t = new Timer();
        t.scheduleAtFixedRate(new GameTimerTask(this), 0, SEC_1);
        TIMERS.add(t);
        showNextQue();
    }

    private void showNextQue() {
        if (qCtr < gameDetail.getTotalQuestions()) {
            QuesAns qa = gameDetail.getQues(qCtr++);
            currentQues = qa;
            lblIdx.setText("Que " + qa.getIdx() + " # ");
            lblNum1.setText(qa.getNum1() + "");
            lblNum2.setText(qa.getNum2() + "");
            txtAnswer.setText(EMPTY);
            SwingUtils.getInFocus(txtAnswer);
        } else {
            gameCompleted();
        }
    }

    private void showQuestionsPanel(boolean hideResult) {
        Arrays.stream(questionPanel.getComponents()).forEach(c -> c.setVisible(hideResult));
        lblResult.setVisible(!hideResult);
        gameButtonsPanel.setVisible(hideResult);
    }

    private void showQuestions() {
        showQuestionsPanel(true);
    }

    private void showResult() {
        showQuestionsPanel(false);
    }

    private void generateQuestions() {
        gameDetail = new GameDetail(tableFrom, tableTo, totalQuestions, Utils.getFormattedDate());

        for (int i = 0; i < totalQuestions; i++) {
            int n1 = (int) (Math.random() * tableTo);
            n1 = Utils.validateInt(n1, tableTo, tableFrom, tableTo);
            int n2 = (int) (Math.random() * TIMES);
            n2 = Utils.validateInt(n2, DEFAULT_TIMES, 1, TIMES);
            gameDetail.addQues(new QuesAns(i + 1, n1, n2));
        }
    }

    private void makeButtonsSquare() {
        // setting hgap and vgap to make game 500x500
        int w = completeGamePanel.getWidth();
        int h = completeGamePanel.getHeight();
        int hgap = (w - AppConstants.BTNS_WIDTH) / 2;
        int vgap = (h - AppConstants.BTNS_HEIGHT) / 2;
        completeGamePanel.setBorder(new EmptyBorder(new Insets(vgap, hgap, vgap, hgap)));
        logger.info("Setting hgap/vgap [" + hgap + F_SLASH + vgap
                + "] for buttons of width/height [" + w + F_SLASH + h + "]");
    }

    private void resetGame() {
        //todo: timer and case of table range and set config
        qCtr = 0;
        generateQuestions();
        gameButtonsPanel.setEnabled(true);
        gameAccuracy = 0;
        totalCorrectPairs = 0;
        gameWaitTime = AppConstants.GAME_WAIT_TIME_SEC;
        gameTime = AppConstants.GAME_TIME_SEC;
        gameStatus = Status.START;
        gameScore = 0;
        updateGameTime();
        showQuestions();
    }

    private void gameCompleted() {
        showResult();
        gameAccuracy = 0;

        gameDetail.getQuesAns().forEach(q -> {
            if (q.getStatus().equals(QuesAns.AnsStatus.correct.val)) {
                totalCorrectPairs++;
            }
        });
        gameAccuracy = (totalCorrectPairs * 100) / gameDetail.getTotalQuestions();
        String msg = "Keep Practicing !!";
        if (gameAccuracy > 40 && gameAccuracy > 60) {
            msg = "That's great !!";
        }
        if (gameAccuracy > 60 && gameAccuracy > 80) {
            msg = "That's wonderful !!";
        }
        if (gameAccuracy > 80 && gameAccuracy > 95) {
            msg = "Brilliant !!";
        }
        if (gameAccuracy > 95) {
            msg = "Excellent, hurray !!";
        }
        lblResult.setText("Result: " + totalCorrectPairs + "/" + gameDetail.getTotalQuestions()
                + " (" + gameAccuracy + "%). " + msg);
        logger.info("Game end as gameScore [" + gameScore +
                "], gameAccuracy [" + gameAccuracy +
                "], totalCorrectPairs [" + totalCorrectPairs +
                "]"
        );
        btnStart.setText(UIName.BTN_START.name);
        enableControls();
        // to optimize this can be saved on exit but for now saving game progress on complete
        saveGameInHistory();
        loadTableData();
        gameStatus = Status.STOP;

        Arrays.stream(completeGamePanel.getComponents()).forEach(c -> c.setEnabled(false));

        Timer t = new Timer();
        t.schedule(new GameCompletedTask(this), SEC_1 * 2);
        TIMERS.add(t);
    }

    public void gameCompletedActions() {
        Arrays.stream(completeGamePanel.getComponents()).forEach(c -> c.setEnabled(true));
        cancelTimers();
        gameDetail = null;
    }

    private void stopGame() {
        gameStatus = Status.STOP;
        enableControls();
        cancelTimers();
        gameTime = AppConstants.GAME_TIME_SEC;
        gameScore = 0;
        updateGameTime();
        showScreen(GameScreens.help);
    }

    public void updateGameTime() {
        if (gameTime == 0) {
            gameStatus = Status.STOP;
            gameCompleted();
            lblTime.setForeground(fg);
        }
        if (gameTime <= AppConstants.ALARM_TIME_SEC && gameTime > 0) {
            lblTime.setForeground(lblTime.getForeground() == fg ? Color.red : fg);
        }
        lblTime.setText(UIName.LBL_TIME.name + SPACE + Utils.formatTime(gameTime--));
    }

    public boolean isGameRunning() {
        return isGameStart();
    }

    public boolean isGameStart() {
        return gameStatus == Status.START;
    }

    private void createBorders() {
        Arrays.stream(componentsToColor).forEach(c -> c.setBorder(SwingUtils.createLineBorder(bg)));
    }

    private void storeAndLoad() {
        // reload tooltips for new data
        loadTableData();
    }

    private void startGame() {
        if (!isGameRunning()) {
            btnStart.setText("Stop");
            startNewGame();
            disableControls();
        } else {
            btnStart.setText(UIName.BTN_START.name);
            stopGame();
            enableControls();
        }
    }

    /**
     * Exit the Application
     */
    private void exitForm() {
        stopGame();
        saveGameInHistory();
        cancelTimers();
        configs.saveConfig(this);
        setVisible(false);
        dispose();
        logger.dispose();
        System.exit(0);
    }

    private void saveGameInHistory() {
        if (gameDetail != null) {
            logger.info("Saving game in history as " + gameDetail.detail());
            gameHistory.put((gameHistory.size() + 1) + "", gameDetail);
            if (gameHistory.size() > HISTORY_LIMIT) {
                gameHistory.remove(0);
            }
            Properties prop = new Properties();
            // re-indexing history
            AtomicInteger idx = new AtomicInteger(1);
            gameHistory.forEach((k, v) ->
                    prop.setProperty(idx.getAndIncrement() + "", prepareScoreCsv(v)));
            Utils.saveProperties(prop, AppPaths.scoresLoc.val, logger);
        }
    }

    private String prepareScoreCsv(GameDetail gameDetail) {
        StringBuilder sb = new StringBuilder();
        sb.append(gameDetail.getTableFrom())
                .append(AppConstants.GAME_DATA_SEP)
                .append(gameDetail.getTableTo())
                .append(AppConstants.GAME_DATA_SEP)
                .append(gameDetail.getTotalQuestions())
                .append(AppConstants.GAME_DATA_SEP)
                .append(gameDetail.getDate())
                .append(AppConstants.GAME_DATA_SEP);

        gameDetail.getQuesAns().forEach(qa ->
                sb.append(qa.getIdx())
                        .append(AppConstants.QA_DATA_SEP)
                        .append(qa.getNum1())
                        .append(AppConstants.QA_DATA_SEP)
                        .append(qa.getNum2())
                        .append(AppConstants.QA_DATA_SEP)
                        .append(qa.getUserAns())
                        .append(AppConstants.QA_SEP));
        return sb.toString();
    }

    private void cancelTimers() {
        TIMERS.forEach(Timer::cancel);
    }

    private void setControlsToEnable() {
        Component[] components = {menuBar, menu, cbTblFrom, cbTblTo, cbQuestions};
        setComponentToEnable(components);
        enableControls();
    }

    private void addBindings() {
        final JComponent[] addBindingsTo = {};
        addKeyBindings(addBindingsTo);
    }

    private void addKeyBindings(JComponent[] addBindingsTo) {
        Action actionTxtSearch = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            }
        };

        List<KeyActionDetails> keyActionDetails = new ArrayList<>();
        keyActionDetails.add(new KeyActionDetails(KS_CTRL_F, actionTxtSearch));

        SwingUtils.addKeyBindings(addBindingsTo, keyActionDetails);
    }

    public String getAppFontSize() {
        return appFontSize + "";
    }

    public String getCNFIdx() {
        return cnfIdx + "";
    }

    public String getTablesFrom() {
        return tableFrom + "";
    }

    public String getTablesTo() {
        return tableTo + "";
    }

    public String getTotalQuestions() {
        return totalQuestions + "";
    }
}
