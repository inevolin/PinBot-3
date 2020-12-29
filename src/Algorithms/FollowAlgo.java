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
import common.KeyValuePair;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.FollowConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;

public class FollowAlgo extends Algo {

    private FollowConfiguration followConfig;

    public FollowAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        followConfig = (FollowConfiguration) config;
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
            case SearchResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/search/boards/?q=" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + p.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>SearchPage(resource=BaseSearchResource(constraint_string=null, show_scope_selector=true, restrict=null, scope=boards, query=" + p.getMappedQuery().getQuery() + "))"
                                + ">SearchPageContent(resource=SearchResource(layout=null, places=null, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=boards))"
                                + ">Grid(resource=SearchResource(layout=null, places=null, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=boards))"
                                + ">GridItems(resource=SearchResource(layout=null, places=null, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=boards))"
                                + ">Board(resource=BoardResource(board_id=" + p.getBoardId() + "))"
                                + ">BoardFollowButton(board_id=" + p.getBoardId() + ", followed=false, class_name=boardFollowUnfollowButton, follow_text=Unfollow, follow_ga_category=board_follow, disabled=false, color=null, text=Follow, log_element_type=37, follow_ga_category=board_follow, user_id=" + p.getUserId() + ", follow_text=Follow, follow_class=null, is_my_board=null)", StandardCharsets.UTF_8.toString());
                data.referer = super.base_url + "/search/boards/?q=" + URLEncoder.encode(p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString());
                break;

            case BoardFollowingResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/following/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + p.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">Grid(resource=BoardFollowingResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=BoardFollowingResource(username=" + p.getUsername() + "))"
                                + ">Board(resource=BoardResource(board_id=" + p.getBoardId() + "))"
                                + ">BoardFollowButton(followed=false, board_id=" + p.getBoardId() + ", class_name=boardFollowUnfollowButton, user_id=" + p.getUserId() + ", follow_class=default, log_element_type=37, text=Follow, color=default, disabled=false, follow_text=Follow, follow_text=Unfollow, is_my_board=undefined, follow_ga_category=board_follow, follow_ga_category=board_follow)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getUsername() + "/following/";
                break;

            case ProfileBoardsResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + p.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserBoards()>Grid(resource=ProfileBoardsResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=ProfileBoardsResource(username=" + p.getUsername() + "))"
                                + ">Board(resource=BoardResource(board_id=" + p.getBoardId() + "))"
                                + ">BoardFollowButton(board_id=" + p.getBoardId() + ", followed=false, class_name=boardFollowUnfollowButton, follow_text=Unfollow, follow_ga_category=board_follow, disabled=false, color=null, text=Follow, log_element_type=37, follow_ga_category=board_follow, user_id=" + p.getUserId() + ", follow_text=Follow, follow_class=null, is_my_board=null)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getUsername() + "/";
                break;

            case RepinFeedResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode(p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + p.getBoardId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserBoards()>Grid(resource=ProfileBoardsResource(username=" + p.getUsername() + "))"
                                + ">GridItems(resource=ProfileBoardsResource(username=" + p.getUsername() + "))"
                                + ">Board(resource=BoardResource(board_id=" + p.getBoardId() + "))"
                                + ">BoardFollowButton(board_id=" + p.getBoardId() + ", followed=false, class_name=boardFollowUnfollowButton, follow_text=Unfollow, follow_ga_category=board_follow, disabled=false, color=null, text=Follow, log_element_type=37, follow_ga_category=board_follow, user_id=" + p.getUserId() + ", follow_text=Follow, follow_class=null, is_my_board=null)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getUsername() + "/";
                break;

        }
        return data;
    }

    private Data getData_Pinner(Pinner p) throws UnsupportedEncodingException {
        Data data = new Data();
        switch (p.getResource()) {
            case SearchResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/search/people/?q=" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>SearchPage(resource=BaseSearchResource(constraint_string=null, show_scope_selector=true, restrict=null, scope=people, query=" + p.getMappedQuery().getQuery() + "))"
                                + ">SearchPageContent(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=people))"
                                + ">Grid(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=people))"
                                + ">GridItems(resource=SearchResource(layout=null, places=false, constraint_string=null, show_scope_selector=true, query=" + p.getMappedQuery().getQuery() + ", scope=people))"
                                + ">User(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserFollowButton(followed=false, class_name=gridItem, follow_text=Unfollow, follow_ga_category=user_follow, disabled=false, is_me=false, text=Follow, follow_class=default, log_element_type=62, follow_ga_category=user_follow, user_id=" + p.getPinnerId() + ", follow_text=Follow, color=default)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/search/people/?q=" + URLEncoder.encode(p.getMappedQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString());
                break;

            case UserFollowersResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/followers/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">Grid(resource=UserFollowersResource(username=" + p.getBaseUsername() + "))"
                                + ">GridItems(resource=UserFollowersResource(username=" + p.getBaseUsername() + "))"
                                + ">User(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserFollowButton(user_id=" + p.getPinnerId() + ", follow_class=default, followed=false, class_name=gridItem, log_element_type=62, text=Follow, color=default, disabled=false, follow_text=Follow, follow_text=Unfollow, is_me=false, follow_ga_category=user_follow, follow_ga_category=user_follow)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getBaseUsername() + "/followers/";
                break;

            case UserFollowingResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/following/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>UserProfilePage(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">UserProfileContent(resource=UserResource(username=" + p.getBaseUsername() + "))"
                                + ">Grid(resource=UserFollowingResource(username=" + p.getBaseUsername() + "))"
                                + ">GridItems(resource=UserFollowingResource(username=" + p.getBaseUsername() + "))"
                                + ">User(resource=UserResource(username=" + p.getUsername() + "))"
                                + ">UserFollowButton(user_id=" + p.getPinnerId() + ", follow_class=default, followed=false, class_name=gridItem, log_element_type=62, text=Follow, color=default, disabled=false, follow_text=Follow, follow_text=Unfollow, is_me=false, follow_ga_category=user_follow, follow_ga_category=user_follow)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/" + p.getBaseUsername() + "/following/";
                break;

            case BoardFollowersResource:
                data.content
                        = "source_url="
                        + URLEncoder.encode("/" + p.getBaseUsername() + "/" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+") + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App>ModalManager>Modal>BoardFollowers>PagedGrid>Grid>GridItems>User>UserFollowButton(user_id=" + p.getPinnerId() + ", follow_class=primary, followed=false, log_element_type=62, text=Follow, color=primary, disabled=false, follow_text=Follow, follow_text=Unfollow, is_me=false, follow_ga_category=user_follow, follow_ga_category=user_follow, state_disabled=true)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "" + "/" + p.getBaseUsername() + "/" + p.getMappedQuery().getQuery().replaceAll("\\s+", "+") + "/";
                break;

            case IndividualUser:
                data.content = "source_url="
                        + URLEncoder.encode("/" + p.getUsername() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"user_id\":\"" + p.getPinnerId() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App>UserProfilePage>UserProfileHeader>UserFollowButton(followed=false, is_me=false, text=Follow, memo=[object Object], disabled=false, suggested_users_menu=[object Object], follow_ga_category=user_follow, follow_text=Follow, follow_class=primary, user_id=" + p.getPinnerId() + ", follow_text=Unfollow, follow_ga_category=user_follow, color=primary, state_disabled=true)", StandardCharsets.UTF_8.toString());
                data.referer = base_url + "/";
                break;
        }
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

        while (lst.entrySet().size() > 0) {
            if (config.isInterrupt) {
                return;
            }
            if (entry.getKey() instanceof Board) {
                Board board = (Board) entry.getKey();
                if (board.isFollowedByMe() || !inspect(board)) {
                    lst.entrySet().remove(entry);
                    addToDups(board);
                } else if (doFollowBoard(board) || board.getAttempts() == MAX_ATTEMPTS) {
                    addToDups(board);
                    lst.remove(board);
                    return; //process ONE
                }
            } else {
                Pinner pinner = (Pinner) entry.getKey();
                if (pinner.isFollowedByMe() || !inspect(pinner)) {
                    lst.entrySet().remove(entry);
                    addToDups(pinner);
                } else if (doFollowPinner(pinner) || pinner.getAttempts() == MAX_ATTEMPTS) {
                    pinner.setTimeFollow((new Date()).getTime()); //record time of following
                    addToDups(pinner);
                    lst.remove(pinner);
                    return; //process ONE
                }
            }
            if (lst.entrySet().size() > 0) {
                entry = (Map.Entry<PinterestObject, Board>) lst.entrySet().toArray()[0];
            }
        }
    }

    private boolean doFollowBoard(Board board) {
        board.setAttempts(board.getAttempts() + 1);
        try {
            Data data = GetData(board, null);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/BoardFollowResource/create/", data.content, getFollowHeaders(data.referer));
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

    private boolean doFollowPinner(Pinner pinner) {
        pinner.setAttempts(pinner.getAttempts() + 1);
        try {
            Data data = GetData(pinner, null);

            KeyValuePair<Integer> resp = http.Post(
                    super.base_url + "/resource/UserFollowResource/create/", data.content, getFollowHeaders(data.referer));
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

    private Map<String, String> getFollowHeaders(String referer) {
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

    private boolean inspect(Pinner pinner) {
        //Check if the Pinner has been unfollowed     
        if (pinbot3.PinBot3.dalMgr.containsObjectExternally(pinner, DALManager.TYPES.duplicates_follow_pinners, account)) {
            return false;
        }
        /*for (PinterestObject p : account.getDuplicates_unfollow()) {
            if (p instanceof Pinner && ((Pinner) p).equals(pinner)) {
                return false;
            }
        }*/

        if (!followConfig.getCriteria_Users()) {
            return true;
        }

        String url = "/" + pinner.getUsername() + "/";
        int pincount = pinner.getPinsCount();
        int followerscount = pinner.getFollowersCount();

        if (!(pincount >= followConfig.getCriteria_UserPinsMin() && pincount <= followConfig.getCriteria_UserPinsMax())) {
            return false;
        }
        if (!(followerscount >= followConfig.getCriteria_UserFollowersMin() && followerscount <= followConfig.getCriteria_UserFollowersMax())) {
            return false;
        }

        try {
            if (!Http.validUrl(base_url + url)) {
                return false;
            }

            String rs = MakeRequest(base_url + url, base_url, Http.ACCEPT_HTML);
            if (rs == null) {
                return false;
            }

            Pattern pattern = Pattern.compile("name=\"pinterestapp:following\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int followingcount = Integer.parseInt(matcher.group(1));
                if (!(followingcount >= followConfig.getCriteria_UserFollowingMin() && followingcount <= followConfig.getCriteria_UserFollowingMax())) {
                    return false;
                }
            }

            pattern = Pattern.compile("name=\"pinterestapp:boards\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int boardcount = Integer.parseInt(matcher.group(1));
                if (!(boardcount >= followConfig.getCriteria_UserBoardsMin() && boardcount <= followConfig.getCriteria_UserBoardsMax())) {
                    return false;
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
        //Check if the Board has been unfollowed
        if (pinbot3.PinBot3.dalMgr.containsObjectExternally(board, DALManager.TYPES.duplicates_follow_boards, account)) {
            return false;
        }
        /*for (PinterestObject p : account.getDuplicates_unfollow()) {
            if (p instanceof Board && ((Board) p).equals(board)) {
                return false;
            }
        }*/

        if (!followConfig.getCriteria_Boards()) {
            return true;
        }

        String url = board.getUrlName();
        String referer = board.getUsername();
        int pincount = board.getPinsCount();

        if (!(pincount >= followConfig.getCriteria_BoardsPinsMin() && pincount <= followConfig.getCriteria_BoardsPinsMax())) {
            return false;
        }

        try {
            if (!Http.validUrl(base_url + url)) {
                return false;
            }

            String rs = MakeRequest(base_url + url, referer, Http.ACCEPT_HTML);
            if (rs == null)
                return false;
            
            Pattern pattern = Pattern.compile(", \"follower_count\":(\\d+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int followers = Integer.parseInt(matcher.group(1));
                if (!(followers >= followConfig.getCriteria_BoardsFollowersMin() && followers <= followConfig.getCriteria_BoardsFollowersMax())) {
                    return false;
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

    @Override
    public void run() {
        try {

            Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, null);
            scraped.keySet().removeIf(x -> x instanceof Pinner ? ((Pinner) x).isFollowedByMe() : ((Board) x).isFollowedByMe());
            while (scraped != null && scraped.size() == 0) {
                scraped = scrapeManager.Scrape(config, account, null);
                scraped.keySet().removeIf(x -> x instanceof Pinner ? ((Pinner) x).isFollowedByMe() : ((Board) x).isFollowedByMe());
            }
            if (scraped == null) {
                prematureFinish = true;
                finalize();
                return;
            }

            int before = scraped.size();
            processOne(scraped);
            int after = scraped.size();

            //if nothing has been unfollowed; then keep scraping for new ones.
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


/*

//testing
            int fez = 0;
            while (fez++ < 1000) {
                try {
                    while (scraped.entrySet().size() > 0) {
                        if (config.isInterrupt) {
                            return;
                        }
                        Map.Entry<PinterestObject, Board> entry = (Map.Entry<PinterestObject, Board>) scraped.entrySet().toArray()[0];
                        if (entry.getKey() instanceof Board) {
                            Board board = (Board) entry.getKey();
                            scraped.entrySet().remove(entry);
                            addToDups(board);

                        } else {
                            Pinner pinner = (Pinner) entry.getKey();
                            scraped.entrySet().remove(entry);
                            addToDups(pinner);
                        }
                    }
                } catch (Exception ex) {
                }
                if (config.isInterrupt) {
                    return;
                }
                System.err.println(this.account.getUsername());
                scraped = scrapeManager.Scrape(config, account, null);
            }
            //testing/

 */
