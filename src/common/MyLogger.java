/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import model.Account;

/**
 *
 * @author UGent
 */
public final class MyLogger {

    public final class LogItem {

        SimpleDateFormat format1 = new SimpleDateFormat("dd MM yyyy  hh:mm:ss");

        public LogItem(String msg, LEVEL lvl) {
            this.msg = msg;
            this.lvl = lvl;
            dt = Calendar.getInstance();
        }
        private Calendar dt;
        private String msg;
        private LEVEL lvl;

        public String getMsg() {
            return msg;
        }

        public LEVEL getLvl() {
            return lvl;
        }

        @Override
        public String toString() {
            return format1.format(dt.getTime()) + ":  " + getMsg();
        }
    }

    public enum LEVEL {
        fine, info
    };
    
    private volatile  Object LOCK = new Object(); //!!! volatile is used to indicate that a variable's value will be modified by different threads.
    private LinkedHashMap<Account, ArrayList<LogItem>> logs = new LinkedHashMap<>();

    public LinkedHashMap<Account, ArrayList<LogItem>> getLogs() {
        synchronized (LOCK) {
            return logs;
        }
    }

    public void log(Account acc, String msg, LEVEL lvl) {
        synchronized (LOCK) {
            LogItem lg = new LogItem(msg, lvl);
            if (!logs.containsKey(acc)) {
                logs.put(acc, new ArrayList<>());
            }
            logs.get(acc).add(lg);

            cleanup();
        }
    }

    void cleanup() {
        for (Map.Entry<Account, ArrayList<LogItem>> kv : logs.entrySet()) {
            if (kv.getValue().size() > 1000) {
                kv.getValue().subList(0, 50).clear();
            }
        }
    }
}
