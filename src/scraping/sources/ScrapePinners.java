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
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;
import model.pinterestobjects.Board;

public abstract class ScrapePinners extends ScrapeSession {

    public ScrapePinners(Account account, AQuery query, AConfiguration config) throws Exception {
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
                Parse("type", "user");
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("username")) {
            return;
        }

        String id = (String) m.get("id").getAsString();
        String username = (String) m.get("username").getAsString();

        if (username.equalsIgnoreCase(this.account.getUsername())) {
            return;
        }

        Pinner p = createPinner();
        p.setPinnerId(id);
        p.setUsername(username);
        p.setBaseUsername(baseUsername);
        try {
            if (m.has("pin_count")) {
                p.setPinsCount(m.get("pin_count").getAsInt());
            }
            if (m.has("follower_count")) {
                p.setFollowersCount(m.get("follower_count").getAsInt());
            }
            if (m.has("explicitly_followed_by_me")) {
                p.setFollowedByMe((Boolean) m.get("explicitly_followed_by_me").getAsBoolean());
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }

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

        // pinners are  mapped to a board for InviteAlgo, otherwise null.
        getFound().put(p, (Board) query.getBoardMapped()); // adding <PinterestObject, Board> to the list so it can be used by Algo worker.
    }

    protected Pinner createPinner() {
        return new Pinner(PinterestObject.PinterestObjectResources.IndividualUser, query);
    }
}
