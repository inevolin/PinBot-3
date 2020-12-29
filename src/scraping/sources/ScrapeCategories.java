/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Category;
import scraping.ScrapeSession;
import static scraping.ScrapeSession.getTimeSinceEpoch;

/**
 *
 * @author UGent
 */
public final class ScrapeCategories extends ScrapeSession {

    public ScrapeCategories(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
    }

    @Override
    protected void run() {
        pinbot3.PinBot3.myLogger.log(account, "Doing scrape for " + this.getClass().getSimpleName(), LEVEL.info);
        try {
            SetUrlAndRef();
            MakeRequest();
            Parse("type","category");
            FirstRequestMode();
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("key")) return;
        
        Category c = new Category(null, query);
        String name = (String) m.get("name").getAsString();
        String key = (String) m.get("key").getAsString();
        c.setName(name);
        c.setKey(key);

        //categories are not mapped to boards of course, null them.
        getFound().put(c, null); // adding <PinterestObject, Board>

    }

    @Override
    protected String GetData() {
        try {
            String str = "";
            str = "/resource/CategoriesResource/get/"
                    + "?source_url="
                    + URLEncoder.encode("/" + super.getQuery().getQuery() + "/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&_=" + getTimeSinceEpoch();

            str = MyUtils.jsonUpperCase(str);
            return str;

        } catch (UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    @Override
    protected void SetUrlAndRef() {
        Url = base_url + GetData();
        Referer = base_url + "/" + super.getQuery().getQuery() + "/";
    }
}
