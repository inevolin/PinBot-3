/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import Managers.DALManager.Errors;
import common.Validator;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Account;
import model.Proxy;

/**
 * FXML Controller class
 *
 * @author UGent
 */
public class AccountController implements Initializable {

    private ExecutorService executor;

    @FXML
    private TextField txtEmail, txtPassword, txtProxyUser, txtProxyPass, txtIP;
    @FXML
    private Button btnSave;
    @FXML
    private CheckBox chkProxy;
    @FXML
    private Label lblStatus;
    private Stage stage;
    private Account account;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
        LoadAccount();
        switchProxy();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        LoadAccount();
        switchProxy();

    }

    private void LoadAccount() {
        if (account == null) {
            return;
        }
        txtEmail.setDisable(true);
        txtEmail.setText(account.getEmail());
        txtPassword.setText(account.getPassword());
        if (account.getProxy() != null) {
            if (account.getProxy().getEnabled() != null && account.getProxy().getEnabled()) {
                chkProxy.setSelected(true);
            }
            txtIP.setText(account.getProxy().getIp() + ":" + account.getProxy().getPort());
            txtProxyUser.setText(account.getProxy().getUser());
            txtProxyPass.setText(account.getProxy().getPassword());
        }

    }

    @FXML
    protected void chkProxy_Click(ActionEvent event) {
        switchProxy();

    }

    private void switchProxy() {
        txtProxyUser.setDisable(!chkProxy.isSelected());
        txtProxyPass.setDisable(!chkProxy.isSelected());
        txtIP.setDisable(!chkProxy.isSelected());
    }

    @FXML
    protected void btnSave_Click(ActionEvent event) {

        if (executor != null) {
            //account.getHttp().Abort();
            executor.shutdown(); //or app will keep running after close!
            btnSave.setText("Save");
            lblStatus.setText("Status");
            lblStatus.setTextFill(Color.BLACK);
            executor = null;
            return;
        }

        String email = txtEmail.getText().trim();
        String passw = txtPassword.getText().trim();
        if (email.length() <= 1 || !Validator.isValidEmailAddress(email)) {
            lblStatus.setText("Invalid email address.");
            lblStatus.setTextFill(Color.RED);
            txtEmail.requestFocus();
            return;
        }
        if (passw.length() <= 3) {
            lblStatus.setText("Invalid password.");
            lblStatus.setTextFill(Color.RED);
            txtPassword.requestFocus();
            return;
        }

        Proxy pr = configureProxy();
        if (pr == null) {
            return;
        }

        if (this.account == null) {
            account = new Account();
        }
        account.setEmail(email);
        account.setPassword(passw);
        account.setProxy(pr);

        lblStatus.setText("Testing account login...");
        lblStatus.setTextFill(Color.ORANGE);
        btnSave.setText("Abort");

        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            if (testLogin()) {
                saveAccount();
                stage.close();
            } else {
                Platform.runLater(() -> lblStatus.setText("Unable to login, wrong email or password?"));
                Platform.runLater(() -> lblStatus.setTextFill(Color.RED));
            }
            Platform.runLater(() -> btnSave.setText("Save"));
            executor.shutdown(); //or app will keep running after close!
            executor = null;
        });

    }

    private Proxy configureProxy() {
        Proxy pr = new Proxy();
        pr.setEnabled(chkProxy.isSelected());
        String ip = txtIP.getText() == null ? "" : txtIP.getText();
        String port = null;
        if (ip.contains(":")) {
            port = ip.substring(ip.indexOf(":") + 1, ip.length());
            ip = ip.substring(0, ip.indexOf(":"));
            pr.setPort(Integer.parseInt(port));
        }
        String user = txtProxyUser.getText() == null ? "" : txtProxyUser.getText();
        String pass = txtProxyPass.getText() == null ? "" : txtProxyPass.getText();
        if (chkProxy.isSelected() && (ip.length() <= 5 || !Validator.isValidProxy(ip + ":" + port))) {
            lblStatus.setText("Invalid proxy (IP:Port)");
            lblStatus.setTextFill(Color.RED);
            txtIP.requestFocus();
            return null;
        } else if (chkProxy.isSelected() && (user.length() > 0 && pass.length() <= 0)) {
            lblStatus.setText("Proxy pass missing.");
            lblStatus.setTextFill(Color.RED);
            txtProxyPass.requestFocus();
            return null;
        } else if (chkProxy.isSelected() && (user.length() <= 0 && pass.length() > 0)) {
            lblStatus.setText("Proxy login missing.");
            lblStatus.setTextFill(Color.RED);
            txtProxyUser.requestFocus();
            return null;
        }
        pr.setIp(ip);
        pr.setUsername(user);
        pr.setPassword(pass);
        return pr;
    }

    private Boolean testLogin() {
        try {
            account.setStatus(Account.STATUS.UNAUTHORIZED);
            pinbot3.PinBot3.accountMgr.Login(account);
            pinbot3.PinBot3.accountMgr.refreshBoards(account);
            Boolean ret = account.getStatus().equals(Account.STATUS.LOGGEDIN) && (account.getMyBoards() > 0 ? account.getBoards().size() > 0 : true);
            return ret; //even if user has no boards, let them proceed ; unless their stats show boards but scraping failed
        } catch (ConnectException ex) {
            common.ExceptionHandler.reportException(ex);
            Platform.runLater(() -> lblStatus.setText("Proxy not working!"));
            Platform.runLater(() -> lblStatus.setTextFill(Color.RED));
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            Platform.runLater(() -> lblStatus.setText("Unable to login, wrong email or password?"));
            Platform.runLater(() -> lblStatus.setTextFill(Color.RED));
        }
        return false;
    }

    private void saveAccount() {
        Errors err = Errors.NO_ERROR;
        if (account.getStatus().equals(Account.STATUS.LOGGEDIN)) {
            err = pinbot3.PinBot3.dalMgr.Save(account);
        }
        switch (err) {
            case NO_ERROR:
                Platform.runLater(() -> txtEmail.setDisable(true));
                Platform.runLater(() -> lblStatus.setText("Account saved!"));
                Platform.runLater(() -> lblStatus.setTextFill(Color.GREEN));
                if (!pinbot3.PinBot3.campaignMgr.getAccounts().contains(account)) {
                    pinbot3.PinBot3.campaignMgr.getAccounts().add(account);
                }
                //clearFields();
                //account = null;
                Platform.runLater(() -> {
                    btnSave.setText("Save");
                });
                executor = null;
                break;
            case EXISTS:
                Platform.runLater(() -> lblStatus.setText("Account already exists!"));
                Platform.runLater(() -> lblStatus.setTextFill(Color.RED));
                break;
            case DATABASE_ERROR:
                Platform.runLater(() -> lblStatus.setText("Database error, contact pinbot@healzer.com"));
                Platform.runLater(() -> lblStatus.setTextFill(Color.RED));
                break;
        }
    }

    /* private void clearFields() {
        txtEmail.clear();
        txtPassword.clear();
        txtIP.clear();
        txtProxyPass.clear();
        txtProxyUser.clear();
        chkProxy.setSelected(false);
    }*/
}
