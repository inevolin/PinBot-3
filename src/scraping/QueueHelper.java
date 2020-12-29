/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scraping;

import java.util.Random;
import java.util.Set;
import model.configurations.PinConfiguration;
import model.configurations.RepinConfiguration;

public class QueueHelper {
    
    public static String getRandomSourceUrl(PinConfiguration _config) {

            if (_config.getSourceUrlRate() == null || _config.getSourceUrlRate() <= 0)
                return "";

            Set<String> c = _config.getSourceUrls();
            if (c != null && !c.isEmpty())
            {
                Random r = new Random();
                if (r.nextInt(100) <= _config.getSourceUrlRate())
                {
                    String f = (String) (c.toArray())[r.nextInt(c.size())];
                    return f;
                }
            }
            return "";
    }
    
    public static String getRandomDescUrl(PinConfiguration _config) {
            if (_config.getDescUrlRate() == null || _config.getDescUrlRate() <= 0)
                return "";

            Set<String> c = _config.getDescUrls();
            if (c != null && !c.isEmpty())
            {
                Random r = new Random();
                if (r.nextInt(100) <= _config.getDescUrlRate())
                {
                    String f = (String) (c.toArray())[r.nextInt(c.size())];
                    return f;
                }
            }
            return "";
    }
    
    public static String getRandomDescUrl(RepinConfiguration _config) {
            if (_config.getDescUrlRate() == null || _config.getDescUrlRate() <= 0)
                return "";

            Set<String> c = _config.getDescUrls();
            if (c != null && !c.isEmpty())
            {
                Random r = new Random();
                if (r.nextInt(100) <= _config.getDescUrlRate())
                {
                    String f = (String) (c.toArray())[r.nextInt(c.size())];
                    return f;
                }
            }
            return "";
    }
    
}
