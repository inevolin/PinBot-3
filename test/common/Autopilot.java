/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.time.LocalTime;
import org.junit.Test;

/**
 *
 * @author UGent
 */
public class Autopilot {

    public Autopilot() {
    }

    @Test
    public void Intervals() {
        LocalTime start = LocalTime.of(23, 0, 0); //start at 'min'
        LocalTime now = LocalTime.of(22, 1, 0);

        System.err.println(now.isAfter(start));

    }
}
