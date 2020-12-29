/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping.sources;

import common.MyUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import model.Account;
import model.configurations.queries.AQuery;
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;
import model.configurations.AConfiguration;

public class ScrapeBoards_SearchResource extends ScrapeBoards {
    
    public ScrapeBoards_SearchResource(Account account, AQuery query, AConfiguration config) throws Exception {
        super(account, query, config);
    }
    
    @Override
    protected void SetUrlAndRef() {
        try{            
            Url = FirstRequest ? (base_url + "/search/boards/?q=" + URLEncoder.encode(super.getQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())) : (base_url + GetData());            
            Referer = base_url;
        }  catch (UnsupportedEncodingException ex){
            
        }  
    }
    
    @Override
    protected String GetData() {
        try {
            String str = "/resource/SearchResource/get/?source_url="
                        + URLEncoder.encode("/search/boards/?q=" + super.getQuery().getQuery().replaceAll("\\s+", "+"), StandardCharsets.UTF_8.toString())
                        + "&data="
                        + URLEncoder.encode("{\"options\":{\"layout\":null,\"places\":false,\"constraint_string\":null,\"show_scope_selector\":true,\"query\":\"" + super.getQuery().getQuery() + "\",\"scope\":\"boards\",\"bookmarks\":[\"" + getBookmark() + "\"]},\"context\":{}}", StandardCharsets.UTF_8.toString())
                        + "&_=" + getTimeSinceEpoch();
            
            str = MyUtils.jsonUpperCase(str);
            return str;
        } catch (UnsupportedEncodingException ex) {
            
        }
        return null;
    }
    
    @Override
    protected Board createBoard(){
        return new Board(PinterestObject.PinterestObjectResources.SearchResource, query);
    }
}
