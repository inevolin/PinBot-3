/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.UnsupportedEncodingException;
import java.util.Set;
import org.junit.Test;
import pinbot3.KeywordGenController;

/**
 *
 * @author UGent
 */
public class KeywordGen {

    public KeywordGen() {
    }

    @Test
    public void KwG_single() throws InterruptedException, UnsupportedEncodingException {
        KeywordGenController c = new KeywordGenController();
        c.initHttp();
        Set<String> ls = c.singleLevel("fashion");
        System.err.println(ls.size());        
        ls.forEach((x) -> {System.err.println(x);});
        

    }
    @Test
    public void KwG_multi() throws InterruptedException, UnsupportedEncodingException {
        KeywordGenController c = new KeywordGenController();
        c.initHttp();
        Set<String> ls = c.multiLevel("fashion");
        System.err.println(ls.size());
        ls.forEach((x) -> {System.err.println(x);});
        

    }

}
