package org.hdu.controller;
import org.hdu.controller.base.BaseController;
import org.hdu.mapper.JobDailyMapper;
import org.hdu.mapper.JobInfoMapper;
import org.hdu.mapper.JobMsgMapper;
import org.hdu.model.JobDaily;
import org.hdu.model.JobInfo;
import org.hdu.model.JobMsg;
import org.hdu.util.AlgorithmUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/crawler")
public class CrawlerController extends BaseController{

    @Resource
    private JobInfoMapper jobInfoMapper;
    @Resource
    private JobDailyMapper jobDailyMapper;
    @Resource
    private JobMsgMapper jobMsgMapper;

    /**
     * 测试服务器是否正常
     */
    @RequestMapping("/test")
    public Map test(){
        System.out.println("test success!");
        return buildResult(CODE_SUCCESS, "test success!");
    }

    /**
     * 监控开始,初始化日报记录，返回日报id
     */
    @RequestMapping("/start")
    public Map start(String appkey, String secret, Long timestamp, String cpu, String ram, Integer crawlerCount, Integer saveCount, Integer interval) {
        System.out.println("收到监控开始消息");
        //验证密钥
        String sign = appkey+timestamp+crawlerCount+saveCount+ram+cpu+interval+appkey;
        String st = AlgorithmUtil.toMD5(sign);
        if(!secret.equals(st)){
            return buildResult(CODE_BUSINESS_ERROR, "秘钥错误,你涉嫌非法调用本接口!");
        }
        //获取任务id
        JobInfo jobInfo = jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //生成日报的初始记录
        JobDaily jobDaily = new JobDaily();
        jobDaily.setJobId(jobInfo.getJobId());
        jobDaily.setStartTime(new Date(timestamp));
        jobDaily.setJobInterval(interval);
        jobDailyMapper.insertSelective(jobDaily);
        int dailyId = jobDaily.getId();
        //生成爬虫状态信息
        JobMsg jobMsg = new JobMsg(jobId, crawlerCount, 0, saveCount, 0, cpu, ram, new Date(timestamp), dailyId);
        jobMsgMapper.insert(jobMsg);
        //返回日报id
        return buildResult("dailyId", dailyId);
    }

    /**
     * 保存从爬虫端发送过来的爬虫状态信息
     */
    @RequestMapping("/sendMessage")
    public Map sendMessage(String appkey, String secret, Long timestamp, String cpu, String ram, Integer crawlerCount, Integer saveCount, Integer dailyId) {
        System.out.println("收到爬虫状态消息");
        //验证密钥
        String sign = appkey+timestamp+crawlerCount+saveCount+ram+cpu+dailyId+appkey;
        String st = AlgorithmUtil.toMD5(sign);
        if(!secret.equals(st)){
            return buildResult(CODE_BUSINESS_ERROR, "秘钥错误,你涉嫌非法调用本接口!");
        }
        //获取任务id
        JobInfo jobInfo = jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //获取上次监控状态信息
        JobMsg lastJobMsg = jobMsgMapper.selectLastJobMsg(jobId, dailyId);
        int lastCrawlerCount = lastJobMsg.getCrawlerCount();
        int lastSaveCount = lastJobMsg.getSaveCount();
        //生成爬虫状态信息
        JobMsg jobMsg = new JobMsg();
        jobMsg.setJobId(jobId);
        jobMsg.setCrawlerCount(crawlerCount);
        jobMsg.setIncCrawlerCount(crawlerCount - lastCrawlerCount);
        jobMsg.setSaveCount(saveCount);
        jobMsg.setIncSaveCount(saveCount - lastSaveCount);
        jobMsg.setCpu(cpu);
        jobMsg.setRam(ram);
        jobMsg.setDailyId(dailyId);
        jobMsg.setMonitorTime(new Date(timestamp));
        jobMsgMapper.insert(jobMsg);
        return buildResult(CODE_SUCCESS, "ok");
    }


    /**
     * 保存日报信息
     */
    @RequestMapping("/sendDaily")
    public Map sendDaily(String appkey, String secret, Long timestamp, Integer totalCount, Long totalSold, Integer dailyId){
        System.out.println("收到日报消息");
        //验证密钥
        String sign = appkey + timestamp + totalCount + totalSold + dailyId + appkey;
        String st = AlgorithmUtil.toMD5(sign);
        if(!secret.equals(st)){
            return buildResult(CODE_BUSINESS_ERROR, "秘钥错误,你涉嫌非法调用本接口!");
        }
        //获取任务id
        JobInfo jobInfo = jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //更新日报信息
        JobDaily jobDaily = jobDailyMapper.selectByPrimaryKey(dailyId);
        //花费时间
        long totalSeconds = (timestamp-jobDaily.getStartTime().getTime())/1000;
        int spendHour = (int)totalSeconds/3600;
        int spendMinute = (int)(totalSeconds-spendHour*3600)/60;
        int spendSeconds = (int)(totalSeconds-spendHour*3600-spendMinute*60);
        String spendTime = spendHour+"小时"+spendMinute+"分钟"+spendSeconds+"秒";
        jobDaily.setEndTime(new Date(timestamp));
        jobDaily.setSpendTime(spendTime);
        jobDaily.setCrawlerTime(spendTime);
        jobDaily.setTotalCount(totalCount);
        jobDaily.setTotalSold(totalSold);
        jobDaily.setCreateTime(new Date());
        jobDailyMapper.updateByPrimaryKey(jobDaily);
        return buildResult(CODE_SUCCESS, "ok");
    }

}
