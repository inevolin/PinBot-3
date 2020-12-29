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
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import scraping.sources.ScrapePinners_BoardFollowersResource;
import scraping.sources.ScrapePinners_PinLikedBy;
import scraping.sources.ScrapePinners_SearchResource;
import scraping.sources.ScrapePinners_UserFollowersResource;
import scraping.sources.ScrapePinners_UserFollowingResource;

public class Pinners_Test {
    
    public Pinners_Test() {
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
    public void TestBoardFollowers() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        //does not contain our data on first request
        AQuery pat = new AQuery("/liatwa/lovely-rose-gold/", null, PinterestObject.PinterestObjectResources.BoardFollowersResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinners_BoardFollowersResource(a, pat, null);
        

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!
/*
        // page n
        int j = 0;
        while (size > 0 && j <= 1) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }
*/
        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    
    @Test
    public void TestSearch() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        am.Login(a);
        
        AQuery pat = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.SearchResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinners_SearchResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!
/*
        // page n
        int j = 0;
        while (size > 0 && j <= 10) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }
*/
        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    
    @Test
    public void TestUserFollowers() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        am.Login(a);
        
        AQuery pat = new AQuery("/CPFblog/followers/", null, PinterestObject.PinterestObjectResources.UserFollowersResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinners_UserFollowersResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!
/*
        // page n
        int j = 0;
        while (size > 0 && lst.size() <= 1010 && j <= 100000) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }
*/
        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    
    @Test
    public void TestUserFollowing() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        //does not contain our data on first request
        AQuery pat = new AQuery("/kelley6292/following/people/", null, PinterestObject.PinterestObjectResources.UserFollowingResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinners_UserFollowingResource(a, pat, null);

        // page 1
        int size = 0;
        ss.call();
        lst.putAll(ss.getFound());
        size = ss.getFound().size();
        ss.getFound().clear(); /// !!!!!!!!!

        // page n
        int j = 0;
        while (size > 0 && j <= 2) {
            j++;
            ss.call();
            lst.putAll(ss.getFound());
            size = ss.getFound().size();
            ss.getFound().clear(); /// !!!!!!!!!
        }

        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    
    @Test
    public void TestPinLikes() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("/pin/250723904235094290/likes/", null, PinterestObject.PinterestObjectResources.UserFollowingResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinners_PinLikedBy(a, pat, null);

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
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
    
}
