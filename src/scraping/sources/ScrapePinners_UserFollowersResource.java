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

public class ScrapePinners_UserFollowersResource extends ScrapePinners {

    public ScrapePinners_UserFollowersResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        if (super.getQuery().getQuery().contains("/")) {
            String[] arr = super.getQuery().getQuery().split("/");
            String s = arr[1];
            super.getQuery().setQuery(s);
        }

    }

    @Override
    protected void SetUrlAndRef() {
        if (FirstRequest) {
            Url = base_url + "/" + super.getQuery().getQuery() + "/followers/";
            Referer = base_url;
            try {
                MakeRequest();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }
        Url = base_url + GetData();
        Referer = base_url + "/" + super.getQuery().getQuery() + "/followers/";

    }

    @Override
    protected String GetData() {
        try {
            String str;

            if (FirstRequest) {
                str = "/resource/UserFollowersResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/followers/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"username\":\"" + super.getQuery().getQuery() + "\"},"
                                + "\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            } else {
                str = "/resource/UserFollowersResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/followers/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"username\":\"" + super.getQuery().getQuery() + "\","
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
    protected Pinner createPinner() {
        return new Pinner(PinterestObject.PinterestObjectResources.UserFollowersResource, query);
    }
}
