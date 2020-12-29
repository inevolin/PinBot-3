/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author UGent
 */
public class CookiesTest {

    public CookiesTest() {
    }

    @Test
    public void Test1() throws Exception {
        Account acc = new Account();
        Login(acc);
        TestPostLogin(acc);
        assertEquals("galeenefashion", acc.getUsername());
    }

    public void Login(Account acc) throws Exception {

        Http instance = new Http(null, acc.getCookieStore());

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", Http.ACCEPT_HTML);
        headers.put("User-Agent", Http.USER_AGENT);
        
        KeyValuePair<Integer> rs = instance.Get("https://www.pinterest.com/login/", headers);

        String appVersion = "";
        Pattern pattern = Pattern.compile("\"app_version\":\"(.+?)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(rs.getKey());
        while (matcher.find()) {
            appVersion = matcher.group(1);
        }
        acc.setAppVersion(appVersion);

        headers = new HashMap<String, String>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", acc.getAppVersion());
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", acc.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Referer", "https://www.pinterest.com/login/");        
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        String Email = "test@gmail.com", Password = ("password").replace("\\", "\\\\");
        acc.setEmail(Email);
        acc.setPassword(Password);
        
        String data = "";
        try {
            data = "source_url=" + URLEncoder.encode("/login/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"username_or_email\":\"" + Email
                            + "\",\"password\":\"" + Password
                            + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&module_path="
                    + URLEncoder.encode("App()>LoginPage()>Login()>Button(class_name=primary, text=Log in, type=submit, size=large)", StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
        }
        rs = instance.Post("https://www.pinterest.com/resource/UserSessionResource/create/", data, headers);

    }

    public void TestPostLogin(Account acc) throws Exception {
        //cmgr = new CookieManager(); <===== this will result in a FAIL.
        Http instance = new Http(null, acc.getCookieStore());

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", Http.ACCEPT_HTML);
        headers.put("User-Agent", Http.USER_AGENT);

        KeyValuePair<Integer> rs = instance.Get("https://www.pinterest.com/", headers);
        String username = "";
        Pattern pattern = Pattern.compile("\"username\":\"([a-zA-Z0-9_\\-]+)\",\"field_set_key\":\"homefeed\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(rs.getKey());
        while (matcher.find()) {
            username = matcher.group(1);
        }
        System.err.println(username);
        acc.setUsername(username);
    }
}
