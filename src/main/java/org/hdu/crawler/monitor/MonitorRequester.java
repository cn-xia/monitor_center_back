package org.hdu.crawler.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.annotation.Resource;

import com.google.gson.Gson;
import org.codehaus.jackson.map.ObjectMapper;
import org.hdu.back.mapper.JobDailyMapper;
import org.hdu.back.mapper.JobInfoMapper;
import org.hdu.back.mapper.JobMsgMapper;
import org.hdu.back.model.JobDaily;
import org.hdu.back.model.JobInfo;
import org.hdu.back.model.JobMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

public class MonitorRequester {
/*	
	private static String server;          //监控中心服务器地址
	
	private static String appkey;		   //每个爬虫任务分配的appkey

	private static int interval;           //间隔时间
	
	private static int dailyId = -1;	   //本次任务对应的日志id
	
	private static final Logger logger = LoggerFactory.getLogger(MonitorRequester.class);
	
	@Value("${crawler.monitor.url}")
    public void setServer(String url) { //给静态变量赋值
        MonitorRequester.server = url;
    } 
	
	@Value("${crawler.monitor.appkey}")
	public void setAppkey(String appkey){
		MonitorRequester.appkey = appkey;
	}
	
	@Value("${crawler.monitor.interval}")
	public void setInterval(int interval){
		MonitorRequester.interval = interval;
	}
	
	*//**
	 * 监控开始
	 * 参数拼接
	 * @param monitorParam
	 * @return
	 *//*
	private static String getStartParamString(MonitorParam monitorParam){
		String crawlerCount = monitorParam.getCrawlerCount()+"";
		String saveCount = monitorParam.getSaveCount()+"";
		String ram = monitorParam.getRam();
		String cpu = monitorParam.getCpu();
		//得到当前时间戳
		Long timestamp = new Date().getTime();
		//生成密钥
		String sign = appkey+timestamp+crawlerCount+saveCount+ram+cpu+interval+appkey;
		String secret = MD5(sign);
		//拼接参数
		String param = "appkey="+appkey+"&secret="+secret+"&timestamp="+timestamp+
				"&cpu="+cpu+"&ram="+ram+"&crawlerCount="+crawlerCount+"&saveCount="+saveCount+"&interval="+interval;
		return param;
	}
	
	*//**
	 * 状态监控
	 * 生成密钥、拼接请求参数
	 * @return
	 *//*
	private static String getCrawlerParamString(MonitorParam monitorParam){
		long crawlerCount = monitorParam.getCrawlerCount();
		long saveCount = monitorParam.getSaveCount();
		String ram = monitorParam.getRam();
		String cpu = monitorParam.getCpu();
		//得到当前时间戳
		Long timestamp = new Date().getTime();
		//生成密钥
		String sign = appkey+timestamp+crawlerCount+saveCount+ram+cpu+dailyId+appkey;
		String secret = MD5(sign);
		//拼接参数
		String param = "appkey="+appkey+"&secret="+secret+"&timestamp="+timestamp+
				"&cpu="+cpu+"&ram="+ram+"&crawlerCount="+crawlerCount+"&saveCount="+saveCount+"&dailyId="+dailyId;
		return param;
	}
	
	*//**
	 * 日报生成
	 * 生成密钥、拼接求参数
	 * @return
	 *//*
	private static String getDailyParamString(MonitorParam monitorParam){
		String totalCount = monitorParam.getTotalCount()+"";
		String totalSold = monitorParam.getTotalSold()+"";
		//得到当前时间戳
		Long timestamp = new Date().getTime();
		//生成密钥
		String sign = appkey + timestamp + totalCount + totalSold + dailyId + appkey;
		String secret = MD5(sign);
		//拼接参数
		String param = "appkey="+appkey+"&secret="+secret+"&timestamp="+timestamp+
				"&totalCount="+totalCount+"&totalSold="+totalSold+"&dailyId="+dailyId;
		return param;
	}
	
	*//**
	 * 错误日志参数
	 * @param monitorParam
	 * @return
	 *//*
	private static String getExceptionParamString(MonitorParam monitorParam){
		String exception = monitorParam.getException();
		//得到当前时间戳
		Long timestamp = new Date().getTime();
		// 生成密钥
		String sign = appkey + timestamp + exception + appkey;
		String secret = MD5(sign);
		// 拼接参数
		String param = "appkey=" + appkey + "&secret=" + secret + "&timestamp="
				+ timestamp + "&exception=" + exception;
		return param;
	}
	
	//监控开始
	public String start(MonitorParam monitorParam){
		String url = server+"/crawler/start";
		String param = getStartParamString(monitorParam);
		String result = sendPost(param,url);
		try { 
			//dailyId = Integer.parseInt(result.trim());
			ObjectMapper mapper = new ObjectMapper();
			Map retMap = mapper.readValue(result, Map.class);
			dailyId = Integer.parseInt(((Map)retMap.get("data")).get("dailyId").toString());
		} catch (Exception e) {
			dailyId = -1;
			result = -1+"";
			logger.error("dailyId get error:",e);
		}
		//获取任务id
        JobInfo jobInfo =jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //生成日报的初始记录
        JobDaily jobDaily = new JobDaily();
        jobDaily.setJobId(jobInfo.getJobId());
        jobDaily.setStartTime(new Date());
        jobDaily.setJobInterval(interval);
        jobDailyMapper.insertSelective(jobDaily);
        dailyId = jobDaily.getId();
        //生成爬虫状态信息
        int crawlerCount = (int)monitorParam.getCrawlerCount();
		int saveCount = (int)monitorParam.getSaveCount();
		String ram = monitorParam.getRam();
		String cpu = monitorParam.getCpu();
        JobMsg jobMsg = new JobMsg(jobId, crawlerCount, 0, saveCount, 0, cpu, ram, new Date(), dailyId);
        jobMsgMapper.insert(jobMsg);
        //返回日报id
		return "dailyId:" + dailyId;
	}
	
	//发送爬虫状态信息
	public String sendMessage(MonitorParam monitorParam){
		String url = server+"/crawler/sendMessage";
		String param = getCrawlerParamString(monitorParam);
		return sendPost(param,url);
		if(dailyId == -1){
        	return "任务启动失败，未生成dailyId";
        }
        //获取任务id
        JobInfo jobInfo = jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //获取上次监控状态信息
        JobMsg lastJobMsg = jobMsgMapper.selectLastJobMsg(jobId, dailyId);
        int lastCrawlerCount = lastJobMsg.getCrawlerCount();
        int lastSaveCount = lastJobMsg.getSaveCount();
        //生成爬虫状态信息
        int crawlerCount = (int)monitorParam.getCrawlerCount();
		int saveCount = (int)monitorParam.getSaveCount();
		String ram = monitorParam.getRam();
		String cpu = monitorParam.getCpu();
        JobMsg jobMsg = new JobMsg();
        jobMsg.setJobId(jobId);
        jobMsg.setCrawlerCount(crawlerCount);
        jobMsg.setIncCrawlerCount(crawlerCount - lastCrawlerCount);
        jobMsg.setSaveCount(saveCount);
        jobMsg.setIncSaveCount(saveCount - lastSaveCount);
        jobMsg.setCpu(cpu);
        jobMsg.setRam(ram);
        jobMsg.setDailyId(dailyId);
        jobMsg.setMonitorTime(new Date());
        jobMsgMapper.insert(jobMsg);
        return "生成爬虫状态信息成功";
	}
	//发送日报信息
	public String sendDaily(MonitorParam monitorParam){
		String url = server+"/crawler/sendDaily";
		String param = getDailyParamString(monitorParam);
		return sendPost(param,url);
		if(dailyId == -1){
        	return "任务启动失败，未生成dailyId";
        }
        //获取任务id
        JobInfo jobInfo = jobInfoMapper.selectByAppkey(appkey);
        int jobId = jobInfo.getJobId();
        //更新日报信息
        JobDaily jobDaily = jobDailyMapper.selectByPrimaryKey(dailyId);
        //花费时间
        Date endTime = new Date();
        int totalCount = (int)monitorParam.getTotalCount();
		long totalSold = monitorParam.getTotalSold();
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
	//发生错误时给监控中心发出警告
	public static String sendException(MonitorParam monitorParam){
		String url = server+"/crawler/sendException";
		String param = getExceptionParamString(monitorParam);
		return sendPost(param,url);
	}
	
	*//**
     * 向URL发送post请求
     * 
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数， name1=value1&name2=value2
     * @return result 响应结果
     *//*
    private static String sendPost(String param,String url) {
    	logger.info(url+"-------------"+param);
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*");
            conn.setRequestProperty("connection", "Keep-Alive");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
        	logger.error("monitorRequester post error：",e);
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        logger.info("Monitor Result:"+result);
        return result;
    	
    }
	
    private static String MD5(String s){
		 char hexDigits[]={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};       
	     try {
	         byte[] btInput = s.getBytes("UTF-8");
	         // 获得MD5摘要算法的 MessageDigest 对象
	         MessageDigest mdInst = MessageDigest.getInstance("MD5");
	         // 使用指定的字节更新摘要
	         mdInst.update(btInput);
	         // 获得密文
	         byte[] md = mdInst.digest();
	         // 把密文转换成十六进制的字符串形式
	         int j = md.length;
	         char str[] = new char[j * 2];
	         int k = 0;
	         for (int i = 0; i < j; i++) {
	             byte byte0 = md[i];
	             str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	             str[k++] = hexDigits[byte0 & 0xf];
	         }
	         return new String(str);
	     } catch (Exception e) {
	    	 logger.error("monitorRequester md5 error"+e);
	         return null;
	     }
    }*/
}
