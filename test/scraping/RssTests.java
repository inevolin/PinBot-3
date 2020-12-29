/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import rss.Feed;
import rss.FeedMessage;
import rss.RSSFeedParser;

/**
 *
 * @author UGent
 */
public class RssTests {

    public RssTests() {
    }

    @Test
    public void t1() throws IllegalArgumentException, IOException {
        RSSFeedParser parser = new RSSFeedParser("http://www.fashiontv.com/rss/gallery/fashion-week-galleries");
        Feed feed = parser.readFeed();
        System.out.println(feed);
        for (FeedMessage message : feed.getMessages()) {
            //images parsed from description text
            List<String> ls = getImagesOfFromHtmlString(message.getDescription());
            for (String img : ls) {
                System.err.println(img);
            }

        }
    }

    public ArrayList<String> getImagesOfFromHtmlString(String str) {
        ArrayList<String> arr_images = new ArrayList<>();
        Pattern pattern = Pattern.compile("(https?://\\s*\\S+\\.(?:jpg|JPG|jpeg|JPEG|png|PNG|gif|GIF|bmp|BMP))");
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            arr_images.add(m.group());
        }
        return arr_images;
    }
}
