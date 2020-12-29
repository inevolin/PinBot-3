/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import common.Data;

import common.Http;
import common.KeyValuePair;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import model.Account;
import model.pinterestobjects.Board;
import model.pinterestobjects.Category;

/**
 * FXML Controller class
 *
 * @author UGent
 */
public class BoardsController implements Initializable {

    protected String base_url;
    private ExecutorService executor;

    @FXML
    private ListView lstBoards;
    @FXML
    private Label lblStatus;
    @FXML
    private Button btnDelete, btnAdd, btnEdit;
    private Account acc;
    private Http http;
    private ObservableList<Board> boards;
    private Set<Category> cats;

    // 1:  login user if not loggedin
    // 2:  load categories
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initBtns();

    }

    public void setAccount(Account acc) {
        executor = Executors.newSingleThreadExecutor();
        toggleNodesDisabled(true);
        this.acc = acc;
        lblStatus.setText("Loading boards...");
        executor.submit(() -> {
            checkLogin();
            this.base_url = this.acc.base_url;
            Platform.runLater(() -> {
                try {
                    fillBoards();
                    loadCategories();
                    toggleNodesDisabled(false);
                    lblStatus.setText("Ready.");
                } catch (InterruptedException ex) {
                    //ignore
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                    
                }
            });
        });
    }

    private void checkLogin() {
        try {
            http = new Http(acc.getProxy(), acc.getCookieStore());
            if (acc.getStatus() != Account.STATUS.LOGGEDIN) {
                pinbot3.PinBot3.accountMgr.Login(acc);
                pinbot3.PinBot3.accountMgr.refreshBoards(acc);
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
    }

    private void fillBoards() throws InterruptedException, ExecutionException {
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
        boards = FXCollections.observableArrayList();
        acc.getBoards().forEach(x -> boards.add(x));
        lstBoards.setItems(boards);
    }

    private void initBtns() {

        btnDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (common.Dialogs.OkCancelDialog( "Are you sure?", "Delete board") != ButtonType.OK) {
                    return;
                } else {
                    try {
                        deleteBoard();
                    } catch (UnsupportedEncodingException | InterruptedException ex) {
                        //ignore                        
                    } catch (Exception ex) {
                        common.ExceptionHandler.reportException(ex);                        
                    }
                }
            }
        }
        );
        btnAdd.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event
            ) {
                try {
                    addBoard();
                } catch (UnsupportedEncodingException | InterruptedException ex) {
                    //ignore
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);                    
                }
            }
        }
        );
        btnEdit.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event
            ) {
                try {
                    editBoard();
                } catch (UnsupportedEncodingException | InterruptedException ex) {
                    //ignore
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);                    
                }
            }
        }
        );

    }

    private void addBoard() throws UnsupportedEncodingException, InterruptedException, Exception {
        Board b = promptMenu("Add a new board.", null);
        if (b != null) {
            if (addBoard_POST(b)) {
                setBoardData(b);
                boards.add(b);
                syncBoards();
                lblStatus.setText("Ok");
                lblStatus.setTextFill(Color.GREEN);

            } else {
                lblStatus.setText("error occured #201.\nA board with same title exists.");
                lblStatus.setTextFill(Color.RED);
            }
        }
    }

    private void editBoard() throws UnsupportedEncodingException, InterruptedException, Exception {
        Board sb = (Board) (lstBoards.getSelectionModel().getSelectedItem());
        if (sb == null) {
            lblStatus.setText("Select a board to edit!");
            return;
        } else if (!setBoardData(sb)) {
            lblStatus.setText("error occured #213");
            return;
        }
        if (promptMenu("Edit your board.", sb) != null) {
            if (!editBoard_POST(sb)) {
                lblStatus.setText("error occured #218");
            } else {
                syncBoards();
                lstBoards.refresh();
            }
        }
    }

    void syncBoards() {
        acc.getBoards().clear();
        acc.getBoards().addAll(boards);
        pinbot3.PinBot3.dalMgr.Save(acc);
    }

    private Boolean setBoardData(Board b) throws InterruptedException, UnsupportedEncodingException, Exception {
        KeyValuePair<Integer> resp = http.Get(getData_EditInfoUrl(b), getHeaders(base_url + "/"));

        Gson gson = new Gson();
        Map<String, LinkedTreeMap> m0 = new HashMap<>();
        m0 = (Map<String, LinkedTreeMap>) gson.fromJson(resp.getKey(), Map.class);
        LinkedTreeMap s = (LinkedTreeMap) m0.get("resource_response");
        if (s.containsKey("data") && s.get("data") instanceof LinkedTreeMap) {
            Boolean ok = (Boolean) s.get("error") == null ? true : false;
            if (ok) {
                String cat = (String) ((LinkedTreeMap) s.get("data")).get("category");
                String desc = (String) ((LinkedTreeMap) s.get("data")).get("description");
                String id = (String) ((LinkedTreeMap) s.get("data")).get("id");
                String urlName = (String) ((LinkedTreeMap) s.get("data")).get("url");
                String name = (String) ((LinkedTreeMap) s.get("data")).get("name");
                String userId = (String) ((LinkedTreeMap) ((LinkedTreeMap) s.get("data")).get("owner")).get("id");
                Integer pin_count = ((LinkedTreeMap) s.get("data")).get("pin_count") instanceof Integer ? (Integer) ((LinkedTreeMap) s.get("data")).get("pin_count") : ((Double) ((LinkedTreeMap) s.get("data")).get("pin_count")).intValue();
                b.setPinsCount(pin_count);
                b.setFollowedByMe(true);
                b.setUsername(acc.getUsername());
                b.setUserId(userId);
                b.setName(name);
                b.setDescription(desc);
                b.setCategory(cat);
                b.setBoardId(id);
                b.setUrlName(urlName);
            }
            return ok;
        }
        return false;
    }

    private void deleteBoard() throws UnsupportedEncodingException, InterruptedException, Exception {
        Board sb = (Board) lstBoards.getSelectionModel().getSelectedItem();
        if (sb == null) {
            lblStatus.setText("Select a board to delete!");
            return;
        } else {
            Iterator<Board> it = boards.iterator();
            while (it.hasNext()) {
                Board nxt = it.next();
                if (nxt.getName().equalsIgnoreCase(sb.getName()) && nxt.getUrlName().equalsIgnoreCase(sb.getUrlName())) {
                    if (deleteBoard_POST(sb)) {
                        lstBoards.getItems().remove(nxt);
                        boards.remove(nxt);
                        syncBoards();
                    } else {
                        lblStatus.setText("error occured #279");
                    }
                    return;
                }
            }

        }
    }

    private String getData_EditInfoUrl(Board b) throws UnsupportedEncodingException {
        long TICKS_AT_EPOCH = 621355968000000000L;
        long ticks = System.currentTimeMillis() * 10000 + TICKS_AT_EPOCH;

        String s = "";
        s = base_url + "/resource/BoardResource/get/"
                + "?source_url="
                + URLEncoder.encode("/" + acc.getUsername() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"board_id\":\""
                        + b.getBoardId()
                        + "\",\"field_set_key\":\""
                        + "edit"
                        + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode("App>UserProfilePage>UserProfileContent>UserBoards>Grid>GridItems>Board>ShowModalButton(module=BoardEdit)", StandardCharsets.UTF_8.toString())
                + "&_=" + Long.toString(ticks);
        return s;
    }

    private Data getData_EditPost(Board b) throws UnsupportedEncodingException {
        Data data = new Data();
        data.content
                = "source_url="
                + URLEncoder.encode("/" + acc.getUsername() + "/" + b.getUrlName() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"name\":\""
                        + b.getName()
                        + "\",\"category\":\""
                        + b.getCategory()
                        + "\",\"description\":\""
                        + b.getDescription().replace("\"", "\\\"")
                        + "\",\"privacy\":\""
                        + "public"
                        + "\",\"layout\":\""
                        + "default"
                        + "\",\"board_id\":\""
                        + b.getBoardId()
                        + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode("App>BoardPage>BoardHeader>BoardInfoBar>ShowModalButton(module=BoardEdit)#App>ModalManager>Modal(state_isVisible=true, showCloseModal=true, state_mouseDownInModal=false, state_showModalMask=true, state_showContainer=false, state_showPositionElement=true, state_slow=false)", StandardCharsets.UTF_8.toString()
                );
        data.referer = base_url + "/";

        return data;
    }

    private Data getDataAdd(Board b) throws UnsupportedEncodingException {
        Data data = new Data();

        data.content
                = "source_url="
                + URLEncoder.encode("/" + acc.getUsername() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"name\":\""
                        + b.getName()
                        + "\",\"category\":\""
                        + b.getCategory()
                        + "\",\"description\":\""
                        + b.getDescription().replace("\"", "\\\"")
                        + "\",\"privacy\":\""
                        + "public"
                        + "\",\"layout\":\""
                        + "default"
                        + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode("App>UserProfilePage>UserProfileContent>UserBoards>Grid>GridItems>BoardCreateRep(ga_category=board_create, text=Create a board, submodule=[object Object])#App>ModalManager>Modal(state_isVisible=true, showCloseModal=true, state_mouseDownInModal=false, state_showModalMask=true, state_showContainer=false, state_showPositionElement=true, state_slow=false)", StandardCharsets.UTF_8.toString());
        data.referer = base_url + "/";

        return data;
    }

    private Data getDataDelete(Board b) throws UnsupportedEncodingException {
        Data data = new Data();
        data.content
                = "source_url="
                + URLEncoder.encode("/" + acc.getUsername() + "/" + b.getUrlName() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"board_id\":\""
                        + b.getBoardId()
                        + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode("App>ModalManager>Modal>ConfirmDialog(template=delete_board)", StandardCharsets.UTF_8.toString());
        data.referer = base_url + "/";

        return data;
    }

    private Map<String, String> getHeaders(String referer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", acc.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", acc.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-Pinterest-AppState", "active");
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("Referer", referer);
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private Boolean editBoard_POST(Board b) throws UnsupportedEncodingException, InterruptedException, Exception {
        Data data = getData_EditPost(b);
        KeyValuePair<Integer> resp = http.Post(base_url + "/resource/BoardResource/update/", data.content, getHeaders(data.referer));

        Gson gson = new Gson();
        Map<String, LinkedTreeMap> m0 = new HashMap<>();
        m0 = (Map<String, LinkedTreeMap>) gson.fromJson(resp.getKey(), Map.class);
        LinkedTreeMap s = (LinkedTreeMap) m0.get("resource_response");
        if (s.containsKey("data") && s.get("data") instanceof LinkedTreeMap) {
            Boolean ok = (Boolean) s.get("error") == null ? true : false;
            return ok;
        }
        return false;
    }

    private Boolean addBoard_POST(Board b) throws UnsupportedEncodingException, InterruptedException, Exception {
        Data data = getDataAdd(b);
        KeyValuePair<Integer> resp = http.Post(base_url + "/resource/BoardResource/create/", data.content, getHeaders(data.referer));
        if (resp.getValue() != 200) {
            return false;
        }

        Gson gson = new Gson();
        Map<String, LinkedTreeMap> m0 = new HashMap<>();
        m0 = (Map<String, LinkedTreeMap>) gson.fromJson(resp.getKey(), Map.class);
        LinkedTreeMap s = (LinkedTreeMap) m0.get("resource_response");
        if (s.containsKey("data") && s.get("data") instanceof LinkedTreeMap) {
            Boolean ok = (Boolean) s.get("error") == null ? true : false;
            if (ok) {
                String id = (String) ((LinkedTreeMap) s.get("data")).get("id");
                b.setBoardId(id);
            }
            return ok;
        }
        return false;
    }

    private Boolean deleteBoard_POST(Board b) throws UnsupportedEncodingException, InterruptedException, Exception {
        Data data = getDataDelete(b);
        KeyValuePair<Integer> resp = http.Post(base_url + "/resource/BoardResource/delete/", data.content, getHeaders(data.referer));

        Gson gson = new Gson();
        Map<String, LinkedTreeMap> m0 = new HashMap<>();
        m0 = (Map<String, LinkedTreeMap>) gson.fromJson(resp.getKey(), Map.class);
        LinkedTreeMap s = (LinkedTreeMap) m0.get("resource_response");
        if (s.containsKey("error")) {
            Boolean ok = (Boolean) s.get("error") == null ? true : false;
            return ok;
        }
        return false;
    }

    private void toggleNodesDisabled(Boolean disable) {
        lstBoards.setDisable(disable);
        btnAdd.setDisable(disable);
        btnEdit.setDisable(disable);
        btnDelete.setDisable(disable);
    }

    private Board promptMenu(String msg, Board editB) {
        // http://examples.javacodegeeks.com/desktop-java/javafx/dialog-javafx/javafx-dialog-example/
        Dialog<Board> dialog = new Dialog<>();
        dialog.setTitle("Board");
        dialog.setHeaderText(msg == null || msg == "" ? "" : "\n" + msg);
        dialog.setResizable(false);
        dialog.setWidth(400);
        Label lblTitle = new Label(), lblDesc = new Label();
        TextField txtTitle = new TextField();
        TextArea txtDesc = new TextArea();
        ComboBox cboCat = new ComboBox();
        for (Category c : cats) {
            KeyValuePair kvp = new KeyValuePair(c.getName(), c);
            cboCat.getItems().add(kvp);
            if (editB != null && editB.getCategory().equalsIgnoreCase(c.getKey())) {
                cboCat.getSelectionModel().select(kvp);
            }
        }
        if (editB != null) {
            txtTitle.setText(editB.getName());
            txtDesc.setText(editB.getDescription());
        } else {
            cboCat.setValue(cboCat.getItems().get(0));
        }
        lblTitle.setText("Title:");
        txtTitle.setMinWidth(400);
        lblDesc.setText("Description (optional):");
        txtDesc.setMinWidth(400);
        cboCat.setMinWidth(400);
        GridPane grid = new GridPane();
        grid.setMinWidth(400);
        grid.add(lblTitle, 2, 1);
        grid.add(txtTitle, 2, 2);
        grid.add(lblDesc, 2, 3);
        grid.add(txtDesc, 2, 4);
        grid.add(cboCat, 2, 5);
        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.setResultConverter(new Callback<ButtonType, Board>() {
            @Override
            public Board call(ButtonType b) {
                if (b == buttonTypeOk) {
                    Board board = editB == null ? new Board() : editB;
                    board.setCategory(((Category) ((KeyValuePair) cboCat.getSelectionModel().getSelectedItem()).getValue()).getKey());
                    board.setName(txtTitle.getText());
                    board.setDescription(txtDesc.getText());
                    return board;
                }
                return null;
            }
        });
        Optional<Board> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    private void loadCategories() throws InterruptedException {
        cats = new LinkedHashSet<>();
        Set<Category> tmp = pinbot3.PinBot3.scrapeMgr.ScrapeBoardCategories(acc);
        if (tmp != null) {
            cats.addAll(tmp);
        }
    }

}
