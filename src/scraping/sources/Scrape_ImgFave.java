/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.Http;
import common.MyLogger.LEVEL;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.ParsePatterns;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.pinterestobjects.Board;
import scraping.QueueHelper;

public class Scrape_ImgFave extends ScrapeSession {

    public Scrape_ImgFave(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
    }

    @Override
    protected void run() {
        pinbot3.PinBot3.myLogger.log(account, "Doing scrape for " + this.getClass().getSimpleName(), LEVEL.info);

        // DO NOT CLEAR here, only the AlgoWorker may remove "used objects", otherwise each action results in a full scraped page.
        //super.found.clear(); // clear previously scraped objects !!!
        while (!EndReached && getFound().isEmpty()) { //or until exception occurs OR until too many errors occur?
            try {
                SetUrlAndRef();
                MakeRequest();
                if (response.getValue() != 200) {
                    EndReached = true;
                    return;
                }
                Parse(null, null);
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    @Override
    protected void MakeRequest() throws InterruptedException, Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Referer", Referer);
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        response = http.Get(Url, headers);
        response.setKey(Http.unescape(response.getKey()));
        FirstRequest = false;
    }

    @Override
    protected String GetData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void SetUrlAndRef() {
        try {
            if (FirstRequest) {
                page = 1;
            }

            Url = "http://imgfave.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString());
            if (page > 1) {
                Url += "/page:" + (page);
            }

            Referer = "http://imgfave.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString());
            if (page > 1) {
                Referer += "/page:" + (page - 1);
            }

            ++page;
        } catch (UnsupportedEncodingException ex) {
            
        }
    }

    @Override
    protected void FindSetBookmark() {

        Pattern pattern = Pattern.compile("<a class=\"btn btn-custom\" href=\"/search/(.+?)/page:2\">Next Page", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response.getKey());
        if (matcher.find()) {
            super.FirstRequest = false;
        } else if (response.getKey().contains("No results found") || response.getKey().contains("</form>of 1</div><span class=\"next_Prev\">")) {
            super.EndReached = true;
        }
    }

    private String generateDescription(List<String> lines) {
        String res = "";
        Random r = new Random();
        int g = r.nextInt(3);
        if (g == 0) {
            res += "#";
            res += lines.get(r.nextInt(lines.size()));
        } else if (g == 1 && lines.size() > 1) {
            for (int i = 0; i < r.nextInt(lines.size() - 1) + 1; i++) {
                res += lines.get(r.nextInt(lines.size())) + " ";
            }
        } else {
            res += lines.get(r.nextInt(lines.size()));
        }

        if (res.equals("")) {
            res = query.getQuery();
        }
        return res.trim();
    }

    @Override
    protected void Parse(String key, String value) {
        if (EndReached) {
            return;
        }
        for (String strPattern : ParsePatterns.ImgFave) {
            Pattern pattern = Pattern.compile(strPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(response.getKey());
            while (!matcher.hitEnd() && matcher.find()) {
                String img = matcher.group("img");
                String title = matcher.group("title");

                List<String> tags = new ArrayList<>();
                tags.add(title);
                String tempTags = matcher.group("tags");
                if (tempTags != null) {
                    Pattern pat = Pattern.compile("\">([a-zA-Z0-9]+)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
                    Matcher m = pat.matcher(tempTags);

                    while (!m.hitEnd() && m.find()) {
                        tags.add(m.group(1));
                    }
                }

                Pin p = new Pin(PinterestObject.PinterestObjectResources.External, this.query);
                p.setHashUrl(img);
                p.setDescription(generateDescription(tags));

                COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!

                // disabling because of "OutOfMemoryError: Java Heap space"
                /*
                if (getFoundAll().contains(p)) {
                    continue; // semi-dupChecker for current session only.
                } else {
                    getFoundAll().add(p);
                }
                 */
                if (account.getDuplicate(config, p) != null) {
                    continue; //dupChecker
                }

                if (config instanceof PinConfiguration) {
                    String url = QueueHelper.getRandomSourceUrl((PinConfiguration) config);
                    if (url == null || url.isEmpty()) {
                        // nothing
                    } else {
                        p.setSourceUrl(url);
                    }
                    String descUrl = QueueHelper.getRandomDescUrl((PinConfiguration) config);
                    if (descUrl != null && !descUrl.isEmpty()) {
                        p.setDescription(p.getDescription() + " " + descUrl);
                    }
                }

                getFound().put(p, (Board) query.getBoardMapped()); // adding <PinterestObject, Board> to the list so it can be used by Algo worker.
            }
        }
    }

    @Deprecated
    @Override
    protected void genObj(JsonObject m) {
    }
}
