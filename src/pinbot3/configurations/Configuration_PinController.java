/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Account;
import model.configurations.PinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import org.apache.commons.lang3.StringUtils;
import pinbot3.configurations.queue.EditQueue_Controller;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;
import model.configurations.queries.Helpers;
import pinbot3.ConfigurationsController;

/**
 * commit: ALT + 3 push: ALT + 4 pull: ALT + 5
 *
 * before you can push, you have to pull what others have changed before you can
 * push , you also need to make a commit of your own changes
 */
public class Configuration_PinController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private Button btnEditQueue;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax, txtDescUrlRate, txtSourceUrlRate;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtSourceURLs, txtDescURLs, txtQueries;
    @FXML
    private ListView lstBoards;

    private PinConfiguration pinCf;
    private ConfigurationsController parentCtrl;

    private Account account;

    private HashMap<Board, LinkedHashSet<String>> mapping = new HashMap<>();
    private ObservableList<Board> boards = FXCollections.observableArrayList();

    public Configuration_PinController(Account acc, PinConfiguration pinCf, ConfigurationsController parentCtrl) {
        this.parentCtrl = parentCtrl;
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Pin.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.pinCf = pinCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {
        initNodes();
        initMapping();
        initLST();
        initTXT();
    }

    void initNodes() {
        chkEnabled.setSelected(pinCf.getIsActive() == null ? false : pinCf.getIsActive());
        chkAutopilot.setSelected(pinCf.getAutopilot() == null ? false : pinCf.getAutopilot());

        txtTimeoutMin.setValue(pinCf.getTimeoutMin() == null ? "20" : pinCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(pinCf.getTimeoutMax() == null ? "40" : pinCf.getTimeoutMax().toString());

        txtActionMin.setValue(pinCf.getActionMin() == null ? "20" : pinCf.getActionMin().toString());
        txtActionMax.setValue(pinCf.getActionMax() == null ? "40" : pinCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(pinCf.getAutopilotStart() == null ? "22:00:00" : pinCf.getAutopilotStart().format(formatter));

        txtDescUrlRate.setValue(pinCf.getDescUrlRate() == null ? "0" : pinCf.getDescUrlRate().toString());
        txtDescURLs.setText(pinCf.getDescUrls() == null ? "" : StringUtils.join(pinCf.getDescUrls(), "\r\n"));

        txtSourceUrlRate.setValue(pinCf.getSourceUrlRate() == null ? "0" : pinCf.getSourceUrlRate().toString());
        txtSourceURLs.setText(pinCf.getSourceUrls() == null ? "" : StringUtils.join(pinCf.getSourceUrls(), "\r\n"));

        btnEditQueue.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {

                    if (parentCtrl.validateAll() != null) {
                        return; //label will show message
                    }

                    FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/configurations/queue/views/EditQueue.fxml"));
                    Parent parent = (Parent) fxmlloader.load();
                    EditQueue_Controller ctrl = fxmlloader.getController();
                    Stage stage = new Stage();
                    Scene scene = new Scene(parent);
                    ctrl.Init(account, pinCf, stage, scene);
                    stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
                    stage.setTitle("Queue editor");
                    stage.initModality(Modality.NONE);
                    stage.initOwner(btnEditQueue.getScene().getWindow());
                    stage.setScene(scene);
                    stage.showAndWait();
                } catch (IOException ex) {
                    common.ExceptionHandler.reportException(ex);
                    
                }
            }
        });
    }

    void initMapping() {
        lstBoards.getItems().clear();
        for (Board b : account.getBoards()) {
            boards.add(b.copy(null));
            mapping.put(b.copy(null), new LinkedHashSet<>());
        }

        for (AQuery q : pinCf.getQueries()) {
            if (mapping.containsKey(q.getBoardMapped())) {
                mapping.get(q.getBoardMapped()).add(q.getQuery());
            }
        }
    }

    void initLST() {
        lstBoards.setItems(boards);
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
        lstBoards.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Board>() {
            @Override
            public void changed(ObservableValue<? extends Board> observable, Board oldValue, Board newValue) {
                String s = "";
                for (String q : mapping.get(newValue)) {
                    s += q + "\n";
                }
                txtQueries.setText(s.trim());
            }
        });
        lstBoards.getSelectionModel().selectFirst();
    }

    void initTXT() {
        txtQueries.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null) {
                    newValue = "";
                }
                Board b = (Board) lstBoards.getSelectionModel().getSelectedItem();
                Set<String> qs = mapping.get(b);
                qs.clear();
                List<String> lst = Arrays.asList(StringUtils.split(newValue.trim(), "\n"));
                qs.addAll(lst);
            }
        });
    }

    public String toCf() {
        try {
            toCf_nodes();
            toCf_mapping();
            return validate();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return "Unkown error at 'Pin' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Pin' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Pin' Configuration.";
            } else if (pinCf.getQueries().size() <= 0 && pinCf.getQueue().size() <= 0) {
                return "Boards/queries mapping missing at 'Pin' Configuration.";
            }
        }
        return null;
    }

    void toCf_nodes() {
        pinCf.setIsActive(chkEnabled.isSelected());
        pinCf.setAutopilot(chkAutopilot.isSelected());

        pinCf.setTimeoutMin(txtTimeoutMin.getValue());
        pinCf.setTimeoutMax(txtTimeoutMax.getValue());

        pinCf.setActionMin(txtActionMin.getValue());
        pinCf.setActionMax(txtActionMax.getValue());

        pinCf.setAutopilotStart(txtAutopilotMin.getValue());

        pinCf.setDescUrlRate(txtDescUrlRate.getValue());
        Set<String> desc_urls = new HashSet<>(Arrays.asList(StringUtils.split(txtDescURLs.getText(), "\r\n")));
        pinCf.setDescUrls(desc_urls);

        pinCf.setSourceUrlRate(txtSourceUrlRate.getValue());
        Set<String> src_urls = new HashSet<>(Arrays.asList(StringUtils.split(txtSourceURLs.getText(), "\r\n")));
        pinCf.setSourceUrls(src_urls);
    }

    void toCf_mapping() {
        pinCf.getQueries().clear();
        for (Map.Entry<Board, LinkedHashSet<String>> entry : mapping.entrySet()) {
            for (String s : entry.getValue()) {
                Board b = entry.getKey().copy(null);
                AQuery q = Helpers.typeByPatter_pin(s, b).copy(null); //
                pinCf.getQueries().add(q);
            }
        }
    }

}
