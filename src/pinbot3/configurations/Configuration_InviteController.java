/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.InviteConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;
import model.configurations.queries.Helpers;

/**
 * commit: ALT + 3 push: ALT + 4 pull: ALT + 5
 *
 * before you can push, you have to pull what others have changed before you can
 * push , you also need to make a commit of your own changes
 */
public class Configuration_InviteController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private ListView lstBoards;
    private ObservableList<Board> boards = FXCollections.observableArrayList();

    private InviteConfiguration inviteCf;
    private Account account;

    public Configuration_InviteController(Account acc, InviteConfiguration inviteCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Invite.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.inviteCf = inviteCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {
        initNodes();
        initBoards();
    }

    void initNodes() {
        chkEnabled.setSelected(inviteCf.getIsActive() == null ? false : inviteCf.getIsActive());
        chkAutopilot.setSelected(inviteCf.getAutopilot() == null ? false : inviteCf.getAutopilot());

        txtTimeoutMin.setValue(inviteCf.getTimeoutMin() == null ? "20" : inviteCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(inviteCf.getTimeoutMax() == null ? "40" : inviteCf.getTimeoutMax().toString());

        txtActionMin.setValue(inviteCf.getActionMin() == null ? "20" : inviteCf.getActionMin().toString());
        txtActionMax.setValue(inviteCf.getActionMax() == null ? "40" : inviteCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(inviteCf.getAutopilotStart() == null ? "22:00:00" : inviteCf.getAutopilotStart().format(formatter));
    }

    private Set<Board> selectedBoards = new HashSet<>();

    void initBoards() {
        lstBoards.getItems().clear();
        lstBoards.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        lstBoards.setItems(boards);
        account.getBoards().forEach(x -> boards.add(x.copy(null)));

        lstBoards.setCellFactory((list) -> {
            return new ListCell<Board>() {
                @Override
                protected void updateItem(Board item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            };
        });

        for (AQuery b : inviteCf.getQueries()) {
            lstBoards.getSelectionModel().select(b.getBoardMapped());
        }
    }

    public String toCf() {
        try {
            toCf_nodes();
            toCf_mapping();
            return validate();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return "Unkown error at 'Invite' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Invite' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Invite' Configuration.";
            } else if (inviteCf.getQueries().size() <= 0) {
                return "Boards selection missing at 'Invite' Configuration.";
            }
        }
        return null;
    }

    void toCf_nodes() {
        inviteCf.setIsActive(chkEnabled.isSelected());
        inviteCf.setAutopilot(chkAutopilot.isSelected());

        inviteCf.setTimeoutMin(txtTimeoutMin.getValue());
        inviteCf.setTimeoutMax(txtTimeoutMax.getValue());

        inviteCf.setActionMin(txtActionMin.getValue());
        inviteCf.setActionMax(txtActionMax.getValue());

        inviteCf.setAutopilotStart(txtAutopilotMin.getValue());
    }

    void toCf_mapping() {
        inviteCf.getQueries().clear();
        for (Object b : lstBoards.getSelectionModel().getSelectedItems()) {
            AQuery q = Helpers.typeByPatter_invite(((Board) b).getUrlName(), (Board) b);
            inviteCf.getQueries().add(q);
        }

    }

}
