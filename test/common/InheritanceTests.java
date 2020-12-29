/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.AccountManager;
import Managers.DALManager;
import Managers.DALManager.Errors;
import Managers.ScrapeManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import model.Account;
import model.Campaign;
import model.configurations.AConfiguration;
import model.configurations.AConfiguration.RunStatus;
import model.Proxy;
import model.configurations.LikeConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class InheritanceTests {

    public InheritanceTests() {
    }

    @Test
    public void testNewAccount() throws IOException {
        DALManager dm = new DALManager();
        Errors err = null;

        Account acc = createTestAccount();
        err = dm.Save(acc); //persist super parent        

        /////////////////////////////////////////////////////////////////////////////////////////
        //  We have scraped something, a Board in this case:
        
        /*Board externalBoard = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        externalBoard.setName("someone's board");

        List<Board> boards = new ArrayList<Board>(acc.getBoards());
        Campaign sc = acc.getSelectCampaign();
        for (AConfiguration cf : sc.getConfigurations()) {
            if (cf instanceof PinConfiguration) {
                ((PinConfiguration) cf).getQueue().add(externalBoard);//externalBoard
            }
        }
        err = dm.SaveCampaign(sc);*/
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        for (Account a : dm.AllAccounts()) {
            System.err.println(a.getEmail());
            for (Board bo : a.getBoards()) {
                System.err.println(" -Board: " + bo.getName());
            }
            for (Campaign ca : a.getCampaigns()) {
                System.err.println("   Campaign: " + ca.getCampaignName());
                for (AConfiguration cf : ca.getConfigurations()) {
                    System.err.println("      " + cf.getClass().getSimpleName() + ": " + cf.getId());

                    if (cf instanceof PinConfiguration) {
                        System.err.println("          queue -- PinConfig");
                        /*for (Map.Entry<PinterestObject, Board> kv : ((PinConfiguration) cf).getQueue().entrySet()) {
                            System.err.println("          ------- " + ((Board) kv.getKey()).getName() + "  will be pinned to board " + kv.getValue().getName());
                        }*/
                    }
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////

        assertEquals(Errors.NO_ERROR, err);
    }

    public Account createTestAccount() {
        /////////////////////////////////////////////////////////////////////////////////////////
        //create super parent
        Account acc = new Account();
        Proxy pr = new Proxy();
        pr.setIp("123");
        pr.setEnabled(false);
        acc.setEmail("healzer" + (new Date()).getTime() + "@gm.com");
        acc.setPassword("");
        acc.setUsername("");
        acc.setProxy(pr);
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        Board b1 = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        b1.setBoardId("123456789");
        b1.setName("fashion corner");
        acc.getBoards().add(b1);
        Board b2 = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        b2.setBoardId("987654321");
        b2.setName("shoes & trinkets");
        acc.getBoards().add(b2);
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        ///////////////campaign 1//////////
        Campaign c = new Campaign();
        c.setCampaignName("name1");

        AConfiguration c1 = new PinConfiguration();
        ((PinConfiguration) c1).status = RunStatus.IDLE;
        ((PinConfiguration) c1).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c1);

        AConfiguration c2 = new RepinConfiguration();
        ((RepinConfiguration) c2).status = RunStatus.IDLE;
        ((RepinConfiguration) c2).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c2);

        acc.getCampaigns().add(c);
        acc.enableCampaign(c);
        ///////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        ///////////////campaign 2//////////
        c = new Campaign();
        c.setCampaignName("name2");

        c1 = new PinConfiguration();
        ((PinConfiguration) c1).status = RunStatus.IDLE;
        ((PinConfiguration) c1).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c1);

        c2 = new RepinConfiguration();
        ((RepinConfiguration) c2).status = RunStatus.IDLE;
        ((RepinConfiguration) c2).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c2);

        // acc.getCampaigns().add(c);
        ///////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        return acc;
    }

    @Test
    public void testMyAccount() throws IOException {
        DALManager dm = new DALManager();
        Errors err = null;

        /////////////////////////////////////////////////////////////////////////////////////////
        //create super parent
        Account acc = new Account();
        Proxy pr = new Proxy();
        pr.setEnabled(false);
        acc.setEmail("test@gmail.com");
        acc.setPassword("password");
        acc.setProxy(pr);
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        Board b1 = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        b1.setBoardId("123456789");
        b1.setName("fashion corner");
        acc.getBoards().add(b1);
        Board b2 = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        b2.setBoardId("987654321");
        b2.setName("shoes & trinkets");
        acc.getBoards().add(b2);
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        ///////////////campaign 1//////////
        Campaign c = new Campaign();
        c.setCampaignName("name1");

        AConfiguration c1 = new PinConfiguration();
        ((PinConfiguration) c1).status = RunStatus.IDLE;
        ((PinConfiguration) c1).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c1);

        AConfiguration c2 = new RepinConfiguration();
        ((RepinConfiguration) c2).status = RunStatus.IDLE;
        ((RepinConfiguration) c2).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c2);

        acc.getCampaigns().add(c);
        acc.enableCampaign(c);
        ///////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        ///////////////campaign 2//////////
        c = new Campaign();
        c.setCampaignName("name2");

        c1 = new PinConfiguration();
        ((PinConfiguration) c1).status = RunStatus.IDLE;
        ((PinConfiguration) c1).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c1);

        c2 = new RepinConfiguration();
        ((RepinConfiguration) c2).status = RunStatus.IDLE;
        ((RepinConfiguration) c2).setIsActive(Boolean.TRUE);
        c.getConfigurations().add(c2);

        acc.getCampaigns().add(c);
        ///////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////

        err = dm.Save(acc); //persist super parent        

        /////////////////////////////////////////////////////////////////////////////////////////
        //  We have scraped something, a Board in this case:
        Board externalBoard = new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        externalBoard.setName("someone's board");

        acc = dm.AllAccounts().get(0);
        List<Board> boards = new ArrayList<>(acc.getBoards());
        Campaign sc = acc.getSelectCampaign();
        for (AConfiguration cf : sc.getConfigurations()) {
            if (cf instanceof PinConfiguration) {
                //((PinConfiguration) cf).getQueue().put(externalBoard, boards.get(0));//externalBoard
            }
        }
        err = dm.Save(acc);
        /////////////////////////////////////////////////////////////////////////////////////////

        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////
        for (Account a : dm.AllAccounts()) {
            System.err.println(a.getEmail());
            for (Board bo : a.getBoards()) {
                System.err.println(" -Board: " + bo.getName());
            }
            for (Campaign ca : a.getCampaigns()) {
                System.err.println("   Campaign: " + ca.getCampaignName());
                for (AConfiguration cf : ca.getConfigurations()) {
                    System.err.println("      " + cf.getClass().getSimpleName() + ": " + cf.getId());

                    if (cf instanceof PinConfiguration) {
                        System.err.println("          queue -- PinConfig");
                        /*for (Map.Entry<PinterestObject, Board> kv : ((PinConfiguration) cf).getQueue().entrySet()) {
                            System.err.println("          ------- " + ((Board) kv.getKey()).getName() + "  will be pinned to board " + kv.getValue().getName());
                        }*/
                    }
                }
            }
        }
        /////////////////////////////////////////////////////////////////////////////////////////

        assertEquals(Errors.NO_ERROR, err);
    }

    @Test
    public void delConf() throws Exception {

        DALManager dm = new DALManager();
        for (Account acc : dm.AllAccounts()) {
            //removing account: clear all OneToMany relations
            acc.getDuplicates_comment().clear();
            acc.getDuplicates_follow().clear();
            acc.getDuplicates_invite().clear();
            acc.getDuplicates_like().clear();
            acc.getDuplicates_message().clear();
            acc.getDuplicates_pin().clear();
            acc.getDuplicates_repin().clear();
            acc.getDuplicates_unfollow().clear();
            acc.getBoards().clear();
            dm.RemoveAccount(acc);
        }
    }

    @Test
    public void HashTest() {
        HashSet<Pin> h = new HashSet<>();
        Pin p = new Pin(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        p.setHashUrl("1");
        h.add(p);

        p = new Pin(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        p.setPinId("132");
        p.setHashUrl("2");
        h.add(p);

        //h.remove(p);
        assertTrue(h.size() == 2);
    }

    public Account MyAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = ("password").replace("\\", "\\\\");
        Proxy pr = new Proxy();
        pr.setEnabled(Boolean.FALSE);
        acc.setProxy(pr);
        acc.setEmail(Email);
        acc.setPassword(Password);
        return acc;
    }

    @Test
    public void AQueryBlackListTest() throws Exception {

        DALManager dm = new DALManager();

        ExecutorService e = Executors.newFixedThreadPool(4);
        ScrapeManager sm = new ScrapeManager(e);
        AccountManager am = new AccountManager();

        Account acc = MyAccount();
        List<Account> accs = dm.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }
        Campaign c = new Campaign();
        c.setCampaignName("name1");        
        acc.enableCampaign(c);
        acc.getCampaigns().add(c);
        dm.Save(acc);
        
        PinConfiguration c1 = new PinConfiguration();
        c.getConfigurations().add(c1);
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setIsActive(Boolean.TRUE);
        Board selectedBoard = (Board) acc.getBoards().toArray()[0];
        AQuery q = new AQuery("luxury fashion 2", selectedBoard, PinterestObject.PinterestObjectResources.External);

        Boolean EXPECTED_VALUE = true;
        q.setBlacklisted(EXPECTED_VALUE);
        c1.getQueries().add(q);

        //does saving account also save blacklist value? => YES !
        dm.Save(acc);

        acc = MyAccount();
        accs = dm.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }
        PinConfiguration fc = (PinConfiguration) acc.getSelectCampaign().getConfigurations().toArray()[0];
        AQuery fq = fc.getQueries().get(0);
        assertTrue(fq.getBlacklisted() == EXPECTED_VALUE);
    }

    @Test
    public void accountSaveTest() throws InterruptedException, IOException {
        //we test adding objects to duplicates & saving account.
        DALManager dm = new DALManager();
        dm.InitEMF();

        Account acc = MyAccount();
        List<Account> accs = dm.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }
        
        System.err.println("We have: " + acc.getDuplicates_pin().size() + " pin-duplicates.");

        LikeConfiguration c1 = new LikeConfiguration();
        c1.status = AConfiguration.RunStatus.IDLE;
        c1.setActionMax(2);
        c1.setActionMin(2);
        c1.setTimeoutMin( 3);
        c1.setTimeoutMax( 6);
        c1.setIsActive(Boolean.TRUE);

        Campaign c = new Campaign();
        c.setCampaignName("name1");
        c.getConfigurations().add(c1);

        acc.enableCampaign(c);
        acc.getCampaigns().add(c);

        AQuery q1 = new AQuery("orbs", null, PinterestObject.PinterestObjectResources.SearchResource);
        c1.getQueries().add(q1);

        Pin pp = new Pin(PinterestObject.PinterestObjectResources.BoardFeedResource, null);
        
        pp.setHashUrl(Long.toString((new Date()).getTime()));
        acc.getDuplicates_pin().add(pp);
        
        dm.Save(acc);
       
    }
}
