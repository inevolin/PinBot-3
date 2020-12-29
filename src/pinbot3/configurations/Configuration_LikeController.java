/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.LikeConfiguration;
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
public class Configuration_LikeController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtQueries;

    private LikeConfiguration likeCf;
    private Account account;

    public Configuration_LikeController(Account acc, LikeConfiguration likeCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Like.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.likeCf = likeCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {
        initNodes();
        initTXT();
    }

    void initNodes() {
        chkEnabled.setSelected(likeCf.getIsActive() == null ? false : likeCf.getIsActive());
        chkAutopilot.setSelected(likeCf.getAutopilot() == null ? false : likeCf.getAutopilot());

        txtTimeoutMin.setValue(likeCf.getTimeoutMin() == null ? "20" : likeCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(likeCf.getTimeoutMax() == null ? "40" : likeCf.getTimeoutMax().toString());

        txtActionMin.setValue(likeCf.getActionMin() == null ? "20" : likeCf.getActionMin().toString());
        txtActionMax.setValue(likeCf.getActionMax() == null ? "40" : likeCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(likeCf.getAutopilotStart() == null ? "22:00:00" : likeCf.getAutopilotStart().format(formatter));
    }

    void initTXT() {
        if (likeCf.getQueries() == null) {
            return;
        }
        String s = "";
        for (AQuery q : likeCf.getQueries()) {
            s += q.getQuery() + "\n";
        }
        txtQueries.setText(s.trim());
    }

    public String toCf() {
        try {
            toCf_nodes();
            toCf_mapping();
            return validate();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return "Unkown error at 'Like' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Like' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Like' Configuration.";
            } else if (likeCf.getQueries().size() <= 0) {
                return "Queries missing at 'Like' Configuration.";
            }
        }
        return null;
    }
    
    void toCf_nodes() {
        likeCf.setIsActive(chkEnabled.isSelected());
        likeCf.setAutopilot(chkAutopilot.isSelected());

        likeCf.setTimeoutMin(txtTimeoutMin.getValue());
        likeCf.setTimeoutMax(txtTimeoutMax.getValue());

        likeCf.setActionMin(txtActionMin.getValue());
        likeCf.setActionMax(txtActionMax.getValue());

        likeCf.setAutopilotStart(txtAutopilotMin.getValue());
    }

    void toCf_mapping() {
        likeCf.getQueries().clear();
        List<String> qs = Arrays.asList(StringUtils.split(txtQueries.getText(), "\n"));
        for (String entry : qs) {
            AQuery q = Helpers.typeByPatter_like(entry, null);
            likeCf.getQueries().add(q);
        }
    }

}
