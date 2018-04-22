package org.hdu.crawler.crawler;

import java.util.List;
import java.util.Map;

import org.hdu.crawler.listener.CrawlerBeginListener;
import org.hdu.crawler.listener.CrawlerEndListener;
import org.hdu.crawler.monitor.MonitorExecute;
import org.hdu.crawler.processor.manager.ProcessorManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.net.HttpResponse;
import cn.edu.hfut.dmic.webcollector.net.Requester;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

@Component
public class HduCrawler extends BreadthCrawler implements ApplicationContextAware{
	private static final Logger logger = LoggerFactory.getLogger(HduCrawler.class);
	
	@Value("${crawler.webcollector.depth}")
	private int depth;
	
	@Value("${crawler.webcollector.threads}")
	private int threads; 
	
	@Value("${crawler.webcollector.resumable}")
	private boolean resumable;
	
	@Value("${crawler.webcollector.topN}")
	private int topN;
	
	@Value("${crawler.webcollector.executeInterval}")
	private int executeInterval;
	
	@Autowired
	private Requester hduRequester;
	
	@Autowired
	private SeedGenerator seedGenerator;
	
	@Autowired
	private ProcessorManager processorManager;
	
	private Map<String, CrawlerEndListener> crawlerEndListenerMap;
	private Map<String, CrawlerBeginListener> crawlerbeginListenerMap;
	
	public static int count = -1;
	public static List<String> domainList = null;
	public static String limitType = null;
	/** 是否已启动爬虫 */
	public static boolean isStart = false;
	
	public HduCrawler(@Value("${crawler.webcollector.crawlPath}") String crawlPath, @Value("${crawler.webcollector.autoParse}") boolean autoParse) {
		super(crawlPath, autoParse);
	}
	
	/* (non-Javadoc)
	 * @see cn.edu.hfut.dmic.webcollector.fetcher.Visitor#visit(cn.edu.hfut.dmic.webcollector.model.Page, cn.edu.hfut.dmic.webcollector.model.CrawlDatums)
	 */
	public void visit(Page page, CrawlDatums next) {
		processorManager.process(page, next);
	}

	@Override
	public void execute(CrawlDatum datum, CrawlDatums next) throws Exception {
		HttpResponse response = requester.getResponse(datum);
        Page page = new Page(datum, response);
        visitor.visit(page, next);
        if(HduCrawler.count!=-1 && MonitorExecute.saveCounter.get()>HduCrawler.count){ //数量达到上限则停止爬虫
        	stop();
        }
	}
	
	public void start(List<String> keywordList, Integer depth, Integer count, List<String> domainList, String limitType) {
		long startTime = System.currentTimeMillis();
		if(depth != null){
			this.depth = depth;
		}
		if(count != null){
			HduCrawler.count = count;
		}
		if(!domainList.isEmpty()){
			HduCrawler.domainList = domainList;
			HduCrawler.limitType = limitType;
		}
		seedGenerator.addSeed(this, keywordList, domainList);
		this.setRequester(hduRequester);
		logger.info("crawler start");
		notifyBeginCrawler();
		logger.info("request start");
		try {
			setResumable(resumable);
			setTopN(topN);
			setThreads(threads);
        	setExecuteInterval(executeInterval);
			start(this.depth);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("request end");
        notifyEndCrawler();
        HduCrawler.isStart = true;
        logger.info("爬取总量：" + MonitorExecute.counter.get());
		logger.info("入库总量：" + MonitorExecute.saveCounter.get());
        logger.info("crawler end" );
		System.out.println("爬取总时间:" + (System.currentTimeMillis()-startTime)/1000 + "秒");
	}
	
	/*public void start() {
		long startTime = System.currentTimeMillis();
		seedGenerator.addSeed(this);
		this.setRequester(hduRequester);
		logger.info("crawler start");
		notifyBeginCrawler();
		logger.info("request start");
		try {
			setResumable(resumable);
			setTopN(topN);
			setThreads(threads);
        	setExecuteInterval(executeInterval);
			start(depth);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		logger.info("搜索视频数：" + HduStarter.liSize.get());
		logger.info("下载百度视频数：" + HduStarter.baiduSize.get());
		logger.info("下载东方头条视频数：" + HduStarter.eastDaySize.get());
		logger.info("下载cctv视频数：" + HduStarter.cctvSize.get());
		logger.info("下载酷6视频数：" + HduStarter.ku6Size.get());
		logger.info("下载youtube视频数：" + HduStarter.youtubeSize.get());
		logger.info("百度搜索关键字网页数：" + HduStarter.baiduSearchSize.get());
		logger.info("request end");
        notifyEndCrawler();
        logger.info("crawler end" );
		System.out.println("爬取总时间:" + (System.currentTimeMillis()-startTime)/1000 + "秒");
	}*/
	
	private void notifyBeginCrawler() {
		if(crawlerbeginListenerMap == null || crawlerbeginListenerMap.isEmpty()) {
			return;
		}
		for(CrawlerBeginListener listener: crawlerbeginListenerMap.values()) {
			listener.crawlerBegin();
		}
 	}

	private void notifyEndCrawler() {
		if(crawlerEndListenerMap == null || crawlerEndListenerMap.isEmpty()) {
			return;
		}
		
		if(crawlerEndListenerMap.containsKey("pipeline")) {
			crawlerEndListenerMap.get("pipeline").crawlerEnd();
		}
		
		for(CrawlerEndListener listener: crawlerEndListenerMap.values()) {
			listener.crawlerEnd();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		crawlerbeginListenerMap = applicationContext.getBeansOfType(CrawlerBeginListener.class);
		crawlerEndListenerMap = applicationContext.getBeansOfType(CrawlerEndListener.class);
	}
}