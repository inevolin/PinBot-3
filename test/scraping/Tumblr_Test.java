/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import Managers.DALManager;
import Managers.ScrapeManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Account;
import model.Campaign;
import model.configurations.AConfiguration;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.sources.Scrape_Tumblr;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author UGent
 */
public class Tumblr_Test {

    public Tumblr_Test() {
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
    public void TestTumblr() throws Exception {
        DALManager dm = new DALManager();
        Account a =MySecondAccount();
        AQuery pat = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.External);

        Map<PinterestObject, Board> lst = new HashMap<>();

        ScrapeSession ss = new Scrape_Tumblr(a, pat, null);

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

    //Similar to the PinAlgo implementation
    @Test
    public void TestScrapeMgr() throws Exception {
        ExecutorService e = Executors.newFixedThreadPool(4);
        ScrapeManager sm = new ScrapeManager(e);

        DALManager dm = new DALManager();
        Account a = dm.AllAccounts().get(0);
        Campaign c = a.getSelectCampaign();
        for (AConfiguration cf : c.getConfigurations()) {
            if (cf.getQueries() == null) {
                cf.setQueries(new ArrayList<AQuery>());
            }
            AQuery nw = new AQuery("fashion", null, PinterestObject.PinterestObjectResources.External);
            cf.getQueries().add(nw);

            System.err.println("" + cf.getClass().getSimpleName() + "   " + cf.toString());
            Map<PinterestObject, Board> lst = new HashMap<>();
            Map<PinterestObject, Board> scraped = sm.Scrape(cf, a, null); //page 1  ; may return null !!!
            if (scraped == null) {
                System.err.println("nothing to scrape***");
            } else {
                lst.putAll(scraped);
                scraped.clear(); /// !!!!!!!!!

                scraped = sm.Scrape(cf, a, null);//page 2
                lst.putAll(scraped);
                scraped.clear(); /// !!!!!!!!!

                int i = 1;
                for (Map.Entry<PinterestObject, Board> kv : lst.entrySet()) {
                    System.err.println(
                            (i++) + " " + ((Pin) kv.getKey()).getHashUrl()
                    );
                }
            }
            System.err.println("__");
            System.err.println("__");
        }

    }

}
