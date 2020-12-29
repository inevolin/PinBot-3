/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.util.regex.Pattern;

/**
 *
 * @author UGent
 */
public final class Validator {

   public static boolean isValidEmailAddress(String email) {
           String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
           java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
           java.util.regex.Matcher m = p.matcher(email);
           return m.matches();
    }
   
    public static boolean isValidProxy(String ip_port) {
           String ePattern = "^((\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})):(\\d{2,5})$";
           java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
           java.util.regex.Matcher m = p.matcher(ip_port);
           return m.matches();
    }
}
