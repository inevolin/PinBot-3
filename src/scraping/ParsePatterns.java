/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;


public class ParsePatterns {
    
        public static String[] ImgFave = {
                "<img class=\"lazy\" data-href=\"(?<img>.+?)\" src=\"(.+?)\" width=\"\\d+\" height=\"\\d+\" alt=\"(?<title>.+?)\"/></a>"
                + "("
                + "<a class=\"(.+?)\" href=\"(.+?)\" style=\"(.+?)\">(.+?)</a>"
                + "|"
                + "<div class=\"image_tags_container\" id=\"(.+?)\">(?<tags><span class=\"image_tags\">)(<a class=\"(.+?)\" href=\"(.+?)\">(?<tag>.+?)</a>)+</span><div class=\"clearfix\"></div></div>"
                + ")?"
                + "</div>"
            };

        //we do not have to match newlines, tabs, etc... we use Pattern.DOTALL option in Matcher method.
        public static String[] Tumblr = {
                "\"high_res\":\"(?<img>.+?)\",\"height\":.+?,\"width\":.+?\\}'>.+?<p>(?<desc>.+?)</p>"
            };
}
