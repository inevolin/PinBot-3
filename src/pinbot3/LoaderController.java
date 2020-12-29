/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import common.MyUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import model.Account;
import model.Campaign;
import model.configurations.AConfiguration;
import pinbot3.helpers.Licensing;
import pinbot3.helpers.Privileges;
import pinbot3.helpers.Updater;

/**
 * FXML Controller class
 *
 * @author UGent
 */
public class LoaderController implements Initializable {

    @FXML
    private Label lbl;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void init() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                bgWork();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Stage stage = (Stage) lbl.getScene().getWindow();
                            stage.hide();
                            loadMain();
                        } catch (IOException ex) {
                            common.ExceptionHandler.reportException(ex);
                            
                        }
                    }
                });
            }
        });
    }

    private void bgWork() {
        try {
            if (pinbot3.PinBot3.IDE_DEBUGGING) {
                PinBot3.PRIVS.getMyPrivs().add(Privileges.TYPES.PREMIUM);
            }

            Boolean restartRequired = getDependencies();
            if (restartRequired) {
                MyUtils.setLbl("Starting...", lbl);
                Thread.sleep(1000);
                MyUtils.attemptRun("PinBot3.jar");
                Thread.sleep(1000);
                pinbot3.PinBot3.SystemExit();
            }

            Updater u = new Updater(lbl);
            pinbot3.PinBot3.VERSION = u.getAppVersion();
            if (!pinbot3.PinBot3.IDE_DEBUGGING) {

                u.checkU();
                u.checkP();

                pinbot3.PinBot3.checkIfRunning();                

                Licensing li = new Licensing(lbl);
                li.check();

                if (PinBot3.PRIVS.isTrial()) {
                    try {
                        MyUtils.setLbl("Trial version loading...", lbl);
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //ignore
                    }
                    //pinbot3.PinBot3.SystemExit();
                }
            }
            
            MyUtils.setLbl("Loading DB...", lbl);
            //pinbot3.PinBot3.dalMgr.InitEMF(); //deprecated since db3
            pinbot3.PinBot3.campaignMgr.InitAccounts();
            for (Account acc : pinbot3.PinBot3.campaignMgr.getAccounts()) {
                acc.setStatus(Account.STATUS.UNAUTHORIZED);
                version_3_0_9_9(acc); //REMOVE IN ALL FUTURE VERSIONS
                pinbot3.PinBot3.dalMgr.Save(acc);
            }
        } catch (Exception exx) {
            try {
                exx.printStackTrace();
                MyUtils.setLbl("Something went wrong!\nSupport: pinbot@healzer.com\nExiting...", lbl);
                Thread.sleep(4000);
            } catch (InterruptedException ex) {
                //ignore
            }
            pinbot3.PinBot3.SystemExit();
        }
    }
    
    private void version_3_0_9_9(Account acc) {
        //Timer uses Map<AConfiguration,Account>
        //however Accounts get overwritten by each new started account because hashCode of AConfiguration is just its name
        //let us give each AConfiguration a unique ID
        long time = System.currentTimeMillis();
        for (Campaign ca : acc.getCampaigns()) {
            for (AConfiguration cf : ca.getConfigurations()) {
                cf.setId(time);
                while (System.currentTimeMillis() <= time) {
                    //do nothing
                }
                time = System.currentTimeMillis();
            }
        }
    }

    private Boolean getDependencies() throws IOException {
        Boolean anyChanges = false;
        try {
            URL url = new URL("https://pinbot3.com/reg/libs/index.php");
            URLConnection conn = url.openConnection();
            String rs = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                rs = reader.lines().collect(Collectors.joining("\n"));
            }
            if (rs != null) {
                String execpath = MyUtils.execPath(true);
                System.err.println(execpath);
                ArrayList<String> check = new ArrayList<>(Arrays.asList(rs.split("\\r?\\n")));
                int mustHave = check.size();

                String lib_path = execpath + File.separator + "lib";
                if (!Files.exists(Paths.get(lib_path))) {
                    Files.createDirectory(Paths.get(lib_path));
                }
                ArrayList<String> local = new ArrayList<>();
                Files.walk(Paths.get(lib_path)).forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        String fn = file.getFileName().toString();
                        local.add(fn);
                    }
                });

                for (String s : local) {
                    if (check.contains(s)) {
                        check.remove(s);
                    }
                }

                if (check.size() > 0) {

                    for (String s : check) {
                        String destin = execpath + File.separator + "lib" + File.separator + s;
                        URL u = new URL("https://pinbot3.com/reg/libs/" + s);
                        try (InputStream in = u.openStream()) {
                            Files.copy(in, Paths.get(destin));
                            anyChanges = true;
                        }
                    }

                }

                int localHave = (int) Files.list(Paths.get(lib_path)).count();
                System.err.println("Server has " + mustHave + " files, we have " + localHave + " files.");
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return anyChanges;
    }

    @Deprecated //db3 may not exist yet, so it always returns false
    private void hasWriteRights() throws UnsupportedEncodingException, InterruptedException {
        String path = MyUtils.db3Path();
        File fpath = new File(path);
        if(!Files.isWritable(fpath.toPath())){
            MyUtils.setLbl("Error #LC201\nSupport: pinbot@healzer.com\nExiting...", lbl);
            Thread.sleep(4000);
            pinbot3.PinBot3.SystemExit();
        }
    }

    private void loadMain() throws IOException {

        Stage stage = new Stage();
        stage.setTitle("PinBot " + (PinBot3.PRIVS.getMyPrivs().contains(Privileges.TYPES.PREMIUM) ? "PREMIUM " : "trial ") + pinbot3.PinBot3.VERSION);
        stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
        stage.initStyle(StageStyle.DECORATED);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {

                try {
                    //pinbot3.PinBot3.campaignMgr.getAccounts().forEach(x -> pinbot3.PinBot3.dalMgr.Save(x));
                    pinbot3.PinBot3.dalMgr.shutdown();
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                }

                Platform.exit();
                pinbot3.PinBot3.SystemExit();
            }
        });

        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Main.fxml"));
        Parent loader = (Parent) fxmlloader.load();
        MainController mainController = fxmlloader.getController();

        Scene scene = new Scene(loader);
        stage.setScene(scene);
        stage.setMinWidth(200);
        stage.setMinHeight(100);
        stage.setWidth(800);
        stage.setHeight(300);
        stage.show();
    }

}
