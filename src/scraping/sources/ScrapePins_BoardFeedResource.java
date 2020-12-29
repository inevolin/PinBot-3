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
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import model.configurations.AConfiguration;

public class ScrapePins_BoardFeedResource extends ScrapePins {

    protected String boardName;
    protected String boardId;

    public ScrapePins_BoardFeedResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        baseUsername = query.getQuery().split("/")[1];
        boardName = query.getQuery().split("/")[2];
    }

    @Override
    protected String GetData() {
        try {
            String str = "/resource/BoardFeedResource/get/?source_url="
                    + URLEncoder.encode("/" + baseUsername + "/" + boardName + "/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"board_id\":\"" + boardId + "\",\"board_url\":\"/" + baseUsername + "/" + boardName + "/\",\"board_layout\":\"default\",\"prepend\":true,\"page_size\":null,\"access\":[],\"bookmarks\":[\"" + getBookmark() + "\"]},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&module_path="
                    + URLEncoder.encode(
                            "UserProfilePage(resource=UserResource(username=" + baseUsername + "))"
                            + ">UserProfileContent(resource=UserResource(username=" + baseUsername + "))"
                            + ">UserBoards()>Grid(resource=ProfileBoardsResource(username=" + baseUsername + "))"
                            + ">GridItems(resource=ProfileBoardsResource(username=" + baseUsername + "))"
                            + ">Board(show_board_context=false, view_type=boardCoverImage, component_type=1, show_user_icon=false, resource=BoardResource(board_id=" + boardId + "))", StandardCharsets.UTF_8.toString())
                    + "&_=" + getTimeSinceEpoch();

            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }

    @Override
    protected void SetUrlAndRef() {
        Referer = base_url + "/" + baseUsername + "/boards/";
        if (FirstRequest) {
            Url = base_url + "/" + baseUsername + "/" + boardName + "/";
            Referer = base_url;
        } else {
            if (boardId == null || boardId.isEmpty()) {
                boardId = setBoardId();
            }
            if (boardId == null || boardId.isEmpty()) {
                FirstRequest = true;
                return;
            }
            Url = base_url + GetData();
        }
    }

    @Override
    protected Pin createPin() {
        return new Pin(PinterestObject.PinterestObjectResources.BoardFeedResource, this.query);
    }

    @Override
    protected void additionalParseInfo(PinterestObject obj) {
        ((Pin) obj).setBoardId_EXT(boardId);
        ((Pin) obj).setBoardName_EXT(boardName);
    }

}
