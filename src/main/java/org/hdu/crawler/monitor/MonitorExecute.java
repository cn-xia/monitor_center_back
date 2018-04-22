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
	
	@Value("${crawler.monitor.active}")
	private static boolean active;//是否启用监控
	
	@Value("${crawler.monitor.interval}")
	public static int interval;//间隔时间
	
	@Value("${crawler.monitor.appkey}")
	public static String appkey;
	
	public static int dailyId = -1;//本次任务对应的日志id	
	
	@Resource
	private MonitorThread startThread;
	@Resource 
	private MonitorThread msgThread;
	@Resource
	private MonitorThread lastMsgThread;
	@Resource
	private MonitorThread dailyThread;
	
	public static final AtomicLong saveCounter = new AtomicLong(0);
	public static final AtomicLong counter = new AtomicLong(0);
	public static final AtomicInteger fileCounter = new AtomicInteger(0);
	public static final AtomicLong soldCounter = new AtomicLong(0);
	
	@Override
	public void crawlerBegin() {//爬虫开始时 开始调用监控 
		if(!active){
			return; //测试代码不启动监控
		}	
		//初始化
		startThread.setState(0);
		startThread.start();	
		//监控
		msgThread.setState(1);
		MonitorThread.flag = true;
		msgThread.start();
		logger.info("---inspectorStart---");
	}

	@Override
	public void crawlerEnd() {//爬虫结束时调用日报接口
		if(!active){
			return; //测试代码不启动监控
		}
		MonitorThread.flag = false;//停止监控
		//最后一次监控
		lastMsgThread.setState(2);
		lastMsgThread.start();
		//日报
		dailyThread.setState(3);
		dailyThread.start();
		logger.info("---call dailyInspector---");
	}
	
	/*public void sendErrorMsg(String exceptionMsg){//发生异常时调用
		if(!active){
			return; 
		}
		MonitorParam monitorParam = new MonitorParam(exceptionMsg);
		MonitorThread mot = new MonitorThread(4,monitorParam);
		mot.start();
	}*/
	
	public static MonitorParam getMsgParam(){
		String cpu = MonitorInfoUtil.getCpuMsg();
		String ram = MonitorInfoUtil.getMemMsg();
		return new MonitorParam(counter.get(), saveCounter.get(), cpu, ram);
	}
	
	public MonitorParam getDailyParam(){
		Long totalCount = counter.get();
		Long totalSales = soldCounter.get();
		return new MonitorParam(totalCount,totalSales);
	}
	
}
