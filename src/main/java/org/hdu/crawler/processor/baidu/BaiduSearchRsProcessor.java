package org.hdu.crawler.processor.baidu;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.Gson;
import org.hdu.back.mapper.WebPageDetailMapper;
import org.hdu.back.mapper.WebPageRelationMapper;
import org.hdu.back.mapper.WebPageResourceMapper;
import org.hdu.back.model.WebPageDetail;
import org.hdu.back.model.WebPageRelation;
import org.hdu.back.model.WebPageResource;
import org.hdu.back.util.AlgorithmUtil;
import org.hdu.crawler.constants.DatumConstants;
import org.hdu.crawler.crawler.DatumGenerator;
import org.hdu.crawler.crawler.HduCrawler;
import org.hdu.crawler.monitor.MonitorExecute;
import org.hdu.crawler.processor.Processor;
import org.hdu.crawler.util.CommonUtil;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;

@Component
public class BaiduSearchRsProcessor implements Processor{

	private static final Logger logger = LoggerFactory.getLogger(BaiduSearchRsProcessor.class);
	@Resource
	private DatumGenerator datumGenerator;
	@Resource
	private WebPageDetailMapper webPageDetailMapper;
	@Resource
	private WebPageResourceMapper webPageResourceMapper;
	@Resource
	private WebPageRelationMapper webPageRelationMapper;

    @Override
    public void process(Page page, CrawlDatums next) {
		MonitorExecute.counter.getAndIncrement();
    	String realUrl = page.getResponse().getRealUrl().toString();
        if(realUrl.contains("http://www.baidu.com/s")){ //暂时不处理再次链接到百度搜索的网页
			return;
		}
		if(page.getHtml()==null || page.select("title").isEmpty()){ //下载失败的数据
        	return;
		}
		if(page.matchUrl("http://xiyou\\.cctv\\.com/interface/index\\?videoId=.*")) {//cctv视频地址信息接口,单独处理
			parseCCTVVideoApi(page, realUrl);
			return;
		}
		if(!CommonUtil.matchCrawl(page.getHtml())){ //计算网页相关度，小于阈值则不爬取
			return;
		}
		parseWebPageDetail(page);
		parseWebSource(page, next);
		parseHref(page, next);
    }

	/**
	 * 解析cctv视频地址接口
	 * @param page
	 */
	private void parseCCTVVideoApi(Page page, String realUrl){
		List<WebPageResource> resourceLs = new LinkedList<>();
		Map videoInfo = new Gson().fromJson(page.getHtml(), Map.class);
		List<Map> data = (List<Map>)videoInfo.get("data");
		List<Map> videoList = (List<Map>)data.get(0).get("videoList");
		Map videoFilePaths = videoList.get(0);
		if(videoFilePaths.containsKey("videoFilePath")) {//标清视频
			String tmp = ((String)videoFilePaths.get("videoFilePath")).split("#")[0];
			String videoFlag = tmp.substring(tmp.lastIndexOf("/")+1);
			String videoFilePath;
			if(videoFlag.contains(page.meta("videoId"))) {
				videoFilePath = tmp + "_001.mp4";
			}else {
				videoFilePath = tmp + ".mp4";
			}
			resourceLs.add(new WebPageResource(videoFilePath, DatumConstants.RESOURCE_TYPE_VIDEO, new Date(), realUrl));
		}
		if(videoFilePaths.containsKey("videoFilePathHD")) {//高清视频
			String tmp = ((String)videoFilePaths.get("videoFilePathHD")).split("#")[0];
			String videoFlag = tmp.substring(tmp.lastIndexOf("/")+1);
			String videoFilePathHD;
			if(videoFlag.contains(page.meta("videoId"))) {
				videoFilePathHD = tmp + "_001.mp4";
			}else {
				videoFilePathHD = tmp + ".mp4";
			}
			resourceLs.add(new WebPageResource(videoFilePathHD, DatumConstants.RESOURCE_TYPE_VIDEO, new Date(), realUrl));
		}
		if(videoFilePaths.containsKey("videoFilePathSHD")) {//超清视频
			String tmp = ((String)videoFilePaths.get("videoFilePathSHD")).split("#")[0];
			String videoFlag = tmp.substring(tmp.lastIndexOf("/")+1);
			String videoFilePathSHD;
			if(videoFlag.contains(page.meta("videoId"))) {
				videoFilePathSHD = tmp + "_001.mp4";
			}else {
				videoFilePathSHD = tmp + ".mp4";
			}
			resourceLs.add(new WebPageResource(videoFilePathSHD, DatumConstants.RESOURCE_TYPE_VIDEO, new Date(), realUrl));
		}
		if(!resourceLs.isEmpty()){
			webPageResourceMapper.batchInsert(resourceLs);
		}
	}

