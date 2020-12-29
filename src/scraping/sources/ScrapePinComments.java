/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import com.google.gson.JsonObject;
import common.MyLogger.LEVEL;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.Account;
import model.pinterestobjects.Comment;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;
import model.configurations.AConfiguration;

public class ScrapePinComments extends ScrapeSession {

    private Pin pin;

    public ScrapePinComments(Account account, Pin pin, AConfiguration config) throws Exception {
        super(account, null, config);
        this.pin = pin;
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
                Parse("type", "comment");
                FirstRequestMode();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
            The_Devil_Of_Infinity();
        }
    }

    @Override
    protected String GetData() {
        return "";
    }

    @Override
    protected void SetUrlAndRef() {
        try {
            Url = base_url
                    + "/resource/PinCommentListResource/get/?source_url="
                    + URLEncoder.encode("/pin/" + pin.getPinId() + "/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"field_set_key\":\"pin_detailed\",\"pin_id\":\"" + pin.getPinId() + "\",\"page_size\":250,\"bookmarks\":[null]},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&module_path="
                    + URLEncoder.encode("App>Closeup>CloseupContent>Pin>PinCommentsPage(count=6, view_type=detailed, bookmark=null, pin_id=" + pin.getPinId() + ", max_num_to_show=3, show_actions=true, image_size=medium, in_gator=true, state_hideShowMoreLink=false, state_hideComments=false, resource=PinCommentListResource(pin_id=" + pin.getPinId() + ", page_size=3))", StandardCharsets.UTF_8.toString())
                    + "&_=" + getTimeSinceEpoch();;

            Referer = base_url + "/pin/" + pin.getPinId() + "/";
        } catch (UnsupportedEncodingException ex) {
            
        }
    }

    @Override
    protected void genObj(JsonObject m) {
        if (!m.has("text")) {
            return;
        }

        String id = (String) m.get("id").getAsString();
        String text = (String) m.get("text").getAsString();
        String username = m.get("commenter").getAsJsonObject().get("username").getAsString();

        Comment c = new Comment(PinterestObject.PinterestObjectResources.PinCommentListResource, query);
        c.setCommentId((id));
        c.setText(text);
        c.setUsername(username);

        COUNT_SCRAPE_EMPTY = 0; // !!!!! WE HAVE FOUND SOMETHING !!!!!

        // disabling because of "OutOfMemoryError: Java Heap space"
        /*
        if (getFoundAll().contains(c)) {
            return; // semi-dupChecker for current session only.
        } else {
            getFoundAll().add(c);
        }
         */
        if (account.getDuplicate(config, c) != null) {
            return; //dupChecker
        }

        //comments are not mapped to a board; null it.
        getFound().put(c, null);
    }

}
