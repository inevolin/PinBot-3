/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import Timers.MainController_LoopGUI;
import Timers.MainController_LoopUpdater;
import java.io.File;
import model.Account;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import model.configurations.AConfiguration;
import model.configurations.AConfiguration.RunStatus;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.UnfollowConfiguration;
import pinbot3.helpers.AccImporter;
import pinbot3.helpers.Privileges;
import pinbot3.helpers.Updater;

/**
 *
 * @author UGent
 */
public class MainController implements Initializable {

    @FXML
    private Label lblStats;
    @FXML
    private Button btnStart, btnAdd, btnStop, btnDelete, btnEdit, btnConfigure, btnRefresh, btnUpdate;
    @FXML
    private FlowPane statusbox;
    @FXML
    public TableView<Account> tbl;
    @FXML
    private TableColumn<Account, String> tblEmailColumn;
    @FXML
    private TableColumn<Account, String> tblAccountStatus;
    @FXML
    private TableColumn<Account, String> tblPinStatusColumn, tblRepinStatusColumn, tblLikeStatusColumn, tblInviteStatusColumn, tblFollowStatusColumn, tblUnfollowStatusColumn, tblCommentStatusColumn, tblMessageStatusColumn;

    @FXML
    private MenuItem mnuKwGen, mnuBoardsMgr, mnuClose, mnuWebsite, mnuBtnImport, mnuLogging, mnuTutorials, mnuDebug;

    private ObservableList<Account> tblAccounts;
    private ExecutorService executor;
    private Timer timer_tbl, timer_update;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        MenuActionsInit();
        mnuInit();
        ButtonHandler();
        TblInit();
        executor = PinBot3.mainControllerExecutor;
        executor.submit(() -> {
            FillTbl();
        });

        timer_tbl = new Timer();
        timer_tbl.scheduleAtFixedRate(new MainController_LoopGUI(this, tbl), 0, 1000L);

