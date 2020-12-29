/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import static common.MyUtils.getRegistryByKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.impl.client.BasicCookieStore;

/**
 *
 * @author healzer
 */
public class ExceptionHandler {

    public static Map<String, Integer> exHashPool = new HashMap<>(); // hashcode ; count#

    private static String exceptionTrace(Exception ex) {
        if (ex == null)
            return "";
        ex.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String sr = sw.toString();
        return sr;
    }

    public static void reportException(Exception ex, String meta) {
        String sr = "---[meta]--\n" + (meta == null ? "" : meta) + "\n---[/meta]--\n";
        sr += exceptionTrace(ex);
        if (sr != null) {
            reportException(sr);
        }
    }

    public static void reportException(Exception ex) {
        String sr = exceptionTrace(ex);
        if (sr != null) {
            reportException(sr);
        }
    }

    private static void reportException(String msg) {
        String hc = Integer.toString(msg.hashCode());
        if (exHashPool.containsKey(hc)) {
            exHashPool.replace(hc, exHashPool.get(hc) + 1);
            // this exception has been reported more than once, we can tell the user about it!
        } else {
            exHashPool.put(hc, 1);
            sendMessage(msg); // we make sure this exception is sent only once
        }
    }

    private static String appendDetails(String msg) {
        String d = "";
        d = "Version: "+ pinbot3.PinBot3.VERSION;
        return d + "\n----\n" + msg;
    }
    private static void sendMessage(String msg) {        
        try {
            msg = appendDetails(msg);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Accept-Encoding", "gzip,deflate,sdch");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            headers.put("Cache-Control", "no-cache");
            headers.put("Pragma", "no-cache");
            headers.put("Accept", Http.ACCEPT_JSON);
            headers.put("User-Agent", Http.USER_AGENT);
            headers.put("Content-Type", "application/json");

            Http http = new Http(null, new BasicCookieStore());
            String url = "https://pinbot3.com/logging/report.php";

            String uuid = getRegistryByKey("UUID"); //are they premium?
            if (uuid == null || uuid.length() == 0) {
                uuid = getRegistryByKey("TRIAL"); //are they trial?
                if (uuid == null || uuid.length() == 0) {
                    uuid = "-1"; //whoops, something is not right
                    msg = "---!!!sendMessageError#76---\n" + msg;
                }
            }
            String post = "{\"uuid\":\"" + uuid + "\",\"msg\":\"" + msg + "\",\"hc\":\"" + msg.hashCode() + "\"}";
            KeyValuePair<Integer> resp = http.Post(url, post, headers);
            System.err.println(resp.getKey());
        } catch (Exception ex) {
            ex.printStackTrace();
            //do not send here, or we get infinite loop if server is down or smthn
        }
    }
}
