/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Managers.AccountManager;
import Managers.CampaignManager;
import java.util.Iterator;
import java.util.List;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

public class FollowAlgoTest {

    public FollowAlgoTest() {
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

    //Both tests do NOT include criteria settings!
    @Test
    public void FollowPinnersAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        pinbot3.PinBot3.campaignMgr.InitAccounts();

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

        Campaign c = new Campaign();
        c.setCampaignName("Def");

        FollowConfiguration c1 = new FollowConfiguration();
        c1.setAutopilot(Boolean.FALSE);
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin(3);
        c1.setTimeoutMax(6);
        c1.setIsActive(Boolean.TRUE);
        c1.setCriteria_Boards(Boolean.FALSE);
        c1.setCriteria_Users(Boolean.FALSE);
        c1.setFollowUsers(Boolean.TRUE);

        c.getConfigurations().add(c1);
        acc.enableCampaign(c);

        am.Login(acc);
        // am.refreshBoards(acc);

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        AQuery q1 = new AQuery("/alexzgao/3d-printing/", null, PinterestObject.PinterestObjectResources.BoardFollowersResource);
        AQuery q2 = new AQuery("alexzgao", null, PinterestObject.PinterestObjectResources.UserFollowersResource);
        AQuery q3 = new AQuery("alexzgao", null, PinterestObject.PinterestObjectResources.UserFollowingResource);
        AQuery q4 = new AQuery("boats", null, PinterestObject.PinterestObjectResources.SearchResource);
        AQuery q5 = new AQuery("/giseleazeem/", null, PinterestObject.PinterestObjectResources.IndividualUser);

        acc.getDuplicates_follow().clear();
        acc.getSelectCampaign().getConfigurations().forEach(z -> {
            if (!(z instanceof FollowConfiguration)) {
                return;
            }
            Iterator<AQuery> aq = z.getQueries().iterator();
            while (aq.hasNext()) {
                AQuery it = aq.next();
                if (it.getQuery().equalsIgnoreCase(q5.getQuery())) {
                    return;
                }
                aq.remove();
            }
            z.getQueries().add(q5);
        });

        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        cm.RunAccount(acc);
        //
        Thread.sleep(1000 * 1000);
    }

    @Test
    public void FollowBoardsAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();
        pinbot3.PinBot3.campaignMgr.InitAccounts();

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

        Campaign c = new Campaign();
        c.setCampaignName("Def");

        FollowConfiguration c1 = new FollowConfiguration();
        c1.setAutopilot(Boolean.FALSE);
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(1);
        c1.setActionMin(1);
        c1.setTimeoutMin(3);
        c1.setTimeoutMax(6);
        c1.setIsActive(Boolean.TRUE);
        c1.setCriteria_Boards(Boolean.FALSE);
        c1.setCriteria_Users(Boolean.FALSE);
        c1.setFollowUsers(Boolean.FALSE);
        c1.setFollowBoards(Boolean.TRUE);

        c.getConfigurations().add(c1);
        acc.getCampaigns().add(c);
        acc.enableCampaign(c);

        pinbot3.PinBot3.dalMgr.printQueriesCount();
        am.Login(acc);
        am.refreshBoards(acc);
        pinbot3.PinBot3.dalMgr.printQueriesCount();

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        AQuery q4 = new AQuery("yachts", null, PinterestObject.PinterestObjectResources.SearchResource);
        acc.getSelectCampaign().getConfigurations().forEach(z -> {
            for (AQuery aq : z.getQueries()) {
                if (aq.getQuery().equalsIgnoreCase(q4.getQuery())) {
                    return;
                }
            }
            z.getQueries().add(q4);
        });

        /*AQuery q2 = new AQuery("andreamendezo95", null);
        q2.setResource(PinterestObject.PinterestObjectResources.BoardFollowingResource);
        c1.getQueries().add(q2);*/
 /*AQuery q3 = new AQuery("cats", null);
        c1.getQueries().add(q3);*/
        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        cm.RunAccount(acc);
        //
        while (true) {
            pinbot3.PinBot3.dalMgr.Save(acc);
            //pinbot3.PinBot3.dalMgr.SaveCampaign(acc.getSelectCampaign());

            pinbot3.PinBot3.dalMgr.printQueriesCount();
            Thread.sleep(2000);
        }
        //Thread.sleep(1000 * 1000);
    }

}
