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
import java.util.Date;
import common.KeyValuePair;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import static model.pinterestobjects.PinterestObject.PinterestObjectResources.IndividualPin;

public class RepinAlgo extends Algo {

    private RepinConfiguration repinConfig;

    public RepinAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        repinConfig = (RepinConfiguration) config;
        unBlackList();
    }

    private void unBlackList() {
        for (AQuery q : repinConfig.getQueries()) {
            Date now = new Date();
            //ONLY unBlacklist if enough time has passed since previous 'pin' timespan 
            if (q.getBlacklisted() != null && q.getBlacklisted()
                    && (now.getTime() >= q.getTimespan_blacklisted() + repinConfig.getBlacklisted_duration())) {
                q.setBlacklisted(Boolean.FALSE);
            }
        }
    }

    @Override
    public void run() {
        try {

            if ((repinConfig.getQueue() == null || repinConfig.getQueue().isEmpty()) && (repinConfig.getQueries() != null && repinConfig.getQueries().size() > 0)) {
                Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, null);
                if (scraped != null) {
                    processOne(scraped);
                }
            } else if (repinConfig.getQueue().size() > 0) {
                // permanent queue is first priority
                processOne(repinConfig.getQueue());
            } else if (repinConfig.getQueries() == null || repinConfig.getQueries().size() == 0) {
                //if we have no queries mapped and queue already finished; let's stop.
                super.config.CountTotal = super.config.CountCompleted;
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

    /*  Perform pin action and remove object from list OR set 'previouslyPinned' to current time if pin is permanentaly stored. */
    @Override
    protected void processOne(Map<PinterestObject, Board> lst) {
        processOne((Set) lst.keySet());
    }

    protected void processOne(Set<Pin> lst) {
        if (lst == null) {
            prematureFinish = true;
            return;
        } else if (lst.isEmpty()) {
            return;
        }

        Pin pin = (Pin) lst.toArray()[0];
        Board board = account.findBoard(pin.getDestinationBoardId());
        while (board == null) {
            // our board does not exist, most likely deleted
            addToDups(pin);
            lst.remove(pin);
            if (lst.isEmpty()) {
                return;
            }
            pin = (Pin) lst.toArray()[0];
            board = account.findBoard(pin.getDestinationBoardId());
        }

        Date now = new Date();
        if (pin.getPreviouslyPinned() == null) { //freshly scraped OR non-permanent pins.
            if (doRepin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
                addToDups(pin);
                process_IF_IndividualPin(pin);
                lst.remove(pin);
            }
        } else if (pin.getPreviouslyPinned().getTime() + pin.getTimeBeforeNextPin() < now.getTime()) { //permanentally stored pins
            //pin only if enough time has passed since previously pinned!
            if (doRepin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
                addToDups(pin);
                process_IF_IndividualPin(pin);
                pin.setPreviouslyPinned(now); //we must save the configuration at some point in time; or here? (todo)
            }
        }
    }

    private void process_IF_IndividualPin(Pin pin) {
        this.config.getQueries().forEach(q -> {
            if (q.getQuery().equalsIgnoreCase(pin.getMappedQuery().getQuery())
                    && q.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                q.setBlacklisted(true);
            }
        });
    }

    private boolean doRepin(Pin pin, Board board) {
        pin.setAttempts(pin.getAttempts() + 1);
        try {
            Data data = GetData(pin, board);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/RepinResource/create/", data.content, getRepinHeaders(data.referer));

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

    private Map<String, String> getRepinHeaders(String referer) {
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

    //IndividualPin nog toevoegen
    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Data data = new Data();
        Pin p = (Pin) obj;

        if (p.getMappedQuery() == null) {
            AQuery tmp = config.getQueries().get(0);
            p.setMappedQuery(tmp);
        }

        switch (p.getResource()) {
            case UserPinsResource:
                if (p.getMappedQuery().getQuery().contains("/")) {
                    p.setUsername(p.getMappedQuery().getQuery().split("/")[1]);
                }
                data.content = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/pins/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + board.getBoardId() + "\",\"description\":\"" + p.getDescription().replaceAll("\n", "\\\\n").replace("\"", "\\\"") + "\",\"link\":\"" + p.getSourceUrl() + "\",\"is_video\":false,\"pin_id\":\"" + p.getPinId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">Grid(resource=UserPinsResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=UserPinsResource(username=" + p.getUsername() + "))"
                                + ">Pin(resource=PinResource(id=" + p.getPinId() + "))"
                                + ">ShowModalButton(module=PinCreate)#Modal(module=PinCreate(resource=PinResource(id=" + p.getPinId() + ")))", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/" + p.getUsername() + "/pins/";
                break;

            case BoardFeedResource:
                if (p.getMappedQuery().getQuery().contains("/")) {
                    p.setUsername(p.getMappedQuery().getQuery().split("/")[1]);
                    p.setBoardName_EXT(p.getMappedQuery().getQuery().split("/")[2]);
                }
                data.content = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"pin_id\":\"" + p.getPinId() + "\",\"description\":\"" + p.getDescription().replaceAll("\n", "\\\\n").replace("\"", "\\\"") + "\",\"link\":" + (p.getSourceUrl() == null || "".equals(p.getSourceUrl()) ? "null" : "\"" + p.getSourceUrl() + "\"") + ",\"is_video\":false,\"board_id\":\"" + board.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App>ModalManager>Modal>PinCreate>BoardPicker>SelectList(view_type=pinCreate, selected_section_index=undefined, selected_item_index=undefined, highlight_matched_text=true, suppress_hover_events=undefined, scroll_selected_item_into_view=true, select_first_item_after_update=false, item_module=[object Object])", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/" + p.getUsername() + "/" + p.getBoardName_EXT() + "/";
                break;
            case SearchResource:
                data.content = "source_url="
                        + URLEncoder.encode("/search/pins/?q=" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + board.getBoardId() + "\",\"description\":\"" + p.getDescription().replaceAll("\n", "\\\\n").replace("\"", "\\\"") + "\",\"link\":\"" + p.getSourceUrl() + "\",\"is_video\":false,\"pin_id\":\"" + p.getPinId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>SearchPage(resource=BaseSearchResource(constraint_string=null, show_scope_selector=true, restrict=null, scope=pins, query=" + p.getMappedQuery().getQuery() + "))"
                                + ">SearchPageContent(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">Grid(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">GridItems(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=pins))"
                                + ">Pin(resource=PinResource(id=" + p.getPinId() + "))"
                                + ">ShowModalButton(module=PinCreate)#Modal(module=PinCreate(resource=PinResource(id=" + p.getPinId() + ")))", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/search/pins/?q=" + URLEncoder.encode(p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString());
                break;
            case IndividualPin:
                data.content = "source_url="
                        + URLEncoder.encode(p.getSourceUrl(), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"pin_id\":\"" + p.getPinId() + "\",\"description\":\"" + p.getDescription().replaceAll("\n", "\\\\n").replace("\"", "\\\"") + "\",\"link\":null,\"is_video\":false,\"board_id\":\"" + board.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App>ModalManager>Modal>PinCreate>BoardPicker>SelectList(view_type=pinCreate, selected_section_index=undefined, selected_item_index=undefined, highlight_matched_text=true, suppress_hover_events=undefined, scroll_selected_item_into_view=true, select_first_item_after_update=false, item_module=[object Object])", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/pin/" + p.getPinId() + "/";
                break;
        }

        return data;
    }

    private int countIndividualQueries() {
        int i = 0;
        for (AQuery q : config.getQueries()) {
            if (q.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                ++i;
            }
        }
        return i;
    }

    private int countIndividual_Blacklisted_Queries() {
        int i = 0;
        for (AQuery q : config.getQueries()) {
            if (q.getResource() == PinterestObject.PinterestObjectResources.IndividualPin
                    && q.getBlacklisted() != null
                    && q.getBlacklisted()) {
                ++i;
            }
        }
        return i;
    }

}
