/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import java.util.HashMap;
import java.util.Map;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import scraping.sources.Scrape_ImgFave;

public class ImgFave_Test {
    
    public ImgFave_Test() {
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password"; //        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }

    public Account MyAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }
    
    @Test
    public void TestImgFave() throws Exception {
        Account a = MySecondAccount();
        AQuery pat = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.External);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new Scrape_ImgFave(a, pat, null);

        // page 1
        ss.call();
        lst.putAll(ss.getFound());
        ss.getFound().clear(); /// !!!!!!!!!

        // page 2
        ss.call();
        lst.putAll(ss.getFound());
        ss.getFound().clear(); /// !!!!!!!!!

        // page 3
        ss.call();
        lst.putAll(ss.getFound());
        ss.getFound().clear(); /// !!!!!!!!!

        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Pin) kv.getKey()).getHashUrl()
            );
        }

        assertTrue(lst.size() > 0);
    }
    
}
