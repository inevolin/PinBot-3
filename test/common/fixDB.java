/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.AccountManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import model.Account;
import model.Campaign;
import model.configurations.AConfiguration;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Comment;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;
import static org.junit.Assert.*;
import pinbot3.helpers.Updater;

/**
 *
 * @author healzer
 */
public class fixDB {

    public fixDB() {
    }

    @Test
    public void testPersisting() throws InterruptedException, IOException {
        //fixDB();
        Board b = new Board("name", "urln", "123456789", "2", "galeeene", null, true, 0, PinterestObject.PinterestObjectResources.SearchResource);
        AQuery q = new AQuery("tester", b, PinterestObject.PinterestObjectResources.External);

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        pinbot3.PinBot3.dalMgr.printQueriesCount();
        //pinbot3.PinBot3.dalMgr.SaveObj(q);
        pinbot3.PinBot3.dalMgr.printQueriesCount();
        System.err.println(q.getId() + "  " + q.getBoardMapped().getId());

        /*
            persisting works
            !!! BUT !!!
            bq is not the same as b.getMappedQuery()
            if we persist b first.
         */
        //assertTrue(b.getId() != null && b.getMappedQuery().getId() != null);
        Board test = (Board) q.getBoardMapped();
        assertFalse(q.equals(test));
    }

    public Account MySecondAccount() {
        Account acc = new Account();
        String Email = "test@gmail.com", Password = "password";
        acc.setEmail(Email);
        acc.setPassword(Password);
        acc.base_url = "https://www.pinterest.com";
        return acc;
    }

    @Test
    public void testPersistingWithAccount() throws InterruptedException, IOException, Exception {
        fixDB(); // ===> RUN THIS if SQL errors (constraints)
        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        AccountManager am = pinbot3.PinBot3.accountMgr;
        Account acc = MySecondAccount();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }
        acc.getBoards().clear();
        am.Login(acc);
        am.refreshBoards(acc);

        Board b = new Board("name", "urln", "123456789", "2", "galeeene", null, true, 0, PinterestObject.PinterestObjectResources.SearchResource);
        //acc.getBoards().add(b);

        //pinbot3.PinBot3.dalMgr.printQueriesCount();
        pinbot3.PinBot3.dalMgr.Save(acc);
        //pinbot3.PinBot3.dalMgr.printQueriesCount();

        System.err.println(((Board) acc.getBoards().toArray()[0]).getId());
        assertTrue(((Board) acc.getBoards().toArray()[0]).getId() != null);
    }

    @Test
    public void testPersistingWithAccountCampaign() throws InterruptedException, IOException, Exception {
        // fixDB(); // ===> RUN THIS if SQL errors (constraints)
        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        AccountManager am = pinbot3.PinBot3.accountMgr;
        Account acc = MySecondAccount();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        pinbot3.PinBot3.dalMgr.printQueriesCount();

        Campaign c = new Campaign();
        c.setCampaignName("name");
        AConfiguration cf = new FollowConfiguration();
        c.getConfigurations().add(cf);

        Board b = new Board("name", "urln", "123456789", "2", "galeeene", null, true, 0, PinterestObject.PinterestObjectResources.SearchResource);
        AQuery q = new AQuery("tester", b, PinterestObject.PinterestObjectResources.External);

        cf.getQueries().add(q);

        acc.getCampaigns().add(c);
        acc.enableCampaign(c);
        pinbot3.PinBot3.dalMgr.Save(acc);
        pinbot3.PinBot3.dalMgr.printQueriesCount();
        Long id = ((AConfiguration) acc.getSelectCampaign().getConfigurations().toArray()[0]).getId();
        System.err.println(id);
        assertTrue(id != null);
    }

    @Test
    public void testPersistingComments() throws InterruptedException, IOException, Exception {
        // fixDB(); // ===> RUN THIS if SQL errors (constraints)
        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();
        pinbot3.PinBot3.dalMgr.InitEMF();

        Account acc = MySecondAccount();
        List<Account> accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        Campaign c = acc.getSelectCampaign();
        CommentConfiguration cc = (CommentConfiguration) c.getConfigurations().stream().filter(x -> x instanceof CommentConfiguration).findAny().get();
        AQuery q = new AQuery("searchquery", null, PinterestObject.PinterestObjectResources.BoardFeedResource);
        Comment m = new Comment(PinterestObject.PinterestObjectResources.BoardFeedResource, q);
        cc.getMapping().clear();
        cc.getMapping().add(m);
        System.err.println(m.getMappedQuery().getQuery());

        pinbot3.PinBot3.dalMgr.Save(acc);

        pinbot3.PinBot3.dalMgr.InitEMF();
        accs = pinbot3.PinBot3.dalMgr.AllAccounts();
        for (Account ac : accs) {
            if (ac.getEmail().equalsIgnoreCase(acc.getEmail())) {
                acc = ac;
                break;
            }
        }

        c = acc.getSelectCampaign();
        cc = (CommentConfiguration) c.getConfigurations().stream().filter(x -> x instanceof CommentConfiguration).findAny().get();
        Iterator<Comment> it = cc.getMapping().iterator();
        m = it.next();

        assertTrue(m.getMappedQuery() != null);
    }

    @Test
    public void fixDB() throws InterruptedException, IOException, Exception {
        // delete "db" : consider corrupt
        // copy "db - Copy" to "db"

        final Path copy = Paths.get("." + File.separatorChar + "db - Copy" + File.separatorChar);
        if (!Files.exists(copy.getFileName())) {
            System.err.println("Missing back-up database 'db - Copy'");
            return;
        }
        final Path delete = Paths.get("." + File.separatorChar + "db" + File.separatorChar);

        if (Files.exists(delete)) {
            Files.walk(delete, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
        Thread.sleep(500);
        copyDirectory(copy.toFile(), delete.toFile());

        pinbot3.PinBot3.InitExecutors();
        pinbot3.PinBot3.InitMgrs();

        Updater u  = new Updater(null);
        //fixDB_sept_2016 patch = new fixDB_sept_2016(null);
        //assertTrue(patch.getMayContinue());
    }
    
    @Test
    public void DBVersionDetection() {
        System.getProperties().put("DBv", "2");
        Properties p = System.getProperties();
        for (Entry o : p.entrySet()) {
            System.err.println(o);
        }
        
    }

    public void copy(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            copyDirectory(sourceLocation, targetLocation);
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }

        for (String f : source.list()) {
            copy(new File(source, f), new File(target, f));
        }
    }

    private void copyFile(File source, File target) throws IOException {
        try (
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(target)) {
            byte[] buf = new byte[1024];
            int length;
            while ((length = in.read(buf)) > 0) {
                out.write(buf, 0, length);
            }
        }
    }

}
