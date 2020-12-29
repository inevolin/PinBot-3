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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.FollowConfiguration;
import model.configurations.queries.AQuery;
import model.configurations.queries.Helpers;
import org.apache.commons.lang3.StringUtils;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;

/**
 * commit: ALT + 3 push: ALT + 4 pull: ALT + 5
 *
 * before you can push, you have to pull what others have changed before you can
 * push , you also need to make a commit of your own changes
 */
public class Configuration_FollowController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot, chkUserCriteria, chkBoardCriteria, chkFollowUsers, chkFollowBoards;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private MySpinner<Integer> txtBoard_FollowersMin, txtBoard_FollowersMax, txtBoard_PinsMin, txtBoard_PinsMax;
    @FXML
    private MySpinner<Integer> txtUser_FollowersMin, txtUser_FollowersMax, txtUser_FollowingMin, txtUser_FollowingMax, txtUser_BoardsMin, txtUser_BoardsMax, txtUser_PinsMin, txtUser_PinsMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtQueries;
    @FXML
    private Button btnResetDups;

    private FollowConfiguration followCf;
    private Account account;

    public Configuration_FollowController(Account acc, FollowConfiguration followCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Follow.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.followCf = followCf;

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
                
                account.clearDuplicates(followCf);
                //account.getDuplicates_follow().clear();
                //pinbot3.PinBot3.dalMgr.Save(account);
            }
        });
    }

    void initNodes() {
        chkEnabled.setSelected(followCf.getIsActive() == null ? false : followCf.getIsActive());
        chkAutopilot.setSelected(followCf.getAutopilot() == null ? false : followCf.getAutopilot());

        txtTimeoutMin.setValue(followCf.getTimeoutMin() == null ? "20" : followCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(followCf.getTimeoutMax() == null ? "40" : followCf.getTimeoutMax().toString());

        txtActionMin.setValue(followCf.getActionMin() == null ? "20" : followCf.getActionMin().toString());
        txtActionMax.setValue(followCf.getActionMax() == null ? "40" : followCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(followCf.getAutopilotStart() == null ? "22:00:00" : followCf.getAutopilotStart().format(formatter));

        chkFollowBoards.setSelected(followCf.getFollowBoards() == null ? false : followCf.getFollowBoards());
        chkFollowUsers.setSelected(followCf.getFollowUsers() == null ? false : followCf.getFollowUsers());
        ////
        chkBoardCriteria.setSelected(followCf.getCriteria_Boards() == null ? false : followCf.getCriteria_Boards());
        txtBoard_FollowersMin.setValue(Integer.toString(followCf.getCriteria_BoardsFollowersMin()));
        txtBoard_FollowersMax.setValue(Integer.toString(followCf.getCriteria_BoardsFollowersMax()));
        txtBoard_PinsMin.setValue(Integer.toString(followCf.getCriteria_BoardsPinsMin()));
        txtBoard_PinsMax.setValue(Integer.toString(followCf.getCriteria_BoardsPinsMax()));
        ////
        chkUserCriteria.setSelected(followCf.getCriteria_Users() == null ? false : followCf.getCriteria_Users());
        txtUser_FollowersMin.setValue(Integer.toString(followCf.getCriteria_UserFollowersMin()));
        txtUser_FollowersMax.setValue(Integer.toString(followCf.getCriteria_UserFollowersMax()));
        txtUser_FollowingMin.setValue(Integer.toString(followCf.getCriteria_UserFollowingMin()));
        txtUser_FollowingMax.setValue(Integer.toString(followCf.getCriteria_UserFollowingMax()));
        txtUser_BoardsMin.setValue(Integer.toString(followCf.getCriteria_UserBoardsMin()));
        txtUser_BoardsMax.setValue(Integer.toString(followCf.getCriteria_UserBoardsMax()));
        txtUser_PinsMin.setValue(Integer.toString(followCf.getCriteria_UserPinsMin()));
        txtUser_PinsMax.setValue(Integer.toString(followCf.getCriteria_UserPinsMax()));
    }

    void initTXT() {
        if (followCf.getQueries() == null) {
            return;
        }
        String s = "";
        for (AQuery q : followCf.getQueries()) {
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
        return "Unkown error at 'Follow' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Follow' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Follow' Configuration.";
            } else if (followCf.getQueries().size() <= 0) {
                return "Queries missing at 'Follow' Configuration.";
            } else if (!(chkFollowUsers.isSelected() || chkFollowBoards.isSelected())) {
                return "Choose to follow users and/or boards at 'Follow' Configuration.";
            } else if (chkUserCriteria.isSelected()) {
                if (txtUser_BoardsMax.getValue() < txtUser_BoardsMin.getValue()) {
                    return "Boards criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }
                if (txtUser_PinsMax.getValue() < txtUser_PinsMin.getValue()) {
                    return "Pins criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }
                if (txtUser_FollowersMax.getValue() < txtUser_FollowersMin.getValue()) {
                    return "Followers criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }
                if (txtUser_FollowingMax.getValue() < txtUser_FollowingMin.getValue()) {
                    return "Following criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }

                if (txtUser_BoardsMin.getValue() <= 0) {
                    return "Boards criteria must be greater than zero, at 'Follow' Configuration.";
                }
                if (txtUser_PinsMin.getValue() <= 0) {
                    return "Pins criteria must be greater than zero, at 'Follow' Configuration.";
                }
                if (txtUser_FollowersMin.getValue() <= 0) {
                    return "Followers criteria must be greater than zero, at 'Follow' Configuration.";
                }
                if (txtUser_FollowingMin.getValue() <= 0) {
                    return "Following criteria must be greater than zero, at 'Follow' Configuration.";
                }
            } else if (chkBoardCriteria.isSelected()) {
                if (txtBoard_FollowersMax.getValue() < txtBoard_FollowersMin.getValue()) {
                    return "Followers criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }
                if (txtBoard_PinsMax.getValue() < txtBoard_PinsMin.getValue()) {
                    return "Pins criteria Max value must be greater or equal to Min value at 'Follow' Configuration.";
                }

                if (txtBoard_FollowersMin.getValue() <= 0) {
                    return "Followers criteria must be greater than zero, at 'Follow' Configuration.";
                }
                if (txtBoard_PinsMin.getValue() <= 0) {
                    return "Pins criteria must be greater than zero, at 'Follow' Configuration.";
                }
            }
        }
        return null;
    }

    void toCf_nodes() {
        followCf.setIsActive(chkEnabled.isSelected());
        followCf.setAutopilot(chkAutopilot.isSelected());

        followCf.setTimeoutMin(txtTimeoutMin.getValue());
        followCf.setTimeoutMax(txtTimeoutMax.getValue());

        followCf.setActionMin(txtActionMin.getValue());
        followCf.setActionMax(txtActionMax.getValue());

        followCf.setAutopilotStart(txtAutopilotMin.getValue());

        followCf.setFollowBoards(chkFollowBoards.isSelected());
        followCf.setFollowUsers(chkFollowUsers.isSelected());

        ////
        followCf.setCriteria_Boards(chkBoardCriteria.isSelected());
        followCf.setCriteria_BoardsFollowersMin(txtBoard_FollowersMin.getValue());
        followCf.setCriteria_BoardsFollowersMax(txtBoard_FollowersMax.getValue());
        followCf.setCriteria_BoardsPinsMin(txtBoard_PinsMin.getValue());
        followCf.setCriteria_BoardsPinsMax(txtBoard_PinsMax.getValue());
        ////
        followCf.setCriteria_Users(chkUserCriteria.isSelected());
        followCf.setCriteria_UserFollowersMin(txtUser_FollowersMin.getValue());
        followCf.setCriteria_UserFollowersMax(txtUser_FollowersMax.getValue());
        followCf.setCriteria_UserFollowingMin(txtUser_FollowingMin.getValue());
        followCf.setCriteria_UserFollowingMax(txtUser_FollowingMax.getValue());
        followCf.setCriteria_UserBoardsMin(txtUser_BoardsMin.getValue());
        followCf.setCriteria_UserBoardsMax(txtUser_BoardsMax.getValue());
        followCf.setCriteria_UserPinsMin(txtUser_PinsMin.getValue());
        followCf.setCriteria_UserPinsMax(txtUser_PinsMax.getValue());
    }

    void toCf_mapping() {
        followCf.getQueries().clear();
        List<String> qs = Arrays.asList(StringUtils.split(txtQueries.getText(), "\n"));
        for (String entry : qs) {
            AQuery q = Helpers.typeByPatter_follow(entry, null);
            followCf.getQueries().add(q);
        }
    }

}
