/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Timers;

import java.util.TimerTask;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import pinbot3.helpers.Updater;

/**
 *
 * @author healzer
 */
public class MainController_LoopUpdater extends TimerTask {

    private Updater u = new Updater(new Label());
    private FlowPane statusbox;
    private Button btnUpdate;

    public MainController_LoopUpdater(Button btnUpdate, FlowPane statusbox) {
        this.btnUpdate = btnUpdate;
        this.statusbox = statusbox;
    }

    @Override
    public void run() {
        try {
            if (u.newPVersion()) {
                Platform.runLater(() -> {
                    statusbox.getChildren().remove(btnUpdate);
                    statusbox.getChildren().add(0, btnUpdate);
                });
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

}
