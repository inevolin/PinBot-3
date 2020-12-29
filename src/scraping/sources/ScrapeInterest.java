/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;
import model.pinterestobjects.Interest;

public abstract class ScrapeInterest extends ScrapeSession {

    public ScrapeInterest(Account account, AQuery query, AConfiguration config) throws Exception {
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
                Parse("type","interest");
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("url_name")) return;
        
        String url_name = (String) m.get("url_name").getAsString();
        Interest p = createInterest();
        p.setFollowedByMe(m.get("is_followed").getAsBoolean());
        p.setUrl_name(url_name);

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!
        
        // disabling because of "OutOfMemoryError: Java Heap space"
        /*
        if (getFoundAll().contains(p)) {
            return; // semi-dupChecker for current session only.
        } else {
            getFoundAll().add(p);
        }
        */

        
        //testThis
        if (account.getDuplicates_interest() != null && account.getDuplicates_interest().contains(p)) {
            return; //dupChecker
        }

        // pinners are  not mapped
        getFound().put(p, null); // adding <PinterestObject, Board> to the list so it can be used by Algo worker.
    }

    protected Interest createInterest() {
        return new Interest(PinterestObject.PinterestObjectResources.InterestFollowingResource, query);
    }
}
