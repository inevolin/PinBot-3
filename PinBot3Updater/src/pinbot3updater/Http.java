/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3updater;

import static pinbot3updater.Http.ACCEPT_HTML;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Http {

    public static String ACCEPT_JSON = "application/json, text/javascript, */*; q=0.01";
    public static String ACCEPT_HTML = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8";
    public static String ACCEPT_ANY = "*/*";

    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0";
    private HttpURLConnection con = null;

    public Http(CookieManager cookieBoss) throws Exception {
        // !!! Each Http object will automatically store cookies in cookieBoss object
        // !!! Each request (Get/Post) sends cookies, and then updates them from response.
        // Oh and please create a new Http object for each process, it's not thread safe :)
        if (cookieBoss == null) {
            throw new Exception("CookieManager may not be null!");
        }
        CookieHandler.setDefault(cookieBoss);
    }

    public String Get(String url, Map<String, String> headers, String accept) {
        String rs = null;
        try {

            SetGetConnection(new URL(url), accept);
            con.setRequestMethod("GET");
            if (headers != null) {
                for (Map.Entry<String, String> kv : headers.entrySet()) {
                    con.setRequestProperty(kv.getKey(), kv.getValue());
                }
            }
            System.err.println("Sending 'GET' request to URL : " + url);

            int status = con.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP
                        || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    String rurl = con.getHeaderField("Location");
                    return Get(rurl, headers, accept);
                } else {
                    rs = ReadResponse(con.getErrorStream());
                }
            } else {
                rs = ReadResponse(con.getInputStream());
            }

        } catch (ProtocolException ex) {
            ex.printStackTrace();
            
        } catch (IOException ex) {
            ex.printStackTrace();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            
        }
        return rs;
    }

    public void Abort() {
        if (con != null) {
            con.disconnect();
        }
    }

    public byte[] getBytes(InputStream is) throws IOException {

        int len;
        int size = 1024;
        byte[] buf;

        if (is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while ((len = is.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);
            }
            buf = bos.toByteArray();
        }
        return buf;
    }

    public static boolean isGzipStream(byte[] bytes) {
        try {
            int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
            return (GZIPInputStream.GZIP_MAGIC == head);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String ReadResponse(InputStream is) throws IOException {
        byte[] bytes = getBytes(is);
        Boolean GZIP = isGzipStream(bytes);
        is = new ByteArrayInputStream(bytes);

        InputStream in = null;
        if (GZIP) {
            in = new GZIPInputStream(is);
        } else {
            in = new BufferedInputStream(is);
        }
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String read;
        while ((read = br.readLine()) != null) {
            sb.append(read);
        }
        br.close();
        if (reader != null) {
            reader.close();
        }
        if (in != null) {
            in.close();
        }
        is.close();

        con.disconnect();
        return sb.toString();
    }

    private void SetGetConnection(URL url, String accept) throws Exception {
        if (url.getProtocol().contains("https")) {
            con = new SecureConnector("TLS").openSecureConnection(url);
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        // default connection settings.
        con.setReadTimeout(1000 * 60);
        con.setConnectTimeout(1000 * 120);
        con.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept", accept);
    }

    public void downloadFile(String fileURL, String saveFilePath) throws MalformedURLException, Exception {
        URL url = new URL(fileURL);
        SetGetConnection(url, ACCEPT_HTML);
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            InputStream inputStream = con.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            int bytesRead = -1;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

        } else {
            System.err.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        con.disconnect();

    }
}
