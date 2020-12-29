/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ //test
package Managers;

import Managers.Adapters.AbstractAConfigurationAdapter;
import Managers.Adapters.AbstractBoardAdapter;
import Managers.Adapters.AbstractPinAdapter;
import Managers.Adapters.AbstractPinnerAdapter;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.MalformedJsonException;
import common.MyUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.Account;
import model.Campaign;
import model.Proxy;
import model.configurations.AConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;

public class DALManager {

    private String db3;
    public static final String PU = "PinBot3PU";
    // Take a look at   /META-INF/persistence.xml
    public EntityManagerFactory emf;
    protected EntityManager em;
    protected Set<String> tableNames;
    private GsonBuilder gb;
    private Gson gson;

    public enum TYPES {
        @Deprecated
        duplicates_follow,
        @Deprecated
        duplicates_unfollow,
        duplicates_comment, duplicates_follow_pinners, duplicates_follow_boards, duplicates_invite, duplicates_like, duplicates_message, duplicates_pin, duplicates_repin, duplicates_unfollow_pinners, duplicates_unfollow_boards, followers, following, campaigns, boards, proxy, duplicates_interest
    };

    public DALManager() {
        db3 = MyUtils.db3Path();

        gb = new GsonBuilder();
        gb.setPrettyPrinting();
        gb.setLenient();
        gb.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        gb.registerTypeAdapter(AConfiguration.class, new AbstractAConfigurationAdapter());
        gb.registerTypeAdapter(Board.class, new AbstractBoardAdapter());
        gb.registerTypeAdapter(Pin.class, new AbstractPinAdapter());
        gb.registerTypeAdapter(Pinner.class, new AbstractPinnerAdapter());
        gb.disableHtmlEscaping();
        gb.serializeNulls();
        this.gson = gb.create();
    }

    public void InitEMF() {
        InitEMF(null);
    }

    public void InitEMF(Map<String, Object> props) {
        if (em != null && em.isOpen()) {
            em.clear();
            em.close();
        }
        if (emf != null && em.isOpen()) {
            emf.close();
        }
        if (props != null) {
            emf = Persistence.createEntityManagerFactory(PU, props);
        } else {
            emf = Persistence.createEntityManagerFactory(PU);
        }
        em = emf.createEntityManager();
    }

    public void shutdown() {
        if (em != null && em.isOpen()) {
            em.clear();
            em.close();
        }
    }

    public enum Errors {
        NO_ERROR, EXISTS, DATABASE_ERROR;
    }

    public void printQueriesCount() {
        Set<String> names = tableNames();

        //EntityManager em = emf.createEntityManager();
        String rs = "------------------\n";
        for (String name : names) {
            try {
                if (name.equalsIgnoreCase("aquery") || name.equalsIgnoreCase("pinterestobject") || name.equalsIgnoreCase("aconfiguration") || name.equalsIgnoreCase("pin") || name.equalsIgnoreCase("pinner") || name.equalsIgnoreCase("board")) {

                    Query query = em.createNativeQuery("select count(1) as c, MAX(ID) as b from " + name);
                    List<Object[]> lst = query.getResultList();
                    if ((int) lst.get(0)[0] > 0) {
                        rs += (name + ": " + lst.get(0)[0] + " id:" + lst.get(0)[1]) + "\n";
                    }

                } else {

                    Query query = em.createNativeQuery("select count(1) as c from " + name);
                    List<Integer> lst = query.getResultList();
                    if (lst.get(0) > 0) {
                        rs += (name + ": " + lst.get(0)) + "\n";
                    }

                }

            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }

        rs += ("------------------");
        rs += "\n";
        System.err.println(rs);
        //em.close();
    }

    public void disconnect() {
        try {
            String url = (String) emf.getProperties().get("javax.persistence.jdbc.url.shutdown");
            String driver = (String) emf.getProperties().get("javax.persistence.jdbc.driver");
            Class.forName(driver);
            DriverManager.getConnection(url, "test", "test");
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
            System.err.println("disconnected");
            try {
                Thread.sleep(200);
            } catch (InterruptedException _ex) {
                common.ExceptionHandler.reportException(_ex);
            }
        }
    }

    public Set<String> tableNames() {
        if (tableNames != null) {
            return tableNames;
        } else {
            tableNames = new HashSet<>();
        }
        try {
            String url = (String) emf.getProperties().get("javax.persistence.jdbc.url");
            String driver = (String) emf.getProperties().get("javax.persistence.jdbc.driver");
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, "test", "test");
            DatabaseMetaData dbmd = connection.getMetaData();
            ResultSet resultSet = dbmd.getTables(null, null, null, null);
            Set<String> names = new HashSet<>();
            while (resultSet.next()) {
                String strTableName = resultSet.getString("TABLE_NAME");
                if (!strTableName.startsWith("SYS")) {
                    names.add(strTableName);
                }
            }
            tableNames.addAll(names);
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return tableNames;
    }

    public synchronized Errors Save(Account obj) {
        System.err.println("Saved: " + obj);
        Errors err = Errors.NO_ERROR;

        try {

            String tmp = db3 + obj.getEmail() + ".js_tmp";
            String file = db3 + obj.getEmail() + ".js";

            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                try (OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); BufferedWriter out = new BufferedWriter(osw)) {
                    gson.toJson(obj, out);
                }
            }

            File f_file = new File(file);
            File f_tmp = new File(tmp);

            if (f_file.exists()) {
                f_file.delete();
            }

            if (f_tmp.exists()) {
                f_tmp.renameTo(f_file);
            }

        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

            err = Errors.DATABASE_ERROR;
        }

