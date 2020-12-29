/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Timers;

import Algorithms.Algo;
import Algorithms.CommentAlgo;
import Algorithms.FollowAlgo;
import Algorithms.InviteAlgo;
import Algorithms.LikeAlgo;
import Algorithms.MessageAlgo;
import Algorithms.PinAlgo;
import Algorithms.RepinAlgo;
import Algorithms.UnfollowAlgo;
import common.MyLogger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.UnfollowConfiguration;

/**
 *
 * @author healzer
 */
public class CampaignManagerTimer {

    private ExecutorService executor;
    private ConcurrentMap<AConfiguration, Account> queue; // this contains all 'running' configurations.

    public CampaignManagerTimer(ConcurrentMap<AConfiguration, Account> queue, ExecutorService executor) {
        this.queue = queue;
        this.executor = executor;
    }

    private volatile boolean is_running; //check if timer is active, otherwise we have to re-init the timer from campaignMgr.
    private Thread theLoopThread;

    public synchronized boolean isRunning() {
        return is_running;
    }

    public synchronized void run() {
        if (!is_running) {
            theLoopThread = new Thread() {
                public void run() {
                    theLoop();
                }
            };
            theLoopThread.start();
        }
    }

    private void theLoop() {
        is_running = true;
        while (is_running) {
            Boolean anyRunning = false;
            //using ConcurrentHashMap to prevent concurrency problem
            //Map<AConfiguration, Account> tmp_queue = new HashMap<AConfiguration, Account>(queue); //concurrency problem: starting account adds to HashMap & iterating the HashMap here
            for (Map.Entry<AConfiguration, Account> kv : queue.entrySet()) {
                if (kv.getValue().getStatus().equals(Account.STATUS.UNAUTHORIZED)) {
                    accountReAuthorization(kv.getValue());
                    anyRunning = true;
                } else if (kv.getValue().getStatus().equals(Account.STATUS.BUSY)) {
                    anyRunning = true;
                }

                if (kv.getValue().getStatus().equals(Account.STATUS.LOGGEDIN)
                        && kv.getKey().status != AConfiguration.RunStatus.ERROR //too many errors occured, so let's stop it.
                        && kv.getKey().runNext != null
                        && kv.getKey().runNext.isBefore(LocalDateTime.now())) {
                    runThis(kv.getKey(), kv.getValue());

                }

                if (kv.getKey().status == AConfiguration.RunStatus.ACTIVE || kv.getKey().status == AConfiguration.RunStatus.SLEEPING || kv.getKey().status == AConfiguration.RunStatus.TIMEOUT) {
                    anyRunning = true;
                    savingAccount(kv.getValue(), false);
                }
            }
            if (!anyRunning) {
                for (Map.Entry<AConfiguration, Account> kv : queue.entrySet()) {
                    savingAccount(kv.getValue(), true);
                }
                is_running = false;
                System.err.println("Notif: CampaignManager THREAD stopped; nothing running.");
                theLoopThread = null;
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }

    private void accountReAuthorization(Account acc) {
        try {
            pinbot3.PinBot3.myLogger.log(acc, "Attempting re-login", MyLogger.LEVEL.info);
            acc.setStatus(Account.STATUS.BUSY);
            acc.reLoginAttempts++;
            if (acc.reLoginAttempts > Account.MAX_RELOGIN_ATTEMPTS) {
                pinbot3.PinBot3.campaignMgr.StopAccount(acc);
                acc.setStatus(Account.STATUS.LOGIN_ERROR);
                pinbot3.PinBot3.myLogger.log(acc, "Re-login too many attempts, aborting all operations.", MyLogger.LEVEL.info);
                return;
            }
            Thread t = new Thread() {
                public void run() {
                    try {
                        pinbot3.PinBot3.accountMgr.Login(acc);
                    } catch (Exception ex) {
                        common.ExceptionHandler.reportException(ex);
                        acc.setStatus(Account.STATUS.UNAUTHORIZED);

                    }
                }
            };
            t.start();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    private void runThis(AConfiguration config, Account acc) {
        try {
            if (config.status == AConfiguration.RunStatus.SLEEPING) {
                //wake up
                Algo.initActionLimits(config);
            }
            config.status = AConfiguration.RunStatus.ACTIVE;
            config.runNext = null; // Algo will have to set the next DateTime when it may run again.
            Runnable worker = null;
            if (config instanceof PinConfiguration) {
                worker = new PinAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof RepinConfiguration) {
                worker = new RepinAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof LikeConfiguration) {
                worker = new LikeAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof InviteConfiguration) {
                worker = new InviteAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof CommentConfiguration) {
                worker = new CommentAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof FollowConfiguration) {
                worker = new FollowAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof UnfollowConfiguration) {
                worker = new UnfollowAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else if (config instanceof MessageConfiguration) {
                worker = new MessageAlgo(config, acc, pinbot3.PinBot3.scrapeMgr);
            } else {
                return;
            }
            System.err.println("thread -- " + worker.getClass().toString() + " -- for: " + acc.getUsername());
            Future<?> future = executor.submit(worker);
            config.future = future;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    private static int SAVE_EVERY_X_SECONDS = 30;

    private void savingAccount(Account acc, boolean force) {
        //save the account every 30 seconds.
        if ((acc.getLastlySaved() == null
                || ((new Date()).getTime() - acc.getLastlySaved().getTime() >= 1000 * SAVE_EVERY_X_SECONDS))
                || force) {
            acc.setLastlySaved(new Date());
            pinbot3.PinBot3.dalMgr.Save(acc);
        }
    }

}
