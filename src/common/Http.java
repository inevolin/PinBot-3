package common;

import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.http.Header;
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
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class Http {

    public static String ACCEPT_JSON = "application/json, text/javascript, */*; q=0.01";
    public static String ACCEPT_HTML = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    public static String ACCEPT_ANY = "*/*";
    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0";
    private static int TIMEOUT = 60000;
    private final model.Proxy proxy;
    private final RequestConfig defaultRequestConfig;
    private CloseableHttpClient client;
    private CookieStore cookieStore;

    public Http(model.Proxy proxy, CookieStore cookieBoss) throws Exception {

        if (cookieBoss == null) {
            throw new Exception("CookieStore must not be null!");
        }
        this.cookieStore = cookieBoss;
        this.proxy = proxy;

        this.defaultRequestConfig = RequestConfig.custom().setSocketTimeout(TIMEOUT).setConnectTimeout(TIMEOUT).setConnectionRequestTimeout(TIMEOUT).build();

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        
        //PoolingHttpClientConnectionManager connectionManagerMultiThreaded = new PoolingHttpClientConnectionManager();
        //connectionManagerMultiThreaded.setDefaultMaxPerRoute(10);
        //connectionManagerMultiThreaded.setMaxTotal(20);

        //clientBuilder.setConnectionManager(connectionManagerMultiThreaded);
        clientBuilder.setDefaultCookieStore(this.cookieStore);
        clientBuilder.setDefaultRequestConfig(defaultRequestConfig);

        HttpHost proxy2 = initProxy(clientBuilder);
        if (proxy2 != null) {
            clientBuilder.setProxy(proxy2);
        }
        this.client = clientBuilder.build();

    }

    private HttpHost initProxy(HttpClientBuilder clientBuilder) {
        if (proxy != null && proxy.getEnabled()) {
            if (proxy.getUser() != null && proxy.getUser().length() > 0 && proxy.getPassword() != null && proxy.getPassword().length() > 0) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword());
                credsProvider.setCredentials(AuthScope.ANY, credentials);
                clientBuilder.setDefaultCredentialsProvider(credsProvider);
            }

            HttpHost pr = new HttpHost(proxy.getIp(), proxy.getPort());
            return pr;
        }
        return null;
    }

    private void addHeaders(HttpRequestBase http, Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> kv : headers.entrySet()) {
                http.addHeader(kv.getKey(), kv.getValue());
            }
        }
    }

    public KeyValuePair<Integer> Get(String url, Map<String, String> headers) throws Exception {
        return Get(url, headers, 1);
    }

    private KeyValuePair<Integer> Get(String url, Map<String, String> headers, int attempt) throws Exception {
        KeyValuePair<Integer> kv = null;
        CloseableHttpResponse re = null;
        System.err.println("GET: " + url);
        HttpGet get = new HttpGet(url);
        try {
            Http.setContentType(headers, false);
            addHeaders(get, headers);
            re = client.execute(get);
            String rs = null;
            
            rs = EntityUtils.toString(re.getEntity());
            //writeLog(url, "GET", headers, re, rs);
            kv = new KeyValuePair<>(rs, re.getStatusLine().getStatusCode());
            System.err.println("\t GET (" + kv.getValue() + ") " + url);
            if (kv.getValue() != 200) {
                writeLog(url, "GET", "", headers, re, rs);
                if (attempt <= 2) { //one more time
                    Thread.sleep(5000);
                    kv = Get(url, headers, attempt + 1); //try again
                }
            }
        } catch (EOFException | SocketTimeoutException | UnknownHostException ex) {
            common.ExceptionHandler.reportException(ex);
            if (attempt <= 3) {
                Thread.sleep(10000);
                return Get(url, headers, attempt + 1); //try again 2 times
            } else {
                return kv;
            }
        } finally {
            if (re != null) {
                re.close();
            }
            get.releaseConnection();
        }
        return kv;
    }

    public KeyValuePair<Integer> Post(String url, String data, Map<String, String> headers) throws Exception {
        return Post(url, data, headers, 1);
    }

    private KeyValuePair<Integer> Post(String url, String data, Map<String, String> headers, int attempt) throws Exception {
        KeyValuePair<Integer> kv = null;
        CloseableHttpResponse re = null;
        System.err.println("POST: " + url);
        HttpPost post = new HttpPost(url);
        try {
            Http.setContentType(headers, true);
            addHeaders(post, headers);
            HttpEntity entity = new StringEntity(data);
            post.setEntity(entity);
            re = client.execute(post);
            String rs = null;
            rs = EntityUtils.toString(re.getEntity());
            //writeLog(url, "POST", headers, re, rs);
            kv = new KeyValuePair<>(rs, re.getStatusLine().getStatusCode());
            System.err.println("\t POST (" + kv.getValue() + ") " + url);
            if (kv.getValue() != 200) {
                writeLog(url, "POST", data, headers, re, rs);
                if (attempt <= 2) { //one more time
                    Thread.sleep(5000);
                    kv = Post(url, data, headers, attempt + 1); //try again
                }
            }
        } catch (EOFException ex) {
            common.ExceptionHandler.reportException(ex);
            if (attempt <= 3) { //two more times
                Thread.sleep(10000);
                return Post(url, data, headers, attempt + 1); //try again
            } else {
                return kv;
            }
        } finally {
            if (re != null) {
                re.close();
            }
            post.releaseConnection();
        }
        return kv;
    }

    public KeyValuePair<Integer> PostFile(String url, HttpEntity multipart, Map<String, String> headers) throws Exception {
        return PostFile(url, multipart, headers, 1);
    }

    private KeyValuePair<Integer> PostFile(String url, HttpEntity multipart, Map<String, String> headers, int attempt) throws Exception {
        KeyValuePair<Integer> kv = null;
        CloseableHttpResponse re = null;
        System.err.println("POST UPLOAD: " + url);
        HttpPost post = new HttpPost(url);
        try {
            /*for (Map.Entry<String, String> kv : headers.entrySet()) {
                System.err.println("\t" + kv.getKey() + ": " + kv.getValue());
            }
            System.err.println("");*/
            addHeaders(post, headers);
            post.setEntity(multipart);
            re = client.execute(post);
            String rs = null;
            rs = EntityUtils.toString(re.getEntity());
            kv = new KeyValuePair<>(rs, re.getStatusLine().getStatusCode());
            //writeLog(url, "POST", headers, re, rs);
        } catch (EOFException ex) {
            common.ExceptionHandler.reportException(ex);
            if (attempt <= 3) {
                Thread.sleep(10000);
                return PostFile(url, multipart, headers, attempt + 1);
            } else {
                return kv;
            }
        } finally {
            if (re != null) {
                re.close();
            }
            post.releaseConnection();
        }
        return kv;
    }

    public String testIf302(String url, Map<String, String> headers) throws Exception {
        return testIf302(url, headers, 1);
    }

    private String testIf302(String url, Map<String, String> headers, int attempt) throws Exception {
        String rs = null;
        CloseableHttpResponse re = null;
        HttpGet get = new HttpGet(url);
        try {
            CloseableHttpClient tmpclient = HttpClients.custom().setDefaultCookieStore(this.cookieStore).disableRedirectHandling().build();
            re = tmpclient.execute(get);
            System.out.println("--test redirect-- " + url + " --  " + re.getStatusLine() + "  ---\n\n");
            rs = EntityUtils.toString(re.getEntity());
            //writeLog(url, "GET", headers, re, rs);
            if (re.getStatusLine().getStatusCode() == 302 || re.getStatusLine().getStatusCode() == 301) {
                rs = re.getFirstHeader("Location").getValue();
                System.out.println("Location: " + rs);
            }
        } catch (EOFException ex) {
            common.ExceptionHandler.reportException(ex);
            if (attempt <= 3) {
                Thread.sleep(10000);
                return testIf302(url, headers, attempt + 1);
            } else {
                return rs;
            }
        } finally {
            if (re != null) {
                re.close();
            }
            get.releaseConnection();
        }
        return rs;
    }

    private void writeLog(String url, String method, String data, Map<String, String> headers, CloseableHttpResponse re, String rs) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(method).append(" ").append(re.getStatusLine().getStatusCode()).append(" ").append(url).append("\n\n");
        for (Map.Entry<String, String> kv : headers.entrySet()) {
            sb.append(kv.getKey()).append("\t" + ": ").append(kv.getValue()).append("\n");
        }
        sb.append("\n");
        for (Header h : re.getAllHeaders()) {
            sb.append("\t").append(h.getName()).append(": ").append(h.getValue());
        }
        sb.append("\n\ndata:\n\n");

        sb.append(data);

        sb.append("\n");
        sb.append("\t").append(rs).append("\n\n-----------------------------\n\n");
        //MyUtils.writeLog(sb.toString(), false);

        ExceptionHandler.reportException(null, sb.toString());
    }

    public void downloadFile(String fileURL, String saveFilePath) throws Exception {
        downloadFile(fileURL, saveFilePath, 1);
    }

    private void downloadFile(String fileURL, String saveFilePath, int attempt) throws Exception {
        System.err.println("GET binary: " + fileURL);
        HttpGet get = new HttpGet(fileURL);
        get.addHeader("Accept", Http.ACCEPT_ANY);
        CloseableHttpResponse re = null;
        try {
            re = client.execute(get);
            if (re.getStatusLine().getStatusCode() == 200) {

                InputStream inputStream = re.getEntity().getContent();
                FileOutputStream outputStream = new FileOutputStream(saveFilePath);
                int bytesRead = -1;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
            }
        } catch (EOFException ex) {
            common.ExceptionHandler.reportException(ex);
            if (attempt <= 3) {
                Thread.sleep(10000);
                downloadFile(fileURL, saveFilePath, attempt + 1);
            } else {
                return;
            }
        } finally {
            if (re != null) {
                re.close();
            }
            get.releaseConnection();
        }

    }

    public void Abort() {
        try {
            client.close();
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

        }
    }

    public static String GetCookieVal(CookieStore s, String key) {
        String val = null;
        try {
            Cookie c = s.getCookies().stream().filter(o -> o.getName().equalsIgnoreCase(key)).findAny().get();
            val = c.getValue();
        } catch (NoSuchElementException ex) {
        }
        return val;
    }

    public static String unescape(String response) {
        response = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(response);
        response = org.apache.commons.lang3.StringEscapeUtils.unescapeHtml3(response);
        response = response.replace("\\", "");
        return response;
    }

    public static String getBoundary() {
        Long TICKS_AT_EPOCH = 621355968000000000L;
        Long tick = System.currentTimeMillis() * 10000 + TICKS_AT_EPOCH;
        return "----------------------------" + Long.toHexString(tick);
    }

    public static boolean validUrl(String url) {
        if (url == null || url.length() <= 1) {
            return false;
        }
        return url.matches("^(http://|https://)(www\\.)?([a-zA-Z0-9\\-]+?)(\\.)([a-zA-Z]+?){1,6}(([a-z0-9A-Z\\?\\&\\%\\=_\\-\\./])+?)$");
    }

    public static void setContentType(Map<String, String> headers, Boolean POST) {
        if (headers != null && headers.keySet().stream().filter(x -> x.equalsIgnoreCase("content-type")).findAny().isPresent()) {
            return;
        }
        String acceptVal = null;
        if (headers.containsKey("Accept")) {
            acceptVal = headers.get("Accept");
        } else if (headers.containsKey("accept")) {
            acceptVal = headers.get("accept");
        }
        if (acceptVal != null) {
            if (POST) {
                headers.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
            } else if (acceptVal.equals(Http.ACCEPT_JSON)) {
                headers.put("Content-type", "application/json; charset=utf-8");
            } else {
                headers.put("Content-type", "text/html; charset=UTF-8");
            }
        } else {
            headers.put("Content-type", "text/html; charset=UTF-8");
        }
    }

}