        return err;
    }

    public synchronized void RemoveAccount(Account a) throws Exception {
        File f = new File(db3 + a.getEmail() + ".js");
        if (f.exists()) {
            f.delete();
        }

        File dir = externalDir(a);
        File[] files = dir.listFiles();
        for (File fi : files) {
            fi.delete();
        }
        dir.delete();
    }

    public File getDB3() {
        return new File(db3);
    }

    private void checkSetSelectCampaign(List<Account> lst) {
        if (lst == null) {
            return;
        } else {
            for (Account acc : lst) {
                if (acc.getSelectCampaign() != null) {
                    acc.enableCampaign(acc.getSelectCampaign());
                } else if (acc.getCampaigns().size() > 0) {
                    Optional<Campaign> found = acc.getCampaigns().stream().filter(x -> x.isSelectedCampaign() == true).findFirst();
                    if (found.isPresent()) {
                        acc.enableCampaign(found.get());
                    } else {
                        acc.enableCampaign(acc.getCampaigns().iterator().next());
                    }
                }
                Save(acc);
            }
        }
    }

    /*
        We have 3 cases:
    1. clean install --> OK
    2. db3 only --> OK
    3. db only --> DB2to3
     */
    public synchronized List<Account> AllAccounts() throws FileNotFoundException, IOException {
        List<Account> lst = new ArrayList<>();

        File dir = getDB3();
        File[] files = dir.listFiles((File dir1, String filename) -> filename.endsWith(".js"));

        if (dir.exists() && files != null && files.length > 0) {
            for (File f : files) {
                try (FileInputStream stream = new FileInputStream(f);
                        InputStreamReader ireader = new InputStreamReader(stream, "UTF-8");
                        JsonReader reader = new JsonReader(ireader);) {
                    reader.setLenient(true);
                    Account acc = processReader(reader);
                    lst.add(acc);
                } catch (Exception ex) {
                    System.err.println(f.getAbsoluteFile());
                    common.ExceptionHandler.reportException(ex);
                }
            }
            checkSetSelectCampaign(lst);
        } else if (Files.exists(Paths.get(MyUtils.db2Path()))) {
            lst = DB2to3(dir);
            createDB3dir(dir); //create if not exists !! db2 to db3
            checkSetSelectCampaign(lst);
            lst = AllAccounts();
        }
        createDB3dir(dir); //create if not exists !! clean install

        return lst;
    }

    private void createDB3dir(File dir) {
        try {
            if (!dir.exists()) {
                Files.createDirectory(Paths.get(db3));
            }
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

        }
    }

    private void processObject(JsonReader reader, Account acc, TYPES type) {
        switch (type) {
            case proxy:
                Proxy px = gson.fromJson(reader, Proxy.class);
                acc.setProxy(px);
                break;
        }
    }

    private void processArray(JsonReader reader, Account acc, TYPES type) throws IOException {
        reader.beginArray();

        Iterator it = null;
        Set<PinterestObject> objs = new LinkedHashSet<>();
        switch (type) {
            case campaigns:
                while (reader.hasNext()) {
                    Campaign c = gson.fromJson(reader, Campaign.class);
                    acc.getCampaigns().add(c);
                }
                break;
            case boards:
                while (reader.hasNext()) {
                    Board c = gson.fromJson(reader, Board.class);
                    acc.getBoards().add(c);
                }
                break;
            case following:
                while (reader.hasNext()) {
                    Pinner c = gson.fromJson(reader, Pinner.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getFollowing().clear();
                break;
            case followers:
                while (reader.hasNext()) {
                    Pinner c = gson.fromJson(reader, Pinner.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getFollowers().clear();
            case duplicates_pin:
                while (reader.hasNext()) {
                    Pin c = gson.fromJson(reader, Pin.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_pin().clear();
                break;
            case duplicates_repin:
                while (reader.hasNext()) {
                    Pin c = gson.fromJson(reader, Pin.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_repin().clear();
                break;
            case duplicates_like:
                while (reader.hasNext()) {
                    Pin c = gson.fromJson(reader, Pin.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_like().clear();
                break;
            case duplicates_invite:
                while (reader.hasNext()) {
                    Pin c = gson.fromJson(reader, Pin.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_invite().clear();
                break;
            case duplicates_comment:
                while (reader.hasNext()) {
                    Pin c = gson.fromJson(reader, Pin.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_comment().clear();
                break;
            case duplicates_message:
                while (reader.hasNext()) {
                    Pinner c = gson.fromJson(reader, Pinner.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                acc.getDuplicates_message().clear();
                break;
            case duplicates_follow:
                while (reader.hasNext()) {
                    gson.fromJson(reader, Object.class);
                }
                break;
            case duplicates_unfollow:
                while (reader.hasNext()) {
                    gson.fromJson(reader, Object.class);
                }
                break;
            case duplicates_follow_pinners:
                while (reader.hasNext()) {
                    Pinner c = gson.fromJson(reader, Pinner.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                it = acc.getDuplicates_follow().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o instanceof Pinner) {
                        it.remove();
                    }
                }
                break;
            case duplicates_follow_boards:
                while (reader.hasNext()) {
                    Board c = gson.fromJson(reader, Board.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                it = acc.getDuplicates_follow().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o instanceof Board) {
                        it.remove();
                    }
                }
                break;
            case duplicates_unfollow_pinners:
                while (reader.hasNext()) {
                    Pinner c = gson.fromJson(reader, Pinner.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                it = acc.getDuplicates_unfollow().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o instanceof Pinner) {
                        it.remove();
                    }
                }
                break;
            case duplicates_unfollow_boards:
                while (reader.hasNext()) {
                    Board c = gson.fromJson(reader, Board.class);
                    objs.add(c);
                }
                storeObjectsExternally(objs, type, acc, true);
                it = acc.getDuplicates_unfollow().iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (o instanceof Board) {
                        it.remove();
                    }
                }
                break;
            case duplicates_interest:
                break;
        }
        reader.endArray();
    }

    private void processProperty(JsonReader reader, String type, Account acc) throws IOException {
        switch (type) {
            case "id":
                acc.setId(reader.nextLong());
                break;
            case "email":
                acc.setEmail(reader.nextString());
                break;
            case "password":
                acc.setPassword(reader.nextString());
                break;
            case "username":
                acc.setUsername(reader.nextString());
                break;
            case "status":
                acc.setStatus(Account.STATUS.valueOf(reader.nextString()));
                break;
            case "app_version":
                acc.setAppVersion(reader.nextString());
                break;
            case "my_following":
                acc.setMyFollowing(reader.nextInt());
                break;
            case "my_followers":
                acc.setMyFollowers(reader.nextInt());
                break;
            case "my_likes":
                acc.setMyLikes(reader.nextInt());
                break;
            case "my_boards":
                acc.setMyBoards(reader.nextInt());
                break;
            case "my_pins":
                acc.setMyPins(reader.nextInt());
                break;
            case "base_url":
                acc.base_url = reader.nextString();
                break;
        }
    }

    private Account processReader(JsonReader reader) throws IOException {

        Account acc = new Account();
        reader.beginObject();
        boolean ct = true;
        String type = null;
        while (reader.hasNext() && ct) {
            JsonToken tk = reader.peek();
            switch (tk) {
                case END_DOCUMENT:
                    ct = false;
                    break;
                case NAME:
                    type = reader.nextName();
                    break;
                case BEGIN_OBJECT:
                    processObject(reader, acc, TYPES.valueOf(type));
                    break;
                case BEGIN_ARRAY:
                    processArray(reader, acc, TYPES.valueOf(type));
                    break;
                case STRING:
                    processProperty(reader, type, acc);
                    break;
                case NUMBER:
                    processProperty(reader, type, acc);
                    break;
                case BOOLEAN:
                    processProperty(reader, type, acc);
                    break;
                case NULL:
                    reader.nextNull(); //?
                    break;
            }
        }
        reader.endObject();

        //System.err.println(acc.getEmail() + " loaded");
        return acc;
    }

    private List<Account> DB2to3(File dir) throws IOException {
        InitEMF();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Account> cq = cb.createQuery(Account.class);
        Root<Account> rootEntry = cq.from(Account.class);
        CriteriaQuery<Account> all = cq.select(rootEntry);
        TypedQuery<Account> allQuery = em.createQuery(all);
        List<Account> lst = allQuery.getResultList();
        return lst;
    }

    private synchronized File externalDir(Account acc) {
        // ./db3/user@gmail.com/
        File dir = new File(getDB3().getAbsolutePath() + File.separatorChar + acc.getEmail());
        if (!dir.exists()) {
            try {
                Files.createDirectory(dir.toPath());
            } catch (IOException ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }
        return dir;
    }

    private synchronized File externalDirFile(Account acc, TYPES type) {
        // ./db3/user@gmail.com/duplicates_comment
        File dir = externalDir(acc);
        File file = new File(dir.getAbsolutePath() + File.separatorChar + type);
        if (!file.exists()) {
            try {
                file.createNewFile();
                saveToExternalFile(file, new HashSet<>());
            } catch (IOException ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }
        return file;
    }

    public synchronized boolean containsObjectExternally(PinterestObject obj, TYPES type, Account acc) {
        PinterestObject found = null;
        found = getObjectExternally(obj, type, acc);
        return found != null;
    }

    public synchronized PinterestObject getObjectExternally(PinterestObject obj, TYPES type, Account acc) {
        File file = externalDirFile(acc, type);
        try (FileInputStream stream = new FileInputStream(file);
                InputStreamReader ireader = new InputStreamReader(stream, "UTF-8");
                JsonReader reader = new JsonReader(ireader);) {

            reader.setLenient(true);
            if (reader.peek() == JsonToken.NULL) {
                return null;
            }
            reader.beginArray();
            while (reader.hasNext()) {
                JsonToken tk = reader.peek();
                switch (tk) {
                    case BEGIN_OBJECT:
                        PinterestObject c = null;
                        switch (type) {
                            case duplicates_comment:
                            case duplicates_pin:
                            case duplicates_repin:
                            case duplicates_like:
                                c = gson.fromJson(reader, Pin.class);
                                break;
                            case duplicates_follow_pinners:
                            case duplicates_unfollow_pinners:
                            case duplicates_message:
                            case duplicates_invite:
                            case following:
                            case followers:
                                c = gson.fromJson(reader, Pinner.class);
                                break;
                            case duplicates_follow_boards:
                            case duplicates_unfollow_boards:
                                c = gson.fromJson(reader, Board.class);
                                break;
                            default:
                                gson.fromJson(reader, Object.class);
                                break;
                        }
                        if (obj.equals(c)) {
                            return c;
                        }
                        break;
                }
            }
            reader.endArray();
        } catch (EOFException ex) {
            //ignore
        } catch (JsonSyntaxException | MalformedJsonException ex) {
            //ignore
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public synchronized Set<PinterestObject> getExternalObjects(TYPES type, Account acc) {
        return getExternalObjects(type, acc, 1);
    }

    private synchronized Set<PinterestObject> getExternalObjects(TYPES type, Account acc, int attempt) {
        Set<PinterestObject> set = new HashSet<>();
        File file = externalDirFile(acc, type);
        try (FileInputStream fsr = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(fsr, "UTF-8");) {

            PinterestObject[] objs = null;
            switch (type) {
                case duplicates_comment:
                case duplicates_pin:
                case duplicates_repin:
                case duplicates_like:
                    objs = gson.fromJson(reader, Pin[].class);
                    break;
                case duplicates_follow_pinners:
                case duplicates_unfollow_pinners:
                case duplicates_message:
                case duplicates_invite:
                case following:
                case followers:
                    objs = gson.fromJson(reader, Pinner[].class);
                    break;
                case duplicates_follow_boards:
                case duplicates_unfollow_boards:
                    objs = gson.fromJson(reader, Board[].class);
                case duplicates_follow:
                case duplicates_unfollow:
                    gson.fromJson(reader, Object[].class);
                    break;
            }

            if (objs != null && objs.length > 0) {
                set = new HashSet<>(Arrays.asList(objs));

                Iterator it = set.iterator();
                while (it.hasNext()) {
                    if (it.next() == null) {
                        it.remove();
                    }
                }
            }
        } catch (JsonSyntaxException | JsonIOException ex) {
            return fixJson2(type, acc, attempt); //make sure reader is closed before launching fix
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return set;
    }

    private synchronized Set<PinterestObject> fixJson2(TYPES type, Account acc, int attempt) {
        if (attempt >= 2) {
            return null;
        }
        File file = externalDirFile(acc, type);
        try (FileReader reader = new FileReader(file);
                BufferedReader br = new BufferedReader(reader);) {

            Set<PinterestObject> set = new HashSet<>();

            List<String> st = new ArrayList<>();
            String line, merged;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("[") || line.equalsIgnoreCase("]")) {
                    continue;
                }
                st.add(line);
                if (line.equalsIgnoreCase("}") || line.equalsIgnoreCase("},")) {
                    PinterestObject obj = null;
                    merged = "";
                    while (st.size() > 0) {
                        merged += st.get(0);
                        st.remove(0);
                    }
                    merged = merged.replace("},", "}");
                    try {
                        switch (type) {
                            case duplicates_comment:
                            case duplicates_pin:
                            case duplicates_repin:
                            case duplicates_like:
                                obj = gson.fromJson(merged, Pin.class);
                                break;
                            case duplicates_follow_pinners:
                            case duplicates_unfollow_pinners:
                            case duplicates_message:
                            case duplicates_invite:
                            case following:
                            case followers:
                                obj = gson.fromJson(merged, Pinner.class);
                                break;
                            case duplicates_follow_boards:
                            case duplicates_unfollow_boards:
                                obj = gson.fromJson(merged, Board.class);
                            default:
                                gson.fromJson(merged, Object.class);
                                break;
                        }
                        if (obj != null) {
                            set.add(obj);
                        }
                    } catch (Exception ex) {
                        //ignore : things may go wrong here due syntax faults
                    }
                }
            }
            br.close();
            reader.close();
            saveToExternalFile(file, set);
            return set;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public synchronized void appendExternalObject(PinterestObject obj, TYPES type, Account acc) {
        try {
            Set<PinterestObject> set = new HashSet<>();
            set.add(obj);
            storeObjectsExternally(set, type, acc, true);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    public synchronized void clearObjectsExernally(TYPES type, Account acc) {
        try {
            HashSet<PinterestObject> empty = new HashSet<>();
            storeObjectsExternally(empty, type, acc, false);
            File file = externalDirFile(acc, type);
            saveToExternalFile(file, empty);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    public synchronized void storeObjectsExternally(Set<PinterestObject> objs, TYPES type, Account acc, boolean append) throws IOException {
        if (objs.size() > 0) {
            if (append) {
                Set<PinterestObject> append_objs = getExternalObjects(type, acc, 1);
                for (PinterestObject obj : append_objs) {
                    objs.add(obj);
                }
            }
            File file = externalDirFile(acc, type);
            saveToExternalFile(file, objs);
        }
    }

    private synchronized void saveToExternalFile(File file, Set<PinterestObject> objs) throws FileNotFoundException, IOException {
        File tmp = new File(file.getAbsolutePath() + "_tmp"); //save JSON to temp file
        try (FileOutputStream fos = new FileOutputStream(tmp, false)) {
            try (OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8"); BufferedWriter out = new BufferedWriter(osw)) {
                gson.toJson(objs, out);
            }
        }

        Files.delete(file.toPath()); // if nothing went wrong with serialization, then we can delete original file
        tmp.renameTo(file);         //  and rename temp file to original file.
        //this should prevent EOFExceptions and malformated JSONs

    }

}
