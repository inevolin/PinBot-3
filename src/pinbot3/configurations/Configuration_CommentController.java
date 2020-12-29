/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import model.Account;
import model.configurations.CommentConfiguration;
import model.configurations.queries.AQuery;
import model.configurations.queries.Helpers;
import model.pinterestobjects.Comment;
import org.apache.commons.lang3.StringUtils;
import pinbot3.views.helpers.MySpinner;
import pinbot3.views.helpers.TimeSpinner;

public class Configuration_CommentController extends AnchorPane {

    @FXML
    private CheckBox chkEnabled, chkAutopilot;
    @FXML
    private Button btnAdd, btnEdit, btnDelete;
    @FXML
    private MySpinner<Integer> txtTimeoutMin, txtTimeoutMax, txtActionMin, txtActionMax;
    @FXML
    private TimeSpinner txtAutopilotMin, txtAutopilotMax;
    @FXML
    private TextArea txtComments;
    @FXML
    private ListView lstQueries;

    private CommentConfiguration commentCf;
    private Account account;

    private ObservableList<AQuery> queries;
    private Map<AQuery, Set<Comment>> mapping = new LinkedHashMap<>();

    public Configuration_CommentController(Account acc, CommentConfiguration commentCf) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/pinbot3/configurations/views/Configuration_Comment.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.account = acc;
        this.commentCf = commentCf;
        this.setDisable(pinbot3.PinBot3.PRIVS.isTrial());

        init();
    }

    void init() {
        initBtns();
        initNodes();
        initTXT();
        initLST();

    }

    void initBtns() {
        btnAdd.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String s = prompt("");
                if (s != "") {
                    AQuery q = Helpers.typeByPatter_comment(s, null);
                    queries.add(q);
                    mapping.put(q, new LinkedHashSet<>());
                    if (!lstQueries.getItems().isEmpty()) {
                        txtComments.setDisable(false);
                    }
                    lstQueries.getSelectionModel().select(q);
                }
            }
        });
        btnEdit.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                AQuery q = (AQuery) lstQueries.getSelectionModel().getSelectedItem();
                String s = prompt(q.getQuery());
                if (s != "") {
                    q.setQuery(s);
                }
                lstQueries.refresh();
            }
        });
        btnDelete.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                txtComments.clear();
                AQuery q = (AQuery) lstQueries.getSelectionModel().getSelectedItem();
                queries.remove(q);
                mapping.remove(q);
                if (lstQueries.getItems().isEmpty()) {
                    txtComments.setDisable(true);
                }
            }
        });
    }

    String prompt(String s) {
        //http://code.makery.ch/blog/javafx-dialogs-official/
        TextInputDialog dialog = new TextInputDialog(s);
        dialog.setTitle("Enter query...");
        dialog.setHeaderText("");
        dialog.setContentText("Query: ");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            System.out.println(result.get());
            return result.get();
        }
        return "";
    }

    void initNodes() {
        chkEnabled.setSelected(commentCf.getIsActive() == null ? false : commentCf.getIsActive());
        chkAutopilot.setSelected(commentCf.getAutopilot() == null ? false : commentCf.getAutopilot());

        txtTimeoutMin.setValue(commentCf.getTimeoutMin() == null ? "20" : commentCf.getTimeoutMin().toString());
        txtTimeoutMax.setValue(commentCf.getTimeoutMax() == null ? "40" : commentCf.getTimeoutMax().toString());

        txtActionMin.setValue(commentCf.getActionMin() == null ? "20" : commentCf.getActionMin().toString());
        txtActionMax.setValue(commentCf.getActionMax() == null ? "40" : commentCf.getActionMax().toString());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        txtAutopilotMin.setValue(commentCf.getAutopilotStart() == null ? "22:00:00" : commentCf.getAutopilotStart().format(formatter));

    }

    //adding comments bug fix
    void initLST() {
        //fill queries
        queries = FXCollections.observableArrayList();
        queries.addAll(commentCf.getQueries());
        for (AQuery q : queries) {
            mapping.put(q, new LinkedHashSet<>());
        }
        lstQueries.setItems(queries);

        //fill comments
        Iterator<Comment> it = commentCf.getMapping().iterator();
        while (it.hasNext()) {
            Comment c = it.next();
            if (c.getSearchQuery() == null) {
                it.remove();
            } else {
                Optional<AQuery> find = mapping.keySet().stream().filter(x -> x.getQuery().equals(c.getSearchQuery())).findAny();
                if (find.isPresent()) {
                    mapping.get(find.get()).add(c);
                }
            }
        }

        lstQueries.setCellFactory((list) -> {
            return new ListCell<AQuery>() {
                @Override
                protected void updateItem(AQuery item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getQuery());
                    }
                }
            };
        });
        lstQueries.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<AQuery>() {
            @Override
            public void changed(ObservableValue<? extends AQuery> observable, AQuery oldValue, AQuery newValue) {
                String s = "";
                if (oldValue != null) {
                    persistComments(oldValue);
                }
                if (newValue != null) {
                    for (Comment c : mapping.get(newValue)) {
                        s += c.getText() + "\n";
                    }
                }
                txtComments.setText(s.trim());
            }
        });
        lstQueries.getSelectionModel().selectFirst();

        if (lstQueries.getItems().isEmpty()) {
            txtComments.setDisable(true);
        } else {
            txtComments.setDisable(false);
        }
    }

    void initTXT() {
        txtComments.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (!newValue) { //focus lost
                    AQuery q = (AQuery) lstQueries.getSelectionModel().getSelectedItem();
                    persistComments(q);
                }
            }
        });
    }

    void persistComments(AQuery q) {
        mapping.get(q).clear();
        List<String> lst = Arrays.asList(StringUtils.split(txtComments.getText().trim(), "\n"));
        for (String s : lst) {
            Comment c0 = new Comment(q.getResource(), q);
            c0.setText(s);
            mapping.get(q).add(c0);
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
        return "Unkown error at 'Comment' Configuration.";
    }

    String validate() {
        if (chkEnabled.isSelected()) {
            if (txtTimeoutMax.getValue() < txtTimeoutMin.getValue()) {
                return "Timeout Max value must be greater or equal to Min value at 'Comment' Configuration.";
            } else if (txtActionMax.getValue() < txtActionMin.getValue()) {
                return "Action Max value must be greater or equal to Min value at 'Comment' Configuration.";
            } else if (commentCf.getMapping().size() <= 0) {
                return "Queries/comments mapping missing at 'Comment' Configuration.";
            }
        }
        return null;
    }

    void toCf_nodes() {
        commentCf.setIsActive(chkEnabled.isSelected());
        commentCf.setAutopilot(chkAutopilot.isSelected());

        commentCf.setTimeoutMin(txtTimeoutMin.getValue());
        commentCf.setTimeoutMax(txtTimeoutMax.getValue());

        commentCf.setActionMin(txtActionMin.getValue());
        commentCf.setActionMax(txtActionMax.getValue());

        commentCf.setAutopilotStart(txtAutopilotMin.getValue());
    }

    void toCf_mapping() {
        Set<Comment> temp = new LinkedHashSet<>();
        for (Map.Entry<AQuery, Set<Comment>> kv : mapping.entrySet()) {
            if (queries.contains(kv.getKey())) {
                temp.addAll(kv.getValue());
            }
        }

        commentCf.getMapping().clear();
        commentCf.getMapping().addAll(temp);

        commentCf.getQueries().clear();
        commentCf.getQueries().addAll(queries);
    }

}