        statusbox.getChildren().remove(btnUpdate);
        timer_update = new Timer();
        timer_update.scheduleAtFixedRate(new MainController_LoopUpdater(btnUpdate, statusbox), 0, 30 * 60 * 1000L);
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    private void TblInit() {
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            ButtonHandler();
        });
        tbl.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tbl.setPlaceholder(new Label("Loading database..."));

        tblEmailColumn.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getEmail()));
        tblAccountStatus.setCellValueFactory(cellData -> accountStatusString(cellData));

        genCallBackCellData(tblPinStatusColumn);
        genCallBackCellData(tblRepinStatusColumn);
        genCallBackCellData(tblLikeStatusColumn);
        genCallBackCellData(tblInviteStatusColumn);
        genCallBackCellData(tblFollowStatusColumn);
        genCallBackCellData(tblUnfollowStatusColumn);
        genCallBackCellData(tblCommentStatusColumn);
        genCallBackCellData(tblMessageStatusColumn);

    }

    private ReadOnlyStringWrapper accountStatusString(CellDataFeatures<Account, String> cellData) {
        if (cellData.getValue().getStatus().equals(Account.STATUS.UNAUTHORIZED)) {
            return new ReadOnlyStringWrapper("LOGGED OUT");
        } else if (cellData.getValue().getStatus().equals(Account.STATUS.LOGGEDIN)) {
            return new ReadOnlyStringWrapper("LOGGED IN");
        } else {
            return new ReadOnlyStringWrapper(cellData.getValue().getStatus().toString());
        }
    }

    void genCallBackCellData(TableColumn<Account, String> tc) {
        String txt = tc.getText();
        Callback<CellDataFeatures<Account, String>, ObservableValue<String>> CB = new Callback<CellDataFeatures<Account, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(CellDataFeatures<Account, String> data) {
                AConfiguration cf = null;
                if (data.getValue().getSelectCampaign() == null || data.getValue().getSelectCampaign().getConfigurations() == null) {
                    return new SimpleStringProperty(RunStatus.IDLE.toString());
                }
                for (AConfiguration c : data.getValue().getSelectCampaign().getConfigurations()) {
                    if (c instanceof PinConfiguration && txt.equalsIgnoreCase("pin")) {
                        cf = c;
                        break;
                    } else if (c instanceof RepinConfiguration && txt.equalsIgnoreCase("repin")) {
                        cf = c;
                        break;
                    } else if (c instanceof LikeConfiguration && txt.equalsIgnoreCase("like")) {
                        cf = c;
                        break;
                    } else if (c instanceof InviteConfiguration && txt.equalsIgnoreCase("invite")) {
                        cf = c;
                        break;
                    } else if (c instanceof FollowConfiguration && txt.equalsIgnoreCase("follow")) {
                        cf = c;
                        break;
                    } else if (c instanceof UnfollowConfiguration && txt.equalsIgnoreCase("unfollow")) {
                        cf = c;
                        break;
                    } else if (c instanceof CommentConfiguration && txt.equalsIgnoreCase("comment")) {
                        cf = c;
                        break;
                    } else if (c instanceof MessageConfiguration && txt.equalsIgnoreCase("message")) {
                        cf = c;
                        break;
                    }
                }
                if (cf != null && (data.getValue().getStatus() == Account.STATUS.ABORT_REQUEST
                        || data.getValue().getStatus() == Account.STATUS.UNAUTHORIZED)) {
                    cf.status = RunStatus.IDLE;
                    if (data.getValue().getLastLogin() != null
                            && ((new Date()).getTime() - data.getValue().getLastlySaved().getTime() < 1000 * 60 * 10) //ten minutes instant-login
                            ) {
                        data.getValue().setStatus(Account.STATUS.LOGGEDIN);
                    } else {
                        data.getValue().setStatus(Account.STATUS.UNAUTHORIZED);
                    }
                }
                return new SimpleStringProperty(
                        cf == null || cf.status == null || cf.status == RunStatus.IDLE
                                ? RunStatus.IDLE.toString()
                                : cf.CountCompleted == null || cf.CountTotal == null ? cf.status.toString()
                                        : cf.CountCompleted + "/" + cf.CountTotal + "  " + cf.status.toString()
                );
            }
        };
        tc.setCellValueFactory(CB);
    }

    private void FillTbl() {
        if (pinbot3.PinBot3.campaignMgr == null) {
            return; //window loading
        }
        try {
            FillAccounts();

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
    }

    private void FillAccounts() throws IOException {
        tblAccounts = (ObservableList<Account>) FXCollections.observableArrayList(pinbot3.PinBot3.campaignMgr.getAccounts());
        int selectedIndex = tbl.getSelectionModel().getSelectedIndex();
        Platform.runLater(() -> {
            tbl.setItems(null);
            tbl.layout();
            tbl.setItems(tblAccounts);
            tbl.refresh();
            if (tbl.getItems() == null || tbl.getItems().size() == 0) {
                tbl.setPlaceholder(new Label("No account(s) added yet."));
            } else {
                tbl.setPlaceholder(new Label(""));
            }
            tbl.getSelectionModel().select(selectedIndex);
        });
    }

    private void mnuInit() {
        mnuBoardsMgrClick();
        mnuKwGetClick();
        mnuCloseClick();
        mnuWebsiteClick();
        mnuTutorialsClick();
        mnuBtnImport_Click();
        mnuLogging_Click();
        //mnuDebug_Click();
    }

    /*void toggleDebugMode() {
        if (pinbot3.PinBot3.Debug) {
            pinbot3.PinBot3.Debug = false;
            mnuDebug.setText("Debug mode (off)");
        } else {
            pinbot3.PinBot3.Debug = true;
            mnuDebug.setText("Debug mode (ON)");
        }
    }*/

 /*void mnuDebug_Click() {
        toggleDebugMode();
        mnuDebug.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                toggleDebugMode();
            }
        });
    }*/
    void mnuWebsiteClick() {
        mnuWebsite.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Privileges.openWebpage("https://pinbot3.com/");
            }
        });
    }

    void mnuTutorialsClick() {
        mnuTutorials.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Privileges.openWebpage("https://pinbot3.com/vip/category/pinbot-3-0/");
            }
        });
    }

    void mnuCloseClick() {
        mnuClose.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                pinbot3.PinBot3.SystemExit();
            }
        });
    }

    void mnuBoardsMgrClick() {
        mnuBoardsMgr.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (pinbot3.PinBot3.PRIVS.isTrial()) {
                        PinBot3.PRIVS.premiumDialog(null);
                        return;
                    }
                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Boards.fxml"));
                    Parent parent = (Parent) fxmlloader.load();
                    BoardsController ctrl = fxmlloader.getController();//edit
                    Account acc = tbl.getSelectionModel().getSelectedItem();
                    ctrl.setAccount(acc);

                    Stage stage = new Stage();
                    stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
                    stage.setTitle("Boards manager");
                    stage.initModality(Modality.NONE);
                    stage.initOwner(btnAdd.getScene().getWindow());
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.show();

                } catch (IOException ex) {
                    common.ExceptionHandler.reportException(ex);

                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);

                }
            }
        });

    }

    void mnuKwGetClick() {
        mnuKwGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (pinbot3.PinBot3.PRIVS.isTrial()) {
                        PinBot3.PRIVS.premiumDialog(null);
                        return;
                    }

                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/KeywordGen.fxml"));
                    Parent parent = (Parent) fxmlloader.load();
                    KeywordGenController ctrl = fxmlloader.getController();
                    Stage stage = new Stage();
                    stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
                    stage.setTitle("Keyword generator");
                    stage.initModality(Modality.NONE);
                    stage.initOwner(btnAdd.getScene().getWindow());
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent t) {
                            if (ctrl != null && ctrl.getExecutor() != null) {
                                ctrl.setIsInterrupted(Boolean.TRUE);
                                ctrl.getExecutor().shutdownNow();
                            }
                        }
                    });
                    stage.setResizable(false);
                    stage.show();
                } catch (IOException ex) {
                    common.ExceptionHandler.reportException(ex);

                }
            }
        });
    }

    void mnuLogging_Click() {
        mnuLogging.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {

                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Logging.fxml"));
                    Parent parent = (Parent) fxmlloader.load();
                    LoggingController ctrl = fxmlloader.getController();
                    Stage stage = new Stage();
                    stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
                    stage.setTitle("Logging");
                    stage.initModality(Modality.NONE);
                    stage.initOwner(btnAdd.getScene().getWindow());
                    Scene scene = new Scene(parent);
                    stage.setScene(scene);
                    stage.setResizable(false);
                    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                        @Override
                        public void handle(WindowEvent t) {
                            if (ctrl.t != null) {
                                ctrl.t.cancel();
                                ctrl.t = null;
                            }
                        }
                    });
                    ctrl.init();
                    stage.show();
                } catch (IOException ex) {
                    common.ExceptionHandler.reportException(ex);

                }
            }
        });
    }

    void mnuBtnImport_Click() {
        mnuBtnImport.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                AccImporter ai = new AccImporter();

                FileChooser fileChooser = new FileChooser();
                File selectedFile = fileChooser.showOpenDialog(null);

                if (selectedFile != null) {
                    try {
                        ai.Import(selectedFile);
                    } catch (IOException ex) {
                        common.ExceptionHandler.reportException(ex);
                    }
                    try {
                        FillAccounts();
                    } catch (IOException ex) {
                        common.ExceptionHandler.reportException(ex);

                    }
                } else {
                }
            }
        });
    }

    @FXML
    protected void btnAddClick(ActionEvent event) throws IOException {
        // System.err.println("You clicked me!");
        if (pinbot3.PinBot3.PRIVS.isTrial() && pinbot3.PinBot3.campaignMgr.getAccounts().size() >= 5) {
            PinBot3.PRIVS.premiumDialog("Trial version can have max. 5 accounts.");
            return;
        }

        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Account.fxml"));
        Parent parent = (Parent) fxmlloader.load();
        Stage stage = new Stage();
        AccountController controller = fxmlloader.getController();
        controller.setStage(stage);
        stage.setTitle("PinBot " + (PinBot3.PRIVS.getMyPrivs().contains(Privileges.TYPES.PREMIUM) ? "PREMIUM " : "trial ") + pinbot3.PinBot3.VERSION);
        stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnAdd.getScene().getWindow());
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        FillAccounts();

    }

    @FXML
    protected void btnEditClick(ActionEvent event) throws IOException {
        // System.err.println("You clicked me!");
        Account acc = tbl.getSelectionModel().getSelectedItem();

        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Account.fxml"));
        Parent parent = (Parent) fxmlloader.load();
        AccountController controller = fxmlloader.getController();

        Stage stage = new Stage();

        controller.setAccount(acc);
        controller.setStage(stage);

        stage.setTitle("PinBot " + (PinBot3.PRIVS.getMyPrivs().contains(Privileges.TYPES.PREMIUM) ? "PREMIUM " : "trial ") + pinbot3.PinBot3.VERSION);
        stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnAdd.getScene().getWindow());
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        FillAccounts();

    }

    @FXML
    protected void btnStartClick(ActionEvent event) {
        for (Object o : tbl.getSelectionModel().getSelectedItems()) {
            Account a = (Account) o;
            Boolean accountIsRunning = accountIsRunning(a);
            Boolean configured = accountIsConfigured(a);
            Boolean validBoards = accountHasValidBoards(a);
            if (!accountIsRunning && configured && validBoards) {
                pinbot3.PinBot3.campaignMgr.RunAccount(a);
            }
        }
        ButtonHandler();
    }

    @FXML
    protected void btnStopClick(ActionEvent event) {
        for (Object o : tbl.getSelectionModel().getSelectedItems()) {
            Account a = (Account) o;
            Boolean accountIsRunning = accountIsRunning(a);
            if (accountIsRunning) {
                pinbot3.PinBot3.campaignMgr.StopAccount(a);
            }
        }
    }

    @FXML
    protected void btnConfigure_Click(ActionEvent event) throws Exception {
        Account acc = (Account) tbl.getSelectionModel().getSelectedItem();

        if (acc.getUsername() == null || acc.getUsername().length() <= 0) {
            common.Dialogs.ErrorDialog("Username missing.\nClick 'Edit' button and 'Save' the account to re-login.", "Username missing");
            return;
        }

        if (acc.getStatus() != Account.STATUS.LOGGEDIN && acc.getMyBoards() != acc.getBoards().size()) {
            common.Dialogs.InfoDialog("Some of your boards are missing.\n This is no big problem, they will appear once you login.\nTo login you can go back and click 'Edit' and save.\nOr you can just run your account.", "Missing boards");
        }
        
        if (acc.getMyBoards() == 0 && acc.getBoards().size() == 0) {
            common.Dialogs.InfoDialog("Your account does not have any boards. Some features will not work without boards. To add boards go to tools > boards manager.", "Missing boards");
        }
        
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Configurations.fxml"));
        Parent parent = (Parent) fxmlloader.load();

        ConfigurationsController controller = fxmlloader.getController();
        controller.Init(acc);

        
        Stage stage = new Stage();
        stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
        stage.setTitle("Configure account");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(btnAdd.getScene().getWindow());
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.setResizable(true); // false
        stage.showAndWait();

        FillAccounts();
    }

    @FXML
    protected void btnDelete_Click(ActionEvent event) throws Exception {
        for (Object obj : tbl.getSelectionModel().getSelectedItems()) {
            Alert alert = new Alert(AlertType.CONFIRMATION, "Delete account '" + ((Account) obj).getEmail() + "' ?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            alert.showAndWait();
            if (alert.getResult() == ButtonType.YES) {
                pinbot3.PinBot3.dalMgr.RemoveAccount((Account) obj);
                pinbot3.PinBot3.campaignMgr.getAccounts().remove((Account) obj);
            }
        }

        FillAccounts();
    }

    @FXML
    protected void btnRefreshClick(ActionEvent event) {

        int c = tbl.getSelectionModel().getSelectedItems().size();
        if (c == 1) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Account acc = (Account) tbl.getSelectionModel().getSelectedItem();
                    try {
                        Platform.runLater(() -> {
                            ButtonHandler();
                            btnRefresh.getParent().setCursor(Cursor.WAIT);
                        });
                        acc.setMyFollowers(-1); //we use this to disable/enable button for acc.
                        if (acc.getStatus() == Account.STATUS.UNAUTHORIZED) {
                            pinbot3.PinBot3.accountMgr.Login(acc);
                        }
                        if (acc.getStatus() == Account.STATUS.LOGGEDIN) {
                            pinbot3.PinBot3.accountMgr.refreshBoards(acc);
                            pinbot3.PinBot3.accountMgr.refreshStats(acc);
                            pinbot3.PinBot3.dalMgr.Save(acc);
                            Platform.runLater(() -> {
                                lblStats.setText("Followers: " + acc.getMyFollowers() + "   Following: " + acc.getMyFollowing() + "   Likes: " + acc.getMyLikes() + "   Pins: " + acc.getMyPins() + "   Boards: " + acc.getMyBoards() + "");
                            });
                        } else {
                            Platform.runLater(() -> {
                                lblStats.setText("Error: unable to login");
                            });
                        }
                    } catch (Exception ex) {
                        common.ExceptionHandler.reportException(ex);

                        acc.setMyFollowers(0); //we use this to disable/enable button for acc.
                    } finally {
                        Platform.runLater(() -> {
                            btnRefresh.getParent().setCursor(Cursor.DEFAULT);
                        });
                    }
                }
            });

        }

    }

    @FXML
    protected void btnUpdateClick(ActionEvent event) {
        Updater u = new Updater(new Label());
        u.btnNewUpdateAttemptRun();
    }

    private boolean accountIsRunning(Account acc) {
        Boolean accountIsRunning = false;
        if (acc.getSelectCampaign() != null) {
            Boolean allIdle = acc.getSelectCampaign().getConfigurations().stream().allMatch((k) -> {
                return k == null || k.status == null || k.status == RunStatus.IDLE;
            });
            accountIsRunning = !allIdle;
        }
        accountIsRunning = acc.getStatus().equals(Account.STATUS.BUSY) ? true : accountIsRunning;
        return accountIsRunning;
    }

    private boolean accountIsConfigured(Account acc) {
        if (acc.getBoards() == null || acc.getBoards().size() == 0) {
            return false;
        }

        Boolean configured = acc.getSelectCampaign() != null && acc.getSelectCampaign().getConfigurations().stream().filter(o -> o.getIsActive() != null && o.getIsActive()).findAny().isPresent();
        configured = acc.getSelectCampaign() != null
                && acc.getSelectCampaign().getConfigurations().stream().filter(
                        o -> (o instanceof PinConfiguration || o instanceof RepinConfiguration || o instanceof InviteConfiguration)
                        && o.getQueries().stream().filter(
                                x -> x.getBoardMapped() != null
                                && x.getBoardMapped().getBoardId() == null)
                        .findAny().isPresent()
                ).findAny().isPresent() == false;

        return configured;
    }

    private boolean accountHasValidBoards(Account acc) {
        Boolean validBoards = acc.getBoards().stream().filter(b -> b.getBoardId() == null).findAny().isPresent() == false;
        return validBoards;
    }

    public void ButtonHandler() {
        int c = tbl.getSelectionModel().getSelectedItems().size();

        if (c == 1) {
            Account acc = (Account) tbl.getSelectionModel().getSelectedItem();
            Boolean accountIsRunning = accountIsRunning(acc);
            Boolean configured = accountIsConfigured(acc);
            Boolean validBoards = accountHasValidBoards(acc);
            btnStart.setDisable(accountIsRunning || !configured || !validBoards);
            btnStop.setDisable(!accountIsRunning);
            btnEdit.setDisable(accountIsRunning);
            mnuBoardsMgr.setDisable(accountIsRunning);
            btnConfigure.setDisable(accountIsRunning || !validBoards);
            btnDelete.setDisable(accountIsRunning);

            if (acc.getMyFollowers() == -1) {
                btnRefresh.setVisible(false);
                lblStats.setVisible(true);
            } else {
                Platform.runLater(() -> {
                    lblStats.setText("Followers: " + acc.getMyFollowers() + "   Following: " + acc.getMyFollowing() + "   Likes: " + acc.getMyLikes() + "   Pins: " + acc.getMyPins() + "   Boards: " + acc.getMyBoards() + "");
                });
                btnRefresh.setVisible(true);
                lblStats.setVisible(true);
            }
        } else if (c > 1) {
            Boolean anyRunning = false, anyIdle = false;
            for (Object obj : tbl.getSelectionModel().getSelectedItems()) {
                if (accountIsRunning((Account) obj)) {
                    anyRunning = true;
                } else {
                    anyIdle = true;
                }
            }
            btnStart.setDisable(anyRunning && !anyIdle);
            btnStop.setDisable(!anyRunning);
            btnEdit.setDisable(true);
            mnuBoardsMgr.setDisable(anyRunning);
            btnConfigure.setDisable(true);
            btnDelete.setDisable(anyRunning);

            btnRefresh.setVisible(false);
            lblStats.setVisible(false);

        } else {
            btnStart.setDisable(true);
            btnStop.setDisable(true);
            btnEdit.setDisable(true);
            mnuBoardsMgr.setDisable(true);
            btnConfigure.setDisable(true);
            btnDelete.setDisable(true);

            btnRefresh.setVisible(false);
            lblStats.setVisible(false);

        }

    }

    private void MenuActionsInit() {

    }

}