	/**
	 * 解析网页详情
	 * @param page
	 * @param next
	 */
	private void parseWebPageDetail(Page page){
		String title = page.select("title").first().text();
		if(title.contains(page.meta("keyword"))){ //过滤与关键字不相关的网页
			WebPageDetail webPageDetail = new WebPageDetail();
			//网页地址md5，作为索引
			String realUrl  = page.getResponse().getRealUrl().toString();
			String realUrlMd5 = AlgorithmUtil.toMD5(realUrl);
			webPageDetail.setUrlMd5(realUrlMd5);
			//网页地址
			webPageDetail.setUrl(realUrl);
			//域名
			webPageDetail.setDomain(page.getResponse().getRealUrl().getHost());
			//标题
			webPageDetail.setTitle(title);
			//文章来源
			if(!page.select("#ne_article_source, .fabiao, .a_source, .source").isEmpty()){
				String src = page.select("#ne_article_source, .fabiao, .a_source, .source").first().text();
				webPageDetail.setSrc(src);
			}
			//创建时间
			String createTimeStr = null;
			if(!page.select(".time, .utime, .time, .date, .a_time").isEmpty()){
				createTimeStr = page.select(".time, .utime, .time, .date, .a_time").first().text();
			}else if(!page.select(".post_time_source").isEmpty()) {
				createTimeStr = page.select(".post_time_source").first().text();
				if(createTimeStr.indexOf("来源") != -1) {
					createTimeStr = createTimeStr.substring(0, createTimeStr.indexOf("来源")).trim();
				}
			}
			if(createTimeStr != null){
				if(createTimeStr.matches("^\\d{4}年\\d{2}月\\d{2}日 \\d{2}:\\d{2}.*")){
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
					Date createTime = null;
					try {
						createTime = sdf.parse(createTimeStr);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					webPageDetail.setCreateTime(createTime);
				}else if(createTimeStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}.*")) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					try {
						Date createTime = sdf.parse(createTimeStr);
						webPageDetail.setCreateTime(createTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			//作者
			String author = null;
			if(!page.select(".author, .author-name, .from, .ep-editor, .show_author, .qq_editor").isEmpty()){
				author = page.select(".author, .author-name, .from, .ep-editor, .show_author, .qq_editor").first().text();
			}else if(!page.select(".user_info").isEmpty()){
				Element userInfo = page.select(".user_info").first();
				if(userInfo.getElementById("uid") != null) {
					author = userInfo.getElementById("uid").text();
				}
			}
			if(author != null){
				webPageDetail.setAuthor(author);
			}
			//关键字
			webPageDetail.setKeyword(page.meta("keyword"));
			//标签
			String tags = null;
			if(!page.select("[target=tags]").isEmpty()){
				tags = page.select("[target=tags]").first().attr("content");
			}else if(!page.select(".article_tags a").isEmpty()){
				Elements ts = page.select(".article_tags a");
				for(Element tag : ts){
					tags = tags + tag.text() + ",";
				}
				tags = tags.substring(0, tags.length()-1);
			}
			if(tags != null) {
				webPageDetail.setTags(tags);
			}
			//内容
			String content = null;
			if(!page.select("article").isEmpty()){
				content = page.select("article").first().text();
			}else if(!page.select(".article_content, .topic-content, .main-content, .article-content-wrap, .sec_article, .post_text, .article, .Cnt-Main-Article-QQ, yc_con_txt").isEmpty()){
				content = page.select(".article_content, .topic-content, .main-content, .article-content-wrap, .sec_article, .post_text, .article, .Cnt-Main-Article-QQ, yc_con_txt").first().text();
			}
			if(content != null){
				webPageDetail.setContent(content);
			}
			//html
			webPageDetail.setHtml(page.getHtml());
			//浏览数
			Integer viewNum = null;
			if(!page.select(".art_click_count, .js-tiejoincount, .num, .cmtNum, .w-num").isEmpty()){
				String txt = page.select(".art_click_count, .js-tiejoincount, .num, .cmtNum, .w-num").first().text();
				if(!StringUtils.isEmpty(txt)){
					try {
						viewNum = Integer.parseInt(txt);
					}catch (Exception e){
					}
				}
			}else if(!page.select(".icon-read").isEmpty()){
				String txt = page.select(".icon-read").first().nextElementSibling().text();
				if(!StringUtils.isEmpty(txt)) {
					try {
						viewNum = Integer.parseInt(txt);
					}catch (Exception e){
					}
				}
			}
			if(viewNum != null){
				webPageDetail.setViewNum(viewNum);
			}
			//评论数
			Integer commentNum = null;
			if(!page.select(".cmtNum, .linkComment, .js_cmtNum, .comment-num, .js-tiecount").isEmpty()){
				String txt = page.select(".cmtNum, .linkComment, .js_cmtNum, .comment-num, .js-tiecount").first().text();
				if(!StringUtils.isEmpty(txt)){
					try {
						commentNum = Integer.parseInt(txt);
					}catch (Exception e){
					}
				}
			}else {
				if (!page.select(".article-pl").isEmpty()) {
					String txt = page.select(".article-pl").first().text().replace("评论", "").trim();
					if (!StringUtils.isEmpty(txt)) {
						try {
							commentNum = Integer.parseInt(txt);
						}catch (Exception e){
						}
					}
				}
			}
			if(commentNum != null){
				webPageDetail.setCommentNum(commentNum);
			}
			//爬取时间
			webPageDetail.setCrawlTime(new Date());
			System.out.println("保存关键字：" + webPageDetail.getKeyword());
			webPageDetailMapper.insertSelective(webPageDetail);
			MonitorExecute.saveCounter.getAndIncrement();
		}
	}

	/**
	 * 解析网页的视频、图片等资源
	 * @param page
	 * @param next
	 */
	private void parseWebSource(Page page, CrawlDatums next){
		List<WebPageResource> resourceLs = new LinkedList<>();
		String srcUrl = page.getResponse().getRealUrl().toString();
		String domain = page.getResponse().getRealUrl().getHost();
		String path = page.getResponse().getRealUrl().getPath();

		//解析图片地址
		Elements imgs = page.select("img");
		if(!imgs.isEmpty()) {
			for (Element img : imgs) {
				//String src = img.attr("abs:src"); 获取绝对路径，暂时不用，可能存在bug
				String src = img.attr("src");
				if(src==null || src.equals("") || src.contains("{")){ //js动态加载、vue的图片暂时取不到
					continue;
				}
				if(src.startsWith("http") || src.startsWith("//")){ //绝对路径

				}else if(src.startsWith("/")){ //以域名为基的相对路径
					src = domain+src;
				}else if(src.startsWith("./")){ //当前目录下
					src = srcUrl + src.substring(1);
				}else if(src.startsWith("../")){ //上一目录下
					src = srcUrl.substring(0, srcUrl.lastIndexOf("/")) + src.substring(2);
				}
				else { //图片文件目录和当前文件在同一目录下的相对路径
					src = domain + path.substring(0, path.lastIndexOf("/")+1) + src;
					//System.out.println("src：" + src);
				}
				WebPageResource resource = new WebPageResource(srcUrl, DatumConstants.RESOURCE_TYPE_PIC, new Date(), src);
				resourceLs.add(resource);
			}
		}

		//解析视频地址
		String videoUrl = null;
		if(page.matchUrl("https://.*\\.ku6\\.com.*")) {//酷6视频地址
			if (page.getHtml().contains("flvURL")) {
				String tmp = page.getHtml().substring(page.getHtml().indexOf("flvURL"), page.getHtml().indexOf("flvURL") + 100);
				videoUrl = tmp.substring(tmp.indexOf("http"), tmp.indexOf("\","));
			}
			if (page.getHtml().contains("playUrl")) {
				String tmp = page.getHtml().substring(page.getHtml().indexOf("playUrl"), page.getHtml().indexOf("playUrl") + 100);
				videoUrl = tmp.substring(tmp.indexOf("http"), tmp.indexOf("\","));
			}
			if (page.select("video").first() != null) {
				Element source = page.select("video source").first();
				if (source == null) {
					return;
				}
				videoUrl = source.attr("src");
			}
		}else if(page.matchUrl("http://baishi\\.baidu\\.com/watch/.*")) { //百度视频
			int start = page.getHtml().indexOf("video=http");
			if (start == -1) {
				logger.info("baidu video parse fail:" + page.getUrl());
				return;
			}
			String tmp = page.getHtml().substring(start + 6, start + 500);
			int end = tmp.indexOf("';");
			String downloadUrl = tmp.substring(0, end);
			try {
				videoUrl = URLDecoder.decode(downloadUrl, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}else if (page.matchUrl("http://video\\.eastday\\.com.*")) { //东方头条视频
			int start = page.getHtml().indexOf("mp4 = ");
			if (start == -1) {
				logger.info("eastday video parse fail:" + page.getUrl());
				return;
			}
			String tmp = page.getHtml().substring(start + 7, start + 100);
			int end = tmp.indexOf("mp4\";");
			videoUrl = "http:" + tmp.substring(0, end + 3);
		}else if (page.matchUrl("http://xiyou\\.cctv\\.com/v-.*")) {//cctv视频播放页面，不是视频地址
			String videoId = page.getUrl().substring(page.getUrl().indexOf("v-")+2, page.getUrl().indexOf(".html"));
			//爬取cctv视频地址信息接口
			next.add(datumGenerator.generateCCTVVideo(videoId, page.getUrl(), page.meta("keyword")));
		}
		if(videoUrl != null){
			WebPageResource resource = new WebPageResource(srcUrl, DatumConstants.RESOURCE_TYPE_VIDEO, new Date(), videoUrl);
			resourceLs.add(resource);
		}
		//插入数据库
		if(!resourceLs.isEmpty()) {
			webPageResourceMapper.batchInsert(resourceLs);
		}
	}

	/**
	 * 解析网页下面的超链接
	 * @param page
	 * @param next
	 */
	private void parseHref(Page page, CrawlDatums next){
		Elements as = page.select("a");
		if(as.isEmpty()){
			return;
		}
		List<WebPageRelation> relationLs = new ArrayList<>();
		for (Element a : as) {
			if (a.hasAttr("href")) {
				String href = a.attr("abs:href");//获取超链接
				String title = a.text();
				if(href.startsWith("http") && title.contains(page.meta("keyword"))) { //过滤与关键字无关的超链接
					try {
						if(limitedDomain(new URL(href).getHost())){ //过滤域名
							continue;
						}
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
					String srcUrl = page.getResponse().getRealUrl().toString();
					WebPageRelation webPageRelation = new WebPageRelation(href, srcUrl, new Date());
					relationLs.add(webPageRelation);
					next.add(datumGenerator.generateBaiduSearchRs(href, page.meta("keyword"), srcUrl));
				}
			}
		}
		//插入数据库
		if(!relationLs.isEmpty()){
			webPageRelationMapper.batchInsert(relationLs);
		}
	}


	/**
	 * 根据用户输入域名地址列表，判断是否限制域名
	 * @param domain
	 */
	private boolean limitedDomain(String domain){
		if(HduCrawler.domainList != null){ //限制域名
			switch (HduCrawler.limitType) {
				case "init": //限初始
					break;
				case "all": //限全部
					boolean isContains = false;
					for(String dm : HduCrawler.domainList){
						if(domain.equals(dm)){
							isContains = true;
							break;
						}
					}
					if(!isContains){
						return true;
					}
					break;
				default:
					break;
			}
			return false;
		}else {
			return false;
		}
	}
}
