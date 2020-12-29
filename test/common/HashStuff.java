/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author healzer
 */
public class HashStuff {

    public HashStuff() {
    }

    @Test
    public void test1() {
        AConfiguration a = new PinConfiguration();
        a.setId(1L);
        AConfiguration b = new PinConfiguration();
        b.setId(2L);
        AConfiguration c = new RepinConfiguration();
        c.setId(3L);        
        AConfiguration d = new RepinConfiguration();
        d.setId(4L);

        Set<AConfiguration> set = new HashSet<>();

        set.add(a);
        set.add(b);
        set.add(c);
        set.add(d);

        assertTrue(set.size() == 4);

        AConfiguration pcf = null;
        try {
            pcf = set.stream().filter(x -> x instanceof RepinConfiguration).findFirst().get();
            set.remove(c);
        } catch (NoSuchElementException ex) {
        }
        
        assertTrue(set.size() == 3);
    }
}
