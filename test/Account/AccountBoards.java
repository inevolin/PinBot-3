/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Account;

import Managers.AccountManager;
import Managers.CampaignManager;
import java.util.ArrayList;
import java.util.List;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import org.junit.Test;

/**
 *
 * @author UGent
 */
public class AccountBoards {

    public AccountBoards() {

    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }

    public Account TestAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "test";
        acc.setEmail(Email);
        acc.setPassword(Password);

        Proxy proxy = new Proxy();
        proxy.setEnabled(true);
        proxy.setIp("138.128.109.219");
        proxy.setPort(8080);
        proxy.setPassword("0LCDpc6Q");
        proxy.setUsername("ardasen");
        acc.setProxy(proxy);
        return acc;
    }

    @Test
    public void ScrapeMyBoardsTest() throws Exception {

        Account acc = MySecondAccount();

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        pinbot3.PinBot3.dalMgr.Save(acc);

        pinbot3.PinBot3.accountMgr.Login(acc);
        pinbot3.PinBot3.accountMgr.refreshBoards(acc);

        pinbot3.PinBot3.dalMgr.Save(acc);
    }

    @Test
    public void TestManyToOne() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

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
        Campaign ca = acc.getSelectCampaign();
        AConfiguration fc = ca.getConfigurations().stream().filter(x -> x instanceof RepinConfiguration).findFirst().get();
        //AQuery q = new AQuery("fashion2", (Board) acc.getBoards().toArray()[0], PinterestObject.PinterestObjectResources.SearchResource);
        //fc.getQueries().add(q);
        
        List<AQuery> lst = new ArrayList<>(fc.getQueries());
        
        fc.getQueries().clear();
        pinbot3.PinBot3.dalMgr.Save(acc);
        
        
        for (AQuery q : lst)
            fc.getQueries().add(q.copy(null));
        
        
        
        
        pinbot3.PinBot3.dalMgr.Save(acc);
    }

    @Test
    public void TestBoardsDeletePersist() throws Exception {

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

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

        System.err.println(acc.getBoards().size());
        pinbot3.PinBot3.dalMgr.Save(acc);

        /*
        am.Login(acc);
        am.refreshBoards(acc);        
        pinbot3.PinBot3.dalMgr.Save(acc);

         */
        acc.getBoards().clear();
        /*
        Iterator<Board> it = acc.getBoards().iterator();
        if (it.hasNext()) {
            Board b = it.next();
            it.remove();
        }*/

        System.err.println(acc.getBoards().size());
        pinbot3.PinBot3.dalMgr.Save(acc);

    }

}
