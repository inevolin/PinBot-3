/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.Http;
import common.MyLogger.LEVEL;
import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.pinterestobjects.Pin;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import scraping.ParsePatterns;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.pinterestobjects.Board;
import scraping.QueueHelper;

public class Scrape_Tumblr extends ScrapeSession {

    public Scrape_Tumblr(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
    }

    @Override
    public void run() {
        pinbot3.PinBot3.myLogger.log(account, "Doing scrape for " + this.getClass().getSimpleName(), LEVEL.info);

        while (!EndReached && getFound().isEmpty()) {
            try {
                SetUrlAndRef();
                MakeRequest();
                if (response.getValue() != 200) {
                    EndReached = true;
                    return;
                }
                Parse(null, null);
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    private int num_posts_shown = 0;
    private int before = 0;

    @Override
    protected void SetUrlAndRef() {
        try {
            if (FirstRequest) {

                page = 1;
                Url = "https://www.tumblr.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString());
                Referer = "https://www.tumblr.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString());

            } else {

                Url = "https://www.tumblr.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString()) + "/post_page/" + page;
                Referer = "https://www.tumblr.com/search/" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString()) + "/post_page/" + (page - 1);
                num_posts_shown += 30;
                before += 31;

            }
            ++page;
        } catch (UnsupportedEncodingException ex) {
            
        }
    }

    @Override
    protected void MakeRequest() throws InterruptedException, Exception {

        if (FirstRequest) {
            MakeFirstRequest();
            FindSetBookmark();
        } else {
            MakeNextRequest();
        }

    }

    private void MakeFirstRequest() throws InterruptedException, Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Referer", Referer);
        headers.put("Accept", Http.ACCEPT_HTML);
        headers.put("User-Agent", Http.USER_AGENT);

        response = http.Get(Url, headers);
        response.setKey(Http.unescape(response.getKey()));
    }

    private void MakeNextRequest() throws InterruptedException, Exception {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-tumblr-form-key", bookmark);
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("Origin", "https://www.tumblr.com");
        headers.put("Referer", Referer);
        headers.put("Accept", Http.ACCEPT_HTML);
        headers.put("User-Agent", Http.USER_AGENT);

        response = http.Post(Url, GetData(), headers);
        response.setKey(Http.unescape(response.getKey()));

        if (response.getKey().contains("posts_html\":\"\"")) {// this indicates any empty result; end of results reached.
            EndReached = true;
            return;
        }
    }

    @Override
    protected String GetData() {
        String rs = "";
        try {
            rs += "q=" + URLEncoder.encode(query.getQuery(), StandardCharsets.UTF_8.toString());
            rs += "&sort=top&post_view=masonry&blogs_before=8&num_blogs_shown=8";
            rs += "&num_posts_shown=" + num_posts_shown + "&before=" + before;
            rs += "&blog_page=" + page + "&post_page=" + page + "&filter_nsfw=true";
            rs += "&filter_post_type=&next_ad_offset=0&ad_placement_id=0&more_posts=true";
        } catch (UnsupportedEncodingException ex) {
            
        }
        return rs;
    }

    @Override
    protected void FindSetBookmark() {

        Pattern pattern = Pattern.compile("<meta name=\"tumblr-form-key\" content=\"(.+?)\" id=\"tumblr_form_key\">", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(response.getKey());
        if (matcher.find()) {
            super.setBookmark(matcher.group(1));
            super.FirstRequest = false;
        } else {
            super.EndReached = true;
        }

    }

    @Override
    protected void Parse(String key, String value) {
        if (EndReached) {
            return;
        }
        for (String strPattern : ParsePatterns.Tumblr) {
            Pattern pattern = Pattern.compile(strPattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(response.getKey());
            while (matcher.find()) {
                //appVersion = matcher.group(1);
                String img = matcher.group("img").replace("\\", "");
                String desc = matcher.group("desc");
                //set source & desc url??
                Pin p = new Pin(PinterestObject.PinterestObjectResources.External, this.query);
                p.setHashUrl(img);
                p.setDescription(MyUtils.stripHtmlTags(desc).replace("\"", "\\\""));

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
