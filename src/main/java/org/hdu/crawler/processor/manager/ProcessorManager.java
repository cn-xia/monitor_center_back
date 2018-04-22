package org.hdu.crawler.processor.manager;

import java.util.HashMap;
import java.util.Map;

import org.hdu.crawler.constants.ProcessorType;
import org.hdu.crawler.exceptions.NoPageProcessorException;
import org.hdu.crawler.processor.Processor;
import org.hdu.crawler.processor.baidu.BaiduSearchProcessor;
import org.hdu.crawler.processor.baidu.BaiduSearchRsProcessor;
import org.hdu.crawler.processor.baidu.video.BaiduVideoSearchProcessor;
import org.hdu.crawler.processor.baidu.video.Ku6PlayProcessor;
import org.hdu.crawler.processor.baidu.video.PlayPageProcessor;
import org.hdu.crawler.processor.facebook.FbFriendsListProcessor;
import org.hdu.crawler.processor.facebook.FbSearchProcessor;
import org.hdu.crawler.processor.youtube.YoutubeListProcessor;
import org.hdu.crawler.processor.youtube.YoutubePlayProcessor;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;

@Component
public class ProcessorManager implements ApplicationContextAware{
	private Map<String, Processor> processors = new HashMap<String, Processor>();
	
	public void process(Page page, CrawlDatums next) {
		Processor pageProcess = getProcessor(page);
		pageProcess.process(page, next);
	}

	public Processor getProcessor(Page page) {
		String processorType = page.meta(ProcessorType.PROCESSOR_TYPE);	
		if(processorType == null || !(processors.keySet().contains(processorType))) {
			throw new NoPageProcessorException();
		}	
		return processors.get(processorType);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		processors.put(ProcessorType.PROCESSOR_TYPE_BAIDU_SEARCH, applicationContext.getBean(BaiduSearchProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_BAIDU_SEARCH_RS, applicationContext.getBean(BaiduSearchRsProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_BAIDU_VIDEO_SEARCH, applicationContext.getBean(BaiduVideoSearchProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_PLAY_PAGE, applicationContext.getBean(PlayPageProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_KU6_PLAY, applicationContext.getBean(Ku6PlayProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_YOUTUBE_LIST, applicationContext.getBean(YoutubeListProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_YOUTUBE_PLAY, applicationContext.getBean(YoutubePlayProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_FACEBOOK_SEARCH, applicationContext.getBean(FbSearchProcessor.class));
		processors.put(ProcessorType.PROCESSOR_TYPE_FACEBOOK_FRIENDS_LIST, applicationContext.getBean(FbFriendsListProcessor.class));
	}
}
