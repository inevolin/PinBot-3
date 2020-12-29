/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.scene.control.Label;
import pinbot3.PinBot3;

/**
 *
 * @author UGent
 */
public class MyUtils {

    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int extensionPos = filename.lastIndexOf('.');
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);

        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        if (index == -1) {
            return "";
        } else {
            return filename.substring(index + 1);
        }
    }

    public static String stripHtmlTags(String str) {
        str = str.replaceAll("<.*?>", "");
        return str;
    }

    public static String jsonUpperCase(String str) {
        Pattern p = Pattern.compile("(%\\d)(\\w)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
        Matcher m = p.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static File downloadTemporaryFile(String path, Http http) throws IOException, Exception {

        String pth = PinBot3.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String decodedPath = URLDecoder.decode(pth, "UTF-8");
        File jarLoc = new File(decodedPath);
        String tmpLoc = jarLoc.getParentFile().getAbsolutePath() + "\\tmp";
        File tmpDir = new File(tmpLoc);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        File tmp = File.createTempFile("pb3", "." + MyUtils.getExtension(path), tmpDir);
        tmp.deleteOnExit(); // file will auto-delete upon program exit, but we will try to delete it after using it.        

        http.downloadFile(path, tmp.getAbsolutePath());

        return tmp;

    }

    public static String db3Path() {
        String db3 = null;
        try {
            db3 = execPath(false) + File.separatorChar + "db3" + File.separatorChar;
            db3 = fix_for_IDE(db3);
        } catch (UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);
            db3 = "." + File.separatorChar + "db3" + File.separatorChar;;
        }
        return db3;
    }

    @Deprecated
    public static String db2Path() {
        String db3 = null;
        try {
            db3 = execPath(false) + File.separatorChar + "db" + File.separatorChar;
            db3 = fix_for_IDE(db3);
        } catch (UnsupportedEncodingException ex) {
            common.ExceptionHandler.reportException(ex);
            db3 = "." + File.separatorChar + "db3" + File.separatorChar;;
        }
        return db3;
    }

    private static String fix_for_IDE(String path) {
        path = path.replaceAll(".dist.run\\d+", "");
        path = path.replaceAll("build.db3", "db3");
        return path;
    }

    public static String execPath(boolean fix_for_IDE) throws UnsupportedEncodingException {
        String execPath = null;

        File f = new File(System.getProperty("java.class.path"));
        File dir = f.getAbsoluteFile().getParentFile();
        String path = dir.toString();
        execPath = path;
        
        if (fix_for_IDE) {
            execPath = fix_for_IDE(execPath);
        }
        return execPath;
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static String spintax(Random rnd, String str) {
        String pat = "\\{[^{}]*\\}";
        Pattern ma;
        ma = Pattern.compile(pat);
        Matcher mat = ma.matcher(str);
        while (mat.find()) {
            String segono = str.substring(mat.start() + 1, mat.end() - 1);
            String[] choies = segono.split("\\|", -1);
            str = str.substring(0, mat.start()) + choies[rnd.nextInt(choies.length)].toString() + str.substring(mat.start() + mat.group().length());
            mat = ma.matcher(str);
        }
        return str;
    }

    public static void logToFile(String msg) {
        try (FileWriter fw = new FileWriter(execPath(false) + File.separatorChar + "logging.txt", true); //version 3
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
            out.println(msg);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
            System.err.println("Error writing log.log \n " + ex.getMessage());
        }
    }

    public static void attemptRun(String fileName) {
        try {
            File jarfile = new File((execPath(false) + File.separatorChar + fileName));
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", jarfile.getAbsolutePath());
            Process p = pb.start();
        } catch (SecurityException | IllegalArgumentException ex) {
            common.ExceptionHandler.reportException(ex);
        } catch (MalformedURLException ex) {
            common.ExceptionHandler.reportException(ex);
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    public static void setLbl(String msg, Label lbl) {
        if (lbl != null) {
            Platform.runLater(() -> lbl.setText(msg));
        }
    }

    public static String getRegistryByKey(String key) {
        try {
            Gson gson = new Gson();
            String filePath = "." + File.separatorChar + "props";
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                String c = new String(Files.readAllBytes(path));
                LinkedTreeMap props = gson.fromJson(c, LinkedTreeMap.class);
                if (props == null) {
                    props = new LinkedTreeMap();
                }
                if (props.containsKey(key)) {
                    return (String) props.get(key);
                }
            }
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public static void storeRegistry(String key, String val) {
        try {
            Gson gson = new Gson();
            String filePath = "." + File.separatorChar + "props";
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
            String c = new String(Files.readAllBytes(path));
            LinkedTreeMap props = gson.fromJson(c, LinkedTreeMap.class);
            if (props == null) {
                props = new LinkedTreeMap();
            }
            props.put(key, val);
            String write = gson.toJson(props);
            Files.write(path, write.getBytes());

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
    }

    public static long getDirectorySize(Path path) {
        try {
            long bytes = 0L;
            Object[] arr = Files.walk(path, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).map(Path::toFile).toArray();
            for (Object o : arr) {
                File f = (File) o;
                bytes += Files.size(f.toPath());
            }
            return bytes;
        } catch (IOException ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return 0L;
    }

}
