/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.helpers;

import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.control.ButtonType;

/**
 *
 * @author UGent
 */
public class Privileges {

    public static enum TYPES {
        PREMIUM
    };

    private Set<TYPES> myPrivs;

    public Set<TYPES> getMyPrivs() {
        if (myPrivs == null) {
            myPrivs = new HashSet<>();
        }
        return myPrivs;
    }

    public Boolean isTrial() {
        return !getMyPrivs().contains(Privileges.TYPES.PREMIUM);
    }

    public void premiumDialog(String msg) {
        if (msg == null) {
            msg = "This feature is for Premium users only.";
        }
        
        if (common.Dialogs.OkCancelDialog(msg + "\nWould you like to upgrade to Premium?", "Premium only.") == ButtonType.OK) {
            openWebpage("https://pinbot3.com/");
        }
    }

    public static void openWebpage(String url) {
        try {
            URI uri = new URL(url).toURI();
            Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
            if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                try {
                    desktop.browse(uri);
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                }
            }
        } catch (URISyntaxException ex) {
            common.ExceptionHandler.reportException(ex);
            
        } catch (MalformedURLException ex) {
            common.ExceptionHandler.reportException(ex);
            
        }
    }
}
