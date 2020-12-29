/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Managers;

import java.util.ArrayList;
import model.configurations.queries.AQuery;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import model.Account;
import model.configurations.AConfiguration;
import model.configurations.CommentConfiguration;
import model.configurations.FollowConfiguration;
import model.configurations.InviteConfiguration;
import model.configurations.LikeConfiguration;
import model.configurations.MessageConfiguration;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;
import model.configurations.UnfollowConfiguration;
import scraping.ScrapeSession;
import model.pinterestobjects.Board;
import model.pinterestobjects.Category;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;
import scraping.sources.IndividualPin;
import scraping.sources.IndividualUser;
import scraping.sources.ScrapeBoards_RepinnedOntoBoards;
import scraping.sources.ScrapeBoards_BoardFollowingResource;
import scraping.sources.ScrapeBoards_ProfileBoardsResource;
import scraping.sources.ScrapeBoards_SearchResource;
import scraping.sources.ScrapeBoards_UserResource;
import scraping.sources.ScrapeCategories;
import scraping.sources.ScrapeInterest_InterestFollowingResource;
import scraping.sources.ScrapePinComments;
import scraping.sources.ScrapePinners_BoardFollowersResource;
import scraping.sources.ScrapePinners_PinLikedBy;
import scraping.sources.ScrapePinners_SearchResource;
import scraping.sources.ScrapePinners_UserFollowersResource;
import scraping.sources.ScrapePinners_UserFollowingResource;
import scraping.sources.ScrapePins_BoardFeedResource;
import scraping.sources.ScrapePins_SearchResource;
import scraping.sources.ScrapePins_UserPinsResource;

/*
    Everything related to handling scrape operations.
    If ScrapeManager returns a NULL from 'Scrape()' this means NOTHING is found.
    In this case the configuration should either stop prematurely OR go into sleeping mode.
 */
public class ScrapeManager {

    private ExecutorService executor;
    private ConcurrentHashMap<AConfiguration, Set<ScrapeSession>> chm;

    public ScrapeManager(ExecutorService executor) {
        this.executor = executor;
        this.chm = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<AConfiguration, Set<ScrapeSession>> getChm() {
        return chm;
    }


    /*
        We should un-blacklist all queries when Autopilot kicks in. (todo)
     */
    public Map<PinterestObject, Board> Scrape(AConfiguration config, Account acc, String extra) throws InterruptedException, ExecutionException {
        ScrapeSession found = determineScrapeSession(config, acc, extra);

        //if not found OR every single Query is blacklisted.
        if (found == null) {
            return null;
        }

        if (found.getFound().isEmpty()) {
            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();
            Map<PinterestObject, Board> getFound = ret.getFound();
            if (getFound.isEmpty() && found.getEndReached()) {
                found.getQuery().setBlacklisted(Boolean.TRUE);
            }
            while ((getFound == null || getFound.isEmpty()) && found != null) {
                found = determineScrapeSession(config, acc, extra);
                if (found == null) {
                    return null;
                }
                worker = found;
                future = executor.submit(worker);
                ret = future.get();
                getFound = ret.getFound();
                if (getFound.isEmpty() && found.getEndReached()) {
                    found.getQuery().setBlacklisted(Boolean.TRUE);
                }
            }
            return getFound;
        } else {
            return found.getFound();
        }
    }

    private AQuery getRndQuery(AConfiguration config) {
        Random r = new Random();
        
        if (config.getQueries().isEmpty())
            return null;
        
        int index = r.nextInt(config.getQueries().size());
        AQuery qry = config.getQueries().get(index);

        //just in case the randomly selected Query is blacklisted, let's find another one.
        //if no non-blacklisted query can be found, return NULL.        
        int j = 0;
        while (qry.getBlacklisted() != null && qry.getBlacklisted() == true && j < config.getQueries().size()) {
            qry = config.getQueries().get(j++);
        }

        if (qry.getBlacklisted() != null && qry.getBlacklisted()) {
            return null;
        } else {
            return qry;
        }
    }

    private ScrapeSession getIfAlreadyDefined(AConfiguration config, AQuery qry) {
        //return null;
        // PERF ISSUE (?) 
        /**/
        if (chm.containsKey(config)) {
            Set<ScrapeSession> set = chm.get(config);
            Random r = new Random();
            List<ScrapeSession> randomList = new ArrayList<>();
            Iterator<ScrapeSession> it = set.iterator();
            while (it.hasNext()) {
                ScrapeSession ss = it.next();
                if (ss != null && ss.getQuery().getQuery().equalsIgnoreCase(qry.getQuery())) {
                    randomList.add(ss);
                }
            }
            if (randomList.size() > 0) {
                return randomList.get(r.nextInt(randomList.size()));
            }

        }
        return null;
        /**/
    }

    private ScrapeSession determineScrapeSession(AConfiguration config, Account acc, String extra) {
        if (config instanceof PinConfiguration) {
            return PinConfig(config, acc);
        } else if (config instanceof RepinConfiguration) {
            return RepinConfig(config, acc);
        } else if (config instanceof LikeConfiguration) {
            return LikeConfig(config, acc);
        } else if (config instanceof InviteConfiguration) {
            return InviteConfig(config, acc);
        } else if (config instanceof CommentConfiguration) {
            return CommentConfig(config, acc);
        } else if (config instanceof FollowConfiguration) {
            return FollowConfig(config, acc);
        } else if (config instanceof UnfollowConfiguration) {
            return UnfollowConfig(config, acc, extra);
        } else if (config instanceof MessageConfiguration) { //misc
            return MessageConfig(config, acc);
        }
        return null;
    }

    private ScrapeSession PinConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.SearchResource) {
                existing = new ScrapePins_SearchResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.UserPinsResource) {
                existing = new ScrapePins_UserPinsResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFeedResource) {
                existing = new ScrapePins_BoardFeedResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                existing = new IndividualPin(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.External) {
                existing = new ScrapePins_SearchResource(acc, qry, config);
            }

            // PERF ISSUE (?) START
            /*if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);*/
            // PERF ISSUE (?) END
            
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession RepinConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.SearchResource) {
                existing = new ScrapePins_SearchResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.UserPinsResource) {
                existing = new ScrapePins_UserPinsResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFeedResource) {
                existing = new ScrapePins_BoardFeedResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                existing = new IndividualPin(acc, qry, config);
            }

