/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import Managers.DALManager.TYPES;
import Managers.ScrapeManager;
import common.Data;
import common.Http;
import common.KeyValuePair;
import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.CommentConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Comment;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;

public class CommentAlgo extends Algo {

    private final CommentConfiguration commentConfig;
    private final Set<Comment> mapping;

    public CommentAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        commentConfig = (CommentConfiguration) config;
        mapping = commentConfig.getMapping();
    }

    private Comment getComment(String queryTxt) {
        List<Comment> comments_specific_query = new ArrayList<>();
        for (Comment co : mapping) {
            if (queryTxt.equalsIgnoreCase(co.getSearchQuery())) {
                comments_specific_query.add(co);
            }
        }
        if (comments_specific_query.size() == 0) {
            return null; //missing mapping
        }
        Random r = new Random();
        return comments_specific_query.get(r.nextInt(comments_specific_query.size()));
    }

    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Pin p = (Pin) obj;
        Comment comment = getComment(p.getMappedQuery().getQuery());
        if (comment == null) {
            return null;
        }

        String sc = comment.getText();
        sc = MyUtils.spintax(new Random(), sc).replace("\"", "\\\"");;
        comment.setText(sc);

        Data data = new Data();

        long TICKS_AT_EPOCH = 621355968000000000L;
        long ticks = System.currentTimeMillis() * 10000 + TICKS_AT_EPOCH;

        data.content
                = "source_url="
                + URLEncoder.encode("/pin/" + p.getPinId() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"pin_id\":\""
                        + p.getPinId()
                        + "\",\"text\":\""
                        + comment.getText()
                        + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode(
                        "App>Closeup>CloseupContent>Pin>PinCommentsPage>PinDescriptionComment(image_src=https://s-media-cache-ak0.pinimg.com/avatars/" + ticks + ".jpg, username=" + account.getUsername() + ", full_name=" + account.getUsername() + ", content=null, show_comment_form=true, view_type=detailed, subtitle=That's you!, pin_id=" + p.getPinId() + ", is_description=false)", StandardCharsets.UTF_8.toString()
                );
        data.referer = super.base_url + "/pin/" + p.getPinId() + "/";

        return data;
    }

    @Override
    protected void processOne(Map<PinterestObject, Board> lst) {
        if (lst == null) {
            prematureFinish = true;
            return;
        } else if (lst.isEmpty()) {
            return;
        }

        Map.Entry<PinterestObject, Board> entry = (Map.Entry<PinterestObject, Board>) lst.entrySet().toArray()[0];
        Pin pin = (Pin) entry.getKey();
        Board board = entry.getValue();

        if (doCommentPin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
            addToDups(pin);
            lst.remove(pin);
        }
    }

    private boolean doCommentPin(Pin pin, Board board) {
        pin.setAttempts(pin.getAttempts() + 1);
        //testThis
        if (pinbot3.PinBot3.dalMgr.containsObjectExternally(pin, TYPES.duplicates_comment, account)) {
            //if (account.getDuplicates_comment().contains(pin)) {
            return true; // prevent multi-commenting same pin.
        }

        try {
            Data data = GetData(pin, board);
            if (data == null) {
                return true; //let it go directly into dups & skip
            }
            KeyValuePair<Integer> resp = http.Post(
                    super.base_url + "/resource/PinCommentResource/create/", data.content, getCommentHeaders(data.referer));

            return ProcessResponse(resp, this.account);

        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (UnknownHostException ex) {
            config.ErrorCount++;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return false;
    }

    private Map<String, String> getCommentHeaders(String referer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", account.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-Pinterest-AppState", "active");
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("Referer", referer);
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    @Override
    public void run() {
        try {

            Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, null);
            if (scraped == null) {
                prematureFinish = true;
                finalize();
                return;
            }

            int before = scraped.size();
            processOne(scraped);
            int after = scraped.size();

            //if nothing has been commented (all pins were already commented on by 'me'); then keep scraping for new ones.
            //in case EndReached (=prematureFinish) for all scrapes, then we should go to 'finalize()'.
            while (!prematureFinish && before == after && !config.status.equals(AConfiguration.RunStatus.ERROR)) {
                if (config.isInterrupt) {
                    return;
                }
                scraped = scrapeManager.Scrape(config, account, null);
                if (scraped == null) {
                    prematureFinish = true;
                    finalize();
                    return;
                }

                before = scraped.size();
                processOne(scraped);
                after = scraped.size();
            }
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (ExecutionException ex) {
            common.ExceptionHandler.reportException(ex);

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        finalize();
    }

}
