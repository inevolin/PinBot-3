/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import static Algorithms.Algo.MAX_ATTEMPTS;
import Managers.ScrapeManager;
import common.Data;
import common.Http;
import common.KeyValuePair;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;

public class InviteAlgo extends Algo {

    private InviteConfiguration inviteConfig;

    public InviteAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        inviteConfig = (InviteConfiguration) config;
        for (AQuery q : inviteConfig.getQueries()) {
            if (q.getQuery() == null) {
                q.setQuery("/" + (q.getBoardMapped()).getUrlName() + "/");
            }
        }
    }

    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Pinner p = (Pinner) obj;
        Data data = new Data();

        switch (p.getResource()) {
            case BoardFollowersResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + board.getUrlName() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + board.getBoardId() + "\",\"invited_user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode(
                                "App()>BoardPage(resource=BoardResource(username=" + p.getBaseUsername() + ", slug=" + board.getUrlName().split("/")[1] + "))"
                                + ">BoardHeader(resource=BoardResource(board_id=" + board.getBoardId() + "))"
                                + ">BoardInfoBar(resource=BoardResource(board_id=" + board.getBoardId() + "))"
                                + ">ShowModalButton(module=BoardCollaboratorInviter)"
                                + "#Modal(module=BoardCollaboratorInviter(resource=BoardResource(board_id=" + board.getBoardId() + ")))", StandardCharsets.UTF_8.toString()
                        );
                data.referer = base_url + "/" + board.getUrlName() + "/";
                break;
        }
        return data;
    }

    private boolean doInvitePinner(Pinner pinner, Board board) {
        pinner.setAttempts(pinner.getAttempts() + 1);
        try {
            Data data = GetData(pinner, board);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/BoardInviteResource/create/", data.content, getInviteHeaders(data.referer));
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

    private Map<String, String> getInviteHeaders(String referer) {
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
        Pinner pinner = (Pinner) entry.getKey();
        Board board = entry.getValue();

        if (doInvitePinner(pinner, board) || pinner.getAttempts() == MAX_ATTEMPTS) {
            addToDups(pinner);
            lst.remove(pinner);
        }

    }
}
