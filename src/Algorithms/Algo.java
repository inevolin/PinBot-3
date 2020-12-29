/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * */
package Algorithms;

import Managers.ScrapeManager;
import common.Data;
import common.Http;
import common.KeyValuePair;
import common.MyLogger;
import common.PinterestErrors;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.CommentConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author UGent
 */
public abstract class Algo implements Runnable {

    protected AConfiguration config;
    protected Http http;
    protected Account account;
    protected ScrapeManager scrapeManager;
    protected String base_url;
    protected Boolean prematureFinish = false;
    protected static int MAX_ATTEMPTS = 2; //if first time fails, try again, then skip it.

    public Algo(AConfiguration config, Account acc, ScrapeManager sm) throws Exception {
        this.config = config;
        this.account = acc;
        this.http = new Http(acc.getProxy(), acc.getCookieStore()); // we define a new Http object BECAUSE the HTTP protocol is NOT thread-safe!
        this.scrapeManager = sm;
        this.base_url = this.account.base_url;
    }

    protected abstract Data GetData(PinterestObject obj, Board board) throws UnsupportedEncodingException;

    protected Boolean ProcessResponse(KeyValuePair<Integer> response, Account acc) {
        pinbot3.PinBot3.myLogger.log(acc, acc.getEmail() + ", response:\n" + response.getValue() + "\n" + response.getKey() + "\n\n", MyLogger.LEVEL.fine);
        String rs = response.getKey();
        if (rs.contains(PinterestErrors.NO_ERROR)) {
            pinbot3.PinBot3.myLogger.log(account, "Success action " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            if (config.CountCompleted != null) {
                ++config.CountCompleted;
            }
            return true;
        } else if (rs.contains(PinterestErrors.STRANGE_ACTIVITY)) {
            pinbot3.PinBot3.myLogger.log(account, "Strange activity detected " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            account.setStatus(Account.STATUS.BLOCKED);
            pinbot3.PinBot3.campaignMgr.StopAccount(acc); //banned/blocked: stop everything !
        } else if (rs.contains(PinterestErrors.AUTHORIZATION_ERROR)) {
            pinbot3.PinBot3.myLogger.log(account, "Authortization error " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            account.setStatus(Account.STATUS.UNAUTHORIZED);
        } else if (rs.contains(PinterestErrors.PINNER_ALREADY_INVITED)) {
            // no action.
            return true;
        } else if (response.getValue() >= 400 && response.getValue() < 500) {
            pinbot3.PinBot3.myLogger.log(account, "ERR " + response.getValue() + " " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            config.ErrorCount++;
        }        
        return false;
    }

    protected abstract void processOne(Map<PinterestObject, Board> lst);

    protected void addToDups(PinterestObject obj) {
        //account.getDuplicates(config).add(obj);
        //testThis
        account.addDuplicate(config, obj);
        // save account... by timer.
    }

    protected String MakeRequest(String url, String referer, String ctype) throws InterruptedException {
        try {
            if (!Http.validUrl(url)) {
                return null;
            }

            KeyValuePair<Integer> resp;
            Map<String, String> headers = null;
            if (ctype.equals(Http.ACCEPT_JSON)) {
                headers = new HashMap<>(getHeadersJSON(referer, ctype));
            } else {
                headers = new HashMap<>(getHeadersRegular(ctype));
            }
            resp = http.Get(url, headers);

            if (resp.getValue() == 200) {
                String rs = resp.getKey();
                if (ctype.equals(Http.ACCEPT_JSON)) {
                    rs = rs.replace("\\", "");
                }
                return rs;
            } else {
                return null;
            }
        } catch (InterruptedException ex) {
            //ignore
            config.isInterrupt = true;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);

        }
        return null;
    }

    private Map<String, String> getHeadersRegular(String accept) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("Accept", accept);
        headers.put("User-Agent", Http.USER_AGENT);

        return headers;
    }

    private Map<String, String> getHeadersJSON(String referer, String accept) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-NEW-APP", "1");
        headers.put("X-APP-VERSION", account.getAppVersion());
        headers.put("X-Requested-With", "XMLHttpRequest");
        //headers.put("X-CSRFToken", account.getCsrf());
        headers.put("Accept-Encoding", "gzip,deflate,sdch");
        headers.put("Accept-Language", "en-US,en;q=0.8");
        headers.put("X-Pinterest-AppState", "active");
        /*headers.put("Cache-Control", "no-cache");
        headers.put("Pragma", "no-cache");*/
        headers.put("Referer", referer);
        headers.put("User-Agent", Http.USER_AGENT);
        headers.put("Accept", accept);

        return headers;
    }

    protected void finalize() {
        if (config.isInterrupt) {
            //don't touch status!
            return;
        } else if (prematureFinish) {
            //config.runNext = AutopilotOrDefault(config, true);            
            if (config.status != AConfiguration.RunStatus.SLEEPING) {
                pinbot3.PinBot3.myLogger.log(account, "Premature finish " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
                config.status = AConfiguration.RunStatus.PREMATURE_FINISH;
            }
            return;
        }

        if (config.ErrorCount >= AConfiguration.MaxErrorCount) {
            config.status = AConfiguration.RunStatus.ERROR;
        } else if (config.CountCompleted >= config.CountTotal) {
            config.runNext = AutopilotOrDefault(config, true);
            if (config.status != AConfiguration.RunStatus.SLEEPING) {
                pinbot3.PinBot3.myLogger.log(account, "Finished " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
                config.status = AConfiguration.RunStatus.FINISHED;
            } else {
                pinbot3.PinBot3.myLogger.log(account, "Finished, going to sleep " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            }
            return;
        }

        if (config.status == AConfiguration.RunStatus.ERROR) {
            pinbot3.PinBot3.myLogger.log(account, "Too many errors, finish, " + this.getClass().getSimpleName(), MyLogger.LEVEL.info);
            return;
        }

        Random r = new Random();
        int delta = config.getTimeoutMax() - config.getTimeoutMin() + 1;//[min,max]
        int amount = r.nextInt(delta <= 0 ? 1 : delta) + config.getTimeoutMin();

        LocalDateTime next = LocalDateTime.now();
        next = next.plusSeconds(amount);//timeout in seconds
        config.runNext = next;
        config.status = AConfiguration.RunStatus.TIMEOUT;

    }

    public static LocalDateTime AutopilotOrDefault(AConfiguration config, Boolean finished) {
        LocalDateTime next = null;
        if (config.getAutopilot()) {
            LocalDateTime nextRun = config.getAutopilotStart().atDate(LocalDate.now());
            LocalDateTime now = LocalDateTime.now();

            if (now.isAfter(nextRun)) {
                //in case it is             17:30 and has to start at 17:00 (first run, next day).
                //in case it finished at    17:30 and has to restart at 17:00 (next day).
                next = nextRun;
                next = next.plusDays(1);
            } else if (now.isBefore(nextRun)) {
                //in case it finished at    17:00 and has to restart at 17:30 (same day).
                //in case it finished at    02:00 and has to restart at 23:00 (same day).
                //in case it is             16:59 and has to start at 17:00 (first run, same day).
                //in case it is             17:00 and has to start at 17:10 (first run, same day).                
                next = nextRun;
            }
            config.status = AConfiguration.RunStatus.SLEEPING;
        }
        return next;
    }

    public static void initActionLimits(AConfiguration cf) {
        cf.CountCompleted = 0;
        Random r = new Random();
        int delta = cf.getActionMax() - cf.getActionMin() + 1;
        cf.CountTotal = r.nextInt(delta <= 0 ? 1 : delta) + cf.getActionMin();
        if (cf instanceof CommentConfiguration) {
            int cnt = countIndividualQueries(cf);
            if (cf.getQueries().size() == cnt) {
                cf.CountTotal = cnt;
            }
        } else if (cf instanceof PinConfiguration) {
            PinConfiguration pinConfig = (PinConfiguration) cf;
            if (pinConfig.getQueries() == null || pinConfig.getQueries().size() == 0) {
                cf.CountTotal = pinConfig.getQueue().size();
            }
            int cnt = countIndividualQueries(cf);
            if (cf.getQueries().size() == cnt && cnt > 0) {
                cf.CountTotal = cnt;
            }
        } else if (cf instanceof RepinConfiguration) {
            RepinConfiguration repinConfig = (RepinConfiguration) cf;
            if (repinConfig.getQueries() == null || repinConfig.getQueries().size() == 0) {
                cf.CountTotal = repinConfig.getQueue().size();
            }
            int cnt = countIndividualQueries(cf);
            if (cf.getQueries().size() == cnt && cnt > 0) {
                cf.CountTotal = cnt;
            }
        }
    }

    private static int countIndividualQueries(AConfiguration cf) {
        int i = 0;
        for (AQuery q : cf.getQueries()) {
            if (q.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                ++i;
            }
        }
        return i;
    }
}
