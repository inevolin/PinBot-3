/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.File;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;

/**
 *
 * @author UGent
 */
public class ImgDownloadTests {

    public ImgDownloadTests() {
    }

    @Test
    public void test1() throws Exception {
        Http http = new Http(null, new BasicCookieStore());
        File tmp = MyUtils.downloadTemporaryFile("https://41.media.tumblr.com/12e9183ae4c80c29a1155efbefea4f1d/tumblr_n7dmi4Pg9H1sshr4ho7_500.jpg", http);
        System.err.println(tmp.getAbsolutePath());

    }

}
