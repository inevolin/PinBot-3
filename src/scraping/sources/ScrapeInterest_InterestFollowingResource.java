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
import model.pinterestobjects.PinterestObject;
import model.configurations.AConfiguration;
import model.pinterestobjects.Interest;

public class ScrapeInterest_InterestFollowingResource extends ScrapeInterest {

    public ScrapeInterest_InterestFollowingResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        if (super.getQuery().getQuery().contains("/")) {
            String[] arr = super.getQuery().getQuery().split("/");
            String s = arr[1];
            super.getQuery().setQuery(s);
        }
    }

    @Override
    protected String GetData() {
        try {
            String str;

            if (FirstRequest) {
                str = "/resource/InterestFollowingResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/following/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"username\":\"" + super.getQuery().getQuery() + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App>UserProfilePage>UserProfileContent>FollowingSwitcher>Button(class_name=leftRounded+navScopeBtn+selected,+text=Topics,+rounded=false,+log_element_type=1227,+state_badgeValue=\"\",+state_accessibilityText=Topics,+is_selected=true)", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            } else {
                str = "/resource/InterestFollowingResource/get/"
                        + "?source_url="
                        + URLEncoder.encode("/" + super.getQuery().getQuery() + "/following/", StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"username\":\"" + super.getQuery().getQuery() + "\","
                                + "\"bookmarks\":[\"" + getBookmark() + "\"]},"
                                + "\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&module_path="
                        + URLEncoder.encode("App(module=[object+Object])", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            }

            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }

    @Override
    protected void SetUrlAndRef() {
        if (FirstRequest) {
            Url = base_url + "/" + super.getQuery().getQuery() + "/following/";
            Referer = base_url;
            try {
                MakeRequest();
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }
        Url = base_url + GetData();
        Referer = base_url + "/" + super.getQuery().getQuery() + "/following/";

    }

    @Override
    protected Interest createInterest() {
        return new Interest(PinterestObject.PinterestObjectResources.InterestFollowingResource, query);
    }
}
