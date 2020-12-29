/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Managers;

import Algorithms.Algo;
import Timers.CampaignManagerTimer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.AConfiguration.RunStatus;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;

public class CampaignManager {

    /*
        This is the backbone (big boss) of the entire application.
        It ensures coupling between the GUI and background operations.
     */
    private List<Account> accounts;
    private ExecutorService executor; //our thread pool
    private ConcurrentMap<AConfiguration, Account> queue; // this contains all 'running' configurations.
    private Timer timer;
    private final Long TIK_TAK = 5000L;

    public CampaignManager(ExecutorService executor) {
        this.executor = executor;
        queue = new ConcurrentHashMap<>();
    }

    public void InitAccounts() {
        try {
            //also used for refreshing !
            accounts = pinbot3.PinBot3.dalMgr.AllAccounts();
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

        }

    }

    /*
        Loop is an internal class, which has one method 'run()'
        that is being executed by the timer every 500ms (= TIK_TAK variable).
    Purpose:    When someone wants to run a user,
                then all the configurations of the select Campaign of that user
                will be added to 'queue'.
                Then the 'run()' will start a new thread of an Algorithm worker.
                It will also set 'runNext' attribute to null value
                which ensures that the configuration won't be run a second time.
                The configuraiton MAY only run a second/third/fourth/... time WHEN
                the Algo itself sets the 'runNext' time. So The Algo
                is responsible for checking how many more actions should be performed,
                and in case the 'Autopilot' feature is on, it will go into sleeping mode.
                When in sleeping mode, the 'runNext' will be set to some time in the future
                which is usually the next day (± 12-24 hours later).
     */
    public List<Account> getAccounts() {
        return accounts;
    }


    /*
        Just as explained for the 'Loop' class above...
        This method starts the timer in case it's not started yet.
        It will check & add each configuration that's marked as 'isActive'.    
     */
    public synchronized void RunAccount(Account a) { 

        if (!a.getStatus().equals(Account.STATUS.LOGGEDIN)) {
            a.setStatus(Account.STATUS.UNAUTHORIZED);
        }
        if (a.getSelectCampaign() == null || a.getSelectCampaign().getConfigurations() == null) {
            return;
        }
        System.err.println("Running for acc " + a.getUsername() + "\t" + a.getEmail());
        for (AConfiguration cf : a.getSelectCampaign().getConfigurations()) {
            if (cf.getIsActive() == null || !cf.getIsActive()) {
                continue;
            }
            cf.status = RunStatus.ACTIVE; //ACTIVE instead of IDLE :: this will ensure Stop button is shown instantly

            cf.runNext = Algo.AutopilotOrDefault(cf, false);
            if (cf.runNext == null) {
                cf.runNext = LocalDateTime.now();
            }
            Algo.initActionLimits(cf);

            a.reLoginAttempts = 0;      //  !!!!!!!!!!!!!
            cf.ErrorCount = 0;          //  !!!!!!!!!!!!!     
            cf.isInterrupt = false;

            if (absentFunc(cf, a)) {
                queue.put(cf, a);
            }
        }
        initTimer();
    }

    private boolean absentFunc(AConfiguration cf, Account acc) {
        for (Map.Entry<AConfiguration, Account> kv : queue.entrySet()) {
            if (kv.getKey().getClass().equals(cf.getClass()) && kv.getValue().getEmail().equalsIgnoreCase(acc.getEmail())) {
                return false; //duplicate prevention
            }
        }
        return true;
    }

    private CampaignManagerTimer campaignTimer;

    private void initTimer() {
        if (campaignTimer == null) {
            campaignTimer = new CampaignManagerTimer(queue, executor);
        }
        campaignTimer.run();
    }

    /*
        Stopping the account is simple:
            1) perform a 'cancel' operation on the thread worker.
            2) remove entry from queue.
     */
    public synchronized void StopAccount(Account a) {
        a.setStatus(Account.STATUS.ABORT_REQUEST);
        for (AConfiguration cf : a.getSelectCampaign().getConfigurations()) {
            if (cf.future != null) {
                cf.future.cancel(true);
            }
            if (queue.containsKey(cf)) {
                queue.remove(cf);
            }
            pinbot3.PinBot3.scrapeMgr.getChm().remove(cf);

            cf.CountCompleted = null;
            cf.CountTotal = null;
            cf.status = RunStatus.IDLE;
            cf.isInterrupt = true;

            for (AQuery q : cf.getQueries()) {
                if (q.getResource() != PinterestObject.PinterestObjectResources.IndividualPin && q.getResource() != PinterestObject.PinterestObjectResources.IndividualUser) {
                    q.setBlacklisted(Boolean.FALSE);
                }
            }
        }

        Thread t = new Thread() {
            public void run() {
                try {
                    //quickly save, especially if any dups before quitting.
                    a.setLastlySaved(new Date());
                    pinbot3.PinBot3.dalMgr.Save(a);
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                }
            }
        };
        t.start();
    }

}
