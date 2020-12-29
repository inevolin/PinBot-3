/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import common.Http;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import common.KeyValuePair;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import model.Account;
import model.pinterestobjects.Board;

/**
 *
 * @author UGent
 */
public class AccountManager {

    /*
        This class and method is solely intended for performing Account/user related operations.
        This includes the login operation, which will get CSRFToken, AppVersion and cookies.
        But also a function to scrape user's boards. (todo)
        And a function to obtain user's statistics (pin count, followers count, ...). (todo)
     */
    private String base_url = "https://www.pinterest.com";

    private void test302_location(Http http, Account acc) {
        try {
            String newbase = http.testIf302(base_url, getGetHeaders());
            if (newbase != null) {
                if (newbase.charAt(newbase.length() - 1) == '/') {
                    base_url = newbase.substring(0, newbase.length() - 1);
                }
            }
            acc.base_url = base_url;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
    }

    public void Login(Account acc) throws Exception {

        acc.setStatus(Account.STATUS.BUSY);

        Http http = new Http(acc.getProxy(), acc.getCookieStore());
        test302_location(http, acc); //TEST 1

        KeyValuePair<Integer> resp = http.Get(base_url + "/login/", getGetHeaders());
        if (resp.getValue() != 200) {
            throw new Exception("Not good");
        }
        acc.setAppVersion(getAppVersion(resp.getKey()));
        //MyUtils.writeLog("1:\n--------------" + acc.getAppVersion() + "---------------\n\n", false);

        if (acc.getStatus() != Account.STATUS.BUSY) {
            acc.setStatus(Account.STATUS.UNAUTHORIZED);
            return;
        }
        resp = http.Post(base_url + "/resource/UserSessionResource/create/", getLoginData(acc), getPostHeaders(acc));
        //MyUtils.writeLog("2:\n", false);

        if (acc.getStatus() != Account.STATUS.BUSY) {
            acc.setStatus(Account.STATUS.UNAUTHORIZED);
            return;
        }
        test302_location(http, acc); //TEST 2

        resp = http.Get(base_url + "/", getGetHeaders());
        //MyUtils.writeLog("3:\n", false);

        acc.setUsername(parseUsername(resp));
        //MyUtils.writeLog("4:\n-----------------------------\n\n", false);

        if (acc.getUsername().length() > 0) {
            refreshStats(acc);
            acc.reLoginAttempts = 0; // !!!!
            acc.setStatus(Account.STATUS.LOGGEDIN);
            acc.setLastLogin(new Date());
        } else {
            acc.setStatus(Account.STATUS.UNAUTHORIZED);
        }

        return;
    }

    private String getAppVersion(String rs) {
        String appVersion = "";
        Pattern pattern = Pattern.compile("\"app_version\": ?\"(.+?)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(rs);
        while (matcher.find()) {
            appVersion = matcher.group(1);
        }
        return appVersion;
    }

    private String getLoginData(Account acc) {
        String Email = acc.getEmail();
        String Password = acc.getPassword().replace("\\", "\\\\").replace("\"", "\\\"");
        String data = "";
        try {
            data = "source_url=" + URLEncoder.encode("/login/?referrer=home_page", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"username_or_email\":\"" + Email
                            + "\",\"password\":\"" + Password
                            + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&module_path="
                    + URLEncoder.encode("App()>LoginPage()>Login()>Button(class_name=primary, text=Log in, type=submit, size=large)", StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
        }
        return data;
    }

    private Map<String, String> getPostHeaders(Account acc) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", acc.getAppVersion());
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", acc.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Referer", base_url + "/login/");
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private Map<String, String> getGetHeaders() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", Http.ACCEPT_HTML);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private String parseUsername(KeyValuePair<Integer> response) throws Exception {
        String username = null;

        Optional<String> ojsonresp = extractJsonFromHtmlScript(response);
        if (!ojsonresp.isPresent() || !isJSONValid(ojsonresp.get())) {
            throw new Exception("#166 AM: fault parsing username");
        }

        Gson gson = new Gson();
        JsonElement element = gson.fromJson(ojsonresp.get(), JsonElement.class);
        JsonObject jo = element.getAsJsonObject();
        JsonObject juser = jo.getAsJsonObject("context").getAsJsonObject("user");
        username = juser.getAsJsonPrimitive("username").getAsString();

        return username;
    }

    private static Pattern patternJsonInHtml_jsInit1 = Pattern.compile("<script type=\"application/json\" id='jsInit1'>(.+?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);

    public static Optional<String> extractJsonFromHtmlScript(KeyValuePair<Integer> response) {
        Optional<String> resp = Optional.empty();
        Matcher m = patternJsonInHtml_jsInit1.matcher(response.getKey());
        if (!m.find()) {
            return resp;
        }
        String found = m.group(1);
        if (found != null && isJSONValid(found)) {
            resp = Optional.of(found);
        }

        return resp;
    }

    public static boolean isJSONValid(String str) {
        Gson gson = new Gson();
        try {
            gson.fromJson(str, JsonElement.class);
        } catch (JsonSyntaxException ex) {
            try {
                gson.fromJson(str, JsonArray.class);
            } catch (JsonSyntaxException ex1) {
                return false;
            }
        }
        return true;
    }

    public void refreshStats(Account acc) throws InterruptedException, Exception {
        Http http = new Http(acc.getProxy(), acc.getCookieStore());
        KeyValuePair<Integer> resp = http.Get(base_url + "/" + acc.getUsername() + "/", getGetHeaders());

        Optional<String> ojsonresp = extractJsonFromHtmlScript(resp);
        if (!ojsonresp.isPresent() || !isJSONValid(ojsonresp.get())) {
            throw new Exception("#222 AM: cannot parse stats");
        }

        Gson gson = new Gson();
        JsonElement element = gson.fromJson(ojsonresp.get(), JsonElement.class);
        JsonObject jo = element.getAsJsonObject();
        JsonObject jdata = jo.getAsJsonObject("tree").getAsJsonObject("data");

        acc.setMyFollowing(jdata.getAsJsonPrimitive("following_count").getAsInt());
        acc.setMyFollowers(jdata.getAsJsonPrimitive("follower_count").getAsInt());
        acc.setMyLikes(jdata.getAsJsonPrimitive("like_count").getAsInt());
        acc.setMyPins(jdata.getAsJsonPrimitive("pin_count").getAsInt());
        acc.setMyBoards(jdata.getAsJsonPrimitive("board_count").getAsInt());
    }

    public void refreshBoards(Account acc) throws InterruptedException, ExecutionException {
        if (acc.getUsername() == null || acc.getUsername().length() == 0) {
            return;
        }

        acc.getBoards().clear();
        Set<Board> rs1 = new HashSet<>();
        Set<Board> tmp = pinbot3.PinBot3.scrapeMgr.ScrapeUserBoards_1(acc);
        if (tmp != null) {
            rs1.addAll(tmp);
        }
        tmp = pinbot3.PinBot3.scrapeMgr.ScrapeUserBoards_2(acc);
        if (tmp != null) {
            rs1.addAll(tmp);
        }
        //delete from scraped all those that already exist in our account
        Iterator<Board> it = rs1.iterator();
        while (it.hasNext()) {
            Board b = it.next();
            acc.getBoards().forEach(x -> {
                if (x.getBoardId().equalsIgnoreCase(b.getBoardId())) {
                    it.remove();
                }
            });
        }
        for (Board b : rs1) {
            acc.getBoards().add(b);
        }

    }
}
