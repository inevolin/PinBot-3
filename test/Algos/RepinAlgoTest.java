/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Managers.AccountManager;
import Managers.CampaignManager;
import Managers.DALManager;
import Managers.ScrapeManager;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class RepinAlgoTest {

    public RepinAlgoTest() {
    }

    public Account MyAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = ("password").replace("\\", "\\\\");
        Proxy pr = new Proxy();
        pr.setEnabled(Boolean.FALSE);
        acc.setProxy(pr);
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password".replace("\\", "\\\\");
        Proxy pr = new Proxy();
        pr.setEnabled(Boolean.FALSE);
        acc.setProxy(pr);
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }

    @Test
    public void RepinAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();
        pinbot3.PinBot3.campaignMgr.InitAccounts();

        ScrapeManager sm = pinbot3.PinBot3.scrapeMgr;
        AccountManager am = pinbot3.PinBot3.accountMgr;
        CampaignManager cm = pinbot3.PinBot3.campaignMgr;

        Account acc = MySecondAccount();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        am.Login(acc);
        am.refreshBoards(acc);

        RepinConfiguration c1 = new RepinConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 1);
        c1.setTimeoutMax( 2);
        c1.setIsActive(Boolean.TRUE);
        c1.setBlacklisted_duration(1000L * 60 * 60); //1hr

        Campaign c = new Campaign();
        c.setCampaignName("name1");
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);

        Board selectedBoard = (Board) acc.getBoards().toArray()[0];
        System.err.println("Going to pin to board: " + selectedBoard.getName());

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        AQuery q1 = new AQuery("earrings", selectedBoard, PinterestObject.PinterestObjectResources.SearchResource);
        //c1.getQueries().add(q1);

        AQuery q2 = new AQuery("/beaherzberg/fashion/", selectedBoard, PinterestObject.PinterestObjectResources.BoardFeedResource);
        //c1.getQueries().add(q2);

        AQuery q3 = new AQuery("beaherzberg", selectedBoard, PinterestObject.PinterestObjectResources.UserPinsResource);
        //c1.getQueries().add(q3);

        AQuery q4 = new AQuery("384143043194676180", selectedBoard, PinterestObject.PinterestObjectResources.IndividualPin); //326651779201918410, werkt niet (pattern kan niet wergevonden worden).
        c1.getQueries().add(q4);

        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        //pinbot3.PinBot3.dalMgr.SaveCampaign(c);
        pinbot3.PinBot3.dalMgr.Save(acc);
        cm.RunAccount(acc);

        Thread.sleep(1000 * 1000);
    }

    @Test
    public void BlackListing() throws InterruptedException, Exception {
        // 1) run RepinAlgoTest()
        // 2) wait less than 60 seconds
        // 3) run this test
        ExecutorService e = Executors.newFixedThreadPool(4);
        ScrapeManager sm = new ScrapeManager(e);
        AccountManager am = new AccountManager();
        ExecutorService ee = Executors.newFixedThreadPool(4);
        CampaignManager cm = new CampaignManager(ee);
        DALManager dm = new DALManager();

        Account acc = MySecondAccount();
        List<Account> accs = dm.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        RepinConfiguration c1 = (RepinConfiguration) acc.getSelectCampaign().getConfigurations().toArray()[0];
        AQuery q = c1.getQueries().get(0);
        System.err.println(q.getBlacklisted());
        assertTrue(q.getBlacklisted() == true);

    }

    @Test
    public void BlackListingExpired() throws InterruptedException, Exception {
        // 1) run RepinAlgoTest()
        int X = 30;
        // 2) wait AT LEAST  X seconds !!!!!
        // 3) run this test
        ExecutorService e = Executors.newFixedThreadPool(4);
        ScrapeManager sm = new ScrapeManager(e);
        AccountManager am = new AccountManager();
        ExecutorService ee = Executors.newFixedThreadPool(4);
        CampaignManager cm = new CampaignManager(ee);
        DALManager dm = new DALManager();

        Account acc = MySecondAccount();
        List<Account> accs = dm.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        RepinConfiguration c1 = (RepinConfiguration) acc.getSelectCampaign().getConfigurations().toArray()[0];
        c1.setBlacklisted_duration(1000L * X);
        AQuery q = c1.getQueries().get(0);
        //testing code from RepinAlgo.java
        Date now = new Date();
        if (q.getBlacklisted() != null && q.getBlacklisted()
                && (now.getTime() >= q.getTimespan_blacklisted() + c1.getBlacklisted_duration())) {
            q.setBlacklisted(Boolean.FALSE);
        }

        assertTrue(q.getBlacklisted() == false);

    }
}
