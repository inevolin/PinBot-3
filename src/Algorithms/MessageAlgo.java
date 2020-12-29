/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithms;

import Managers.DALManager;
import Managers.ScrapeManager;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import common.Data;
import common.Http;
import common.KeyValuePair;
import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.MessageConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author UGent
 */
public class MessageAlgo extends Algo {

    private MessageConfiguration messageConfig;
    private List<String> messages;

    public MessageAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        this.messageConfig = (MessageConfiguration) config;
        this.messages = messageConfig.getMessages();
    }

    private String getMessage() {
        Random r = new Random();
        return messages.get(r.nextInt(messages.size()));
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

        if (inspect(pinner)) {
            if ((doMessagePinner(pinner, board) || pinner.getAttempts() == MAX_ATTEMPTS)) {
                addToDups(pinner);
                lst.remove(pinner);

            }
        } else {
            addToDups(pinner);
            lst.remove(pinner);
        }
    }

    private boolean doMessagePinner(Pinner pinner, Board board) {
        pinner.setAttempts(pinner.getAttempts() + 1);
        //testThis
        if (pinbot3.PinBot3.dalMgr.containsObjectExternally(pinner, DALManager.TYPES.duplicates_message, account)) {
            return false;
        }
        /*if (account.getDuplicates_message().contains(pinner)) {
            return true; // prevent multi-messaging same pinner.
        }*/

        try {
            Data data = GetData(pinner, board);
            KeyValuePair<Integer> resp = http.Post(base_url + "/resource/ConversationsResource/create/", data.content, getMessageHeaders(data.referer));
            String convoId = getConversationId(resp.getKey());
            if (convoId == null) {
                return false;
            }

            data = GetData2(pinner, board, convoId);
            resp = http.Post(base_url + "/resource/ConversationMessagesResource/create/", data.content, getMessageHeaders(data.referer));

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

    private String getConversationId(String json) {

        String convId = null;

        Gson gson = new Gson();
        Map<String, LinkedTreeMap> m0 = new HashMap<>();
        m0 = (Map<String, LinkedTreeMap>) gson.fromJson(json, Map.class);
        LinkedTreeMap m1 = (LinkedTreeMap) m0.get("resource_response");
        m1 = (LinkedTreeMap) m1.get("data");
        //m1= (LinkedTreeMap) m1.get("last_message");
        convId = (String) m1.get("id");

        return convId;

    }

    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Pinner p = (Pinner) obj;

        String message = MyUtils.spintax(new Random(), getMessage()).replace("\"", "\\\"");

        Data data = new Data();
        data.content
                = "source_url="
                + URLEncoder.encode("/" + p.getUsername() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"user_ids\":[\"" + p.getPinnerId() + "\"],\"emails\":[]},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode(
                        "App>UserProfilePage>UserProfileHeader>DropdownButton>Dropdown>UserDropdown(user_id=" + p.getPinnerId() + ", user=[object Object], view_type=other, resource=UserConnectedResource(user_id=" + p.getPinnerId() + "))", StandardCharsets.UTF_8.toString());
        data.referer = base_url + "/";
        return data;
    }

    protected Data GetData2(PinterestObject obj, Board board, String convoId) throws UnsupportedEncodingException {
        Pinner p = (Pinner) obj;

        String message = MyUtils.spintax(new Random(), getMessage()).replace("\"", "\\\"");

        Data data = new Data();
        data.content
                = "source_url="
                + URLEncoder.encode("/" + p.getUsername() + "/", StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{\"conversation_id\":\"" + convoId + "\",\"text\":\"" + message + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode(
                        "App>ActiveConversations>Conversation>Dropdown>ConversationPopup>TextField", StandardCharsets.UTF_8.toString())
                + "(" + URLEncoder.encode("autofocus=true, autogrow=true, prevent_default_on_enter=true, stop_propagation_on_enter=true, maxheight=32, placeholder=Add a message", StandardCharsets.UTF_8.toString()) + ")";
        data.referer = base_url + "/";
        return data;
    }

    private Map<String, String> getMessageHeaders(String referer) {
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

    private boolean inspect(Pinner pinner) {
        if (messageConfig.getCriteria_Users() == null || !messageConfig.getCriteria_Users()) {
            return true;
        }

        String url = "/" + pinner.getUsername() + "/";
        String referer = pinner.getBaseUsername();
        int pincount = pinner.getPinsCount();
        int followerscount = pinner.getFollowersCount();

        if (!(pincount >= messageConfig.getCriteria_UserPinsMin() && pincount <= messageConfig.getCriteria_UserPinsMax())) {
            return false;
        }
        if (!(followerscount >= messageConfig.getCriteria_UserFollowersMin() && followerscount <= messageConfig.getCriteria_UserFollowersMax())) {
            return false;
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
                if (!(followingcount >= messageConfig.getCriteria_UserFollowingMin() && followingcount <= messageConfig.getCriteria_UserFollowingMax())) {
                    return false;
                }
            }

            pattern = Pattern.compile("name=\"pinterestapp:boards\" content=\"(\\d+)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            matcher = pattern.matcher(rs);
            if (matcher.find()) {
                int boardcount = Integer.parseInt(matcher.group(1));
                if (!(boardcount >= messageConfig.getCriteria_UserBoardsMin() && boardcount <= messageConfig.getCriteria_UserBoardsMax())) {
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
            if (scraped == null) {
                prematureFinish = true;
                finalize();
                return;
            }

            int before = scraped.size();
            processOne(scraped);
            int after = scraped.size();

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
