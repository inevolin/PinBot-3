/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;

/**
 *
 * @author UGent
 */
public abstract class ScrapeBoards extends ScrapeSession {

    public ScrapeBoards(Account account, AQuery query, AConfiguration config) throws Exception {
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
                Parse("type","board");
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("name")) return;
        
        String boardId = (String) m.get("id").getAsString();
        String boardName = (String) m.get("name").getAsString();
        Board b = createBoard();
        b.setBoardId(boardId);
        b.setName(boardName);

        try {
            String userId = (String) m.get("owner").getAsJsonObject().get("id").getAsString();
            String username = (String) m.get("owner").getAsJsonObject().get("username").getAsString();
            String url = (String) m.get("url").getAsString();
            Integer pin_count = m.get("pin_count").getAsInt();
            boolean followed_by_me = (Boolean) m.get("followed_by_me").getAsBoolean();

            b.setUrlName(url);
            b.setFollowedByMe(followed_by_me);
            b.setUserId(userId);
            b.setUsername(username);
            b.setPinsCount(pin_count);
        } catch (NullPointerException ex) {
            common.ExceptionHandler.reportException(ex);
            //Exception thrown for scraping "own" boards; they only have 3 key/values.
        }

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!
        
        // disabling because of "OutOfMemoryError: Java Heap space"
        /*
            if (getFoundAll().contains(b)) {
                return; // semi-dupChecker for current session only.
            } else {
                getFoundAll().add(b);
            }
        */

        if (account.getDuplicate(config, b) != null) {
            return; //dupChecker
        }

        //boards are not mapped to boards of course, null them.
        getFound().put(b, null); // adding <PinterestObject, Board> to the list so it can be used by Algo worker.
    }

    protected Board createBoard() {
        return new Board(PinterestObject.PinterestObjectResources.BoardFeedResource, query);
    }

}
