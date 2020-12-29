/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Timers;

import java.util.TimerTask;
import javafx.scene.control.TableView;
import model.Account;
import pinbot3.MainController;

/**
 *
 * @author healzer
 */
public class MainController_LoopGUI extends TimerTask {

    private MainController mCtl;
    public TableView<Account> tbl;

    public MainController_LoopGUI(MainController mCtl, TableView<Account> tbl) {
        this.mCtl = mCtl;
        this.tbl = tbl;
    }

    @Override
    public void run() {
        try {
            tbl.refresh();
            mCtl.ButtonHandler();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
