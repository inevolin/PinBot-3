/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pinbot3.helpers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.Proxy;

/**
 *
 * @author UGent
 */
public class AccImporter {

    public void Import(File file) throws FileNotFoundException, IOException {
        Acc[] reviews = null;
        String content = getContent(file);
        switch (content) {
            case "json":
                reviews = readJSON(file);
                break;

            case "csv":
                reviews = readCSV(file);
                break;

            case "empty":
                break;

            case "unsupported":
                break;

            case "default":
                break;
        }

        if (reviews != null) {
            for (Acc acc : reviews) {
                try {

                    Account a = new Account();
                    a.setEmail(acc.Email);
                    a.setPassword(acc.Password);

                    Proxy pr = new Proxy();
                    if (acc.WebProxy != null) {
                        pr.setIp(acc.WebProxy.Ip);
                        pr.setPort(Integer.parseInt(acc.WebProxy.Port));
                        pr.setUsername(acc.WebProxy.User);
                        pr.setPassword(acc.WebProxy.Pass);
                        pr.setEnabled(acc.ValidProxy);
                        a.setProxy(pr);
                    }

                    a.setStatus(Account.STATUS.UNAUTHORIZED);
                    pinbot3.PinBot3.dalMgr.Save(a);
                    pinbot3.PinBot3.campaignMgr.getAccounts().add(a);
                } catch (Exception ex) {
                    common.ExceptionHandler.reportException(ex);
                }
            }
        }
    }

    private String getContent(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        if ((line = br.readLine()) != null) {
            if (line.startsWith("[")) {
                return "json";
            } else {
                Pattern p = Pattern.compile("^(.+?)@(.+?),(.+?),(.*?),(.*?),(.*?)$");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    return "csv";
                } else {
                    return "unsupported";
                }
            }
        }

        return "empty";
    }

    private Acc[] readJSON(File file) throws FileNotFoundException {
        JsonReader reader = new JsonReader(new FileReader(file));
        reader.setLenient(true);
        return new Gson().fromJson(reader, Acc[].class);
    }

    private Acc[] readCSV(File file) throws FileNotFoundException, IOException {
        List<Acc> listAccs;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            listAccs = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");

                Acc acc = new Acc();
                acc.Email = values[0].trim();
                acc.Password = values[1].trim();

                WebProxy webProxy = new WebProxy();
                if (values.length > 2) {
                    String[] ip_port = values[2].split(":");
                    webProxy.Ip = ip_port[0].trim();
                    webProxy.Port = ip_port[1].trim();
                    if (values.length == 5) {
                        webProxy.User = values[3].trim();
                        webProxy.Pass = values[4].trim();
                    }
                    acc.WebProxy = webProxy;
                    acc.ValidProxy = true;
                }

                listAccs.add(acc);
            }
        }
        return listAccs.toArray(new Acc[listAccs.size()]);
    }

    public class Acc {

        public String Email, Password;
        public Boolean ValidProxy;
        public WebProxy WebProxy;
    }

    public class WebProxy {

        public String Ip, Port, User, Pass;
    }
}
