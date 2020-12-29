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
import model.configurations.AConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;

public class ScrapeBoards_UserResource extends ScrapeBoards {

    public ScrapeBoards_UserResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        if (super.getQuery().getQuery().contains("/")) {
            String s = (super.getQuery().getQuery().split("/"))[1];
            super.getQuery().setQuery(s);
        }
    }

    @Override
    protected void SetUrlAndRef() {

        if (FirstRequest) {
            Url = base_url + "/" + super.getQuery().getQuery() + "/";
            Referer = base_url;
            try {
                MakeRequest();
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
            String str = "";

            if (FirstRequest) {
                str = "/resource/UserResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"username\":\"" + super.getQuery().getQuery() + "\","
                                + "\"invite_code\":null},"
                                + "\"context\":{},"
                                + "\"module\":{\"name\":\"UserProfileContent\","
                                + "\"options\":{\"tab\":\"boards\"}},"
                                + "\"render_type\":1,"
                                + "\"error_strategy\":0}", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            } else {
                str = "/resource/UserResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"field_set_key\":\"grid_item\","
                                + "\"username\":\"" + super.getQuery().getQuery() + "\","
                                + "\"bookmarks\":[\"" + getBookmark() + "\"]},"
                                + "\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            }

            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }

    @Override
    protected Board createBoard() {
        return new Board(PinterestObject.PinterestObjectResources.ProfileBoardsResource, query);
    }
}
