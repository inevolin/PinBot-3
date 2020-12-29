/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algos;

import Managers.AccountManager;
import Managers.CampaignManager;
import Managers.ScrapeManager;
import java.util.List;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

public class LikeAlgoTest {
    public LikeAlgoTest() {
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
    public void LikeAlgoTest() throws Exception {
        
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

        //am.Login(acc);
        //am.refreshBoards(acc);

        LikeConfiguration c1 = new LikeConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 3);
        c1.setTimeoutMax( 6);
        c1.setIsActive(Boolean.TRUE);

        Campaign c = new Campaign();
        c.setCampaignName("name1");
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);
        
        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        
        AQuery q1 = new AQuery("orbs", null, PinterestObject.PinterestObjectResources.SearchResource);
        //c1.getQueries().add(q1);
        
        AQuery q2 = new AQuery("/aanderson0883/funny/", null, PinterestObject.PinterestObjectResources.BoardFeedResource);
        //c1.getQueries().add(q2);
        
        AQuery q3 = new AQuery("jessicab127", null, PinterestObject.PinterestObjectResources.UserPinsResource);
        c1.getQueries().add(q3);
        
        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////

        //acc.getDuplicates().clear(); // !!!!

        //dm.Save(acc);
        cm.RunAccount(acc);

        Thread.sleep(1000 * 1000);
    }
}
