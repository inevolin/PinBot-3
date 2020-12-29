/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;
import common.Http;
import common.KeyValuePair;
import common.MyUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import org.apache.http.impl.client.BasicCookieStore;
import pinbot3.PinBot3;

public final class Licensing {

    private final String CHECK = "https://pinbot3.com/reg/check.php";
    private final String TRIAL = "https://pinbot3.com/reg/trial.php";
    private Label lbl;

    public Licensing(Label lbl) {
        this.lbl = lbl;

    }

    public void check() {
        Platform.runLater(() -> lbl.setText("Checking license..."));
        init();
    }

    private void init() {
        PinBot3.PRIVS = new Privileges();
        String uuid = MyUtils.getRegistryByKey("UUID");
        if (uuid != null && uuid.length() > 0) {
            YES(uuid);
        } else {
            NO();
        }
    }

    void YES(String uuid) {
        KeyValuePair<Integer> resp = serverCheck(true, uuid);
        if (resp == null || resp.getKey().length() <= 0) {
            onErrorExit();
        } else if (resp.getKey().contains("error-occured")) {
            onErrorExit();
        } else if (resp.getKey().contains("invalid-uuid")) {
            NO();
        } else {
            processReponse(resp.getKey());
        }

    }

    void onErrorExit() {
        try {
            Platform.runLater(() -> lbl.setText("Server Error!\nTry again later.\nSupport: pinbot@healzer.com\nExiting..."));
            Thread.sleep(4000);
        } catch (InterruptedException ex) {
            //ignore
        }
        pinbot3.PinBot3.SystemExit();
    }

    void NO() {

        try {

            final FutureTask query = new FutureTask(new Callable() {
                @Override
                public String call() throws Exception {
                    String s = null;
                    String additionalMsg = "";
                    while (s == null || s.length() == 0) {
                        s = promptMenuLicense(additionalMsg);
                        if (s.equalsIgnoreCase("trial")) {
                            continueAsTrial();
                        } else if (s.equalsIgnoreCase("no-action")) {
                            s = null;
                        } else if (s.length() > 0) {
                            KeyValuePair<Integer> resp = serverCheck(false, s);
                            if (resp == null || resp.getKey().length() == 0) {
                                onErrorExit();
                            } else if (resp.getKey().contains("error-occured")) {
                                onErrorExit();
                            } else if (resp.getKey().contains("already-registered")) {
                                additionalMsg = "License already registered elsewhere.";
                                s = null;
                            } else if (resp.getKey().contains("invalid-tid")) {
                                additionalMsg = "License ID not found.";
                                s = null;
                            } else {
                                MyUtils.storeRegistry("UUID", resp.getKey());
                                YES(resp.getKey());
                            }
                        }
                    }
                    return s;
                }
            });
            Platform.runLater(query);
            query.get();

        } catch (InterruptedException | ExecutionException ex) {
            common.ExceptionHandler.reportException(ex);
            
        }

    }

