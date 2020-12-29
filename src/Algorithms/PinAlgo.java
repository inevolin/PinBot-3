/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */   // test
package Algorithms;

import Managers.ScrapeManager;
import common.Data;
import common.Http;
import common.KeyValuePair;
import common.MyUtils;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.PinConfiguration;
import model.pinterestobjects.Board;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

/**
 *
 * @author UGent
 */
public class PinAlgo extends Algo {

    private PinConfiguration pinConfig;

    public PinAlgo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        super(config, acc, sm);
        pinConfig = (PinConfiguration) config;
    }

    @Override
    public void run() {
        try {

            removeNonExistingImages(pinConfig.getQueue()); //!!!
            if ((pinConfig.getQueue() == null || pinConfig.getQueue().isEmpty()) && (pinConfig.getQueries() != null && pinConfig.getQueries().size() > 0)) {
                Map<PinterestObject, Board> scraped = scrapeManager.Scrape(config, account, null);
                if (scraped != null) {
                    processOne(scraped);
                }
            } else if (pinConfig.getQueue().size() > 0) {
                // permanent queue is first priority
                processOne(pinConfig.getQueue());
            } else if (pinConfig.getQueries() == null || pinConfig.getQueries().size() == 0) {
                //if we have no queries mapped and queue already finished; let's stop.
                super.config.CountTotal = super.config.CountCompleted;
            }

        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (ExecutionException ex) {
            common.ExceptionHandler.reportException(ex);            
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        finalize();
    }

    private void removeNonExistingImages(Set<Pin> lst) {
        Set<Pin> imgs = new HashSet<>(lst);
        for (Pin p : imgs) {
            try {
                processPinURL(p);
                File file = new File(p.getPinUrl());
                if (!file.exists()) {
                    lst.remove(p);
                }
            } catch (Exception ex) {
                common.ExceptionHandler.reportException(ex);
            }
        }
    }

    /*  Perform pin action and remove object from list OR set 'previouslyPinned' to current time if pin is permanentaly stored. */
    @Override
    protected void processOne(Map<PinterestObject, Board> lst) {
        processOne((Set) lst.keySet());
    }

    protected void processOne(Set<Pin> lst) {
        if (lst == null) {
            prematureFinish = true;
            return;
        } else if (lst.isEmpty()) {
            return;
        }

        Pin pin = (Pin) lst.toArray()[0];
        Board board = account.findBoard(pin.getDestinationBoardId());
        while (board == null) {
            // our board does not exist, most likely deleted
            addToDups(pin);
            lst.remove(pin);
            if (lst.isEmpty())
                return;
            pin = (Pin) lst.toArray()[0];
            board = account.findBoard(pin.getDestinationBoardId());
        }

        Date now = new Date();
        if (pin.getPreviouslyPinned() == null) { //freshly scraped OR non-permanent pins.
            if (doPin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
                pin.setPreviouslyPinned(now);
                addToDups(pin);
                lst.remove(pin);
            }
        } else if (pin.getPreviouslyPinned().getTime() + pin.getTimeBeforeNextPin() < now.getTime()) { //permanentally stored pins
            //pin only if enough time has passed since previously pinned!
            if (doPin(pin, board) || pin.getAttempts() == MAX_ATTEMPTS) {
                pin.setPreviouslyPinned(now);
                addToDups(pin);
            }
        }
    }
    
    private class Upload_Failed_Exception extends Exception {}

    private Boolean doPin(Pin pin, Board board) {
        pin.setAttempts(pin.getAttempts() + 1);
        File tmp = null;
        try {
            tmp = processPin(pin);
            if (tmp == null) {
                return true; //image doesn't exist or upload failed => remove from queue.
            }
            Data data = GetData(pin, board);
            //System.err.println("_116:" + data.content);

            KeyValuePair<Integer> resp = http.Post(
                    base_url + "/resource/PinResource/create/", data.content, getPinHeaders(data.referer));

            //System.err.println("_125: " + resp);
            return ProcessResponse(resp, this.account);

        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (UnknownHostException ex) {
            config.ErrorCount++;
        } catch (Upload_Failed_Exception ex) {
            //ignore
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);            
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
        return false; // no success
    }

    private File processPin(Pin pin) throws IOException, Exception {
        File tmp = null;
        tmp = processPinURL(pin);
        //System.err.println("_145_1:" + tmp);
        String newUrl = uploadImage(pin.getPinUrl());
        //System.err.println("_145_2:" + newUrl);
        if (newUrl == null) {
            return null; //upload failed
        }
        pin.setPinUrl(newUrl);
        return tmp;
    }

    private File processPinURL(Pin pin) throws IOException, Exception {
        File tmp = null;
        if (pin.getHashUrl().contains("http")) {
            String url = pin.getHashUrl();
            //System.err.println("_141:" + url);
            tmp = MyUtils.downloadTemporaryFile(url, http);
            //System.err.println("_142:" + tmp);
            pin.setPinUrl(tmp.getAbsolutePath());
        } else {
            try {
                String decodedPath = URLDecoder.decode(pin.getHashUrl(), "UTF-8");
                //System.err.println("_145:" + decodedPath);
                pin.setPinUrl(decodedPath.replace('/', File.separatorChar).replace("file:\\", ""));
                tmp = new File(pin.getPinUrl());
            } catch (Exception ex) {
                // System.err.println("_184" + ex.getMessage());
            }
        }
        return tmp;
    }

    public String uploadImage(String path) {
        try {
            String boundary = Http.getBoundary();

            File file = new File(path);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addTextBody("qquuid", UUID.randomUUID().toString(), ContentType.TEXT_PLAIN);
            builder.addTextBody("qqfilename", file.getName(), ContentType.TEXT_PLAIN);
            builder.addTextBody("qqtotalfilesize", Long.toString(file.length()), ContentType.TEXT_PLAIN);

            String filename = file.getName();
            String ext = MyUtils.getExtension(path).replace(".", "").toLowerCase();
            ext = ext.equals("jpg") ? "jpeg" : ext;
            builder.addBinaryBody("img", file, ContentType.create("image/" + ext), filename);

            HttpEntity multipart = builder.build();

            String url = base_url + "/upload-image/";
            KeyValuePair<Integer> resp = http.PostFile(url, multipart, getUploadHeaders(base_url, boundary, file.getName()));

            if (resp.getValue() != 200) {
                throw new Upload_Failed_Exception();
            }

            url = null;
            Pattern pattern = Pattern.compile("http(?:s*?)://(.+?)\\\",", Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(resp.getKey());
            while (matcher.find()) {
                url = "https://" + matcher.group(1);
            }

            return url;

        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (UnknownHostException ex) {
            config.ErrorCount++;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private Map<String, String> getUploadHeaders(String referer, String boundary, String filename) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", account.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-Pinterest-AppState", "active");
        headers.put("Cache-Control", "no-cache");
        headers.put("Referer", referer);
        headers.put("Origin", base_url);
        headers.put("X-File-Name", filename);
        headers.put("Pragma", "no-cache");
        headers.put("Accept", Http.ACCEPT_ANY);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private Map<String, String> getPinHeaders(String referer) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        headers.put("X-CSRFToken", account.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-Pinterest-AppState", "active");
        headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");
        headers.put("Referer", referer);
        headers.put("Accept", Http.ACCEPT_JSON);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    @Override
    protected Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException {
        Data data = new Data();
        Pin p = (Pin) obj;

        //"board_id":"458030293291498379"},"context":{}}&module_path=
        /*switch (p.getResource()) {
            case External:*/
        String sourceurl = ("/" + board.getUrlName() + "/").replace("//", "/");
        data.content = "source_url="
                + URLEncoder.encode(sourceurl, StandardCharsets.UTF_8.toString())
                + "&data="
                + URLEncoder.encode("{\"options\":{"
                        + "\"method\":\"uploaded\""
                        + ",\"description\":\"" + p.getDescription().replaceAll("\n", "\\\\n").replace("\"", "\\\"") + "\",\"link\":\"" + p.getSourceUrl() + "\""
                        + ",\"image_url\":\"" + p.getPinUrl() + "\""
                        + ",\"board_id\":\"" + board.getBoardId() + "\""
                        + "},\"context\":{}}", StandardCharsets.UTF_8.toString())
                + "&module_path="
                + URLEncoder.encode("App>ModalManager>Modal>PinCreate>PinCreateBoardPicker>BoardPicker>SelectList(view_type=pinCreate, selected_section_index=undefined, selected_item_index=undefined, highlight_matched_text=true, suppress_hover_events=undefined, scroll_selected_item_into_view=true, select_first_item_after_update=false, item_module=[object Object])", StandardCharsets.UTF_8.toString());

        data.referer = base_url + "" + sourceurl;
        /*break;
        }*/
        return data;
    }

}
