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
import model.pinterestobjects.Board;
import model.pinterestobjects.Comment;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import static org.junit.Assert.*;
import scraping.sources.ScrapePinComments;

/**
 *
 * @author healzer
 */
public class Comments_Test {

    public Comments_Test() {
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
    public void TestScrapeComments() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);

        Pin p = new Pin(PinterestObject.PinterestObjectResources.IndividualPin, null);
        p.setPinId(("61783826113629595"));

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapePinComments(a, p, null);

        ss.call();
        lst.putAll(ss.getFound());
        ss.getFound().clear(); /// !!!!!!!!!

        int i = 1;
        for (PinterestObject c : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Comment) c).getUsername() + ": " + ((Comment) c).getText()
            );
        }

        assertTrue(!lst.isEmpty());
    }
}
