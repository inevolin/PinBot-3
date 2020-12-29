/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3updater;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class FXMLDocumentController implements Initializable {

    private String fileName = "PinBot3.jar"; //The file that will be saved on your computer
    private String execPath = null;
    private String UPDATETXT = "https://pinbot3.com/reg/update.txt";
    private String UPDATEJAR = "https://pinbot3.com/reg/PinBot3.jar";

    @FXML
    private Label lbl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    setLbl("Looking for updates...");
                    bgWork();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    finish("Update ERROR!\nPlease try again later.\nSupport: pinbot@healzer.com\nExiting updater...", 4000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    finish("Update ERROR!\nPlease try again later.\nSupport: pinbot@healzer.com\nExiting updater...", 4000);
                }
            }
        });

    }

    private void bgWork() throws IOException, Exception {
        execPath();
        if (getThisVersion() < getNewVersion()) {
            setLbl("Updating...");
            Boolean success = download();
            if (success) {
                attemptRun(fileName);
                finish("Success!\nExiting updater...", 2000);
                //attempt start downloaded JAR
            } else {
                finish("Update ERROR!\nPlease try again later.\nSupport: pinbot@healzer.com\nExiting updater...", 4000);
            }
        } else {
            finish("Nothing to update.\nExiting...", 3000);
        }
    }

    private void execPath() throws UnsupportedEncodingException {
        /*String path = pinbot3updater.PinBot3Updater.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        execPath = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());*/
        File f = new File(System.getProperty("java.class.path"));
        File dir = f.getAbsoluteFile().getParentFile();
        String path = dir.toString();
        execPath = path;
         
        System.err.println(execPath);
    }

    int getThisVersion() throws IOException {
        File tmp = new File(execPath + File.separatorChar + fileName);
        if (!Files.exists(tmp.toPath())) {
            return -1;
        }
        JarFile jf = new JarFile(execPath + File.separatorChar + fileName);
        Manifest fm = jf.getManifest();
        String v = fm.getMainAttributes().getValue("Implementation-Version");
        System.err.println(v);
        jf.close();
        return Integer.parseInt(v.replace(".", ""));
    }

    int getNewVersion() throws IOException, Exception {
        Http http = new Http(new CookieManager());        
        String rs = http.Get(UPDATETXT, null, Http.ACCEPT_ANY);   
        System.err.println(rs);
        return Integer.parseInt(rs.trim().replace(".", ""));
    }

    Boolean download() {
        try {
            Http http = new Http(new CookieManager());
            http.downloadFile(UPDATEJAR, execPath + File.separatorChar + fileName);            
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }
        return false;
    }

    void finish(String msg, int delay) {
        setLbl(msg);
        Long start = (new Date()).getTime();
        Long stop = (new Date()).getTime();
        while ((stop - start) < delay) {
            try {
                Thread.sleep(100);
                stop = (new Date()).getTime();
            } catch (InterruptedException ex) {
                //ignore
            }
        }
        System.exit(0);
    }

    void setLbl(String msg) {
        Platform.runLater(() -> lbl.setText(msg));
    }

    void attemptRun(String fileName) {
        try {
            File jarfile = new File((execPath + File.separatorChar + fileName));
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarfile.getAbsolutePath());
            Process p = pb.start();
        } catch (SecurityException | IllegalArgumentException ex) {
            ex.printStackTrace();
            
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            
        } catch (IOException ex) {
            ex.printStackTrace();
            
        }
    }

}