    private void continueAsTrial() {
        //have they already registered for trial?
        String check = MyUtils.getRegistryByKey("TRIAL");
        if (check != null && check.length() > 0) {
            // all okay, continue loading...
        } else {
            //they must register/opt-in to use trial
            Boolean proceed = false;
            KeyValuePair<String> ret = promptMenuEmail("");
            while (!proceed) {
                if (ret == null) {
                    pinbot3.PinBot3.SystemExit();
                } else {
                    KeyValuePair<Integer> resp = serverRegister(ret.getKey(), ret.getValue());
                    if (resp != null && resp.getKey().contains("ok")) {
                        MyUtils.storeRegistry("TRIAL", resp.getKey().split(":")[1] ); //new version: server returns ok:ID  (ID is AutoIncrementID so we can retrieve name&email)
                        proceed = true;
                    } else {
                        ret = promptMenuEmail(resp.getKey());
                    }
                }

            }
        }
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", Http.ACCEPT_ANY);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    KeyValuePair<Integer> serverCheck(Boolean fromFile, String qry) {
        KeyValuePair<Integer> resp = null;
        try {
            Http http = new Http(null, new BasicCookieStore());
            String url = CHECK + "?" + (fromFile ? "uuid" : "tid") + "=" + URLEncoder.encode(qry, StandardCharsets.UTF_8.toString());

            resp = http.Get(url, getHeaders());
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
        return resp;
    }

    KeyValuePair<Integer> serverRegister(String name, String email) {
        KeyValuePair<Integer> resp = null;
        try {
            Http http = new Http(null, new BasicCookieStore());
            String url = TRIAL + "?" + "name=" + URLEncoder.encode(name, StandardCharsets.UTF_8.toString()) + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
            resp = http.Get(url, getHeaders());
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
        return resp;
    }

    void processReponse(String rs) {
        try {
            Gson gson = new Gson();
            LinkedTreeMap m = gson.fromJson(rs, LinkedTreeMap.class);

            if (m.get("premium") != null && (Boolean) m.get("premium")) {
                PinBot3.PRIVS.getMyPrivs().add(Privileges.TYPES.PREMIUM);
            }

        } catch (JsonSyntaxException ex) {
            common.ExceptionHandler.reportException(ex);
            
            onErrorExit();
        }

    }

    //adjust prompt: field for license; button SUBMIT; button exit; button Trial (disabled)
    String promptMenuLicense(String msg) {
        // http://examples.javacodegeeks.com/desktop-java/javafx/dialog-javafx/javafx-dialog-example/
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Licensing");
        dialog.setHeaderText("Enter your License ID below." + (msg == null || msg == "" ? "" : "\n" + msg));
        dialog.setResizable(false);
        dialog.setWidth(700);
        TextField text1 = new TextField();
        text1.setMinWidth(300);
        GridPane grid = new GridPane();
        grid.setMinWidth(700);
        grid.add(text1, 2, 1);
        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Ok", ButtonData.OK_DONE);
        ButtonType buttonTypeTrial = new ButtonType("Trial", ButtonData.OK_DONE);
        ButtonType buttonTypeBuy = new ButtonType("Buy Premium", ButtonData.OK_DONE);
        ButtonType buttonTypeReset = new ButtonType("Reset license", ButtonData.OK_DONE);
        ButtonType buttonTypeClose = new ButtonType("Exit", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeTrial);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeBuy);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeReset);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeClose);
        dialog.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {
                if (b == buttonTypeOk) {
                    String ret = text1.getText();
                    return ret;
                } else if (b == buttonTypeTrial) {
                    return "trial";
                } else if (b == buttonTypeBuy) {
                    Privileges.openWebpage("https://pinbot3.com/");
                    return "no-action";
                } else if (b == buttonTypeReset) {
                    Privileges.openWebpage("https://pinbot3.com/order/reset.php");
                    return "no-action";
                } else {
                    pinbot3.PinBot3.SystemExit();//close button clicked
                }
                return null;
            }
        });
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }

    KeyValuePair<String> promptMenuEmail(String msg) {
        // http://examples.javacodegeeks.com/desktop-java/javafx/dialog-javafx/javafx-dialog-example/
        Dialog<KeyValuePair<String>> dialog = new Dialog<>();
        dialog.setTitle("Trial version");
        dialog.setHeaderText("Enter your name and email address.\n" + msg);
        dialog.setResizable(false);
        dialog.setWidth(400);
        TextField txtName = new TextField();
        txtName.setMinWidth(400);
        TextField txtEmail = new TextField();
        txtEmail.setMinWidth(400);
        GridPane grid = new GridPane();
        grid.setMinWidth(400);
        Label lblName = new Label("Name: ");
        Label lblEmail = new Label("Email: ");
        grid.add(txtName, 2, 1);
        grid.add(txtEmail, 2, 2);
        grid.add(lblName, 1, 1);
        grid.add(lblEmail, 1, 2);
        dialog.getDialogPane().setContent(grid);
        ButtonType buttonTypeOk = new ButtonType("Register", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(buttonTypeOk);
        dialog.setResultConverter(new Callback<ButtonType, KeyValuePair<String>>() {
            @Override
            public KeyValuePair<String> call(ButtonType b) {
                if (b == buttonTypeOk) {
                    return new KeyValuePair<>(txtName.getText(), txtEmail.getText());
                }
                pinbot3.PinBot3.SystemExit();//close button clicked
                return null;
            }
        });
        Optional<KeyValuePair<String>> result = dialog.showAndWait();
        if (result.isPresent()) {
            return result.get();
        }
        return null;
    }


    /*
        * uuid file exists?
            YES:    - send uuid content to server.
                    - read message, fill PRIVS from response.
                    - continue...
            
            NO:     - prompt license id.
                    - send license id to server.
                    - read message, fill PRIVS from response.
                    - if invalid license ID.
                        -- re-prompt (until valid license or TRIAL clicked).
     */
}
