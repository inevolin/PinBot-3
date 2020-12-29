/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Managers.AccountManager;
import Managers.CampaignManager;
import java.util.Date;
import java.util.List;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.UnfollowConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

public class UnfollowAlgoTest {

    public UnfollowAlgoTest() {
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

    //Both test do NOT include criteria settings!
    @Test
    public void UnfollowUsersAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();
        pinbot3.PinBot3.campaignMgr.InitAccounts();

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

         am.Login(acc);
        // am.refreshBoards(acc);
        UnfollowConfiguration c1 = new UnfollowConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;

        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin(3);
        c1.setTimeoutMax( 6);
        c1.setIsActive(Boolean.TRUE);

        c1.setCriteria_Boards(Boolean.FALSE);
        c1.setCriteria_Users(Boolean.FALSE);
        c1.setUnfollowBoards(Boolean.FALSE);
        
        c1.setUnfollowUsers(Boolean.TRUE);
        c1.setUnfollowNonFollowers(Boolean.TRUE);
        c1.setUnfollowOnlyRecordedFollowings(Boolean.FALSE);

        Campaign c = new Campaign();
        c.setCampaignName(Long.toString((new Date()).getTime()));
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        AQuery qBoards = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.BoardFollowingResource);
        //c1.getQueries().add(qBoards);
        AQuery qUsers = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.UserFollowingResource);
        c1.getQueries().add(qUsers);

        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        cm.RunAccount(acc);
        //pinbot3.PinBot3.dalMgr.Save(acc);
        Thread.sleep(1000 * 1000);
    }

    @Test
    public void UnfollowBoardsAlgoTest() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();
        pinbot3.PinBot3.campaignMgr.InitAccounts();

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

         am.Login(acc);
        // am.refreshBoards(acc);
        UnfollowConfiguration c1 = new UnfollowConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;

        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin(3);
        c1.setTimeoutMax( 6);
        c1.setIsActive(Boolean.TRUE);

        c1.setCriteria_Boards(Boolean.FALSE);
        c1.setCriteria_Users(Boolean.FALSE);
        c1.setUnfollowBoards(Boolean.TRUE);
        
        c1.setUnfollowUsers(Boolean.FALSE);
        c1.setUnfollowNonFollowers(Boolean.FALSE);
        c1.setUnfollowOnlyRecordedFollowings(Boolean.FALSE);

        Campaign c = new Campaign();
        c.setCampaignName(Long.toString((new Date()).getTime()));
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        AQuery qBoards = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.BoardFollowingResource);
        c1.getQueries().add(qBoards);

        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        cm.RunAccount(acc);
        //pinbot3.PinBot3.dalMgr.Save(acc);
        Thread.sleep(1000 * 1000);
    }
}
