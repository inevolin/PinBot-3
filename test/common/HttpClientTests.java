/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Proxy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author healzer
 */
public class HttpClientTests {

    public HttpClientTests() {
    }

    @Test
    public void t1_cookies_same() throws IOException {

        //SIMULATION of same user account activity (series of GETs)
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\UGent\\.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "tester");
        HttpHost proxy = new HttpHost("localhost", 8888);//debugging

        // https://hc.apache.org/httpcomponents-client-ga/examples.html
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom().setProxy(proxy).setDefaultCookieStore(cookieStore).build();
        HttpGet get = new HttpGet("https://www.pinterest.com");
        String A = null, B = null;
        try {
            CloseableHttpResponse re = client.execute(get);
            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine());
            //System.out.println(EntityUtils.toString(re.getEntity()));
            System.err.println(cookieStore.getCookies().get(1).getValue());
            A = cookieStore.getCookies().get(1).getValue();
        } catch (IOException e) {
            System.out.println(e);
        }

        get = new HttpGet("https://www.pinterest.com");
        try {
            CloseableHttpResponse re = client.execute(get);
            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine());
            //System.out.println(EntityUtils.toString(re.getEntity()));
            System.err.println(cookieStore.getCookies().get(1).getValue());
            B = cookieStore.getCookies().get(1).getValue();
        } catch (IOException e) {
            System.out.println(e);
        }

        assertTrue(A != null && B != null & A == B);

    }

    @Test
    public void t1_cookies_different() throws IOException {

        //SIMULATION of Different accounts activity (parallel of GETs)
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\UGent\\.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "tester");
        HttpHost proxy = new HttpHost("localhost", 8888);//debugging

        // https://hc.apache.org/httpcomponents-client-ga/examples.html
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(15000)
                .setConnectTimeout(15000)
                .setConnectionRequestTimeout(15000)
                .build();

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom().setProxy(proxy).setDefaultCookieStore(cookieStore).setDefaultRequestConfig(defaultRequestConfig).build();
        HttpGet get = new HttpGet("https://www.pinterest.com");
        String A = null, B = null;
        try {
            CloseableHttpResponse re = client.execute(get);
            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine());
            //System.out.println(EntityUtils.toString(re.getEntity()));
            System.err.println(cookieStore.getCookies().get(1).getValue());
            A = cookieStore.getCookies().get(1).getValue();
        } catch (IOException e) {
            System.out.println(e);
        }

        //crucial part //
        cookieStore = new BasicCookieStore(); //new cookie container
        client = HttpClients.custom().setProxy(proxy).setDefaultCookieStore(cookieStore).build(); //new client to use cookie container
        //crucial part //

        get = new HttpGet("https://www.pinterest.com");
        try {
            CloseableHttpResponse re = client.execute(get);
            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine());
            //System.out.println(EntityUtils.toString(re.getEntity()));
            System.err.println(cookieStore.getCookies().get(1).getValue());
            B = cookieStore.getCookies().get(1).getValue();
        } catch (IOException e) {
            System.out.println(e);
        }

        assertTrue(A != null && B != null & A != B);

    }

    @Test
    public void moved302() throws Exception {
        String url302 = "https://healzer.com/pinbot/";

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).disableRedirectHandling().build();
        HttpGet get = new HttpGet(url302);

        String rs = "";
        try {
            CloseableHttpResponse re = client.execute(get);

            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine());
            if (re.getStatusLine().getStatusCode() == 302 || re.getStatusLine().getStatusCode() == 301) {
                rs = re.getFirstHeader("Location").getValue();
                System.out.println("Location: " + rs);
            } else {
                System.out.println(EntityUtils.toString(re.getEntity()));
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        if (rs != null && rs.length() > 0 && rs.charAt(rs.length() - 1) == '/') {
            rs = rs.substring(0, rs.length() - 1);
        }
        assertEquals("https://pinbot3.com", rs);
    }

    @Test
    public void testPost() throws Exception {

        //SIMULATION of Different accounts activity (parallel of GETs)
        System.setProperty("javax.net.ssl.trustStore", "C:\\Users\\UGent\\.keystore");
        System.setProperty("javax.net.ssl.trustStorePassword", "tester");
        HttpHost proxy = new HttpHost("localhost", 8888);//debugging

        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(15000)
                .setConnectTimeout(15000)
                .setConnectionRequestTimeout(15000)
                .build();

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom().setProxy(proxy).setDefaultCookieStore(cookieStore).setDefaultRequestConfig(defaultRequestConfig).build();

        //////////////////////////////////////
        //////////////////////////////////////
        HttpGet get = new HttpGet("https://www.pinterest.com/login/");
        get.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        get.addHeader("Accept-Language", "en-US,en;q=0.8");
        get.addHeader("Accept", Http.ACCEPT_ANY);

        CloseableHttpResponse re = client.execute(get);
        String rs = EntityUtils.toString(re.getEntity());

        String appVersion = "";
        Pattern pattern = Pattern.compile("\"app_version\":\"(.+?)\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(rs);
        while (matcher.find()) {
            appVersion = matcher.group(1);
        }

        Cookie c = cookieStore.getCookies().stream().filter(o -> o.getName().equalsIgnoreCase("csrftoken")).findAny().get();
        String csrf = c.getValue();
        //////////////////////////////////////
        //////////////////////////////////////

        HttpPost post = new HttpPost("https://www.pinterest.com/resource/UserSessionResource/create/");
        post.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        post.addHeader("Accept-Language", "en-US,en;q=0.8");
        post.addHeader("X-NEW-APP", "1");
        post.addHeader("X-APP-VERSION", appVersion);
        post.addHeader("Cache-Control", "no-cache");
        post.addHeader("Pragma", "no-cache");
        post.addHeader("X-Requested-With", "XMLHttpRequest");
        post.addHeader("X-CSRFToken", csrf);
        post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        post.addHeader("Referer", "https://www.pinterest.com/login/");
        post.addHeader("Accept", Http.ACCEPT_JSON);

        String Email = "test@gmail.com", Password = ("password").replace("\\", "\\\\");
        String data = "";
        try {
            data = "source_url=" + URLEncoder.encode("/login/", StandardCharsets.UTF_8.toString())
                    + "&data="
                    + URLEncoder.encode("{\"options\":{\"username_or_email\":\"" + Email
                            + "\",\"password\":\"" + Password
                            + "\"},\"context\":{}}", StandardCharsets.UTF_8.toString())
                    + "&module_path="
                    + URLEncoder.encode("App()>LoginPage()>Login()>Button(class_name=primary, text=Log in, type=submit, size=large)", StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
        }

        HttpEntity entity = new StringEntity(data);
        post.setEntity(entity);

        re = client.execute(post);

        c = cookieStore.getCookies().stream().filter(o -> o.getName().equalsIgnoreCase("csrftoken")).findAny().get();
        csrf = c.getValue();

        //////////////////////////////////////
        //////////////////////////////////////
        get = new HttpGet("https://www.pinterest.com/");
        get.addHeader("Accept-Encoding", "gzip,deflate,sdch");
        get.addHeader("Accept-Language", "en-US,en;q=0.8");
        get.addHeader("Accept", Http.ACCEPT_ANY);

        re = client.execute(get);
        rs = EntityUtils.toString(re.getEntity());

        String username = "";
        pattern = Pattern.compile("\"username\":\"([a-zA-Z0-9_\\-]+)\",\"field_set_key\":\"homefeed\"", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        matcher = pattern.matcher(rs);
        while (matcher.find()) {
            username = matcher.group(1);
        }
        System.err.println(username);

        //String expResult = "";
        assertEquals("galeenefashion", username);
    }

    @Test
    public void testing_proxy() throws IOException {
        Proxy proxy = new Proxy();
        proxy.setEnabled(Boolean.TRUE);
        proxy.setIp("78.143.251.21");
        proxy.setPort(21263);

        proxy.setUsername("healzernevo");
        proxy.setPassword("dxqobyc3ho");//connection should fail at first.
        CredentialsProvider credsProvider = null;
        if (proxy.getUser() != null && proxy.getUser().length() > 0 && proxy.getPassword() != null && proxy.getPassword().length() > 0) {
            credsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword());
            credsProvider.setCredentials(AuthScope.ANY, credentials);
        }

        HttpHost pr = new HttpHost(proxy.getIp(), proxy.getPort());

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient client = HttpClients.custom()
                .setProxy(pr)
                .setDefaultCookieStore(cookieStore)
                .setDefaultCredentialsProvider(credsProvider)
                .build();

        //https://pinbot3.com/myip.php
        HttpGet get = new HttpGet("http://www.ip-adress.eu/");//https://www.pinterest.com
        String A = null;
        try {
            CloseableHttpResponse re = client.execute(get);
            System.out.println("----------------------------------------");
            System.out.println(re.getStatusLine().getStatusCode());
            if (re.getStatusLine().getStatusCode() == 200) {
                A = EntityUtils.toString(re.getEntity());
                System.out.println(A);
            }
        } catch (IOException e) {
            System.out.println(e);
        }

        assertTrue(A != null && A.length() > 0 && A.contains(proxy.getIp()));

    }
}
