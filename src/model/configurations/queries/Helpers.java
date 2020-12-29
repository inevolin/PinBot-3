/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model.configurations.queries;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author healzer
 */
public class Helpers {

    public static AQuery typeByPatter_pin(String s, Board b) {
        //return new AQuery(s, b, PinterestObject.PinterestObjectResources.External);
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                put("^/(.+?)/pins/$", PinterestObject.PinterestObjectResources.UserPinsResource);
                put("^/pin/(.+?)/$", PinterestObject.PinterestObjectResources.IndividualPin);
                put("^/(.+?)/(.+?)/$", PinterestObject.PinterestObjectResources.BoardFeedResource);
                put("^(.+?)$", PinterestObject.PinterestObjectResources.SearchResource);
            }
        };
        return findExpr(s, b, mp);
    }

    public static AQuery typeByPatter_repin(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                put("^/(.+?)/pins/$", PinterestObject.PinterestObjectResources.UserPinsResource);
                put("^/pin/(.+?)/$", PinterestObject.PinterestObjectResources.IndividualPin);
                put("^/(.+?)/(.+?)/$", PinterestObject.PinterestObjectResources.BoardFeedResource);
                put("^(.+?)$", PinterestObject.PinterestObjectResources.SearchResource);
            }
        };
        return findExpr(s, b, mp);
    }

    public static AQuery typeByPatter_like(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                put("^/(.+?)/pins/$", PinterestObject.PinterestObjectResources.UserPinsResource);
                //todo individual pin: similar to repinAlgo ; if only indidivual pin queries, adjust ActionTotal !!! @override (todo)
                put("^/(.+?)/(.+?)/$", PinterestObject.PinterestObjectResources.BoardFeedResource);
                put("^(.+?)$", PinterestObject.PinterestObjectResources.SearchResource);
            }
        };
        return findExpr(s, b, mp);
    }
    
    public static AQuery typeByPatter_invite(String s, Board b) {
        return new AQuery(s, b, PinterestObject.PinterestObjectResources.BoardFollowersResource);
    }

    
    public static AQuery typeByPatter_comment(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                put("^/(.+?)/pins/$", PinterestObject.PinterestObjectResources.UserPinsResource);
                put("^/pin/(\\d+)/$", PinterestObject.PinterestObjectResources.IndividualPin);
                put("^/(.+?)/(.+?)/$", PinterestObject.PinterestObjectResources.BoardFeedResource);
                put("^(.+?)$", PinterestObject.PinterestObjectResources.SearchResource);
            }
        };
        return findExpr(s, b, mp);
    }

    
    public static AQuery typeByPatter_message(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                //this must be our user's username!!!
                put("^(.+?)$", PinterestObject.PinterestObjectResources.UserFollowersResource);
            }
        };
        return findExpr(s, b, mp);
    }

    
    public static AQuery typeByPatter_follow(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                put("^/pin/(.+?)/likes/$", PinterestObject.PinterestObjectResources.PinLikesResource);
                put("^/pin/(.+?)/repins/$", PinterestObject.PinterestObjectResources.RepinFeedResource);
                put("^/(.+?)/(.+?)/followers/$", PinterestObject.PinterestObjectResources.BoardFollowersResource);
                put("^/(.+?)/following/people/$", PinterestObject.PinterestObjectResources.UserFollowingResource);
                put("^/(.+?)/following/boards/$", PinterestObject.PinterestObjectResources.BoardFollowingResource);
                put("^/(.+?)/followers/$", PinterestObject.PinterestObjectResources.UserFollowersResource);
                put("^/(.+?)/boards/$", PinterestObject.PinterestObjectResources.ProfileBoardsResource);
                put("^/(.+?)/$", PinterestObject.PinterestObjectResources.IndividualUser);
                put("^(.+?)$", PinterestObject.PinterestObjectResources.SearchResource);
            }
        };
        return findExpr(s, b, mp);
    }

    
    public static AQuery typeByPatter_unfollow(String s, Board b) {
        Map<String, PinterestObject.PinterestObjectResources> mp = new LinkedHashMap<String, PinterestObject.PinterestObjectResources>() {
            {
                //this must be our user's username and user's board!!!
                put("^/(.+?)/boards/$", PinterestObject.PinterestObjectResources.BoardFollowingResource);
                //this must be our user's username!!!
                put("^/(.+?)/following/$", PinterestObject.PinterestObjectResources.UserFollowingResource);
            }
        };
        return findExpr(s, b, mp);
    }

    
    private static AQuery findExpr(String s, Board mappedBoard, Map<String, PinterestObject.PinterestObjectResources> mp) {
        Pattern pattern;
        Matcher matcher;
        for (Map.Entry<String, PinterestObject.PinterestObjectResources> kv : mp.entrySet()) {
            pattern = Pattern.compile(kv.getKey(), Pattern.DOTALL | Pattern.CASE_INSENSITIVE & Pattern.MULTILINE);
            matcher = pattern.matcher(s);
            if (matcher.find()) {
                AQuery nQ = new AQuery(s, mappedBoard == null ? null : mappedBoard.copy(null), kv.getValue());
                return nQ;
            }
        }
        return null;
    }
}
