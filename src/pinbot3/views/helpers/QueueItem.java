/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.views.helpers;

import common.KeyValuePair;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import model.Account;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import pinbot3.configurations.queue.EditQueue_Controller;

public class QueueItem extends VBox {

    private ImageView imageView;
    private VBox vBoxControls;
    private ComboBox cboBoard;
    private TextArea txtDesc;
    private TextField txtLink;
    private Button btnDelete;
    private Image image;

    private String imageUrl, description, sourceUrl;

    private boolean deleted = false;

    private PinterestObject object;
    private KeyValuePair selectedBoardKVP;
    private Account account;
    private EditQueue_Controller parent;

    private final int WIDTH = 100, HEIGHT = 100;

    public QueueItem(Account acc, KeyValuePair selectedBoardKVP, PinterestObject pinObject, EditQueue_Controller parent) {
        this.object = pinObject;
        this.selectedBoardKVP = selectedBoardKVP;
        this.account = acc;
        this.parent = parent;

        Pin p = (Pin) pinObject;
        imageUrl = p.getHashUrl();
        description = p.getDescription();
        sourceUrl = p.getSourceUrl();

        init();
    }

    private void init() {
        setAlignment(Pos.CENTER);
        setSpacing(5);

        image = new Image(imageUrl, true);
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(HEIGHT);
        imageView.setFitWidth(WIDTH);
        imageView.setImage(image);

        VBox containerImg = new VBox();
        containerImg.setOnMouseEntered((MouseEvent event) -> {
            parent.showImage(image);
        });
        containerImg.setOnMouseExited((MouseEvent event) -> {
            parent.hideImage(event);
        });
        containerImg.setMinHeight(HEIGHT);
        containerImg.setMinWidth(WIDTH + 50);
        containerImg.setAlignment(Pos.CENTER);
        containerImg.getChildren().add(imageView);

        cboBoard = new ComboBox();
        cboBoard.setMinWidth(WIDTH + 50);

        for (Board board : account.getBoards()) {
            KeyValuePair kvp = new KeyValuePair(board.getName(), board);
            cboBoard.getItems().add(kvp);
        }

        cboBoard.getSelectionModel().select(selectedBoardKVP);

        txtDesc = new TextArea();
        txtDesc.setMinWidth(WIDTH + 50);
        txtDesc.setPrefColumnCount(2);
        txtDesc.setWrapText(true);
        txtDesc.setText(description);

        txtLink = new TextField();
        txtLink.setMinWidth(WIDTH + 50);
        txtLink.setText(sourceUrl != null ? sourceUrl : "");

        btnDelete = new Button("Delete");
        btnDelete.setMinWidth(60);
        btnDelete.setOnAction((ActionEvent event) -> {
            setVisible(false);
            setDisable(true);
            deleted = true;
        });

        vBoxControls = new VBox();
        vBoxControls.setSpacing(5);
        vBoxControls.setAlignment(Pos.CENTER);
        vBoxControls.getChildren().addAll(cboBoard, txtDesc, txtLink, btnDelete);

        this.getChildren().addAll(containerImg, vBoxControls);
    }

    public PinterestObject getPinterestObject() {
        ((Pin) object).setDescription(txtDesc.getText());
        ((Pin) object).setSourceUrl(txtLink.getText());
        ((Pin) object).setDestinationBoardId(getBoard().getBoardId());
        return object;
    }

    public Board getBoard() {
        return  (Board) ((KeyValuePair) cboBoard.getSelectionModel().getSelectedItem()).getValue();
    }

    public String getUID() {
        return imageUrl;
    }

    public boolean isDeleted() {
        return deleted;
    }

}
