/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import common.MyLogger;
import common.MyLogger.LogItem;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import model.Account;

/**
 * FXML Controller class
 *
 * @author UGent
 */
public class LoggingController implements Initializable {

    @FXML
    private ComboBox cbo;
    @FXML
    private ListView lst;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    private ObservableList<Account> accs;
    private ObservableList<LogItem> logs;
    public Timer t;

    public void init() {
        if (pinbot3.PinBot3.campaignMgr.getAccounts().size() > 0) {
            accs = (ObservableList<Account>) FXCollections.observableArrayList(pinbot3.PinBot3.campaignMgr.getAccounts());
            cbo.setItems(accs);
            cbo.valueProperty().addListener(new ChangeListener<Account>() {
                @Override
                public void changed(ObservableValue<? extends Account> observable, Account oldValue, Account newValue) {
                    fill(newValue);
                }
            });
            cbo.getSelectionModel().select(0);

            t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Account ac = (Account) cbo.getSelectionModel().getSelectedItem();
                        fill(ac);
                    } catch (Exception ex) {
                        common.ExceptionHandler.reportException(ex);
                    }
                }
            }, 0, 1000L);
        }
    }

    void fill(Account ac) {
        if (pinbot3.PinBot3.myLogger.getLogs().containsKey(ac)) {
            ArrayList<LogItem> arr = pinbot3.PinBot3.myLogger.getLogs().get(ac);
            arr = arr.stream().filter(x -> x.getLvl() == MyLogger.LEVEL.info).collect(Collectors.toCollection(ArrayList::new));

            logs = (ObservableList<LogItem>) FXCollections.observableArrayList(arr);
            Collections.reverse(logs);
            Platform.runLater(() -> lst.setItems(logs));
        }
    }

}
