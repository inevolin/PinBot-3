/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.MessageConfiguration;
import model.configurations.queries.AQuery;
import org.apache.commons.lang3.StringUtils;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;
import model.configurations.queries.Helpers;

/**
 * commit: ALT + 3 push: ALT + 4 pull: ALT + 5
 *
 * before you can push, you have to pull what others have changed before you can
 * push , you also need to make a commit of your own changes
 */
public class Configuration_MessageController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtMessages;
    @FXML
    private Button btnResetDups;

    private MessageConfiguration messageCf;
    private Account account;

    public Configuration_MessageController(Account acc, MessageConfiguration messageCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Message.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.messageCf = messageCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {

        initNodes();
        initTXT();
        initBtnResetDups();
    }

    void initBtnResetDups() {
        btnResetDups.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                //System.err.println("msg dups: " + account.getDuplicates_message().size());
                //account.getDuplicates_message().clear();
                
                account.clearDuplicates(messageCf);
                
                //pinbot3.PinBot3.dalMgr.Save(account);
            }
        });
    }

    void initNodes() {
        chkEnabled.setSelected(messageCf.getIsActive() == null ? false : messageCf.getIsActive());
        chkAutopilot.setSelected(messageCf.getAutopilot() == null ? false : messageCf.getAutopilot());

        txtTimeoutMin.setValue(messageCf.getTimeoutMin() == null ? "20" : messageCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(messageCf.getTimeoutMax() == null ? "40" : messageCf.getTimeoutMax().toString());

        txtActionMin.setValue(messageCf.getActionMin() == null ? "20" : messageCf.getActionMin().toString());
        txtActionMax.setValue(messageCf.getActionMax() == null ? "40" : messageCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(messageCf.getAutopilotStart() == null ? "22:00:00" : messageCf.getAutopilotStart().format(formatter));
    }

    void initTXT() {
        if (messageCf.getMessages() == null) {
            return;
        }
        String s = "";
        for (String q : messageCf.getMessages()) {
            s += q + "\n";
        }
        txtMessages.setText(s.trim());
    }

    public String toCf() {
        try {
            toCf_nodes();
            toCf_mapping();
            return validate();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return "Unkown error at 'Message' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Invite' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Invite' Configuration.";
            } else if (messageCf.getMessages().size() <= 0) {
                return "Boards selection missing at 'Invite' Configuration.";
            }
        }
        return null;
    }

    void toCf_nodes() {
        messageCf.setIsActive(chkEnabled.isSelected());
        messageCf.setAutopilot(chkAutopilot.isSelected());

        messageCf.setTimeoutMin(txtTimeoutMin.getValue());
        messageCf.setTimeoutMax(txtTimeoutMax.getValue());

        messageCf.setActionMin(txtActionMin.getValue());
        messageCf.setActionMax(txtActionMax.getValue());

        messageCf.setAutopilotStart(txtAutopilotMin.getValue());
    }

    void toCf_mapping() {
        messageCf.getQueries().clear();
        AQuery q = Helpers.typeByPatter_message(account.getUsername(), null);
        messageCf.getQueries().add(q);

        messageCf.setMessages(Arrays.asList(StringUtils.split(txtMessages.getText().trim(), "\n")));

    }

}
