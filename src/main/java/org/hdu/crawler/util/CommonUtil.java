package org.hdu.crawler.util;

import org.hdu.crawler.crawler.HduCrawler;

public class CommonUtil {

    /**
     * 相关度大于阈值时爬取
     * @param html
     * @return
     */
    public static boolean matchCrawl(String html){
        double score = getWebPageScore(html);
        if(score >= HduCrawler.threshold){
            return true;
        }else {
            return false;
        }
    }

    /**
     * 计算出网页的相关度(0到1之间）
     * @param html
     * @return
     */
    public static double getWebPageScore(String html){
        return 1;
    }
}
