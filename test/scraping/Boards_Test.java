/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import Managers.AccountManager;
import Managers.DALManager;
import Managers.ScrapeManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import scraping.sources.ScrapeBoards_RepinnedOntoBoards;
import scraping.sources.ScrapeBoards_BoardFollowingResource;
import scraping.sources.ScrapeBoards_ProfileBoardsResource;
import scraping.sources.ScrapeBoards_SearchResource;

public class Boards_Test {

    public Boards_Test() {
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password"; //        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        acc.base_url = "https://www.pinterest.com";
        return acc;
    }

    @Test public void misc() throws IOException, Exception {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        ScrapeManager sm = new ScrapeManager(executor);
        DALManager dm = new DALManager();
        List<Account> lst = dm.AllAccounts();
        
        AConfiguration cf = lst.get(0).getCampaigns().iterator().next().getConfigurations().iterator().next();
        sm.getChm().put(cf, new HashSet<>());
        for (int i = 0; i < 10; i++)
        {            
            AQuery pat = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.SearchResource);
            ScrapeSession ss = new ScrapeBoards_SearchResource(lst.get(0), pat, cf);
            if (sm.getChm().containsKey(cf)) {
                Set<ScrapeSession> set = sm.getChm().get(cf);
                set.add(ss);
            }
        }
        System.err.println("");
        
    }
    @Test
    public void TestSearch() throws Exception {
        pinbot3.PinBot3.dalMgr = new DALManager();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        Account a = accs.get(0);
        
        AccountManager am = new AccountManager();
        am.Login(a);
        
        AQuery pat = new AQuery("diy", null, PinterestObject.PinterestObjectResources.SearchResource);

        AConfiguration cf = null;
        Iterator<AConfiguration> it = a.getCampaigns().iterator().next().getConfigurations().iterator();
        while (it.hasNext()) {
            cf = it.next();
            if (cf instanceof FollowConfiguration)
                break;
        }
        for (int i = 0; i < 100; i++) {
            ScrapeSession ss = new ScrapeBoards_SearchResource(a, pat, cf);
            ss.call();
            PinterestObject obj = ss.getFound().entrySet().iterator().next().getKey();
            a.addDuplicate(cf, obj);
            ss.getFound().clear();
        }
        /*
        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j < 1) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Board) kv.getKey()).getName()
            );
        }


        assertTrue(!lst.isEmpty());
*/
    }

    @Test
    public void TestBoardFollowing() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("/kelley6292/following/boards/", null, PinterestObject.PinterestObjectResources.BoardFollowingResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapeBoards_BoardFollowingResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j <= 10) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Board) kv.getKey()).getName()
            );
        }

        assertTrue(!lst.isEmpty());
    }

    @Test
    public void TestProfileBoards() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("kelley6292", null, PinterestObject.PinterestObjectResources.ProfileBoardsResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapeBoards_ProfileBoardsResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j < 100) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Board) kv.getKey()).getName()
            );
        }

        assertTrue(!lst.isEmpty());
    }

    @Test
    public void TestRepinRepinners() throws Exception {
        Account a = MySecondAccount();
        AQuery pat = new AQuery("/pin/61783826113629595/repins/", null, PinterestObject.PinterestObjectResources.RepinFeedResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapeBoards_RepinnedOntoBoards(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j < 3) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Board) kv.getKey()).getName()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    

}
