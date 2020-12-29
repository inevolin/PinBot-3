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
import model.pinterestobjects.Category;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import static org.junit.Assert.*;
import scraping.sources.ScrapeCategories;

/**
 *
 * @author healzer
 */
public class Categories_test {

    public Categories_test() {
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
    public void t1() throws Exception {
        Account acc = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(acc);
        AQuery pat = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.CategoriesResource);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new ScrapeCategories(acc, pat, null);

        ss.call();
        lst.putAll(ss.getFound());
        
        int i = 1;
        for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
            System.err.println(
                    (i++) + " " + ((Category) kv.getKey()).getName()
            );
        }

        assertTrue(!lst.isEmpty());
    }

}
