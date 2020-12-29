/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3;

import Managers.AccountManager;
import Managers.CampaignManager;
import Managers.DALManager;
import Managers.ScrapeManager;
import common.MyLogger;
import common.MyUtils;
import static common.MyUtils.execPath;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import pinbot3.helpers.Privileges;

/**
 *
 * @author UGent
 */
public class PinBot3 extends Application {

    public static ExecutorService campaignManagerExecutor, mainControllerExecutor, scrapeManagerExecutor;
    public static ScrapeManager scrapeMgr;
    public static AccountManager accountMgr;
    public static DALManager dalMgr;
    public static CampaignManager campaignMgr;
    public static Privileges PRIVS = new Privileges();
    public static MyLogger myLogger = new MyLogger();
    public static String VERSION = "3.0";
    public static Boolean IDE_DEBUGGING = false;

    public PinBot3() throws IOException, Exception {
        cleanup();
        InitExecutors();
        pinbot3.PinBot3.InitMgrs();
    }

    private static final int PORT = 9999;
    private static ServerSocket socket;

    public static void checkIfRunning() {
        try {
            socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
        } catch (Exception ex) {
            //ignore
            common.Dialogs.ErrorDialog("Another PinBot process is already running. \n Exiting now...", "Already running");
            SystemExit();
        }
    }

    public static void SystemExit() {
        System.exit(0);
    }

    private void cleanup() {
        try {
            File del = new File(execPath(false) + File.separatorChar + "log.log");
            if (del.exists()) {
                double bytes = del.length();
                double kilobytes = (bytes / 1024);
                double megabytes = (kilobytes / 1024);
                if (megabytes >= 100)
                    del.delete();
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        try {
            File f = new File("./log.txt");
            f.delete();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        try {
            String lockFile = MyUtils.execPath(false) + File.separatorChar + "db3.lck";
            File floc = new File(lockFile);
            if (floc.exists()) {
                floc.delete();
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                TerminateExecutors();
                System.exit(0); //we have a timer running in CampaignManager (as thread), keeping this process alive.
            }
        });

        loadLoader(stage);

    }

    private void loadLoader(Stage stage) throws IOException {
        stage.setTitle("PinBot 3.0");
        stage.getIcons().add(new Image("/pinbot3/imgs/logo.png"));
        stage.initStyle(StageStyle.TRANSPARENT);
        FXMLLoader fxmlloader = new FXMLLoader(getClass().getResource("/pinbot3/views/Loader.fxml"));
        Parent parent = (Parent) fxmlloader.load();
        LoaderController loaderController = fxmlloader.getController();

        Scene scene = new Scene(parent);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        stage.show();

        loaderController.init();
    }

    public static void InitMgrs() {
        dalMgr = new DALManager(); // 1
        scrapeMgr = new ScrapeManager(scrapeManagerExecutor); // 2
        accountMgr = new AccountManager();        // 3
        campaignMgr = new CampaignManager(campaignManagerExecutor);  // 4
        
    }

    public static void InitExecutors() {
        campaignManagerExecutor = Executors.newFixedThreadPool(10); //change to 50 max or so.
        scrapeManagerExecutor = Executors.newFixedThreadPool(10); //change to 50 max or so.
        mainControllerExecutor = Executors.newSingleThreadExecutor();
    }

    private void TerminateExecutors() {
        campaignManagerExecutor.shutdownNow();
        scrapeManagerExecutor.shutdownNow();
        mainControllerExecutor.shutdownNow();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0) {
            pinbot3.PinBot3.IDE_DEBUGGING = true;
            System.err.println("ide debug mode enabled");
        }
        /*
        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("debug")) {
                pinbot3.PinBot3.Debug = true;
                System.err.println("debug mode enabled");
                break;
            }
        }

        MyUtils.writeLog("debug mode", false);*/

        launch(args);

    }

}
