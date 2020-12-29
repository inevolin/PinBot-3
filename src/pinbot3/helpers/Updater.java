/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.helpers;

import common.Http;
import common.KeyValuePair;
import common.MyUtils;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javafx.application.Platform;
import javafx.scene.control.Label;
import org.apache.http.impl.client.BasicCookieStore;

public class Updater implements Serializable {

    private final String FileNameU = "PinBot3Updater.jar"; //The file that will be saved on your computer
    private final String FileNameP = "PinBot3.jar"; //The file that will be saved on your computer
    private String execPath_local = null;
    private String execPath_pinbot3 = null;
    private final String UPDATERTXT = "https://pinbot3.com/reg/updater.txt";
    private final String UPDATERJAR = "https://pinbot3.com/reg/PinBot3Updater.jar";
    private final String UPDATETXT = "https://pinbot3.com/reg/update.txt";
    private Label lbl;

    public Updater(Label lbl) {
        try {
            this.lbl = lbl;
            this.execPath_local = MyUtils.execPath(true);
            this.execPath_pinbot3 = MyUtils.execPath(false); // used for PinBot3.jar localization
            if (execPath_local == null) {
                Platform.runLater(() -> lbl.setText("Error updating!\nPlease try again later.\nSupport: pinbot@healzer.com\nExiting..."));
                Thread.sleep(4000);
                pinbot3.PinBot3.SystemExit();
            }
        } catch (UnsupportedEncodingException | InterruptedException ex) {
            //ignore
        }
    }

    public Boolean newPVersion() throws IOException, Exception {
        return getThisVersion(FileNameP) < getNewVersion(UPDATETXT);
    }

    public int getCurrentProgramVersion() {
        try {
            return getThisVersion(FileNameP);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
            return -1;
        }
    }

    public void checkP() {
        Boolean ret;
        try {
            Platform.runLater(() -> lbl.setText("Checking for update\n (2/2) ..."));
            if (getThisVersion(FileNameP) < getNewVersion(UPDATETXT)) {
                Platform.runLater(() -> lbl.setText("Launching updater..."));
                MyUtils.attemptRun(FileNameU);
                Thread.sleep(1000);
                pinbot3.PinBot3.SystemExit();
                ret = false;
            } else {
                ret = true;
            }
        } catch (UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);

            ret = false;
        } catch (InterruptedException ex) {
            //ignore
            ret = false;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            ret = false;
        }
        if (!ret) {
            try {
                Platform.runLater(() -> lbl.setText("Error updating!\nPlease try again later.\nSupport: pinbot@healzer.com\nExiting..."));
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                //ignore
            } finally {
                pinbot3.PinBot3.SystemExit();
            }
        }

    }

    public void btnNewUpdateAttemptRun() {
        MyUtils.attemptRun(FileNameU);
        pinbot3.PinBot3.SystemExit();
    }

    public void checkU() {
        try {
            Platform.runLater(() -> lbl.setText("Checking for update\n (1/2) ..."));
            File updaterFile = new File(execPath_local + File.separatorChar + FileNameU);
            if (!updaterFile.exists() || getThisVersion(FileNameU) < getNewVersion(UPDATERTXT)) {
                Platform.runLater(() -> lbl.setText("Updating..."));
                Boolean success = download(FileNameU, UPDATERJAR);
                if (success) {
                    Platform.runLater(() -> lbl.setText("Success!"));
                } else {
                    Platform.runLater(() -> lbl.setText("\"Update ERROR!\nPlease try again later.\nSupport: pinbot@healzer.com\n...\""));
                    Thread.sleep(4000);
                }
            }
        } catch (UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);

        } catch (InterruptedException | IOException ex) {
            common.ExceptionHandler.reportException(ex);

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
    }

    public String getAppVersion() {
        try {
            return getThisVersionStr(FileNameP, execPath_pinbot3);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return "3.0";
    }

    String getThisVersionStr(String fileName, String execPath) throws IOException {
        File tmp = new File(execPath + File.separatorChar + fileName);
        if (!Files.exists(tmp.toPath())) {
            return null;
        }
        JarFile jf = new JarFile(execPath + File.separatorChar + fileName);
        Manifest fm = jf.getManifest();
        String v = fm.getMainAttributes().getValue("Implementation-Version");
        System.err.println(v);
        jf.close();
        return v;
    }

    int getThisVersion(String fileName) throws IOException {
        try {
            return Integer.parseInt(getThisVersionStr(fileName, execPath_pinbot3).replace(".", ""));
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            return 0;
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

    int getNewVersion(String updatetxt) throws IOException, Exception {
        Http http = new Http(null, new BasicCookieStore());
        KeyValuePair<Integer> resp = http.Get(updatetxt, getHeaders());
        return Integer.parseInt(resp.getKey().trim().replace(".", ""));
    }

    Boolean download(String fileNameU, String strJar) {
        try {
            Http http = new Http(null, new BasicCookieStore());
            http.downloadFile(strJar, execPath_local + File.separatorChar + fileNameU);
            return true;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return false;
    }

}
