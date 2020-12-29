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
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;

public class IndividualUser extends ScrapeSession {

    protected String username;

    public IndividualUser(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        username = query.getQuery().split("/")[1];
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
                Parse("username", username);
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
        Url = base_url + "/" + username + "/";
        Referer = base_url;
    }

    @Override
    protected void Parse(String key, String value) {
        super.Parse(key, value);
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("explicitly_followed_by_me")) return;
        
        String id = (String) m.get("id").getAsString();
        boolean followedByMe = (Boolean) m.get("explicitly_followed_by_me").getAsBoolean();

        Pinner p = new Pinner(PinterestObject.PinterestObjectResources.IndividualUser, query);
        p.setPinnerId((id));
        p.setBaseUsername(baseUsername);
        p.setUsername(username);
        p.setFollowedByMe(followedByMe);

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!
        EndReached = true;
        // disabling because of "OutOfMemoryError: Java Heap space"
        /*
        if (getFoundAll().contains(p)) {
            return;
        } else {
            getFoundAll().add(p);
        }
        */

        if (account.getDuplicate(config, p) != null) {
            return;
        }

        //users are not mapped to boards => null.
        getFound().put(p, null);
    }

}
