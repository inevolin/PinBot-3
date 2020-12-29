/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import Managers.AccountManager;
import java.util.HashMap;
import java.util.Map;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import scraping.sources.ScrapePins_BoardFeedResource;
import scraping.sources.ScrapePins_SearchResource;
import scraping.sources.ScrapePins_UserPinsResource;

public class Pins_Test {

    public Pins_Test() {
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password"; //        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        acc.base_url = "https://www.pinterest.com";
        return acc;
    }

    @Test
    public void TestBoardFeed() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("/liatwa/lovely-rose-gold/", null, PinterestObject.PinterestObjectResources.BoardFeedResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePins_BoardFeedResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j <= 1) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pin) p).getDescription()
            );
        }

        assertTrue(!lst.isEmpty());
    }

    @Test
    public void TestSearch() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.SearchResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePins_SearchResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j <= 1) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pin) p).getDescription()
            );
        }

        assertTrue(!lst.isEmpty());
    }

    @Test
    public void TestUserPins() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("/liatwa/pins/", null, PinterestObject.PinterestObjectResources.UserPinsResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePins_UserPinsResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j <= 1) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pin) p).getDescription()
            );
        }

        assertTrue(!lst.isEmpty());
    }

}
