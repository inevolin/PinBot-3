/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.DALManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import model.Account;
import model.Campaign;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.queries.AQuery;
import model.configurations.queries.Helpers;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

/**
 *
 * @author healzer
 */
public class SaveOverFlowFix {

    DALManager dm = new DALManager();

    public SaveOverFlowFix() {
        dm.InitEMF();

        String i = "dat";
        Board b = new Board(i, i, i, i, i, null, true, 0, PinterestObject.PinterestObjectResources.BoardFeedResource);
        LinkedHashSet<String> lhs = new LinkedHashSet<>();
        for (int j = 0; j < 100; j++) {
            lhs.add(Integer.toString(j));
        }
        mapping.put(b, lhs);
    }

    private HashMap<Board, LinkedHashSet<String>> mapping = new HashMap<>();

    void toCf_mapping(AConfiguration pinCf, Account acc) {
        //pinCf.getQueries().clear();
        if (pinCf.getQueries().size() > 20) {
            int j = 0;
            while (pinCf.getQueries().size() > 0) {
                pinCf.getQueries().remove(0);
                if (j++ == 10) {
                    dm.Save(acc);
                    j = 0;
                }
            }
        }
        System.err.println("After clear, we have: " + pinCf.getQueries().size() + " queries");
        for (Map.Entry<Board, LinkedHashSet<String>> entry : mapping.entrySet()) {
            for (String s : entry.getValue()) {
                Board b = entry.getKey().copy(null);
                AQuery q = Helpers.typeByPatter_pin(s, b).copy(null); //
                pinCf.getQueries().add(q);
            }
        }
        /* for (int i = 0; i < 200; i++) {
            pinCf.getQueries().remove(0);
        }*/
    }

    @Test
    public void t() throws InterruptedException, IOException {
        /*
            9 oct 2016, problem:
            Saving a configuration for 2nd time throws Stack Overflow Exception, but why?
                why => Too many queries are being deleted at once.
         */

        Account acc = dm.AllAccounts().get(0); // specific client's account

        Campaign c = acc.getCampaigns().iterator().next();
        Iterator<AConfiguration> icf = c.getConfigurations().iterator();
        AConfiguration cf = icf.next();
        while (icf.hasNext() && !(cf instanceof PinConfiguration)) {
            cf = icf.next();
        }

        for (int i = 0; i < 1; i++) {
            System.err.println("We have: " + cf.getQueries().size() + " queries");
            System.err.println("Emulate btnSave...");
            toCf_mapping(cf, acc);
            System.err.println("We have: " + cf.getQueries().size() + " queries");
            dm.Save(acc);
        }

    }
}
