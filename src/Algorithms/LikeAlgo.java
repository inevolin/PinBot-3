/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import Managers.ScrapeManager;
import common.Data;
import common.Http;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import common.KeyValuePair;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.LikeConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;

public class LikeAlgo extends Algo {
    
    private LikeConfiguration likeConfig;
    
    public LikeAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        likeConfig = (LikeConfiguration) config;
    }
    
    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Pin p = (Pin) obj;
        
        Data data = new Data();
        switch (p.getResource()) {
            case UserPinsResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/pins/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"pin_id\":\"" + p.getPinId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">Grid(resource=UserPinsResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=UserPinsResource(username=" + p.getUsername() + "))"
                                + ">Pin(resource=PinResource(id=" + p.getPinId() + "))"
                                + ">PinLikeButton(liked=false, source_interest_id=null, has_icon=true, text=Like, class_name=likeSmall, pin_id=" + p.getPinId() + ", show_text=false, ga_category=like)", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/" + p.getUsername() + "/pins/";
                break;
            
            case BoardFeedResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"pin_id\":\"" + p.getPinId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>BoardPage(resource=BoardResource(username=" + p.getUsername() + ", slug=" + p.getBoardName_EXT() + "))"
                                + ">Grid(resource=BoardFeedResource(board_id=" + p.getBoardId_EXT() + ", board_url=/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/, board_layout=default, prepend=true, page_size=null, access=))"
                                + ">GridItems(resource=BoardFeedResource(board_id=" + p.getBoardId_EXT() + ", board_url=/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/, board_layout=default, prepend=true, page_size=null, access=))"
                                + ">Pin(resource=PinResource(id=" + p.getPinId() + "))"
                                + ">PinLikeButton(ga_category=like, class_name=likeSmall, liked=false, pin_id=" + p.getPinId() + ", has_icon=true, show_text=false, source_interest_id=undefined, text=Like)", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/";
                break;
            
            case SearchResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/search/pins/?q=" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"pin_id\":\"" + p.getPinId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>SearchPage(resource=BaseSearchResource(constraint_string=null, show_scope_selector=true, restrict=null, scope=pins, query=" + p.getMappedQuery().getQuery() + "))"
                                + ">SearchPageContent(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">Grid(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">GridItems(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">Pin(resource=PinResource(id=" + p.getPinId() + "))"
                                + ">PinLikeButton(liked=false, source_interest_id=null, has_icon=true, text=Like, class_name=likeSmall, pin_id=" + p.getPinId() + ", show_text=false, ga_category=like)", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/search/pins/?q=" + URLEncoder.encode(p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString());
                break;
        }
        return data;
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

            //if nothing has been pinned (all pins were already liked by 'me'); then keep scraping for new ones.
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
        
        while (pin.isLikedByMe() && lst.entrySet().size() > 0) {//while liked by me, keep searching.
            if (config.isInterrupt) {                
                return;
            }
            addToDups(pin);
            lst.remove(pin);
            if (lst.entrySet().isEmpty()) {
                return;
            } else {
                entry = (Map.Entry<PinterestObject, Board>) lst.entrySet().toArray()[0];
                pin = (Pin) entry.getKey();
            }
        }
        
        Board board = entry.getValue();
        if (pin.getPreviouslyPinned() == null) { //freshly scraped OR non-permanent pins.
            if (doLikePin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
                addToDups(pin);
                lst.remove(pin);
            }
        }
    }
    
    private Map<String, String> getLikeHeaders(String referer) {
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
    
    private boolean doLikePin(Pin pin, Board board) {
        pin.setAttempts(pin.getAttempts() + 1);
        try {
            Data data = GetData(pin, board);
            
            KeyValuePair<Integer> resp = http.Post(base_url + "/resource/PinLikeResource/create/", data.content, getLikeHeaders(data.referer));
            
            if (resp.getKey().contains("Parameter 'source_interest_id was not numeric (was )")) {
                config.status = AConfiguration.RunStatus.ERROR;
            }
            
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
}
