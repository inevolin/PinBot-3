/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import Managers.Adapters.AbstractAConfigurationAdapter;
import Managers.Adapters.AbstractBoardAdapter;
import Managers.Adapters.AbstractPinAdapter;
import Managers.Adapters.AbstractPinnerAdapter;
import Managers.DALManager;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author UGent
 */
public class Misc {

    public Misc() {
    }
    
    @Test
    public void ExtObj2() throws IOException {
        DALManager dm = new DALManager();
        Account acc = dm.AllAccounts().get(0);
        Set<PinterestObject> objs = dm.getExternalObjects(DALManager.TYPES.duplicates_follow_pinners, acc);
        System.err.println(objs.size());
    }

    @Test
    public void ExtObj() throws UnsupportedEncodingException, FileNotFoundException, IOException {
        GsonBuilder gb = new GsonBuilder();
        gb.setPrettyPrinting();
        //gb.setLenient();
        gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gb.registerTypeAdapter(AConfiguration.class, new AbstractAConfigurationAdapter());
        gb.registerTypeAdapter(Board.class, new AbstractBoardAdapter());
        gb.registerTypeAdapter(Pin.class, new AbstractPinAdapter());
        gb.registerTypeAdapter(Pinner.class, new AbstractPinnerAdapter());
        gb.disableHtmlEscaping();
        gb.serializeNulls();
        Gson gson = gb.create();

        File f = new File("C:\\Users\\UGent\\Desktop\\tanikaedwardss@gmail.com\\duplicates_like");

        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(f), "UTF-8");
            Pin[] objs = null;
            objs = gson.fromJson(reader, Pin[].class);
            System.out.println(objs.length);
        } catch (JsonSyntaxException ex) {
            Stack<String> st = new Stack<>();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line;
                while ((line = br.readLine()) != null) {
                    st.push(line);
                }
            }
            if (st.peek().equalsIgnoreCase("]") || st.peek().equalsIgnoreCase("[]")) {
                //well formated
                //this case should never be raised!
            } else {
                while (!st.peek().equalsIgnoreCase("},")) {
                    st.pop();
                }
                List<String> rst = new ArrayList<String>(st);
                StringBuilder stringBuilder = new StringBuilder();
                while (rst.size() > 0) {
                    stringBuilder.append(rst.get(0));
                    rst.remove(0);
                    stringBuilder.append("\n");
                }
                stringBuilder.append("]");
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(f))) {
                    writer.write(stringBuilder.toString());
                }
            }

        }
    }

    @Test
    public void SimilarHashURL() {
        String u1 = "https://s-media-cache-ak0.pinimg.com/564x/66/a5/93/66a593a6d39ae6c8673d863e322c1001.jpg";
        String u2 = "https://s-media-cache-ak0.pinimg.com/564x/66/a5/93/66a593a6d39ae6c8673d863e322c1001.jpg";
        String o3 = "https://s-media-cache-ak0.pinimg.com/originals/69/42/64/69426441fdd363b720248e72e535c535.jpg";

        double d = org.apache.commons.lang3.StringUtils.getJaroWinklerDistance(u2, u1);
        System.err.println(d);
    }

    @Test
    public void report() {
        for (int i = 0; i < 2; i++) { //we should see only one HTTP POST request (second one is the same and shall be prevented)
            try {
                String f = "balh";
                f = null;
                f.charAt(6);
            } catch (Exception ex) {
                ExceptionHandler.reportException(ex);
            }
        }
    }

    @Test
    public void Encoding() throws UnsupportedEncodingException {
        File file = new File("C:\\Users\\UGent\\AppData\\Local\\Temp\\pb31462419955813504323.jpg");
        String s = URLEncoder.encode("pb37040270119853645050.jpg", StandardCharsets.UTF_8.toString());
        System.err.println(s);
    }

    @Test
    public void cyclicCopy() {
        Board b = new Board("fez", "url", "123", "suerid", "galeene", null, false, 0, PinterestObject.PinterestObjectResources.BoardFeedResource);
        AQuery q = new AQuery("", b, PinterestObject.PinterestObjectResources.BoardFeedResource);

        AQuery c = q.copy(null);
    }

    @Test
    public void Spintax() {
        Random r = new Random();
        String s = "this is a test";
        System.err.println(MyUtils.spintax(r, s));

        s = "{Hello|Hi} {World|People}! {C{#|++|}|Java} is an {awesome|amazing} language.";
        System.err.println(MyUtils.spintax(r, s));

        s = "{Hello|Hi} {World|People}! {OOP} is an {awesome|amazing} language.";
        System.err.println(MyUtils.spintax(r, s));
    }

    @Test
    public void MyLog() throws UnsupportedEncodingException {
        //String t = URLEncoder.encode(, StandardCharsets.UTF_8.toString());

        //we must get:
        String h = "Renew+Your+membership+and+get+Spark!%5CnSign+up+Here";
        String s = "Renew Your membership and {\"get\"} Spark!\nSign up Here";

        String t = s;

        t = t.replaceAll("\n", "\\\\n");
        t = t.replace("\"", "\\\"");
        //t = URLEncoder.encode(t, StandardCharsets.UTF_8.toString());

        System.err.println(t);

        //Assert.isTrue(t.equals(h), "test");
    }

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    @Test
    public void fileStringBytes() throws IOException {
        String path = "C:\\Users\\UGent\\Pictures\\balling\\6792781-free-interior-wallpaper.jpg";
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String c = new String(encoded, StandardCharsets.UTF_8);

        System.err.println(c);
    }

    @Test
    public void regex() {
        String pattern = "\\<meta property\\=\"og:url\" name\\=\"og:url\" content\\=\"(https://[A-Za-z]{2}\\.pinterest\\.com)\\/\" data-app\\>";
        System.err.println(pattern);
        String rs = "<meta property=\"og:url\" name=\"og:url\" content=\"https://nl.pinterest.com/\" data-app>";
        System.err.println(rs);
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        java.util.regex.Matcher m = p.matcher(rs);
        if (m.matches()) {
            System.out.println(m.group(1));
            assertTrue(1 == 1);
        } else {
            assertTrue(1 == 0);
        }
    }

}
