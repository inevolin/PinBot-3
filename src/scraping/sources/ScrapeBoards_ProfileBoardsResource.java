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

public class ScrapeBoards_ProfileBoardsResource extends ScrapeBoards {

    public ScrapeBoards_ProfileBoardsResource(Account account, AQuery query, AConfiguration config) throws Exception {
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
                str = "/resource/BoardsResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + query.getQuery() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"filter\":\"public\",\"sort\":\"profile\",\"field_set_key\":\"profile_grid_item\",\"skip_board_create_rep\":true,"
                                + "\"username\":\"" + query.getQuery() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            } else {
                str = "/resource/BoardsResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + query.getQuery() + "/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"filter\":\"public\",\"sort\":\"profile\",\"field_set_key\":\"profile_grid_item\",\"skip_board_create_rep\":true,"
                                + "\"username\":\"" + query.getQuery() + "\"},"
                                + "\"bookmarks\":[\"" + bookmark + "\"]},"
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
