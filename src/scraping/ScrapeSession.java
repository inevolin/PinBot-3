/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.Http;
import common.KeyValuePair;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.pinterestobjects.PinterestObject;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import scraping.sources.ScrapeBoards_RepinnedOntoBoards;
import scraping.sources.ScrapePinners_PinLikedBy;

/**
 *
 * @author UGent
 */
public abstract class ScrapeSession implements Callable<ScrapeSession> {

    protected String base_url;
    protected Account account;
    protected String bookmark;
    protected Map<PinterestObject, Board> found; //used for returning to Algo
    protected List<PinterestObject> foundAll;//used to store previously scraped ones, preventing dups (from current session)

    protected Boolean FirstRequest = true, EndReached = false;

    protected int page;//, amountToScrape;
    protected AQuery query;
    protected String baseUsername;
    protected String Url, Referer;
    protected KeyValuePair<Integer> response;
    protected Http http;
    protected AConfiguration config;

    ///////////////
    //when MAX_SCRAPE_NULL is reached then the EndReached will kick in.
    protected int MAX_SCRAPE_EMPTY = 3;
    protected int COUNT_SCRAPE_EMPTY = 0;
    ///////////////

    public ScrapeSession(Account account, AQuery query, AConfiguration config) throws Exception {
        this.account = account;
        this.found = new HashMap<>();
        this.foundAll = new ArrayList<>();
        this.query = query.copy(null); // !!!!!
        this.config = config;
        this.http = new Http(this.account.getProxy(), account.getCookieStore());
        this.base_url = this.account.base_url;
    }

    public AQuery getQuery() {
        return query; //we use this to see if ScrapeSession already defined in 'chm'.
    }

    public Map<PinterestObject, Board> getFound() {
        return found;
    }

    public void setFound(Map<PinterestObject, Board> found) {
        this.found = found;
    }

    public List<PinterestObject> getFoundAll() {
        return foundAll;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    //make this & class abstract
    protected abstract void run();

    @Override
    public ScrapeSession call() throws Exception {
        run();
        return this;
    }

    protected void The_Devil_Of_Infinity() {
        if (config != null && config.isInterrupt) //!!!!!!!!!! config can be null & boards disappear...
            this.EndReached = true; // should stop scraping when we click STOP
        
        if (getFound() == null || getFound().isEmpty()) {
            ++COUNT_SCRAPE_EMPTY;
        }
        if (COUNT_SCRAPE_EMPTY >= MAX_SCRAPE_EMPTY) {
            this.EndReached = true;
        }
    }

    protected void FirstRequestMode() {
        ///////////////////////////////////////////////////////////////        
        // There is a difference between scraping from loggedd-in and lurker mode.
        // So we are going to scrape and parse HTMLed JSON before using GetData JSON.
        if (FirstRequest) {
            FirstRequest = false;
        }
        // eg:
        /*
            let's scrape /username/followers/
            if we are not loggedin we will get results
            if we are loggedin we will not get any results, but parsing shouldn't throw Exception
                thus we are going to set FirstRequest = false, next time it'll try GetData URL.
        
         */
    }

    protected abstract String GetData();

    protected abstract void SetUrlAndRef();

    protected abstract void genObj(JsonObject m);

    protected void Parse(String key, String value) {
        Gson gson = new Gson();
        String json = response.getKey();

        JsonElement element = gson.fromJson(json, JsonElement.class);
        JsonObject jo = element.getAsJsonObject();
        Parse(jo, key, value);
    }

    private void Parse(JsonObject jo, String key, String value) {
        if (jo.isJsonObject()) {

            if (jo.has(key) && jo.get(key).isJsonPrimitive() && jo.get(key).getAsString().equalsIgnoreCase(value)) {
                try {
                    if (this instanceof ScrapeBoards_RepinnedOntoBoards && jo.has("pin") && jo.get("pin").isJsonObject() && jo.get("pin").getAsJsonObject().has("board") && jo.get("pin").getAsJsonObject().get("board").isJsonObject()) {
                        genObj(jo.get("pin").getAsJsonObject().get("board").getAsJsonObject());
                    } else if (this instanceof ScrapePinners_PinLikedBy && jo.has("user") && jo.get("user").isJsonObject()) {
                        genObj(jo.get("user").getAsJsonObject());
                    } else {
                        genObj(jo);
                    }
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                }
                return;
            }

            for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                JsonElement val = entry.getValue();
                if (val.isJsonArray()) {
                    for (JsonElement e : val.getAsJsonArray()) {
                        if (e.isJsonObject()) {
                            Parse(e.getAsJsonObject(), key, value);
                        }
                    }
                } else if (val.isJsonObject()) {
                    Parse(val.getAsJsonObject(), key, value);
                }
            }
        }
    }

    protected void FindSetBookmark() {
        String pat = "\"bookmarks\": ?\\[?\"(?=[^\\-])(.+?)\"\\]?";
        Pattern pattern = Pattern.compile(pat, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE); //
        Matcher matcher = pattern.matcher(response.getKey());
        Boolean hasMatch = false;
        while (matcher.find()) {
            setBookmark(matcher.group(1));
            if (this.bookmark != null && this.bookmark != "null") {
                FirstRequest = false;
                EndReached = false;
                hasMatch = true;
                return;
            }
        }
        if (response.getValue() != 500) {
            if (hasMatch == false) {
                EndReached = true;
            }
        }
    }

    protected void MakeRequest() throws Exception {
        response = http.Get(Url, getDefaultHeaders(Http.ACCEPT_JSON));
        //response = Http.unescape(response);
    }

    protected void MakeRequest(Map<String, String> headers) throws Exception {
        response = http.Get(Url, headers);
        //response = Http.unescape(response);
    }

    protected String setBoardId() {
        String pat = "\"board_id\":\"(\\d+)\"";
        Pattern pattern = Pattern.compile(pat, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response.getKey());
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    protected Map<String, String> getDefaultHeaders(String accept) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("Cache-Control", "no-cache");
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Pragma", "no-cache");
        if (accept == Http.ACCEPT_JSON) {
            headers.put("X-Requested-With", "XMLHttpRequest");
            headers.put("X-CSRFToken", account.getCsrf());
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        }
        headers.put("Referer", Referer);
        headers.put("Accept", accept);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    public static Long getTimeSinceEpoch() {
        return (new Date()).getTime();
    }

    public Boolean getEndReached() {
        return EndReached;
    }

    public void setEndReached(Boolean EndReached) {
        this.EndReached = EndReached;
    }

    public void setFirstRequest(Boolean FirstRequest) {
        this.FirstRequest = FirstRequest;
    }

    protected String truncString(String s) {
        if (s == null) {
            return null;
        }
        return s.substring(0, Math.min(s.length(), 255));
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += query.hashCode();
        hash += account.hashCode();
        hash += config.hashCode();
        return hash;
    }
    
    
}
