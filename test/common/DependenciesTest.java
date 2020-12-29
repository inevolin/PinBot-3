/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.http.impl.client.BasicCookieStore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author healzer
 */
public class DependenciesTest {

    public DependenciesTest() {
    }

    @Test
    public void test1() {
        try {
            URL url = new URL("https://pinbot3.com/reg/libs/index.php");
            URLConnection conn = url.openConnection();
            String rs = null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                rs = reader.lines().collect(Collectors.joining("\n"));
            }
            if (rs != null) {
                String execpath = MyUtils.execPath(true);
                
                ArrayList<String> check = new ArrayList<>(Arrays.asList(rs.split("\\r?\\n")));
                int mustHave = check.size();
                
                String lib_path = execpath + File.separator + "lib";
                if (!Files.exists(Paths.get(lib_path))) {
                    Files.createDirectory(Paths.get(lib_path));
                }
                ArrayList<String> local = new ArrayList<>();
                Files.walk(Paths.get(lib_path)).forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        String fn = file.getFileName().toString();
                        local.add(fn);
                    }
                });

                for (String s : local) {
                    if (check.contains(s)) {
                        check.remove(s);
                    }
                }

                if (check.size() > 0) {
                    Http http = new Http(null, new BasicCookieStore());
                    for (String s : check) {
                        http.downloadFile("https://pinbot3.com/reg/libs/" + s, execpath + File.separator + "lib" + File.separator + s);
                    }
                }
                
                int localHave = (int) Files.list(Paths.get(lib_path)).count();
                System.err.println("Server has " + mustHave + " files, we have " + localHave + " files.");
                assertTrue(mustHave == localHave);
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }
}
