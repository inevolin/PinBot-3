/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.configurations.queue;

import common.KeyValuePair;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Account;
import model.pinterestobjects.Board;

public class BoardSelector_Controller {

    @FXML
    private ComboBox cboBoards;
    @FXML
    private Button btnOk;    
    @FXML
    private AnchorPane root;
    @FXML
    private GridPane grid;
    @FXML
    private Text txtStatus;

    private Stage stage;
    private TextField fieldURL;

    private boolean ok;
    private boolean showURL = false;
    private URL url;

    public void Init(Account acc, Stage stage, boolean showURL) {
        
        this.showURL = showURL; 
        if (showURL){
            Label lblURL = new Label("Enter URL:");
            lblURL.setMinWidth(85);
            
            fieldURL = new TextField();
            fieldURL.setMinWidth(150);
            fieldURL.setPromptText("http://www.example.com");
            
            grid.add(lblURL, 0, 1);
            grid.add(fieldURL, 1, 1);
            
            txtStatus.setFill(Color.RED);            
        }

        for (Board board : acc.getBoards()) {
            KeyValuePair kvp = new KeyValuePair(board.getName(), board);
            cboBoards.getItems().add(kvp);
        }

        cboBoards.getSelectionModel().selectFirst();
        ok = false;
        this.stage = stage;
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        btnOk.setOnAction((ActionEvent event) -> {
            if (showURL && !checkURL()){
                return;
            }
            
            ok = true;
            stage.close();
        });

    }
    
    private boolean checkURL(){
        if (fieldURL.getText() == null || "".equals(fieldURL.getText())){
            txtStatus.setText("No URL entered");
            fieldURL.requestFocus();
            return false;
        } else {
            try {
                url = new URL(fieldURL.getText());
                return true;
            } catch (MalformedURLException ex) {
                common.ExceptionHandler.reportException(ex);
                txtStatus.setText("Invalid URL");
                fieldURL.requestFocus();
                return false;
            }
        }
    }

    public boolean isOk() {
        return ok;
    }

    public Board getSelectedBoard() {
        return (Board) ((KeyValuePair) cboBoards.getSelectionModel().getSelectedItem()).getValue();
    }
    
    public String getURL(){
        return url == null ? null : url.toExternalForm();
    }

}
