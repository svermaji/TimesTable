package com.sv.timestable;

import com.sv.core.Constants;
import com.sv.core.Utils;
import com.sv.core.config.DefaultConfigs;
import com.sv.timestable.task.AppFontChangerTask;
import com.sv.timestable.task.GameTimerTask;
import com.sv.timestable.task.GameCompletedTask;
import com.sv.timestable.task.WaitTimerTask;
import com.sv.swingui.KeyActionDetails;
import com.sv.swingui.SwingUtils;
import com.sv.swingui.UIConstants;
import com.sv.swingui.component.*;
import com.sv.swingui.component.table.AppTable;
import com.sv.swingui.component.table.AppTableHeaderToolTip;
import com.sv.swingui.component.table.CellRendererCenterAlign;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sv.core.Constants.*;
import static com.sv.swingui.UIConstants.*;
import static java.util.stream.Collectors.toMap;

/**
 * Java Game as MatchPair
 */
public class TimesTable extends AppFrame {

    /**
     * This is config and program will search getter
     * of each enum to store in config file.
     * <p>
     * e.g. if enum is Xyz then when storing getXyz will be called
     */
    public enum Configs {
        AppFontSize, CNFIdx, TablesRange, TimeRange, TotalQuestions, TimeOrCount
    }

    public enum Status {
        NOT_STARTED, START, PAUSED, AUTO_PAUSED, STOP
    }

    private DefaultConfigs configs;
    private Map<String, GameInfo> gameInfos;
    private Map<String, GameScores> gameScores;
    private final JPopupMenu tblUsersPopupMenu = new JPopupMenu();
    private AppMenuItem tblUserMISetUser, tblUserMIDelUser;
    private TitledBorder titledBorder;
    private JMenuBar menuBar;
    private AppMenu menu;
    private JTextPane tpHelp;
    private AppButton btnStart, btnUser, btnPause, btnHistory, btnExit;
    private AppTextField txtUser;
    private AppLabel lblTime, lblWaitTime, lblScore, lblLevel;
    private AppTable tblTopScore, tblRecentScore, tblUsers;
    private DefaultTableModel topScoreModel, recentScoreModel, userModel;
    private AppPanel topPanel, centerPanel, buttonsPanel, btnsPanel,
            waitPanel, historyPanel, waitLblsPanel, userPanel, tblPanel, helpPanel;
    private LineGraphPanel graphPanel;
    private JScrollPane jspHelp;
    private JSplitPane splitPane;
    private JComponent[] componentsToColor;
    private List<JComponent> commonScreens;
    private GameInfo gameInfo;

    private Status gameStatus = Status.NOT_STARTED;
    private boolean noUserPwd;
    private String username, fontName;
    private int gameLevel = 1, gameScore, gameAccuracy, cnfIdx = 0, gameBtnFontSize,
            totalCorrectPairs, totalWrongPairs;

    private final List<Timer> TIMERS = new ArrayList<>();
    private final ColorsNFonts[] APP_COLORS = SwingUtils.getFilteredCnF(false);
    private final CellRendererCenterAlign CENTER_RENDERER = new CellRendererCenterAlign();
    private final String TITLE_HEADING = "Controls";
    private final int MAX_NAME = 12;

    private static int gamePairMatched = 0;
    private static int gameTime = 0, gameWaitTime = 0;

