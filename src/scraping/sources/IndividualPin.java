/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import java.util.Date;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.QueueHelper;
import scraping.ScrapeSession;

public class IndividualPin extends ScrapeSession {

    public IndividualPin(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        if (super.getQuery().getQuery().contains("/")) {
            String s = (super.getQuery().getQuery().split("/"))[2];
            super.getQuery().setQuery(s);
        }
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
                Parse("id", super.getQuery().getQuery());
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }

    }

    @Override
    protected String GetData() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void SetUrlAndRef() {
        Url = base_url + "/pin/" + super.getQuery().getQuery() + "/";

        Referer = base_url;
    }

    @Override
    protected void Parse(String key, String value) {
        super.Parse(key, value);
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("images")) {
            return;
        }

        String pinId = m.get("id").getAsString();
        String image = (m.get("images").getAsJsonObject()).get("orig").getAsJsonObject().get("url").getAsString();
        Boolean likedByMe = (Boolean) m.get("liked_by_me").getAsBoolean();
        String desc = (String) m.get("description").getAsString();;
        String link = (String) m.get("link").getAsString();;

        Pin p = new Pin(PinterestObject.PinterestObjectResources.IndividualPin, this.query);
        p.setPinId((super.getQuery().getQuery()));
        p.setHashUrl(image);

        p.setDescription(desc);
        p.setLikedByMe(likedByMe);

        if (config instanceof RepinConfiguration) {
            String descUrl = QueueHelper.getRandomDescUrl((RepinConfiguration) config);
            if (descUrl != null && !descUrl.isEmpty()) {
                p.setDescription(desc + " " + descUrl);
            }
        }

        //testThis
        Pin found = (Pin) account.getDuplicate(config, p);
        if (found != null) {
            Date now = new Date();
            if (found.getPreviouslyPinned() == null) {
                found.setPreviouslyPinned(now);
            }
            p.setPreviouslyPinned(found.getPreviouslyPinned());
            p.setTimeBeforeNextPin(1000L * 60 * 60 * 24);
            query.setBlacklisted(true);
        }

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!

        getFound().put(p, (Board) query.getBoardMapped());
    }

}
