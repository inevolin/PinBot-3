/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import common.Http;
import common.KeyValuePair;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;

/**
 * FXML Controller class
 *
 * @author UGent
 */
public class KeywordGenController implements Initializable {

    private ExecutorService executor;
    private Boolean isInterrupted = false;

    @FXML
    private TextField txtKw;
    @FXML
    private TextArea txtKws;
    @FXML
    private Button btnGen, btnCopy;
    @FXML
    private Label lblStatus;
    @FXML
    private CheckBox chkMultiLevel;
    private Http http;
    private static final String URL = "http://clients1.google.com/complete/search?hl=en-usa&client=hp&q=";
    private Map<String, Set<String>> cache = new LinkedHashMap<>();

    public void setIsInterrupted(Boolean isInterrupted) {
        this.isInterrupted = isInterrupted;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initHttp();
        initBtns();
        initNodes();
    }

    public void initHttp() {
        try {
            http = new Http(null, new BasicCookieStore());
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
    }

    private void initNodes() {
        txtKw.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    btnSubmit();
                }
            }
        });

    }

    private void initBtns() {
        btnGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                btnSubmit();
            }
        });
        btnCopy.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Clipboard clipboard = toolkit.getSystemClipboard();
                StringSelection strSel = new StringSelection(txtKws.getText());
                clipboard.setContents(strSel, null);
            }
        });
    }

    private void toggleNodesDisabled(Boolean disable) {
        btnGen.setDisable(disable);
        btnCopy.setDisable(disable);
        txtKw.setDisable(disable);
        txtKws.setDisable(disable);
        chkMultiLevel.setDisable(disable);
    }

    private void btnSubmit() {
        String kw = txtKw.getText();
        if (kw == null || kw.length() == 0) {
            lblStatus.setText("Enter a keyword.");
            return;
        }
        toggleNodesDisabled(true);
        lblStatus.setText("Working...");
        action(kw, chkMultiLevel.isSelected());
    }

    private void action(String kw, Boolean multiLevel) {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                if (multiLevel) {
                    Set<String> ls = multiLevel(kw);
                    fillKws(ls);
                } else {
                    Set<String> ls = singleLevel(kw);
                    fillKws(ls);
                }
            } catch (InterruptedException | UnsupportedEncodingException ex) {
                common.ExceptionHandler.reportException(ex);
                
            } finally {
                executor.shutdown();
                executor = null;
                Platform.runLater(() -> {
                    lblStatus.setText("Done!");
                    toggleNodesDisabled(false);
                });
            }
        });
    }

    public Set<String> singleLevel(String kw) throws InterruptedException, UnsupportedEncodingException {
        Set<String> ls = cacheGet(kw);
        if (ls == null) {
            ls = start(kw);
            cachePut(kw, ls);
        }
        ls.remove(kw);//we don't need original kw in results
        return ls;
    }

    String multiLevelStatus(int count) {
        int total = 2 + (int) ('z' - 'a');
        return "Working... (" + count + "/" + total + ")";
    }
    private int countMLVL = 0;

    public Set<String> multiLevel(String kw) throws InterruptedException, UnsupportedEncodingException {
        countMLVL = 0;
        Set<String> ls = new LinkedHashSet<>();
        Platform.runLater(() -> lblStatus.setText(multiLevelStatus(countMLVL)));
        ls.addAll(singleLevel(kw));
        countMLVL++;
        Platform.runLater(() -> lblStatus.setText(multiLevelStatus(countMLVL)));
        for (char alpha = 'a'; alpha <= 'z'; alpha++) {
            String _kw = kw + " " + alpha; //suffix
            Set<String> ls_tmp = cacheGet(_kw);
            if (ls_tmp == null) {
                ls_tmp = start(_kw);
                cachePut(_kw, ls);
            }
            ls_tmp.remove(_kw);
            ls.addAll(ls_tmp);
            countMLVL++;
            Platform.runLater(() -> lblStatus.setText(multiLevelStatus(countMLVL)));
            if (isInterrupted) {
                return ls;
            }
        }
        ls.remove(kw);//we don't need original kw in results
        return ls;
    }

    void cachePut(String kw, Set<String> ls) {
        if (ls != null && ls.size() > 0) {
            if (cache.get(kw) == null) {
                cache.put(kw, ls);
            } else {
                cache.get(kw).addAll(ls);
            }
        }
    }

    Set<String> cacheGet(String kw) {
        return cache.get(kw);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", Http.ACCEPT_ANY);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private Set<String> start(String kw_) {
        try {
            String kw = URLEncoder.encode(kw_, StandardCharsets.UTF_8.toString());
            String _url = URL + kw;

            KeyValuePair<Integer> resp = http.Get(_url, getHeaders());
            Set<String> ls = parse(resp.getKey());
            return ls;
        } catch (InterruptedException | UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);
            
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
        return null;
    }

    private Set<String> parse(String json) {
        Set<String> ls = new LinkedHashSet<>();
        String rs = Http.unescape(json);
        rs = rs.replaceAll("\\<.*?\\>", "");
        Pattern pattern = Pattern.compile("\\[\"(.+?)\"");
        Matcher matcher = pattern.matcher(rs);
        while (matcher.find()) {
            String kw = (String) matcher.group(1);
            ls.add(kw);
        }

        return ls;
    }

    private void fillKws(Set<String> ls) {
        if (ls != null) {
            String[] arr = ls.toArray(new String[ls.size()]);
            String s = StringUtils.join(arr, "\r\n");
            Platform.runLater(() -> txtKws.setText(s));

        }
    }
}
