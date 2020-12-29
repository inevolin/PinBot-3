/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import model.Account;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.QueueHelper;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;
import model.pinterestobjects.Board;

public abstract class ScrapePins extends ScrapeSession {

    public ScrapePins(Account account, AQuery query, AConfiguration config) throws Exception {
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
                FindSetBookmark();
                Parse("type", "pin");
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }

    }

    protected abstract void additionalParseInfo(PinterestObject obj);

    protected void genObj(JsonObject m) {
        if (!m.has("images")) {
            return;
        }

        String pinId = (String) m.get("id").getAsString();
        String image = "";
        JsonObject imgs = m.get("images").getAsJsonObject();
        if (imgs.has("orig")) {
            image = imgs.get("orig").getAsJsonObject().get("url").getAsString();
        } else {
            image = imgs.entrySet().iterator().next().getValue().getAsJsonObject().get("url").getAsString();
        }

        Boolean likedByMe = m.has("liked_by_me") ? (Boolean) m.get("liked_by_me").getAsBoolean() : false;
        String desc = truncString((String) m.get("description").getAsString());
        String link = m.get("link").isJsonNull() ? "" : truncString((String) m.get("link").getAsString());

        Pin p = createPin();
        p.setPinId((pinId));
        p.setHashUrl(image);

        p.setDescription(desc);
        p.setLikedByMe(likedByMe);

        additionalParseInfo(p);

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!

        // disabling because of "OutOfMemoryError: Java Heap space"
        /*
        if (getFoundAll().contains(p)) {
            return; // semi-dupChecker for current session only.
        } else {
            getFoundAll().add(p);
        }
         */
        if (account.getDuplicate(config, p) != null) {
            return; //dupChecker
        }

        if (config instanceof PinConfiguration) {
            String url = QueueHelper.getRandomSourceUrl((PinConfiguration) config);
            if (url == null || url.isEmpty()) {
                p.setSourceUrl(link);
            } else {
                p.setSourceUrl(url);
            }
            String descUrl = QueueHelper.getRandomDescUrl((PinConfiguration) config);
            if (descUrl != null && !descUrl.isEmpty()) {
                p.setDescription(p.getDescription() + " " + descUrl);
            }
        } else if (config instanceof RepinConfiguration) {
            String descUrl = QueueHelper.getRandomDescUrl((RepinConfiguration) config);
            if (descUrl != null && !descUrl.isEmpty()) {
                p.setDescription(p.getDescription() + " " + descUrl);
            }
        }

        getFound().put(p, (Board) query.getBoardMapped()); // adding <PinterestObject, Board> to the list so it can be used by Algo worker.
    }

    protected Pin createPin() {
        return new Pin(PinterestObject.PinterestObjectResources.IndividualPin, this.query);
    }

}
