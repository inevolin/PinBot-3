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
import model.Campaign;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import org.apache.commons.lang3.StringUtils;
import pinbot3.configurations.queue.EditQueue_Controller;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;
import model.configurations.queries.Helpers;
import pinbot3.ConfigurationsController;

/**
 *
 *
 * @author ROBT
 */
public class Configuration_RepinController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private Button btnEditQueue;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax, txtDescUrlRate;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtDescURLs, txtQueries;
    @FXML
    private ListView lstBoards;

    private RepinConfiguration repinCf;
    private Campaign campaign;
    private Account account;

    private ConfigurationsController parentCtrl;

    private HashMap<Board, LinkedHashSet<String>> mapping = new HashMap<>();
    private ObservableList<Board> boards = FXCollections.observableArrayList();

    public Configuration_RepinController(Account acc, RepinConfiguration repinCf, ConfigurationsController parentCtrl) {
        this.parentCtrl = parentCtrl;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/pinbot3/configurations/views/Configuration_Repin.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.repinCf = repinCf;

        init();
    }

    void init() {
        initNodes();
        initMapping();
        initLST();
        initTXT();
    }

    void initNodes() {
        chkEnabled.setSelected(repinCf.getIsActive() == null ? false : repinCf.getIsActive());
        chkAutopilot.setSelected(repinCf.getAutopilot() == null ? false : repinCf.getAutopilot());

        txtTimeoutMin.setValue(repinCf.getTimeoutMin() == null ? "20" : repinCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(repinCf.getTimeoutMax() == null ? "40" : repinCf.getTimeoutMax().toString());

        txtActionMin.setValue(repinCf.getActionMin() == null ? "20" : repinCf.getActionMin().toString());
        txtActionMax.setValue(repinCf.getActionMax() == null ? "40" : repinCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(repinCf.getAutopilotStart() == null ? "22:00:00" : repinCf.getAutopilotStart().format(formatter));

        txtDescUrlRate.setValue(repinCf.getDescUrlRate() == null ? "0" : repinCf.getDescUrlRate().toString());
        txtDescURLs.setText(repinCf.getDescUrls() == null ? "" : StringUtils.join(repinCf.getDescUrls(), "\r\n"));

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
                    ctrl.Init(account, repinCf, stage, scene);
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
            mapping.put(b, new LinkedHashSet<>());
        }

        for (AQuery q : repinCf.getQueries()) {
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
        return "Unkown error at 'Repin' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Repin' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Repin' Configuration.";
            } else if (repinCf.getQueries().size() <= 0 && repinCf.getQueue().size() <= 0) {
                return "Boards/queries mapping missing at 'Repin' Configuration.";
            }
        }
        return null;
    }

    void toCf_nodes() {
        repinCf.setIsActive(chkEnabled.isSelected());
        repinCf.setAutopilot(chkAutopilot.isSelected());

        repinCf.setTimeoutMin(txtTimeoutMin.getValue());
        repinCf.setTimeoutMax(txtTimeoutMax.getValue());

        repinCf.setActionMin(txtActionMin.getValue());
        repinCf.setActionMax(txtActionMax.getValue());

        repinCf.setAutopilotStart(txtAutopilotMin.getValue());

        repinCf.setDescUrlRate(txtDescUrlRate.getValue());
        Set<String> desc_urls = new HashSet<>(Arrays.asList(StringUtils.split(txtDescURLs.getText(), "\r\n")));
        repinCf.setDescUrls(desc_urls);

    }

    void toCf_mapping() {
        repinCf.getQueries().clear();
        for (Map.Entry<Board, LinkedHashSet<String>> entry : mapping.entrySet()) {
            for (String s : entry.getValue()) {
                AQuery q = Helpers.typeByPatter_repin(s, entry.getKey());
                repinCf.getQueries().add(q);
            }
        }
    }
}
