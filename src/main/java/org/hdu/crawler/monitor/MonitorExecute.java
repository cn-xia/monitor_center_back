package org.hdu.crawler.monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import org.hdu.crawler.listener.CrawlerBeginListener;
import org.hdu.crawler.listener.CrawlerEndListener;
import org.hdu.crawler.util.MonitorInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MonitorExecute implements CrawlerBeginListener,CrawlerEndListener{

	private static final Logger logger = LoggerFactory.getLogger(MonitorExecute.class);
	
	@Resource 
	private MonitorThread msgThread;
	
	@Value("${crawler.monitor.active}")
	private boolean active;//是否启用监控
	public static String appkey;//任务key
	public static int interval;//间隔时间
	public static int dailyId = -1;//本次任务对应的日志id	

	public static final AtomicLong counter = new AtomicLong(0);
	public static final AtomicLong saveCounter = new AtomicLong(0);
	public static final AtomicInteger fileCounter = new AtomicInteger(0);
	//public static final AtomicLong soldCounter = new AtomicLong(0);
	
	@Value("${crawler.monitor.appkey}")
	public void setAppkey(String appkey){
		MonitorExecute.appkey = appkey;
	}
	
	@Value("${crawler.monitor.interval}")
	public void setInterval(int interval){
		MonitorExecute.interval = interval;
	}
	
	@Override
	public void crawlerBegin() {//爬虫开始时 开始调用监控 
		if(!active){
			return; //测试代码不启动监控
		}		
		//监控
		MonitorThread.flag = true;
		if(!MonitorThread.isRunning){
			MonitorThread.isRunning = true;
			msgThread.start();
		}
		logger.info("start monitor");
	}

	@Override
	public void crawlerEnd() {//爬虫结束时调用日报接口
		if(!active){
			return; //测试代码不启动监控
		}
		MonitorThread.flag = false;//停止监控
		logger.info("end monitor");
	}
	
	/*public void sendErrorMsg(String exceptionMsg){//发生异常时调用
		if(!active){
			return; 
		}
		MonitorParam monitorParam = new MonitorParam(exceptionMsg);
		MonitorThread mot = new MonitorThread(4,monitorParam);
		mot.start();
	}
	
	public static MonitorParam getMsgParam(){
		String cpu = MonitorInfoUtil.getCpuMsg();
		String ram = MonitorInfoUtil.getMemMsg();
		return new MonitorParam(counter.get(), saveCounter.get(), cpu, ram);
	}
	
	public MonitorParam getDailyParam(){
		Long totalCount = counter.get();
		Long totalSales = soldCounter.get();
		return new MonitorParam(totalCount,totalSales);
	}*/
	
}
