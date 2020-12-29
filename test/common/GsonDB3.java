/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.DALManager;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import model.Account;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import org.junit.Test;

/**
 *
 * @author healzer
 */
public class GsonDB3 {

    DALManager dm = new DALManager();

    @Test
    public void t2() throws IOException {
        List<Account> accs = dm.AllAccounts();
        for (Account acc : accs) {
            dm.Save(acc);
        }
    }

    @Test
    public void t3del() throws IOException, Exception {
        Account sel = null;
        List<Account> accs = dm.AllAccounts();
        for (Account acc : accs) {
            if (acc.getEmail().equalsIgnoreCase("test@gmail.com")) {
                sel = acc;
            }
        }

        if (sel != null) {
            dm.RemoveAccount(sel);
        }
    }

    @Test
    public void t_getdup() throws IOException, Exception {
        Account sel = null;
        List<Account> accs = dm.AllAccounts();
        for (Account acc : accs) {
            if (acc.getEmail().equalsIgnoreCase("test@gmail.com")) {
                sel = acc;
            }
        }

        if (sel != null) {
            Set<PinterestObject> objs = dm.getExternalObjects(DALManager.TYPES.duplicates_follow_pinners, sel);
            System.err.println();

            Pinner p = new Pinner("suername", "baseusername", null, 0, 0, 0, 0, "132456", true, 0, PinterestObject.PinterestObjectResources.SearchResource);
            Board b = new Board("name", "urlName", "123456", "123456", "123132456", null, true, 0, PinterestObject.PinterestObjectResources.SearchResource);
            dm.appendExternalObject(p, DALManager.TYPES.duplicates_follow_pinners, sel);
            dm.appendExternalObject(b, DALManager.TYPES.duplicates_follow_boards, sel);

        }
    }

    @Test
    public void t_getdup2() throws IOException, Exception {
        Account sel = null;
        List<Account> accs = dm.AllAccounts();
        for (Account acc : accs) {
            if (acc.getEmail().equalsIgnoreCase("test@gmail.com")) {
                sel = acc;
            }
        }

        if (sel != null) {
            Set<PinterestObject> objs = dm.getExternalObjects(DALManager.TYPES.duplicates_pin, sel);
            System.err.println();

            if (objs.size() == 0) {
                Pin p = new Pin(null, "123456", "username", "boardName_EXT", "boardId_EXT", 0L, true, null, PinterestObject.PinterestObjectResources.SearchResource, "123456");
                dm.appendExternalObject(p, DALManager.TYPES.duplicates_pin, sel);
            }
        }
    }

}