    private int[][] gameSequences;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new com.sv.matchpair.MathPractise().initComponents());
    }

    public TimesTable() {
        super("Match Pair");
    }

    /**
     * This method initializes the form.
     */
    private void initComponents() {
        configs = new DefaultConfigs(logger, Utils.getConfigsAsArr(Configs.class));
        gameInfos = new ConcurrentHashMap<>();
        gameScores = new ConcurrentHashMap<>();
        gamePairs = new ConcurrentHashMap<>();
        loadGameSequences();
        loadConfigValues();
        loadGameConfigs();
        loadGameScores();
        logger.setSimpleClassName(true);
        setEchoChar('*');

        CENTER_RENDERER.setShowSameTipOnRow(true);

        List<WindowChecks> windowChecks = new ArrayList<>();
        windowChecks.add(WindowChecks.WINDOW_ACTIVE);
        applyWindowActiveCheck(windowChecks.toArray(new WindowChecks[0]));

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
        UIName uin = UIName.BTN_USER;
        btnUser = new AppButton(uin.name + Constants.SPACE + username, uin.mnemonic, uin.tip);
        btnUser.addActionListener(e -> changeUsername());
        uin = UIName.LBL_USER;
        txtUser = new AppTextField(username, 10, new String[]{});
        txtUser.setToolTipText(uin.tip);
        txtUser.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    saveUsername();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    doNotSaveUsername();
                }
            }
        });
        txtUser.setVisible(false);
        uin = UIName.BTN_LEVEL;
        lblLevel = new AppLabel(uin.name, uin.mnemonic, uin.tip);
        updateLevel();
        uin = UIName.BTN_START;
        btnStart = new AppButton(uin.name, uin.mnemonic, uin.tip);
        btnStart.addActionListener(e -> startGame());
        uin = UIName.BTN_PAUSE;
        btnPause = new AppButton(uin.name, uin.mnemonic, uin.tip);
        btnPause.addActionListener(e -> pauseGame());
        uin = UIName.LBL_TIME;
        lblTime = new AppLabel(uin.name, uin.mnemonic, uin.tip);
        uin = UIName.LBL_SCORE;
        lblScore = new AppLabel(uin.name, uin.mnemonic, uin.tip);
        updateScore();
        uin = UIName.BTN_HISTORY;
        btnHistory = new AppButton(uin.name, uin.mnemonic, uin.tip);
        btnHistory.addActionListener(e -> showHistory());

        menuBar = new JMenuBar();
        userPanel = new AppPanel();
        userPanel.setLayout(new GridLayout(1, 1));
        userPanel.add(btnUser);
        AppToolBar tbControls = new AppToolBar();
        tbControls.add(userPanel);
        tbControls.add(btnStart);
        tbControls.add(btnPause);
        tbControls.add(lblLevel);
        tbControls.add(lblTime);
        tbControls.add(lblScore);
        tbControls.add(btnHistory);
        tbControls.add(menuBar);
        tbControls.add(btnExit);
        tbControls.setLayout(new GridLayout(1, tbControls.getComponentCount()));
        tbControls.setMargin(new Insets(1, 3, 1, 3));
        topPanel.add(tbControls);
        topPanel.setSize(topPanel.getWidth(), 100);
        topPanel.setBorder(SwingUtils.createLineBorder(Color.BLUE));

        centerPanel = new AppPanel(new BorderLayout());
        buttonsPanel = new AppPanel(new BorderLayout());

        helpPanel = new AppPanel(new BorderLayout());
        helpPanel.setName("Help Panel");
        waitPanel = new AppPanel(new BorderLayout());
        waitPanel.setName("Wait Panel");
        historyPanel = new AppPanel(new BorderLayout());
        historyPanel.setName("History Panel");
        graphPanel = new LineGraphPanel(logger);
        graphPanel.setMargin(100);
        graphPanel.setyAxisGap(300);
        graphPanel.setLineJoinsPointsCenter(configs.getBooleanConfig(Configs.LineJoinsPointsCenter.name()));
        graphPanel.setFirstPointOnBaseLine(configs.getBooleanConfig(Configs.FirstPointOnBaseLine.name()));
        graphPanel.setDrawBaseLines(configs.getBooleanConfig(Configs.DrawBaseLines.name()));
        historyPanel.add(graphPanel);
        btnsPanel = new AppPanel(new BorderLayout());
        btnsPanel.setName("Btns Panel");

        prepareWaitScreen();
        setupHelp();
        setAllTables();

        buttonsPanel.add(helpPanel);
        buttonsPanel.add(waitPanel);
        buttonsPanel.add(btnsPanel);
        buttonsPanel.add(historyPanel);

        AppLabel[] lbls = {lblLevel, lblWaitTime, lblTime, lblScore};
        Arrays.stream(lbls).forEach(l -> l.setHorizontalAlignment(SwingConstants.CENTER));
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tblPanel, buttonsPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setResizeWeight(0.01);
        centerPanel.add(splitPane, BorderLayout.CENTER);
        parentContainer.add(topPanel, BorderLayout.NORTH);
        parentContainer.add(centerPanel, BorderLayout.CENTER);

        maximiseWin();
        enableControls();
        SwingUtils.getInFocus(btnStart);
        if (!Utils.hasValue(username)) {
            username = ADMIN_UN;
        }
        if (gameScores.isEmpty()) {
            storeAndLoad();
        }

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
        menu.add(SwingUtils.getLineGraphMenu(this, graphPanel, logger));
        menu.addSeparator();
        uin = UIName.MI_NOUSERPWD;
        AppCheckBoxMenuItem miNoUserPwd = new AppCheckBoxMenuItem(uin.name, noUserPwd, uin.mnemonic, uin.tip);
        menu.add(miNoUserPwd);
        miNoUserPwd.addActionListener(e -> changeNoUserPwd(miNoUserPwd.isSelected()));
        uin = UIName.MI_CHANGEPWD;
        AppMenuItem miChangePwd = new AppMenuItem(uin.name, uin.mnemonic, uin.tip);
        menu.add(miChangePwd);
        miChangePwd.addActionListener(e -> showChangePwdScreen());
        uin = UIName.MI_CHANGEMYPWD;
        AppMenuItem miChangeMyPwd = new AppMenuItem(uin.name, uin.mnemonic, uin.tip);
        menu.add(miChangeMyPwd);
        miChangeMyPwd.addActionListener(e -> showChangePwdScreen(username, true));
        menu.addSeparator();
        uin = UIName.MI_PAIR_TYPES;
        AppMenu miPT = new AppMenu(uin.name, uin.mnemonic, uin.tip);
        uin = UIName.MI_PT_CHARS;
        AppRadioButtonMenuItem miChars = new AppRadioButtonMenuItem(
                uin.name, isPairType(PairType.Chars), uin.mnemonic, uin.tip);
        miChars.addActionListener(e -> changePairType(PairType.Chars));
        miPT.add(miChars);
        uin = UIName.MI_PT_SYMBOLS;
        AppRadioButtonMenuItem miSymbols = new AppRadioButtonMenuItem(
                uin.name, isPairType(PairType.Symbols), uin.mnemonic, uin.tip);
        miSymbols.addActionListener(e -> changePairType(PairType.Symbols));
        miPT.add(miSymbols);
        uin = UIName.MI_PT_SMILEYS;
        AppRadioButtonMenuItem miSmileys = new AppRadioButtonMenuItem(
                uin.name, isPairType(PairType.Smileys), uin.mnemonic, uin.tip);
        miSmileys.addActionListener(e -> changePairType(PairType.Smileys));
        miPT.add(miSmileys);
        ButtonGroup bg = new ButtonGroup();
        bg.add(miChars);
        bg.add(miSymbols);
        bg.add(miSmileys);
        menu.add(miPT);
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

        componentsToColor = new JComponent[]{btnUser, txtUser, btnStart, btnPause, btnHistory, lblLevel, lblTime, lblScore,
                menuBar, menu, btnExit, tblTopScore.getTableHeader(), tblRecentScore.getTableHeader(),
                tblUsers.getTableHeader()
        };
        commonScreens = new ArrayList();
        commonScreens.add(helpPanel);
        commonScreens.add(waitPanel);
        commonScreens.add(historyPanel);
        commonScreens.add(btnsPanel);
        colorChange(cnfIdx);
        setControlsToEnable();
        addBindings();
        updateUNAutoComplete();
        showHelp();
        if (isSingleUser()) {
            setUsername(username);
        }

        new Timer().schedule(new AppFontChangerTask(this), SEC_1);
    }

    private void changeNoUserPwd(boolean val) {
        usernameForPwd = ADMIN_UN;
        Map<String, String> params = new ConcurrentHashMap<>();
        params.put(AppConstants.PRM_NAME_ACTION, AppConstants.PRM_VAL_NOPWD);
        params.put(AppConstants.PRM_NOPWD_ACTION, val + EMPTY);
        setAuthenticationParams(params);
        showLockScreen();
    }

    private void changeNoUserPwdAction(boolean val) {
        noUserPwd = val;
        if (noUserPwd) {
            gameScores.values().forEach(g -> deleteUserSecretFile(g.getUsername()));
        }
    }

    private boolean isCharsPairType() {
        return pairType == PairType.Chars;
    }

    private boolean isPairType(PairType pt) {
        return pairType == pt;
    }

    private void changePairType(PairType pt) {
        pairType = pt;
        updateLevel();
    }

    private void showHistory() {
        if (graphPanel != null) {
            graphPanel.setData(prepareGraphData(getUserRecentScores()));
            setGraphColors();
            graphPanel.setToolTipColorsNFont(fg, bg, SwingUtils.getNewFont(getFont(), fontName));
            showScreen(GameScreens.history);
        }
    }

    private void setGraphColors() {
        if (graphPanel != null) {
            Font f = SwingUtils.getPlainNewFont(fontName, appFontSize);
            graphPanel.setGraphFont(f);
            graphPanel.setLineWidth(AppConstants.GRAPH_LINE_WIDTH);
            graphPanel.setPointWidth(AppConstants.GRAPH_POINT_WIDTH);
            graphPanel.setFontColor(fg);
            graphPanel.setPointColor(fg);
            graphPanel.setLineColor(bg);
            graphPanel.setToolTipColorsNFont(fg, bg, f);
        }
    }

    private List<LineGraphPanelData> prepareGraphData(List<GameScore> scores) {
        List<LineGraphPanelData> data = new ArrayList<>();
        List<GameScore> scoresForGraph = new ArrayList<>();
        scores.stream().limit(AppConstants.GRAPH_POINTS_TO_DRAW_LIMIT).forEach(scoresForGraph::add);
        Collections.reverse(scoresForGraph);
        // returning last GRAPH_POINT_LIMIT only
        scoresForGraph.stream().limit(AppConstants.GRAPH_POINTS_TO_DRAW_LIMIT).forEach(s -> {
            data.add(new LineGraphPanelData(s.getScoreAsInt(), s.shortString(), true, false));
        });
        return data;
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
                toShow = btnsPanel;
                break;
            case "wait":
            case "wrong":
                toShow = waitPanel;
                break;
            case "history":
                toShow = historyPanel;
                break;
        }
        AppPanel finalToShow = toShow;
        commonScreens.forEach(c -> {
            if (null != c) {
                if (finalToShow == null || !c.getName().equals(finalToShow.getName())) {
                    c.setVisible(false);
                    buttonsPanel.remove(c);
                }
            }
        });
        if (toShow != null) {
            toShow.setVisible(true);
            buttonsPanel.add(toShow, BorderLayout.CENTER);
        }
        SwingUtils.updateUIFor(buttonsPanel);
        logger.info("Showing screen for " + Utils.addBraces(nm));
    }

    // This method will decide which panel to show
    private boolean isScreenVisible(GameScreens gs) {
        AppPanel toShow = null;
        String nm = gs.name();
        switch (nm) {
            case "help":
                toShow = helpPanel;
                break;
            case "game":
                toShow = btnsPanel;
                break;
            case "wait":
            case "wrong":
                toShow = waitPanel;
                break;
            case "history":
                toShow = historyPanel;
                break;
        }

        boolean result = toShow != null && toShow.isVisible();
        //logger.info("isScreenVisible screen for " + Utils.addBraces(nm) + " is " + Utils.addBraces(result));
        return result;
    }

    private void showHelpInBrowser() {
        new RunCommand(new String[]{AppPaths.openHelpLoc.val + SPACE + Utils.getCurrentDir()}, logger);
    }

    private void showHelp() {
        showScreen(GameScreens.help);
    }

    private void prepareWaitScreen() {
        lblWaitTime = new AppLabel();
        BoxLayout boxlayout = new BoxLayout(waitPanel, BoxLayout.PAGE_AXIS);
        waitPanel.setLayout(boxlayout);
        int r = 3, c = 3;
        waitLblsPanel = new AppPanel(new GridLayout(r, c));
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                waitLblsPanel.add((i == 1 && j == 1) ? lblWaitTime : new AppLabel());
            }
        }
        waitPanel.add(waitLblsPanel);
        lblWaitTime.setBorder(EMPTY_BORDER);
        lblWaitTime.setHorizontalAlignment(SwingConstants.CENTER);
        lblWaitTime.setVerticalAlignment(SwingConstants.CENTER);
        lblWaitTime.setPreferredSize(waitPanel.getPreferredSize());
    }

    public void appWindowGainedFocus() {
        logger.info("window focus gained");
        if (gameStatus == Status.AUTO_PAUSED) {
            // as we are calling pauseGame this will start the game
            gameStatus = Status.PAUSED;
            pauseGame();
        }
    }

    public void appWindowLostFocus() {
        logger.info("window focus lost");
        if (gameStatus == Status.START) {
            gameStatus = Status.AUTO_PAUSED;
            pauseGame();
        }
    }

    // call by reflection
    public void dblClickUser(AppTable table, Object[] params) {
        int selRow = table.getSelectedRow();
        String un = table.getValueAt(selRow, 1).toString();
        setUsername(un);
    }

    private void setAllTables() {
        String[] topScoreCols = new String[]{"Top Score", "Date"};
        String[] recentScoreCols = new String[]{"Recent Score", "Date"};
        String[] userCols = new String[]{"#", "User", "Top Score"};
        String[] userColsTT = new String[]{"#", "Set user by double click or right click", "Top Score"};

        topScoreModel = SwingUtils.getTableModel(topScoreCols);
        recentScoreModel = SwingUtils.getTableModel(recentScoreCols);
        userModel = SwingUtils.getTableModel(userCols);

        tblTopScore = new AppTable(topScoreModel);
        tblRecentScore = new AppTable(recentScoreModel);
        tblUsers = new AppTable(userModel);

        tblTopScore.setTableHeader(new AppTableHeaderToolTip(tblTopScore.getColumnModel(), topScoreCols));
        tblRecentScore.setTableHeader(new AppTableHeaderToolTip(tblRecentScore.getColumnModel(), recentScoreCols));
        tblUsers.setTableHeader(new AppTableHeaderToolTip(tblUsers.getColumnModel(), userColsTT));
        tblUsers.addDblClickOnRow(this, new Object[]{}, "dblClickUser");

        UIName uin = UIName.MI_SETUSER;
        tblUserMISetUser = new AppMenuItem(uin.name, uin.mnemonic);
        tblUserMISetUser.addActionListener(evt -> setUsernameFromUser());
        uin = UIName.MI_DELUSER;
        tblUserMIDelUser = new AppMenuItem(uin.name, uin.mnemonic);
        tblUserMIDelUser.addActionListener(evt -> {
            usernameForPwd = ADMIN_UN;
            Map<String, String> params = new ConcurrentHashMap<>();
            params.put(AppConstants.PRM_NAME_ACTION, AppConstants.PRM_VAL_DELUSER);
            setAuthenticationParams(params);
            showLockScreen();
        });
        tblUsersPopupMenu.add(tblUserMISetUser);
        tblUsersPopupMenu.add(tblUserMIDelUser);
        // sets the popup menu for the table
        tblUsers.setComponentPopupMenu(tblUsersPopupMenu);
        tblUsers.setOpaque(true);
        tblUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    tblUsers.clearSelection();
                    int row = tblUsers.rowAtPoint(e.getPoint());
                    tblUsers.addRowSelectionInterval(row, row);
                }
            }
        });

        setTable(tblTopScore, topScoreModel);
        setTable(tblRecentScore, recentScoreModel);
        setTable(tblUsers, userModel);
        loadTableData();

        tblPanel = new AppPanel(new GridLayout(3, 1));
        tblPanel.add(new JScrollPane(tblTopScore));
        tblPanel.add(new JScrollPane(tblRecentScore));
        tblPanel.add(new JScrollPane(tblUsers));
        tblPanel.setBorder(EMPTY_BORDER);
    }

    public void authenticationSuccess(Map<String, String> params) {
        logger.info("Params in authenticationSuccess " + params);
        if (isAdminUser(params.get(PRM_UN))) {
            if (params.get(AppConstants.PRM_NAME_ACTION).equals(AppConstants.PRM_VAL_DELUSER)) {
                deleteUser();
            } else if (params.get(AppConstants.PRM_NAME_ACTION).equals(AppConstants.PRM_VAL_NOPWD)) {
                changeNoUserPwdAction(Utils.getBoolean(params.get(AppConstants.PRM_NOPWD_ACTION), false));
            }
        } else {
            setUsernameInApp();
        }
    }

    public void pwdChangedStatus(boolean pwdChanged, Map<String, String> params) {
        logger.info("password change status [" + pwdChanged + "]");
        if (pwdChanged && !isAdminUser(params.get(PRM_UN))) {
            setUsernameInApp();
        }
    }

    private void deleteUser() {
        String un = tblUsers.getValueAt(tblUsers.getSelectedRow(), 1).toString();
        logger.info("Deleting user " + Utils.addBraces(un));
        if (Utils.hasValue(un)) {
            deleteUserSecretFile(un);
            removeFromGameScores(un);
        }
    }

    public void escOnchangePwdScreen() {
        doNotSaveUsername();
    }

    private void removeFromGameScores(String k) {
        if (gameScores.containsKey(getUsernameForMap(k))) {
            gameScores.remove(getUsernameForMap(k));
            String firstKey = (String) gameScores.keySet().toArray()[0];
            setUsername(gameScores.get(firstKey).getUsername());
            loadTableData();
        }
    }

    private boolean isSingleUser() {
        return gameScores.size() == 1;
    }

    private void loadTableData() {
        GameScores gs = getUserGameScores();
        if (gs != null) {
            populateScoreTbl(gs.getTopScores(), topScoreModel, tblTopScore);
            populateScoreTbl(gs.getRecentScores(), recentScoreModel, tblRecentScore);
        }
        populateUsersTopScore(userModel);
    }

    private void populateScoreTbl(List<GameScore> list, DefaultTableModel model, AppTable tbl) {
        // empty
        model.setRowCount(0);
        tbl.emptyRowTooltips();
        int sz = list.size();
        for (int i = 0; i < sz; i++) {
            if (i < AppConstants.DEFAULT_TABLE_ROWS - 1) {
                GameScore gs = list.get(i);
                model.addRow(new String[]{gs.getScore(), gs.getDate()});
                tbl.addRowTooltip(new String[]{gs.shortString()});
            }
        }
        if (AppConstants.DEFAULT_TABLE_ROWS > sz) {
            int n = AppConstants.DEFAULT_TABLE_ROWS - sz;
            SwingUtils.createEmptyRows(model.getColumnCount(), n, model);
        }
    }

    private void populateUsersTopScore(DefaultTableModel model) {
        // empty
        model.setRowCount(0);
        Map<String, GameScores> topScores = new ConcurrentHashMap<>();
        for (GameScores v : gameScores.values()) {
            topScores.put(v.getUsername(), v);
        }

        Map<String, GameScores> sorted = topScores.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(
                        Comparator.comparingInt(GameScores::getTopScore))))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        // crown colors
        String[] cc = {"#FDD017", "#C0C0C0", "#CD7F32", "black"};
        int CCLimit = 3;
        AtomicInteger a = new AtomicInteger();
        sorted.forEach((k, v) -> {
            model.addRow(new String[]{
                            "<html><span color='" + cc[a.get()] + "'>&#x1F451;</span></html>",
                            v.getUsername(),
                            v.getTopScore() + ""
                    }
            );
            if (a.intValue() < CCLimit) {
                a.getAndIncrement();
            }
        });
        tblUsers.getColumnModel().getColumn(0).setMaxWidth((int) (appFontSize * 1.5));
    }

    public List<GameButton> prepareGameButtons(GameInfo gi) {
        Integer[] seq = AppUtils.getRandomGameSeq(gi, gameSequences);
        logger.info("Sequence selected as " + Arrays.toString(seq));

        int total = gi.getRows() * gi.getCols();
        List<GameButton> list = new ArrayList<>(total);
        List<String> textList = new ArrayList<>(total);
        int elem = total - AppConstants.PAIRS_COUNT;
        Random rand = new Random();
        String[] arr = getGameChars();
        int arrayLen = arr.length;
        logger.info("Shuffling from [" + arrayLen + "] characters of type [" + pairType.name() + "].");
        while (textList.size() < elem) {
            String s = arr[rand.nextInt(arrayLen)];
            if (!textList.contains(s)) {
                textList.add(s);
            }
        }
        int x = 0;
        // if sequence is GAME_SEQ_LIMIT_MAX then make two colors in same
        for (int i = 0; i < AppConstants.PAIRS_COUNT; i++) {
            int seqE = seq[i];
            if (seqE < AppConstants.GAME_SEQ_LIMIT_MIN) {
                i--;
            } else {
                textList.add(x, textList.get(x));
                gamePairs.put(textList.get(x), new ArrayList<>());
                if (seqE > AppConstants.GAME_SEQ_LIMIT_MAX && i < AppConstants.PAIRS_COUNT - 1) {
                    textList.add(x + 2, textList.get(x + 2));
                    i++;
                    gamePairs.put(textList.get(x + 2), new ArrayList<>());
                }
                x += seqE;
            }
        }
        AtomicInteger k = new AtomicInteger();
        AtomicInteger t = new AtomicInteger();
        Color[] colors = gi.getColorsRandomly();
        boolean charsPT = isCharsPairType();
        // first 3 element in sequence must be > 2
        Arrays.stream(seq).forEach(i -> {
            for (int j = 0; j < i; j++) {
                String textCode = textList.get(t.getAndIncrement());
                String text = textCode;
                if (!charsPT) {
                    text = Utils.getUnicodeStr(textCode);
                }
                list.add(new GameButton(text, textCode, colors[k.intValue()], this));
            }
            k.getAndIncrement();
        });
        return list;
    }

    private String[] getGameChars() {
        String[] arr = AppConstants.GAME_CHARS;
        switch (pairType) {
            case Symbols:
                arr = AppConstants.GAME_SYMBOLS;
                break;
            case Smileys:
                arr = AppConstants.GAME_SMILEYS;
                break;
        }
        return arr;
    }

    public synchronized void checkGameButton(GameButton gb) {
        if (isGameRunning()) {
            if (AppUtils.lastButton != null && !AppUtils.lastButton.isClicked()) {
                AppUtils.lastButton = null;
                gb = null;
            }
            if (AppUtils.lastButton != null && AppUtils.lastButton.isClicked() && gb.isClicked()
                    && AppUtils.lastButton.getText().equals(gb.getText())) {
                gb.setVisible(false);
                AppUtils.lastButton.setVisible(false);
                AppUtils.lastButton = null;
                gb = null;
                gamePairMatched++;
                gameScore += gameInfo.getMatchScore();
                updateScore();
                totalCorrectPairs++;
                if (gamePairMatched == AppConstants.PAIRS_COUNT) {
                    logger.info("Matches done for user [" + username + "], game level [" + gameLevel + "], " +
                            "game score [" + gameScore + "]");
                    gameLevel++;
                    updateLevel();
                    gamePairMatched = 0;
                    createButtons();
                }
            } else {
                if (AppUtils.lastButton != null && AppUtils.lastButton.isClicked() && gb.isClicked()
                        && !(AppUtils.lastButton.getText().equals(gb.getText()))) {
                    logger.warn("Wrong match by user [" + username + "], game level [" + gameLevel + "], " +
                            "game score [" + gameScore + "], " +
                            "button 1 [" + AppUtils.lastButton + "] and button 2 [" + gb + "]");
                    AppUtils.lastButton = null;
                    gb = null;
                    gamePairMatched = 0;
                    totalWrongPairs++;
                    showWrongMatchScreen();
                }
            }
            if (AppUtils.lastButton == null && gb != null) {
                AppUtils.lastButton = gb;
            }
        }
    }

    private void showWrongMatchScreen() {
        setWaitScreen();
        lblWaitTime.setBackground(Color.red);
        lblWaitTime.setForeground(Color.white);
        lblWaitTime.setText("Wrong");
        showScreen(GameScreens.wrong);
        // this wont impact by cancelTimers bcoz gameTime not changed
        gameWaitTime = 0;
        Timer t = new Timer();
        t.scheduleAtFixedRate(new WaitTimerTask(this), AppConstants.WRONG_PAIR_MSG_TIME, SEC_1);
        TIMERS.add(t);
    }

    private void updateLevel() {
        lblLevel.setText(UIName.BTN_LEVEL.name + SPACE + gameLevel + SPACE + pairType.name());
    }

    private void updateScore() {
        lblScore.setText(UIName.LBL_SCORE.name + SPACE + gameScore);
    }

    private void maximiseWin() {
        setToCenter();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private void loadGameScores() {
        Properties props = Utils.readPropertyFile(AppPaths.scoresLoc.val, logger);
        props.stringPropertyNames().forEach(k -> {
            if (k.endsWith(AppConstants.PROP_SCORES_SUFFIX)) {
                String v = props.getProperty(k);
                String user = getUserFromProp(k);
                if (gameScores.containsKey(getUsernameForMap(user))) {
                    logger.warn("Duplicate file found for user " + Utils.addBraces(user));
                } else {
                    gameScores.put(getUsernameForMap(user), getGameScores(user, v));
                }
            }
        });
        //logger.info("All players scores = " + gameScores);
    }

    private GameScores getGameScores(String user, String gameScoreCsv) {
        return new GameScores(user, processScores(gameScoreCsv));
    }

    private List<GameScore> getUserTopScores() {
        return getUserGameScores().getTopScores();
    }

    private List<GameScore> getUserRecentScores() {
        return getUserGameScores().getRecentScores();
    }

    private GameScores getUserGameScores() {
        return gameScores.get(getUsernameForMap());
    }

    private int getUserTopScore() {
        return getUserGameScores().getTopScore();
    }

    private List<GameScore> processScores(String scoreStr) {
        List<GameScore> list = new ArrayList<>();
        String[] scoreDate = scoreStr.split(AppConstants.SCORE_SEP);
        Arrays.stream(scoreDate).forEach(sd -> {
            if (Utils.hasValue(sd)) {
                String[] arr = sd.split(AppConstants.SCORE_DATA_SEP_FOR_SPLIT);
                list.add(new GameScore(arr[0], arr[1], arr[2], arr[3], arr[4]));
            }
        });
        return list;
    }

    private String getUserFromProp(String k) {
        if (k.contains(Constants.DASH)) {
            k = k.substring(0, k.indexOf(Constants.DASH));
        }
        return k;
    }

    private void loadGameSequences() {
        List<String> lines = Utils.readFile(AppPaths.gameSeqLoc.val, logger);
        gameSequences = new int[lines.size()][];
        AtomicInteger a = new AtomicInteger();
        lines.forEach(l -> gameSequences[a.getAndIncrement()] =
                Arrays.stream(l.split(COMMA)).mapToInt(Utils::convertToInt).toArray());
        logger.info("Game sequences load as " + Arrays.deepToString(gameSequences));
    }

    private void loadGameConfigs() {
        List<String> paths = Utils.listFiles(AppPaths.gameConfigsLoc.val, logger);
        paths.forEach(p -> {
            GameInfo gi = makeGameInfoObj(Utils.readPropertyFile(p, logger));
            gameInfos.put(gi.getGameLevel(), gi);
        });
        logger.info("Game configs load as " + gameInfos);
    }

    private GameInfo makeGameInfoObj(Properties props) {
        GameInfo gameInfo = new GameInfo();
        gameInfo.setGameLevel(props.getProperty("game-level"));
        gameInfo.setMatchScore(Utils.convertToInt(props.getProperty("match-score")));
        gameInfo.setRows(Utils.convertToInt(props.getProperty("rows")));
        gameInfo.setCols(Utils.convertToInt(props.getProperty("cols")));
        List<String> colorProps = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        props.stringPropertyNames().forEach(p -> {
            if (p.startsWith("color-")) {
                colorProps.add(props.getProperty(p));
            }
        });
        // should be in format <R,G,B>
        colorProps.forEach(cs -> {
            String[] arr = cs.split(Constants.COMMA);
            if (arr.length == 3) {
                colors.add(new Color(Utils.convertToInt(arr[0]),
                        Utils.convertToInt(arr[1]), Utils.convertToInt(arr[2])));
            } else {
                colors.add(AppUtils.getColor(cs));
            }
        });
        gameInfo.setColors(colors.toArray(new Color[0]));
        return gameInfo;
    }

    private void loadConfigValues() {
        cnfIdx = configs.getIntConfig(Configs.CNFIdx.name());
        appFontSize = configs.getIntConfig(Configs.AppFontSize.name());
        username = configs.getConfig(Configs.Username.name());
        if (!Utils.hasValue(username) || !isSingleUser()) {
            username = "default";
        }
        gameBtnFontSize = configs.getIntConfig(Configs.GameBtnFontSize.name());
        String ptCfg = configs.getConfig(Configs.PairType.name());
        if (!Utils.hasValue(ptCfg)) {
            ptCfg = PairType.Chars.name();
        }
        pairType = PairType.valueOf(ptCfg);
        noUserPwd = configs.getBooleanConfig(Configs.NoUserPwd.name());
            logger.info("All configs: cnfIdx [" + cnfIdx +
                    "], appFontSize [" + appFontSize +
                    "], gameBtnFontSize [" + gameBtnFontSize +
                    "], pairType [" + ptCfg +
                    "], noUserPwd [" + noUserPwd +
                    "], username " + Utils.addBraces(username));
    }

    private void setTable(AppTable tbl, DefaultTableModel model) {
        tbl.setScrollProps();
        tbl.setRowHeight(appFontSize + 4);
        tbl.setBorder(EMPTY_BORDER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            tbl.getColumnModel().getColumn(i).setCellRenderer(CENTER_RENDERER);
        }
    }

    public void changeGameBtnFont() {
        SwingUtils.changeFont(btnsPanel, gameBtnFontSize);
    }

    public void changeAppFont() {
        SwingUtils.applyAppFont(this, appFontSize, this, logger);
        changeGameBtnFont();
        SwingUtils.changeFont(txtUser, appFontSize);
    }

    // This will be called by reflection from SwingUI jar
    public void appFontChanged(Integer fs) {
        appFontSize = fs;
        logger.info("Application font changed to " + Utils.addBraces(fs));

        SwingUtils.changeFont(tblUserMISetUser, appFontSize);
        SwingUtils.changeFont(tblUserMIDelUser, appFontSize);
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

        Arrays.stream(componentsToColor).forEach(c -> c.setBorder(SwingUtils.createLineBorder(fg)));
        SwingUtils.setComponentColor(componentsToColor, bg, fg, hbg, hfg);
        // to make exit button different
        SwingUtils.setComponentColor(btnExit, hbg, hfg, bg, fg);
        Arrays.stream(componentsToColor).forEach(c ->
                SwingUtils.applyTooltipColorNFont(c, bg, fg, SwingUtils.getNewFont(c.getFont(), fontName)));

        buttonsPanel.setBorder(SwingUtils.createLineBorder(hbg, 10));
        AppTable[] tbls = {tblUsers, tblTopScore, tblRecentScore};
        Arrays.stream(tbls).forEach(t -> t.setRowHeight(appFontSize + 4));
        Arrays.stream(tbls).forEach(t ->
                SwingUtils.applyTooltipColorNFontAllChild(t, fg, bg, SwingUtils.getNewFontSize(t.getFont(), appFontSize)));

        setGraphColors();
    }

    private String[] getUsernames() {
        return gameScores.values().stream().map(GameScores::getUsername).toArray(String[]::new);
    }

    private void updateUNAutoComplete() {
        txtUser.setAutoCompleteArr(getUsernames());
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
        SwingUtils.changeFont(lblWaitTime, gameBtnFontSize);
        Arrays.stream(waitLblsPanel.getComponents()).forEach(c ->
                SwingUtils.setComponentColor((JComponent) c, UIConstants.ORIG_COLOR, fg));
    }

    private void setGameScreen() {
        createButtons();
        showScreen(GameScreens.game);
        makeButtonsSquare();
        Timer t = new Timer();
        t.scheduleAtFixedRate(new GameTimerTask(this), 0, SEC_1);
        TIMERS.add(t);
    }

    private void makeButtonsSquare() {
        // setting hgap and vgap to make game 500x500
        int w = btnsPanel.getWidth();
        int h = btnsPanel.getHeight();
        int hgap = (w - AppConstants.BTNS_WIDTH) / 2;
        int vgap = (h - AppConstants.BTNS_HEIGHT) / 2;
        btnsPanel.setBorder(new EmptyBorder(new Insets(vgap, hgap, vgap, hgap)));
        logger.info("Setting hgap/vgap [" + hgap + F_SLASH + vgap
                + "] for buttons of width/height [" + w + F_SLASH + h + "]");
    }

    private void resetGame() {
        gameLevel = 1;
        gameAccuracy = 0;
        totalWrongPairs = 0;
        totalCorrectPairs = 0;
        gameWaitTime = AppConstants.GAME_WAIT_TIME_SEC;
        gameTime = AppConstants.GAME_TIME_SEC;
        gameStatus = Status.START;
        gameScore = 0;
        updateScore();
        updateLevel();
        updateGameTime();
    }

    private void createButtons() {
        gamePairMatched = 0;
        gamePairs.clear();
        gameInfo = getGameInfoFor(gameLevel);
        int rows = gameInfo.getRows();
        int cols = gameInfo.getCols();
        if (btnsPanel != null) {
            btnsPanel.removeAll();
            btnsPanel.repaint();
        }
        btnsPanel.setLayout(new GridLayout(rows, cols));

        // randomize buttons
        List<GameButton> gameBtns = prepareGameButtons(gameInfo);
        List<GameButton> gameBtnsRandomize = new ArrayList<>(gameBtns);
        Collections.shuffle(gameBtnsRandomize);
        int r = 0, c = 0;
        for (GameButton b : gameBtnsRandomize) {
            String textCode = b.getTextCode();
            if (gamePairs.containsKey(textCode)) {
                b.setGamePosition(r, c);
                gamePairs.get(textCode).add(b);
            }
            btnsPanel.add(b);
            c++;
            if (c == cols) {
                c = 0;
                r++;
            }
        }
        logger.info("Game pairs are [" + gamePairs.size() + "]. Details: " + gamePairs);
        changeGameBtnFont();
    }

    private GameInfo getGameInfoFor(int gameLevel) {
        GameInfo gi = gameInfos.containsKey(gameLevel + "") ?
                gameInfos.get(gameLevel + "") : gameInfos.get("default");
        logger.info("Returning game info for level " + Utils.addBraces(gameLevel));
        return gi;
    }

    private void gameCompleted() {
        gameAccuracy = 0;
        if (totalCorrectPairs > 0 || totalWrongPairs > 0) {
            gameAccuracy = (totalCorrectPairs * 100) / (totalCorrectPairs + totalWrongPairs);
        }
        logger.info("Game end as username [" + username +
                "], gameScore [" + gameScore +
                "], gameAccuracy [" + gameAccuracy +
                "], gameLevel [" + gameLevel +
                "], totalCorrectPairs [" + totalCorrectPairs +
                "], totalWrongPairs [" + totalWrongPairs +
                "]"
        );
        btnStart.setText(UIName.BTN_START.name);
        enableControls();
        getUserGameScores().addScore(new GameScore(gameScore, Utils.getFormattedDate(), gameAccuracy, gameLevel, pairType.name().toLowerCase()));
        // to optimize this can be save on exit but for now saving game progress on complete
        saveScores();
        loadTableData();
        gameStatus = Status.STOP;

        Arrays.stream(btnsPanel.getComponents()).forEach(c -> c.setEnabled(false));
        for (Map.Entry<String, List<GameButton>> entry : gamePairs.entrySet()) {
            List<GameButton> v = entry.getValue();
            GameButton b1 = v.get(0), b2 = v.get(1);
            if (b1.isVisible() && b2.isVisible()) {
                b1.setBackground(AppConstants.GAME_BTN_CLICK_COLOR);
                b2.setBackground(AppConstants.GAME_BTN_CLICK_COLOR);
                break;
            }
        }

        Timer t = new Timer();
        t.schedule(new GameCompletedTask(this), SEC_1 * 2);
        TIMERS.add(t);
    }

    private String getUsernameForMap(String u) {
        return u.toLowerCase();
    }

    private String getUsernameForMap() {
        return getUsernameForMap(username);
    }

    // This will be called by reflection from SwingUI jar
    public void lineGraphChanged(LineGraphPanel graphPanel, String methodName, Boolean value) {
        logger.info("lineGraphChanged: " + graphPanel);
        graphPanel.repaint();
    }

    // This will be called by reflection from SwingUI jar
    public void lineGraphFailed(LineGraphPanel graphPanel, String methodName, Boolean value) {
        logger.error("lineGraphFailed: " + graphPanel);
    }

    public void gameCompletedActions() {
        Arrays.stream(btnsPanel.getComponents()).forEach(c -> c.setEnabled(true));
        showHistory();
        cancelTimers();
    }

    private void stopGame() {
        gameStatus = Status.STOP;
        btnPause.setText(UIName.BTN_PAUSE.name);
        enableControls();
        cancelTimers();
        gameLevel = 1;
        gameTime = AppConstants.GAME_TIME_SEC;
        gameScore = 0;
        updateScore();
        updateLevel();
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
        return isGameStart() || isGamePaused();
    }

    public void performPauseAction() {
        btnPause.setForeground(btnPause.getForeground() == fg ? Color.red : fg);
    }

    public boolean isGameStart() {
        return gameStatus == Status.START;
    }

    public boolean isGamePaused() {
        return gameStatus == Status.PAUSED || gameStatus == Status.AUTO_PAUSED;
    }

    private void createBorders() {
        Arrays.stream(componentsToColor).forEach(c -> c.setBorder(SwingUtils.createLineBorder(bg)));
    }

    private void changeUsername() {
        btnUser.setVisible(false);
        txtUser.setVisible(true);
        userPanel.remove(btnUser);
        userPanel.add(txtUser);
        SwingUtils.getInFocus(txtUser);
        txtUser.selectAll();
    }

    private void setUsernameFromUser() {
        setUsername(tblUsers.getValueAt(tblUsers.getSelectedRow(), 1).toString());
    }

    private void setUsername(String un) {
        username = Utils.convertToTitleCase(un);
        if (noUserPwd) {
            usernameForPwd = ADMIN_UN;
            setUsernameInApp();
        } else {
            usernameForPwd = username;
            showLockScreen();
        }
    }

    private void setUsernameInApp() {
        btnUser.setText(UIName.BTN_USER.name + SPACE + username);
        txtUser.setText(username);
        // just to hide controls
        doNotSaveUsername();
        storeAndLoad();
        updateUNAutoComplete();
        if (isScreenVisible(GameScreens.history)) {
            // to refresh
            showHistory();
        }
    }

    private void saveUsername() {
        if (isValidName(txtUser.getText().trim())) {
            setUsername(txtUser.getText().trim());
        } else {
            getToolkit().beep();
            if (username.length() > MAX_NAME) {
                txtUser.setText("max " + MAX_NAME + " char");
            } else {
                txtUser.setText("Fill Name");
            }
        }
    }

    private void storeAndLoad() {
        if (!gameScores.containsKey(getUsernameForMap())) {
            gameScores.put(getUsernameForMap(), new GameScores(username, null));
        }
        // reload tooltips for new data
        loadTableData();
    }

    private void doNotSaveUsername() {
        btnUser.setVisible(true);
        txtUser.setVisible(false);
        userPanel.remove(txtUser);
        userPanel.add(btnUser);
    }

    private boolean isValidName(String username) {
        return Utils.hasValue(username) && username.length() < MAX_NAME;
    }

    private boolean isOkToStart() {
        if (!noUserPwd) {
            usernameForPwd = username;
            return isSecretFileExists();
        }
        return true;
    }

    private void startGame() {
        if (isOkToStart()) {
            if (!isGameRunning()) {
                btnStart.setText("Stop");
                startNewGame();
                disableControls();
            } else {
                btnStart.setText(UIName.BTN_START.name);
                stopGame();
                enableControls();
            }
        } else {
            showLockScreen();
        }
    }

    private void pauseGame() {
        if (gameStatus != Status.AUTO_PAUSED) {
            gameStatus = gameStatus == Status.PAUSED ? Status.START : Status.PAUSED;
        }
        if (isGamePaused()) {
            btnPause.setText("Resume");
            showScreen(GameScreens.none);
        } else {
            btnPause.setText(UIName.BTN_PAUSE.name);
            btnPause.setForeground(fg);
            showScreen(GameScreens.game);
        }
    }

    /**
     * Exit the Application
     */
    private void exitForm() {
        stopGame();
        saveScores();
        cancelTimers();
        configs.saveConfig(this);
        setVisible(false);
        dispose();
        logger.dispose();
        System.exit(0);
    }

    private void saveScores() {
        Properties prop = new Properties();
        gameScores.values().forEach(gs ->
                prop.setProperty(gs.getUsername() + AppConstants.PROP_SCORES_SUFFIX, prepareScoreCsv(gs.getRecentScores())));
        Utils.saveProperties(prop, AppPaths.scoresLoc.val, logger);
    }

    private String prepareScoreCsv(List<GameScore> score) {
        StringBuilder sb = new StringBuilder();
        score.forEach(s -> sb.append(s.getScore())
                .append(AppConstants.SCORE_DATA_SEP)
                .append(s.getDate())
                .append(AppConstants.SCORE_DATA_SEP)
                .append(s.getAccuracy())
                .append(AppConstants.SCORE_DATA_SEP)
                .append(s.getLevel())
                .append(AppConstants.SCORE_DATA_SEP)
                .append(s.getType())
                .append(AppConstants.SCORE_SEP)
        );
        return sb.toString();
    }

    private void cancelTimers() {
        TIMERS.forEach(Timer::cancel);
    }

    private void setControlsToEnable() {
        Component[] components = {menuBar, menu, btnHistory, btnUser, tblUsers, tblUserMISetUser, tblUserMIDelUser};
        setComponentToEnable(components);
        setComponentContrastToEnable(new Component[]{btnPause});
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

    public String getGameBtnFontSize() {
        return gameBtnFontSize + "";
    }

    public String getUsername() {
        return username;
    }

    public String getLineJoinsPointsCenter() {
        return graphPanel.isLineJoinsPointsCenter() + "";
    }

    public String getDrawBaseLines() {
        return graphPanel.isDrawBaseLines() + "";
    }

    public String getFirstPointOnBaseLine() {
        return graphPanel.isFirstPointOnBaseLine() + "";
    }

    public String getPairType() {
        return pairType.name();
    }

    public String getNoUserPwd() {
        return noUserPwd + "";
    }
}
