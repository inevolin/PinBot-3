/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.UnfollowConfiguration;
import model.configurations.queries.AQuery;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;
import model.configurations.queries.Helpers;
/**
 * commit: ALT + 3 push: ALT + 4 pull: ALT + 5
 *
 * before you can push, you have to pull what others have changed before you can
 * push , you also need to make a commit of your own changes
 */
public class Configuration_UnfollowController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot, chkUserCriteria, chkBoardCriteria, chkUnfollowUsers, chkUnfollowBoards, chkNFs, chkRNFs;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private MySpinner<Integer> txtBoard_FollowersMin, txtBoard_FollowersMax, txtBoard_PinsMin, txtBoard_PinsMax;
    @FXML
    private MySpinner<Integer> txtUser_FollowersMin, txtUser_FollowersMax, txtUser_FollowingMin, txtUser_FollowingMax, txtUser_BoardsMin, txtUser_BoardsMax, txtUser_PinsMin, txtUser_PinsMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private MySpinner<Integer> txtHoursPassed;

    private UnfollowConfiguration unfollowCf;
    private Account account;

    public Configuration_UnfollowController(Account acc, UnfollowConfiguration unfollowCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Unfollow.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.unfollowCf = unfollowCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {
        initNodes();
    }

    void initNodes() {
        chkEnabled.setSelected(unfollowCf.getIsActive() == null ? false : unfollowCf.getIsActive());
        chkAutopilot.setSelected(unfollowCf.getAutopilot() == null ? false : unfollowCf.getAutopilot());

        txtTimeoutMin.setValue(unfollowCf.getTimeoutMin() == null ? "20" : unfollowCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(unfollowCf.getTimeoutMax() == null ? "40" : unfollowCf.getTimeoutMax().toString());

        txtActionMin.setValue(unfollowCf.getActionMin() == null ? "20" : unfollowCf.getActionMin().toString());
        txtActionMax.setValue(unfollowCf.getActionMax() == null ? "40" : unfollowCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(unfollowCf.getAutopilotStart() == null ? "22:00:00" : unfollowCf.getAutopilotStart().format(formatter));
        ////
        chkBoardCriteria.setSelected(unfollowCf.getCriteria_Boards() == null ? false : unfollowCf.getCriteria_Boards());
        txtBoard_FollowersMin.setValue(Integer.toString(unfollowCf.getCriteria_BoardsFollowersMin()));
        txtBoard_FollowersMax.setValue(Integer.toString(unfollowCf.getCriteria_BoardsFollowersMax()));
        txtBoard_PinsMin.setValue(Integer.toString(unfollowCf.getCriteria_BoardsPinsMin()));
        txtBoard_PinsMax.setValue(Integer.toString(unfollowCf.getCriteria_BoardsPinsMax()));
        ////
        chkUserCriteria.setSelected(unfollowCf.getCriteria_Users() == null ? false : unfollowCf.getCriteria_Users());
        txtUser_FollowersMin.setValue(Integer.toString(unfollowCf.getCriteria_UserFollowersMin()));
        txtUser_FollowersMax.setValue(Integer.toString(unfollowCf.getCriteria_UserFollowersMax()));
        txtUser_FollowingMin.setValue(Integer.toString(unfollowCf.getCriteria_UserFollowingMin()));
        txtUser_FollowingMax.setValue(Integer.toString(unfollowCf.getCriteria_UserFollowingMax()));
        txtUser_BoardsMin.setValue(Integer.toString(unfollowCf.getCriteria_UserBoardsMin()));
        txtUser_BoardsMax.setValue(Integer.toString(unfollowCf.getCriteria_UserBoardsMax()));
        txtUser_PinsMin.setValue(Integer.toString(unfollowCf.getCriteria_UserPinsMin()));
        txtUser_PinsMax.setValue(Integer.toString(unfollowCf.getCriteria_UserPinsMax()));
        ///
        chkUnfollowUsers.setSelected(unfollowCf.getUnfollowUsers() == null ? false : unfollowCf.getUnfollowUsers());
        chkUnfollowBoards.setSelected(unfollowCf.getUnfollowBoards() == null ? false : unfollowCf.getUnfollowBoards());
        chkNFs.setSelected(unfollowCf.getUnfollowNonFollowers() == null ? false : unfollowCf.getUnfollowNonFollowers());
        chkRNFs.setSelected(unfollowCf.getUnfollowOnlyRecordedFollowings() == null ? false : unfollowCf.getUnfollowOnlyRecordedFollowings());
        txtHoursPassed.setValue(Long.toString(unfollowCf.getTimeBetweenFollowAndUnfollow() == null ? 120 : unfollowCf.getTimeBetweenFollowAndUnfollow() / 1000 / 3600));
    }

    public String toCf() {
        try {
            toCf_nodes();
            toCf_mapping();
            return validate();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return "Unkown error at 'Unfollow' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
            } else if (!(chkUnfollowUsers.isSelected() || chkUnfollowBoards.isSelected())) {
                return "Choose to unfollow users and/or boards at 'Unfollow' Configuration.";
            } else if (chkUserCriteria.isSelected()) {
                if (txtUser_BoardsMax.getValue() < txtUser_BoardsMin.getValue()) {
                    return "Boards criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
                }
                if (txtUser_PinsMax.getValue() < txtUser_PinsMin.getValue()) {
                    return "Pins criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
                }
                if (txtUser_FollowersMax.getValue() < txtUser_FollowersMin.getValue()) {
                    return "Followers criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
                }
                if (txtUser_FollowingMax.getValue() < txtUser_FollowingMin.getValue()) {
                    return "Following criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
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
                    return "Followers criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
                }
                if (txtBoard_PinsMax.getValue() < txtBoard_PinsMin.getValue()) {
                    return "Pins criteria Max value must be greater or equal to Min value at 'Unfollow' Configuration.";
                }
                
                if (txtBoard_FollowersMin.getValue()<= 0) {
                    return "Followers criteria must be greater than zero, at 'Follow' Configuration.";
                }
                if (txtBoard_PinsMin.getValue()<= 0) {
                    return "Pins criteria must be greater than zero, at 'Follow' Configuration.";
                }
            }
        }
        return null;
    }

    void toCf_nodes() {
        unfollowCf.setIsActive(chkEnabled.isSelected());
        unfollowCf.setAutopilot(chkAutopilot.isSelected());

        unfollowCf.setTimeoutMin(txtTimeoutMin.getValue());
        unfollowCf.setTimeoutMax(txtTimeoutMax.getValue());

        unfollowCf.setActionMin(txtActionMin.getValue());
        unfollowCf.setActionMax(txtActionMax.getValue());

        unfollowCf.setAutopilotStart(txtAutopilotMin.getValue());

        ////
        unfollowCf.setCriteria_Boards(chkBoardCriteria.isSelected());
        unfollowCf.setCriteria_BoardsFollowersMin(txtBoard_FollowersMin.getValue());
        unfollowCf.setCriteria_BoardsFollowersMax(txtBoard_FollowersMax.getValue());
        unfollowCf.setCriteria_BoardsPinsMin(txtBoard_PinsMin.getValue());
        unfollowCf.setCriteria_BoardsPinsMax(txtBoard_PinsMax.getValue());
        ////
        unfollowCf.setCriteria_Users(chkUserCriteria.isSelected());
        unfollowCf.setCriteria_UserFollowersMin(txtUser_FollowersMin.getValue());
        unfollowCf.setCriteria_UserFollowersMax(txtUser_FollowersMax.getValue());
        unfollowCf.setCriteria_UserFollowingMin(txtUser_FollowingMin.getValue());
        unfollowCf.setCriteria_UserFollowingMax(txtUser_FollowingMax.getValue());
        unfollowCf.setCriteria_UserBoardsMin(txtUser_BoardsMin.getValue());
        unfollowCf.setCriteria_UserBoardsMax(txtUser_BoardsMax.getValue());
        unfollowCf.setCriteria_UserPinsMin(txtUser_PinsMin.getValue());
        unfollowCf.setCriteria_UserPinsMax(txtUser_PinsMax.getValue());
        ///
        unfollowCf.setUnfollowUsers(chkUnfollowUsers.isSelected());
        unfollowCf.setUnfollowBoards(chkUnfollowBoards.isSelected());
        unfollowCf.setUnfollowNonFollowers(chkNFs.isSelected());
        unfollowCf.setUnfollowOnlyRecordedFollowings(chkRNFs.isSelected());
        unfollowCf.setTimeBetweenFollowAndUnfollow(txtHoursPassed.getValue() * 1000 * 3600L);
    }

    void toCf_mapping() {
        unfollowCf.getQueries().clear();
        if (unfollowCf.getUnfollowUsers()) {
            AQuery q = Helpers.typeByPatter_unfollow("/" + account.getUsername() + "/following/", null);
            unfollowCf.getQueries().add(q);
        }
        if (unfollowCf.getUnfollowBoards()) {
            AQuery q = Helpers.typeByPatter_unfollow("/" + account.getUsername() + "/boards/", null);
            unfollowCf.getQueries().add(q);
        }
    }

}
