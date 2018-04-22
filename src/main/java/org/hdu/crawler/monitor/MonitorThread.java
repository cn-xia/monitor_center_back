package org.hdu.crawler.monitor;

import java.util.Date;

import javax.annotation.Resource;

import org.hdu.back.mapper.JobDailyMapper;
import org.hdu.back.mapper.JobInfoMapper;
import org.hdu.back.mapper.JobMsgMapper;
import org.hdu.back.model.JobDaily;
import org.hdu.back.model.JobInfo;
import org.hdu.back.model.JobMsg;
import org.hdu.crawler.util.MonitorInfoUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class MonitorThread extends Thread{
	
	public static boolean flag ; //是否监控
	
	private int state = -1;
	
	@Resource
	private JobInfoMapper jobInfoMapper;
	@Resource
	private JobDailyMapper jobDailyMapper;
	@Resource
	private JobMsgMapper jobMsgMapper;
	
	public MonitorThread(){
	}
	
	public void setState(int state){
		this.state = state;
	}
	
	@Override
	public void run() {
		switch(state){
			case 0: //就绪
				String result = startMonitor();
				if("-1".equals(result)){
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					startMonitor(); //发生错误再次尝试请求
				}
				break;
			case 1: //运行
				while(flag){
					try {
						Thread.sleep(MonitorExecute.interval*60*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(!flag){
						break;
					}
					sendMessage();
				}
				break;
			case 2:
				sendMessage(); //结束时最后一次调msg
				break;
			case 3: //结束
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendDaily();//日报接口
				break;
			case 4:
				//MonitorRequester.sendException(monitorParam);//错误通知接口
		}
	}

	//监控开始
	public String startMonitor(){
		//获取任务id
        JobInfo jobInfo =jobInfoMapper.selectByAppkey(MonitorExecute.appkey);
        int jobId = jobInfo.getJobId();
        //生成日报的初始记录
        JobDaily jobDaily = new JobDaily();
        jobDaily.setJobId(jobInfo.getJobId());
        jobDaily.setStartTime(new Date());
        jobDaily.setJobInterval(MonitorExecute.interval);
        jobDailyMapper.insertSelective(jobDaily);
        MonitorExecute.dailyId = jobDaily.getId();
        //生成爬虫状态信息
        int crawlerCount = MonitorExecute.counter.intValue();
		int saveCount = MonitorExecute.saveCounter.intValue();
		String ram = MonitorInfoUtil.getMemMsg();
		String cpu = MonitorInfoUtil.getCpuMsg();
        JobMsg jobMsg = new JobMsg(jobId, crawlerCount, 0, saveCount, 0, cpu, ram, new Date(), MonitorExecute.dailyId);
        jobMsgMapper.insert(jobMsg);
        //返回日报id
		return MonitorExecute.dailyId+"";
	}
	
	//发送爬虫状态信息
	public String sendMessage(){
		if(MonitorExecute.dailyId == -1){
	    	return "任务启动失败，未生成dailyId";
	    }
	    //获取任务id
	    JobInfo jobInfo = jobInfoMapper.selectByAppkey(MonitorExecute.appkey);
	    int jobId = jobInfo.getJobId();
	    //获取上次监控状态信息
	    JobMsg lastJobMsg = jobMsgMapper.selectLastJobMsg(jobId, MonitorExecute.dailyId);
	    int lastCrawlerCount = lastJobMsg.getCrawlerCount();
	    int lastSaveCount = lastJobMsg.getSaveCount();
	    //生成爬虫状态信息
	    int crawlerCount = MonitorExecute.counter.intValue();
		int saveCount = MonitorExecute.saveCounter.intValue();
		String ram = MonitorInfoUtil.getMemMsg();
		String cpu = MonitorInfoUtil.getCpuMsg();
	    JobMsg jobMsg = new JobMsg();
	    jobMsg.setJobId(jobId);
	    jobMsg.setCrawlerCount(crawlerCount);
	    jobMsg.setIncCrawlerCount(crawlerCount - lastCrawlerCount);
	    jobMsg.setSaveCount(saveCount);
	    jobMsg.setIncSaveCount(saveCount - lastSaveCount);
	    jobMsg.setCpu(cpu);
	    jobMsg.setRam(ram);
	    jobMsg.setDailyId(MonitorExecute.dailyId);
	    jobMsg.setMonitorTime(new Date());
	    jobMsgMapper.insert(jobMsg);
	    return "生成爬虫状态信息成功";
	}
	
	//发送日报信息
	public String sendDaily(){
		if(MonitorExecute.dailyId == -1){
	    	return "任务启动失败，未生成dailyId";
	    }
	    //获取任务id
	    JobInfo jobInfo = jobInfoMapper.selectByAppkey(MonitorExecute.appkey);
	    int jobId = jobInfo.getJobId();
	    //更新日报信息
	    JobDaily jobDaily = jobDailyMapper.selectByPrimaryKey(MonitorExecute.dailyId);
	    //花费时间
	    Date endTime = new Date();
	    int totalCount = MonitorExecute.counter.intValue();
		long totalSold = MonitorExecute.counter.longValue();
	    long totalSeconds = (endTime.getTime()-jobDaily.getStartTime().getTime())/1000;
	    int spendHour = (int)totalSeconds/3600;
	    int spendMinute = (int)(totalSeconds-spendHour*3600)/60;
	    int spendSeconds = (int)(totalSeconds-spendHour*3600-spendMinute*60);
	    String spendTime = spendHour+"小时"+spendMinute+"分钟"+spendSeconds+"秒";
	    jobDaily.setEndTime(endTime);
	    jobDaily.setSpendTime(spendTime);
	    jobDaily.setCrawlerTime(spendTime);
	    jobDaily.setTotalCount(totalCount);
	    jobDaily.setTotalSold(totalSold);
	    jobDaily.setCreateTime(new Date());
	    jobDailyMapper.updateByPrimaryKey(jobDaily);
	    return "生成日报信息成功";
	}
		
}
