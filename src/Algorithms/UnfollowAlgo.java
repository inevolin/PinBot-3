/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import Managers.DALManager;
import Managers.ScrapeManager;
import common.Data;
import common.Http;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import common.KeyValuePair;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.UnfollowConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import scraping.ScrapeSession;

public class UnfollowAlgo extends Algo {

    private UnfollowConfiguration unfollowConfig;

    public UnfollowAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        unfollowConfig = (UnfollowConfiguration) config;
    }

    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        if (obj instanceof Board) {
            return getData_Board((Board) obj);
        } else {
            return getData_Pinner((Pinner) obj);
        }
    }

    private Data getData_Board(Board p) throws UnsupportedEncodingException {
        Data data = new Data();
        switch (p.getResource()) {
            case BoardFollowingResource:
                data.content
                        = "source_url=/" + p.getUsername() + "/following/"
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + p.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">Grid(resource=BoardFollowingResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=BoardFollowingResource(username=" + p.getUsername() + "))"
                                + ">Board(resource=BoardResource(board_id=" + p.getBoardId() + "))"
                                + ">BoardFollowButton(followed=true, board_id=" + p.getBoardId() + ", class_name=boardFollowUnfollowButton, user_id=" + p.getUserId() + ", follow_class=default, log_element_type=37, text=Unfollow, color=dim, disabled=false, follow_text=Follow, unfollow_text=Unfollow, is_my_board=undefined, follow_ga_category=board_follow, unfollow_ga_category=board_unfollow)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getUsername() + "/following/";
                break;
        }
        return data;
    }

    private Data getData_Pinner(Pinner p) throws UnsupportedEncodingException {
        Data data = new Data();
        switch (p.getResource()) {
            case UserFollowingResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getBaseUsername() + "/following/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">Grid(resource=UserFollowingResource(username=" + p.getBaseUsername() + "))"
                                + ">GridItems(resource=UserFollowingResource(username=" + p.getBaseUsername() + "))"
                                + ">User(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserFollowButton(user_id=" + p.getPinnerId() + ", follow_class=default, followed=true, class_name=gridItem, log_element_type=62, text=Unfollow, color=dim, disabled=false, follow_text=Follow, unfollow_text=Unfollow, is_me=false, follow_ga_category=user_follow, unfollow_ga_category=user_unfollow)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getBaseUsername() + "/following/";
                break;
        }
        return data;
    }

    @Deprecated
    @Override
    protected void processOne(Map<PinterestObject, Board> lst) {
    }

    protected void processOneBoard(Set<PinterestObject> lst) {
        if (lst == null) {
            prematureFinish = true;
            return;
        } else if (lst.isEmpty()) {
            return;
        }

        PinterestObject entry = (PinterestObject) lst.toArray()[0];

        while (lst.size() > 0) {
            if (config.isInterrupt) {

                return;
            }
            Board board = (Board) entry;
            if (!inspect(board) || !board.isFollowedByMe()) {
                lst.remove(board);
                addToDups(board);
            } else if (doUnfollowBoard(board) || board.getAttempts() == MAX_ATTEMPTS) {
                addToDups(board);
                lst.remove(board);
                account.setMyFollowing(account.getMyFollowing() - 1);
                return; //process ONE
            }
            if (lst.size() > 0) {
                entry = (PinterestObject) lst.toArray()[0];
            }
        }

    }

    protected void processOnePinner(Set<PinterestObject> lst) {
        if (lst == null) {
            prematureFinish = true;
            return;
        } else if (lst.isEmpty()) {
            return;
        }

        Pinner entry = (Pinner) lst.toArray()[0];

        while (lst.size() > 0) {
            if (config.isInterrupt) {

                return;
            }
            Pinner pinner = (Pinner) entry;
            if (!inspect(pinner) || !pinner.isFollowedByMe()) {
                lst.remove(pinner);
                addToDups(pinner);
            } else if (doUnfollowPinner(pinner) || pinner.getAttempts() == MAX_ATTEMPTS) {
                addToDups(pinner);
                lst.remove(pinner);
                if (account.myFollowing() != null && account.myFollowing().contains(pinner)) {
                    account.myFollowing().remove(pinner); //remove from our "followings"-list
                    account.setMyFollowing(account.getMyFollowing() - 1); //decrement followings-int.
                }
                return; //process ONE
            }
            if (lst.size() > 0) {
                entry = (Pinner) lst.toArray()[0];
            }
        }
    }

    private boolean doUnfollowBoard(Board board) {
        board.setAttempts(board.getAttempts() + 1);
        try {
            Data data = GetData(board, null);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/BoardFollowResource/delete/", data.content, getUnfollowHeaders(data.referer));
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

    private boolean doUnfollowPinner(Pinner pinner) {
        pinner.setAttempts(pinner.getAttempts() + 1);
        try {
            Data data = GetData(pinner, null);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/UserFollowResource/delete/", data.content, getUnfollowHeaders(data.referer));
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

    private Map<String, String> getUnfollowHeaders(String referer) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", account.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        /*headers.put("X-Pinterest-AppState", "active");
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");*/
        headers.put("Referer", referer);
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private ScrapeSession ssUsers, ssBoards;

    @Override
    public void run() {
        try {

            Set<String> picks = new HashSet<>();
            if (unfollowConfig.getUnfollowUsers()) {
                picks.add("U");
            }
            if (unfollowConfig.getUnfollowBoards()) {
                picks.add("B");
            }

            Random r = new Random();
            int pickedIndex = r.nextInt(picks.size());
            String pickedString = (String) picks.toArray()[pickedIndex];

            if (pickedString.equalsIgnoreCase("U")) {
                if (unfollowConfig.getUnfollowNonFollowers()) {
                    runUserComplex();
                } else {
                    runUserRegular(account, config);
                }
            } else if (pickedString.equalsIgnoreCase("B")) {
                runBoard(account, config);
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

    void runUserRegular(Account account, AConfiguration config) throws Exception {
        Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, "U");
        if (config.isInterrupt) {
            return;
        }
        if (scraped == null) {
            prematureFinish = true;
            finalize();
            return;
        }

        int before = scraped.size();
        processOnePinner(scraped.keySet());
        int after = scraped.size();

        //if nothing has been unfollowed; then keep scraping for new ones.
        //in case EndReached (=prematureFinish) for all scrapes, then we should go to 'finalize()'.
        while (!prematureFinish && before == after && !config.status.equals(AConfiguration.RunStatus.ERROR)) {
            if (config.isInterrupt) {
                return;
            }
            scraped = scrapeManager.Scrape(config, account, "U");
            if (scraped == null) {
                prematureFinish = true;
                finalize();
                return;
            }

            before = scraped.size();
            processOneBoard(scraped.keySet());
            after = scraped.size();
        }
    }

    void runUserComplex() throws InterruptedException, ExecutionException {
        updateInterests();
        updateFollowing();
        updateFollowers();
        Set<Pinner> nonFollowers = retrieveNonFollowers();
        if (config.isInterrupt) {
            return;
        }
        Set<PinterestObject> list = (Set) nonFollowers;
        processOnePinner(list);
    }

    void runBoard(Account account, AConfiguration config) throws InterruptedException, ExecutionException {
        Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, "B");
        if (config.isInterrupt) {

            return;
        }
        if (scraped == null) {
            prematureFinish = true;
            finalize();
            return;
        }

        int before = scraped.size();
        processOneBoard(scraped.keySet());
        int after = scraped.size();

        //if nothing has been unfollowed; then keep scraping for new ones.
        //in case EndReached (=prematureFinish) for all scrapes, then we should go to 'finalize()'.
        while (!prematureFinish && before == after && !config.status.equals(AConfiguration.RunStatus.ERROR)) {
            if (config.isInterrupt) {

                return;
            }
            scraped = scrapeManager.Scrape(config, account, "B");
            if (scraped == null) {
                prematureFinish = true;
                finalize();
                return;
            }

            before = scraped.size();
            processOneBoard(scraped.keySet());
            after = scraped.size();
        }
    }

    private Boolean almostAllScraped(int x, int y) {
        //x : list.size()
        //y : scraped int
        if (x == 0 && y > 0) {
            return false;
        } else if (x == 0 && y == 0) {
            return false;
        } else if (x > y) {
            return true;
        }
        double d = (double) ((double) (y - x) / (double) y);
        return d <= 0.01; //stop scraping if true
        //return x >= y - 1;
    }

    private void updateInterests() {
        if (config.isInterrupt) {
            return;
        }
        try {
            if (account.getMyInterests() == 0) {
                Set<PinterestObject> scraped = scrapeManager.ScrapeUserFollowingInterestsAccount(config, account);
                if (scraped == null) {
                    return;
                }
                account.setMyInterests(scraped.size());
                scraped.clear();
            }
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    private void updateFollowing() {
        if (config.isInterrupt) {
            return;
        }
        try {
            if (almostAllScraped(account.myFollowing().size(), account.getMyFollowing() - account.getMyInterests())) {
                return;
            }
            Set<PinterestObject> scraped = scrapeManager.ScrapeUserFollowingAccount(config, account);
            if (scraped == null) {
                return;
            }
            for (PinterestObject p : scraped) {
                Pinner pp = (Pinner) p;
                pinbot3.PinBot3.dalMgr.appendExternalObject(pp, DALManager.TYPES.following, account);
                account.myFollowing().add(pp);
                //account.getFollowing().add(pp);
            }
            scraped.clear();
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    private void updateFollowers() throws InterruptedException, ExecutionException {
        if (config.isInterrupt) {
            return;
        }
        try {

            if (almostAllScraped(account.myFollowers().size(), account.getMyFollowers())) {
                return;
            }
            Set<PinterestObject> scraped = scrapeManager.ScrapeUserFollowersAccount(config, account);
            if (scraped == null) {
                return;
            }
            for (PinterestObject p : scraped) {
                Pinner pp = (Pinner) p;
                pinbot3.PinBot3.dalMgr.appendExternalObject(pp, DALManager.TYPES.followers, account);
                account.myFollowers().add(pp);
                //account.getFollowers().add(pp);

            }
            scraped.clear();
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    private Set<Pinner> retrieveNonFollowers() {
        if (config.isInterrupt) {
            return null;
        }
        Set<Pinner> nonFollowers = new HashSet<>();
        Set<Pinner> following = account.myFollowing();
        Set<Pinner> followers = account.myFollowers();
        for (Iterator<Pinner> it = following.iterator(); it.hasNext();) {
            Pinner p = it.next();
            if (!followers.contains(p)) {
                nonFollowers.add(p);
            }
        }
        return nonFollowers;
    }

    private boolean inspect(Pinner pinner) {
        //Check if enough time has passed between follow and unfollow
        //testThis
        if (unfollowConfig.getUnfollowOnlyRecordedFollowings()) {
            if (pinbot3.PinBot3.dalMgr.containsObjectExternally(pinner, DALManager.TYPES.duplicates_unfollow_pinners, account)) {
                return false;
            }
            /*for (PinterestObject p : account.getDuplicates_follow()) {
                if (p instanceof Pinner && ((Pinner) p).equals(pinner)
                        && (new Date()).getTime() - ((Pinner) p).getTimeFollow() <= unfollowConfig.getTimeBetweenFollowAndUnfollow()) {
                    return false;
                }
            }*/
        }

        if (!unfollowConfig.getCriteria_Users()) {
            return true;
        }

        String url = "/" + pinner.getUsername() + "/";
        String referer = pinner.getBaseUsername();
        int pincount = pinner.getPinsCount();
        int followerscount = pinner.getFollowersCount();

        if (!(pincount >= unfollowConfig.getCriteria_UserPinsMin() && pincount <= unfollowConfig.getCriteria_UserPinsMax())) {
            return true;
        }
        if (!(followerscount >= unfollowConfig.getCriteria_UserFollowersMin() && followerscount <= unfollowConfig.getCriteria_UserFollowersMax())) {
            return true;
        }

        try {
            if (!Http.validUrl(base_url + url)) {
                return false;
            }

            String rs = MakeRequest(base_url + url, referer, Http.ACCEPT_HTML);
            if (rs == null) {
                return false;
            }

            Pattern pattern = Pattern.compile("name=\"pinterestapp:following\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int followingcount = Integer.parseInt(matcher.group(1));
                if (!(followingcount >= unfollowConfig.getCriteria_UserFollowingMin() && followingcount <= unfollowConfig.getCriteria_UserFollowingMax())) {
                    return true;
                }
            }

            pattern = Pattern.compile("name=\"pinterestapp:boards\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int boardcount = Integer.parseInt(matcher.group(1));
                if (!(boardcount >= unfollowConfig.getCriteria_UserBoardsMin() && boardcount <= unfollowConfig.getCriteria_UserBoardsMax())) {
                    return true;
                }
            }
            return true;
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
            return false;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            return false;
        }
    }

    private boolean inspect(Board board) {
        if (!unfollowConfig.getCriteria_Boards()) {
            return true;
        }

        String url = "/" + board.getUrlName() + "/";
        String referer = board.getUsername();
        int pincount = board.getPinsCount();

        if (!(pincount >= unfollowConfig.getCriteria_BoardsPinsMin() && pincount <= unfollowConfig.getCriteria_BoardsPinsMax())) {
            return true;
        }

        try {
            if (!Http.validUrl(base_url + url)) {
                return false;
            }

            String rs = MakeRequest(base_url + url, referer, Http.ACCEPT_HTML);
            if (rs == null) {
                return false;
            }

            Pattern pattern = Pattern.compile("name=\"pinterestapp:followers\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int followers = Integer.parseInt(matcher.group(1));
                if (!(followers >= unfollowConfig.getCriteria_BoardsFollowersMin() && followers <= unfollowConfig.getCriteria_BoardsFollowersMax())) {
                    return true;
                }
            }
            return true;
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
            return false;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            return false;
        }
    }

}
