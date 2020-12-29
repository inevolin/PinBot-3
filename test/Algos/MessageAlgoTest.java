/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Managers.AccountManager;
import Managers.CampaignManager;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

public class MessageAlgoTest {
    public MessageAlgoTest() {
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
    public void MessageAlgoTest() throws Exception {

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
        MessageConfiguration c1 = new MessageConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;

        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 3);
        c1.setTimeoutMax( 6);
        c1.setIsActive(Boolean.TRUE);
        c1.setCriteria_Users(Boolean.FALSE);

        Campaign c = new Campaign();
        c.setCampaignName(Long.toString((new Date()).getTime()));
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);

        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        
        AQuery q1 = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.UserFollowersResource);
        c1.setMessages(Arrays.asList(new String[]{"Hi there! Thanks for following me, how are you doing?"}));
        c1.getQueries().add(q1);
         
 
        /*AQuery q1 = new AQuery("epsilondzeta", null);
        c1.setMessages(Arrays.asList(new String[]{"Nice", "Beautiful", "Great car"}));
        c1.getQueries().add(q1);
         */ 
        
         
        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        //acc.getDuplicates().clear(); // !!!!
        cm.RunAccount(acc);
        //pinbot3.PinBot3.dalMgr.Save(acc);
        Thread.sleep(1000 * 1000);
    }
    
}
