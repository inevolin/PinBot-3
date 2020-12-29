/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import model.configurations.AConfiguration;

public class ScrapePinners_BoardFollowersResource extends ScrapePinners {

    String boardName;
    String boardId;

    public ScrapePinners_BoardFollowersResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        baseUsername = (query.getQuery().split("/"))[1];
        boardName = (query.getQuery().split("/"))[2];
    }

    @Override
    protected void SetUrlAndRef() {
        if (FirstRequest) {
            Url = base_url + "/" + baseUsername + "/" + boardName + "/";
            Referer = base_url;
            try {
                MakeRequest();
                boardId = setBoardId();
                if (boardId == null || boardId.isEmpty()) {
                    FirstRequest = true;
                    return;
                }
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }

        Url = base_url + GetData();
        Referer = base_url + "/" + super.getQuery().getQuery() + "/";

    }

    @Override
    protected String GetData() {
        try {
            String str;

            if (FirstRequest) {
                str = "/resource/BoardFollowersResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + baseUsername + "/" + boardName + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + boardId + "\",\"field_set_key\":\"grid_item\",\"page_size\":9},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App()>BoardPage(inviter_user_id=null, show_follow_memo=null, tab=pins, resource=BoardResource(username=" + baseUsername + ", slug=" + boardName + "))", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            } else {
                str = "/resource/BoardFollowersResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + baseUsername + "/" + boardName + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"board_id\":\"" + boardId + "\",\"field_set_key\":\"grid_item\",\"page_size\":9,\"bookmarks\":[\"" + getBookmark() + "\"]},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("Modal()>BoardFollowers(resource=BoardFollowersResource(board_id=" + boardId + ", page_size=9))>PagedGrid(resource=BoardFollowersResource(board_id=" + boardId + ", page_size=9))>Button(class_name=moreItems, log_element_type=179, text=Load more followers)", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            }

            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }

    @Override
    protected Pinner createPinner() {
        return new Pinner(PinterestObject.PinterestObjectResources.BoardFollowersResource, query);
    }
}
