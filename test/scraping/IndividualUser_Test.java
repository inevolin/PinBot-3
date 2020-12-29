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
import scraping.sources.IndividualUser;

public class IndividualUser_Test {
    public IndividualUser_Test() {
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
    public void TestIndividualUser() throws Exception {
        Account a = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(a);
        
        AQuery pat = new AQuery("/alfabeta1717/", null, PinterestObject.PinterestObjectResources.IndividualUser);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new IndividualUser(a, pat, null);

        ss.call();
        lst.putAll(ss.getFound());
        ss.getFound().clear();

        int i = 1;
        for (PinterestObject p : lst.keySet()) {
            System.err.println(
                    (i++) + " " + ((Pinner) p).getUsername()
            );
        }

        assertTrue(!lst.isEmpty());
    }
}
