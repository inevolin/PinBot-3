/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import static junit.framework.Assert.assertEquals;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import scraping.ScrapeSession;
import scraping.sources.ScrapeBoards_SearchResource;

public class KeyValuePairTests {
    
    public KeyValuePairTests() {
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password"; //        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }
    
    @Test
    public void testHashCode() throws Exception{
        Account a = MySecondAccount();
        AQuery pat = new AQuery("cars", null, PinterestObject.PinterestObjectResources.SearchResource);

        ArrayList<PinterestObject> lst = new ArrayList<>();

        ScrapeSession ss = new ScrapeBoards_SearchResource(a, pat, null);

        // page 1
        ss.call();
        lst.addAll(ss.getFound().keySet());
        ss.getFound().clear(); /// !!!!!!!!!
        
              
        
        LinkedHashSet<KeyValuePair> set = new LinkedHashSet<>();
        
        set.add(createKVP(lst.get(0)));
        assertEquals(set.size(), 1);
        
        set.add(createKVP(lst.get(1)));
        assertEquals(set.size(), 2);
        
        set.add(createKVP(lst.get(0)));
        assertEquals(set.size(), 2);
        
        set.remove(createKVP(lst.get(0)));
        assertEquals(set.size(), 1);
        
        set.add(createKVP(lst.get(0)));
        assertEquals(set.size(), 2);
        
    }
    
    private KeyValuePair createKVP(PinterestObject obj){
        Board b = (Board) obj;
        return new KeyValuePair(b.getName(), b);
    }
    
}
