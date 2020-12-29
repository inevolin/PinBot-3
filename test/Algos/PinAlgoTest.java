/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Algorithms.PinAlgo;
import Managers.AccountManager;
import Managers.CampaignManager;
import Managers.ScrapeManager;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

/**
 *
 * @author UGent
 */
public class PinAlgoTest {

    public PinAlgoTest() {
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

    @Test
    public void UploadTest() throws Exception {

        ScrapeManager sm = pinbot3.PinBot3.scrapeMgr;
        AccountManager am = pinbot3.PinBot3.accountMgr;

        Account acc = MyAccount();
        am.Login(acc);

        PinAlgo pa = new PinAlgo(null, acc, sm);
        pa.uploadImage("C:\\Users\\UGent\\AppData\\Local\\Temp\\pinbot3_1717985027962817461.jpg");
    }

    @Test
    public void PinAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();
        pinbot3.PinBot3.campaignMgr.InitAccounts();

        ScrapeManager sm = pinbot3.PinBot3.scrapeMgr;
        AccountManager am = pinbot3.PinBot3.accountMgr;
        CampaignManager cm = pinbot3.PinBot3.campaignMgr;

        Account acc = MyAccount();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }
        //dm.Save(acc);

        am.Login(acc);
        am.refreshBoards(acc);

        // assign campaign
        Campaign c = new Campaign();
        c.setCampaignName("name1");        
        acc.enableCampaign(c);
        acc.getCampaigns().add(c);
        pinbot3.PinBot3.dalMgr.Save(acc);

        // assign Pin configuration
        PinConfiguration c1 = new PinConfiguration();
        c.getConfigurations().add(c1);

        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setIsActive(Boolean.TRUE);
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 1);
        c1.setTimeoutMax( 3);
        c1.setIsActive(Boolean.TRUE);

        Board selectedBoard = (Board) acc.getBoards().toArray()[0];
        System.err.println("Going to pin to board: " + selectedBoard.getName());
        AQuery q = new AQuery("luxury fashion", selectedBoard, PinterestObject.PinterestObjectResources.External);
        c1.getQueries().add(q);

        cm.RunAccount(acc);

        Thread.sleep(1000 * 1000);
    }

    @Test
    public void randomTest() {
        HashSet<Integer> h = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Random r = new Random();
            int max = 5;
            int min = 2;
            int delta = max - min + 1;
            int amount = r.nextInt(delta <= 0 ? 1 : delta) + min;
            h.add(amount);
        }
        for (int i : h) {
            System.err.println(i);
        }
    }
}
