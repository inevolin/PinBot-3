/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.AccountManager;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import model.Account;
import model.Proxy;
import model.configurations.FollowConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import scraping.ScrapeSession;
import scraping.sources.ScrapeBoards_SearchResource;

/**
 *
 * @author healzer
 */
public class HeapSizeException {
    
    public HeapSizeException() {
    }
    
    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password".replace("\\", "\\\\");
        Proxy pr = new Proxy();
        pr.setEnabled(Boolean.FALSE);
        acc.setProxy(pr);
        acc.setEmail(Email);
        acc.setPassword(Password);
        acc.base_url = "https://www.pinterest.com";
        return acc;
    }
    
    
    // We just disabled getFoundAll functionality (the semi-dup checker) ; let's see how it goes using only regular dupChecker.
    @Test
    public void T1() throws Exception {
        Account acc = MySecondAccount();
        AccountManager am = new AccountManager();
        //am.Login(acc);
        
        AQuery q = new AQuery("abundance", null, PinterestObject.PinterestObjectResources.SearchResource);
        
        FollowConfiguration fc = new FollowConfiguration();
        fc.setActionMin(1000);
        fc.setActionMax(1000);
        fc.setIsActive(Boolean.TRUE);
        fc.setTimeoutMin(1);
        fc.setTimeoutMin(1);
        fc.getQueries().add(q);        
        
        ScrapeSession ss = new ScrapeBoards_SearchResource(acc, q, fc);
        
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<ScrapeSession> worker = ss;
        Future<ScrapeSession> future = executor.submit(worker);
        ScrapeSession ret = future.get();
        while (!ret.getFound().isEmpty()) {
            ret.getFound().clear();
            System.err.println(ret.getFoundAll().size());
            
            worker = ss;
            future = executor.submit(worker);
            ret = future.get();
        }
    }
}
