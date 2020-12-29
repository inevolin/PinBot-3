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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import model.configurations.AConfiguration;
import static scraping.ScrapeSession.getTimeSinceEpoch;

public class ScrapePinners_PinLikedBy extends ScrapePinners {

    String pinId;
    String aggregated_pin_data_id;

    public ScrapePinners_PinLikedBy(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
        pinId = (query.getQuery().split("/"))[2];
    }

    @Override
    protected void SetUrlAndRef() {
        if (FirstRequest) {
            Url = base_url + "/pin/" + pinId + "/activity/";
            Referer = base_url;
        } else {
            Url = base_url + GetData();
            Referer = base_url + super.getQuery().getQuery();
        }
    }

    @Override
    protected String GetData() {
        try {
            String str = "/resource/AggregatedActivityFeedResource/get/"
                    + "?source_url="
                    + URLEncoder.encode("/pin/" + pinId + "/activity/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"bookmarks\":[\"" + getBookmark() + "\"],\"aggregated_pin_data_id\":\"" + aggregated_pin_data_id + "\",\"page_size\":19},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&_=" + getTimeSinceEpoch();

            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }

    @Override
    protected void Parse(String key, String value) {
        super.Parse("type", "likepinactivity");
        set_aggregated_pin_data_id();
    }

    protected void set_aggregated_pin_data_id() {
        String pat = "\"aggregated_pin_data\": ?\\{\"id\": ?\"(\\d+?)\",";
        Pattern pattern = Pattern.compile(pat, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE); //
        Matcher matcher = pattern.matcher(response.getKey());
        while (matcher.find()) {
            aggregated_pin_data_id = matcher.group(1);
        }
    }

    @Override
    protected Pinner createPinner() {
        return new Pinner(PinterestObject.PinterestObjectResources.BoardFollowersResource, query);
    }
}
