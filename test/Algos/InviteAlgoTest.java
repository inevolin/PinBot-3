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
import model.configurations.InviteConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

public class InviteAlgoTest {
    public InviteAlgoTest() {
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
    public void InviteAlgoTest() throws Exception {

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
        am.refreshBoards(acc);

        InviteConfiguration c1 = new InviteConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 1);
        c1.setTimeoutMax( 2);
        c1.setIsActive(Boolean.TRUE);

        Campaign c = new Campaign();
        c.setCampaignName("name_" + (new Date()).getTime());
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);
        
        ////////////////////////////////
        ////////////////////////////////
        ////////////////////////////////
        
        Board randomB = (Board) acc.getBoards().toArray()[0];
        //if pinner has already been invited, you'll get 403 Forbidden.
        AQuery q1 = new AQuery("/"+randomB.getUrlName()+"/", randomB,PinterestObject.PinterestObjectResources.BoardFollowersResource); 
        c1.getQueries().add(q1);
        
        ///////////////////////////////
        ////////////////////////////////
        ////////////////////////////////

        //acc.getDuplicates().clear(); // !!!!

        //dm.Save(acc);
        cm.RunAccount(acc);

        Thread.sleep(1000 * 1000);
    }
    
}