            // PERF ISSUE (?) S
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);
            */
            // PERF ISSUE (?) END
            
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession LikeConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.SearchResource) {
                existing = new ScrapePins_SearchResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.UserPinsResource) {
                existing = new ScrapePins_UserPinsResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFeedResource) {
                existing = new ScrapePins_BoardFeedResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                existing = new IndividualPin(acc, qry, config);
            }

            // PERF ISSUE (?) 
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);
            */
            
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession InviteConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFollowersResource) {
                existing = new ScrapePinners_BoardFollowersResource(acc, qry, config);
            }

            // PERF ISSUE (?) 
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);
            */
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession CommentConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.SearchResource) {
                existing = new ScrapePins_SearchResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.UserPinsResource) {
                existing = new ScrapePins_UserPinsResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFeedResource) {
                existing = new ScrapePins_BoardFeedResource(acc, qry, config);
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.IndividualPin) {
                existing = new IndividualPin(acc, qry, config);
            }

            // PERF ISSUE (?) 
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);
            */
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession MessageConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            } else if (qry.getResource() == PinterestObject.PinterestObjectResources.UserFollowersResource) {
                existing = new ScrapePinners_UserFollowersResource(acc, qry, config);
            }

            // PERF ISSUE (?) 
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            chm.get(config).add(existing);
            */
            return existing;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession FollowConfig(AConfiguration config, Account acc) {
        //take random from queries
        // !!!!!!!!!!!!!
        //   ANY ALGO that asks for scrape, must make sure there are queries first !!!
        //   THIS MUST BE DONE IN Algo !   if (config.getQueries()!=null&&config.getQueries().size() > 0)
        // !!!!!!!!!!!!!
        try {
            AQuery qry = getRndQuery(config);
            if (qry == null) {
                return null;
            }
            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            }

            ScrapeSession ses1 = existing, ses2 = null;
            if (((FollowConfiguration) config).getFollowBoards() != null && ((FollowConfiguration) config).getFollowBoards()) {
                if (null != qry.getResource()) {
                    switch (qry.getResource()) {
                        case BoardFollowingResource:
                            ses1 = new ScrapeBoards_BoardFollowingResource(acc, qry, config);
                            break;
                        case ProfileBoardsResource:
                            ses1 = new ScrapeBoards_ProfileBoardsResource(acc, qry, config);
                            break;
                        case SearchResource:
                            ses1 = new ScrapeBoards_SearchResource(acc, qry, config);
                            break;
                        case RepinFeedResource:
                            ses1 = new ScrapeBoards_RepinnedOntoBoards(acc, qry, config);
                            break;
                        default:
                            break;
                    }
                }
            }

            if (((FollowConfiguration) config).getFollowUsers() != null && ((FollowConfiguration) config).getFollowUsers()) {
                if (null != qry.getResource()) {
                    switch (qry.getResource()) {
                        case BoardFollowersResource:
                            ses2 = new ScrapePinners_BoardFollowersResource(acc, qry, config);
                            break;
                        case SearchResource:
                            ses2 = new ScrapePinners_SearchResource(acc, qry, config);
                            break;
                        case IndividualUser:
                            ses2 = new IndividualUser(acc, qry, config);
                            break;
                        case UserFollowersResource:
                            ses2 = new ScrapePinners_UserFollowersResource(acc, qry, config);
                            break;
                        case UserFollowingResource:
                            ses2 = new ScrapePinners_UserFollowingResource(acc, qry, config);
                            break;
                        case PinLikesResource:
                            ses2 = new ScrapePinners_PinLikedBy(acc, qry, config);
                            break;
                        default:
                            break;
                    }
                }
            }

            // PERF ISSUE (?) 
            
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            } else if (chm.get(config).size() >= 10) {                
                //an exception for follow configuration
                //let us maintain max 10 scrapesessions per configuration
                ScrapeSession rem = chm.get(config).iterator().next(); //pop a random one from collection before adding new one
                chm.get(config).remove(rem);
            }
            if (ses1 != null) {
                chm.get(config).add(ses1);
            }
            if (ses2 != null) {
                chm.get(config).add(ses2);
            }
            
            return ses1 == null ? ses2 : ses1;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    private ScrapeSession UnfollowConfig(AConfiguration config, Account acc, String U_or_B) {
        try {
            AQuery qry = null;
            for (AQuery specificQ : config.getQueries()) {
                if (U_or_B.equals("B") && specificQ.getResource() == PinterestObject.PinterestObjectResources.BoardFollowingResource) {
                    qry = specificQ;
                    break;
                } else if (U_or_B.equals("U") && specificQ.getResource() == PinterestObject.PinterestObjectResources.UserFollowingResource) {
                    qry = specificQ;
                    break;
                }
            }

            ScrapeSession existing = getIfAlreadyDefined(config, qry);
            if (existing != null) {
                return existing;
            }

            // PERF ISSUE (?) 
            /*
            if (!chm.containsKey(config)) {
                chm.put(config, new HashSet<>());
            }
            */

            if (U_or_B.equals("B")) {
                if (((UnfollowConfiguration) config).getUnfollowBoards() != null && ((UnfollowConfiguration) config).getUnfollowBoards()) {
                    if (qry.getResource() == PinterestObject.PinterestObjectResources.BoardFollowingResource) {
                        existing = new ScrapeBoards_BoardFollowingResource(acc, qry, config);
                    }
                }
            } else if (U_or_B.equals("U")) {
                if (((UnfollowConfiguration) config).getUnfollowUsers() != null && ((UnfollowConfiguration) config).getUnfollowUsers()) {
                    if (qry.getResource() == PinterestObject.PinterestObjectResources.UserFollowingResource) {
                        existing = new ScrapePinners_UserFollowingResource(acc, qry, config);
                    }
                }
            }
            // PERF ISSUE (?) 
            /*
            chm.get(config).add(existing);
            */
            return existing;

        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Map<PinterestObject, Board> ScrapeComments(AConfiguration config, Account acc, Pin pin) throws InterruptedException, ExecutionException {
        try {
            ScrapePinComments session = new ScrapePinComments(acc, pin, config);

            Callable<ScrapeSession> worker = session;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();
            // ret.getEndReached() ? then worker.setBlacklist(true); (todo) (see todo above)
            Map<PinterestObject, Board> getFound = ret.getFound();
            /*while(getFound != null && getFound.isEmpty()) {
                getFound = this.Scrape(config, acc);
            }*/
            return getFound;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Set<Board> ScrapeUserBoards_1(Account acc) throws InterruptedException, ExecutionException {
        Set<Board> cast = new HashSet<>();
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.ProfileBoardsResource);
            ScrapeSession found = new ScrapeBoards_UserResource(acc, q, null);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            Set<PinterestObject> getFound = new HashSet<>();

            getFound.addAll(ret.getFound().keySet());
            if (getFound.size() > 0) {
                found.setFirstRequest(Boolean.FALSE);
                found.setEndReached(false);
                ret.getFound().clear();
            }
            while (ret.getFound() != null && !found.getEndReached()) {
                future = executor.submit(worker);
                ret = future.get();
                if (ret.getFound() != null) {
                    getFound.addAll(ret.getFound().keySet());
                    ret.getFound().clear();
                }
            }
            cast = (Set) getFound;
            return cast;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return cast;
    }

    public Set<Board> ScrapeUserBoards_2(Account acc) throws InterruptedException, ExecutionException {
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.ProfileBoardsResource);
            ScrapeSession found = new ScrapeBoards_ProfileBoardsResource(acc, q, null);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            Set<PinterestObject> getFound = new HashSet<>();

            getFound.addAll(ret.getFound().keySet());
            if (getFound.size() > 0) {
                found.setFirstRequest(Boolean.FALSE);
                found.setEndReached(false);
                ret.getFound().clear();
            }
            while (ret.getFound() != null && !found.getEndReached()) {
                future = executor.submit(worker);
                ret = future.get();
                if (ret.getFound() != null) {
                    getFound.addAll(ret.getFound().keySet());
                    ret.getFound().clear();
                }
            }
            Set<Board> cast = (Set) getFound;
            return cast;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Set<PinterestObject> ScrapeUserFollowersAccount(AConfiguration config, Account acc) throws InterruptedException, ExecutionException {
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.UserFollowersResource);
            ScrapeSession found = new ScrapePinners_UserFollowersResource(acc, q, config);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            // ret.getEndReached() ? then worker.setBlacklist(true); (todo) (see todo above)
            Set<PinterestObject> getFound = new HashSet<>();
            getFound.addAll(ret.getFound().keySet());
            while (ret.getFound() != null && !found.getEndReached()) {
                future = executor.submit(worker);
                ret = future.get();
                if (ret.getFound() != null) {
                    getFound.addAll(ret.getFound().keySet());
                    ret.getFound().clear();
                }
            }
            return getFound;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Set<PinterestObject> ScrapeUserFollowingAccount(AConfiguration config, Account acc) throws InterruptedException, ExecutionException {
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.UserFollowingResource);
            ScrapeSession found = new ScrapePinners_UserFollowingResource(acc, q, config);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            // ret.getEndReached() ? then worker.setBlacklist(true); (todo) (see todo above)
            Set<PinterestObject> getFound = new HashSet<>();
            getFound.addAll(ret.getFound().keySet());
            while (ret.getFound() != null && !found.getEndReached()) {
                future = executor.submit(worker);
                ret = future.get();
                if (ret.getFound() != null) {
                    getFound.addAll(ret.getFound().keySet());
                    ret.getFound().clear();
                }
            }
            return getFound;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Set<PinterestObject> ScrapeUserFollowingInterestsAccount(AConfiguration config, Account acc) throws InterruptedException, ExecutionException {
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.InterestFollowingResource);
            ScrapeSession found = new ScrapeInterest_InterestFollowingResource(acc, q, config);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            // ret.getEndReached() ? then worker.setBlacklist(true); (todo) (see todo above)
            Set<PinterestObject> getFound = new HashSet<>();
            getFound.addAll(ret.getFound().keySet());
            while (ret.getFound() != null && !found.getEndReached()) {
                future = executor.submit(worker);
                ret = future.get();
                if (ret.getFound() != null) {
                    getFound.addAll(ret.getFound().keySet());
                    ret.getFound().clear();
                }
            }
            return getFound;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

    public Set<Category> ScrapeBoardCategories(Account acc) throws InterruptedException {
        try {
            AQuery q = new AQuery(acc.getUsername(), null, PinterestObject.PinterestObjectResources.CategoriesResource);
            ScrapeSession found = new ScrapeCategories(acc, q, null);

            Callable<ScrapeSession> worker = found;
            Future<ScrapeSession> future = executor.submit(worker);
            ScrapeSession ret = future.get();

            Set<PinterestObject> getFound = new HashSet<>();

            getFound.addAll(ret.getFound().keySet());
            ret.getFound().clear();
            Set<Category> cast = (Set) getFound;
            return cast;
        } catch (InterruptedException ex) {
            //ignore
            throw ex;
        } catch (Exception ex) {
            common.ExceptionHandler.reportException(ex);
        }
        return null;
    }

}
