package org.hdu.back.controller;

import org.hdu.back.controller.base.BaseController;
import org.hdu.back.mapper.JobDailyMapper;
import org.hdu.back.mapper.JobInfoMapper;
import org.hdu.back.mapper.JobMsgMapper;
import org.hdu.back.mapper.WebPageDetailMapper;
import org.hdu.back.model.JobDaily;
import org.hdu.back.model.JobInfo;
import org.hdu.back.util.CmdUtil;
import org.hdu.back.util.FileUtil;
import org.hdu.crawler.crawler.HduCrawler;
import org.hdu.crawler.monitor.MonitorExecute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web")
public class WebController extends BaseController{
	
	private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Resource
    private JobInfoMapper jobInfoMapper;
    @Resource
    private JobDailyMapper jobDailyMapper;
    @Resource
    private JobMsgMapper jobMsgMapper;
    @Resource
    private WebPageDetailMapper webPageDetailMapper;
    @Resource
    private HduCrawler hduCrawler;

    /**
     * 根据前端输入的关键字，启动爬虫任务
     * @return
     */
    @RequestMapping("/startCrawl")
    public Map startCrawl(String keyword){
    	logger.info("收到关键字爬虫请求");
    	return buildResult(CODE_BUSINESS_ERROR, "该接口暂时关闭");
        /*if(StringUtils.isEmpty(keyword)){
            return buildResult(CODE_BUSINESS_ERROR, "关键字不能为空");
        }
        logger.info("关键字：" + keyword);
        String result = CmdUtil.execCmd("java -jar SearchCrawler-1.0-SNAPSHOT.jar", new File("./crawler"));
        if(result.contains("成功")){
            return buildResult(CODE_SUCCESS, result);
        }else {
            return buildResult(CODE_BUSINESS_ERROR, result);
        }*/
    }
    
    /**
     * 根据前端输入的主题信息，启动爬虫任务
     * @param subFile 主题文件
     * @param depth 层数
     * @param count 总量
     * @param domainFile 域名文件
     * @param limitType init仅限初始 all限全部
     * @return
     */
    @RequestMapping("/startSubCrawl")
    public Map startSubCrawl(MultipartFile subFile, final Integer depth, final Integer count, MultipartFile domainFile, final String limitType){
    	logger.info("收到主题爬虫请求");
        if(HduCrawler.isStart){
        	return buildResult(CODE_BUSINESS_ERROR, "爬虫已启动，不能再次启动，请耐心等待爬虫完成");
        }
    	if(StringUtils.isEmpty(subFile)){
    		return buildResult(CODE_BUSINESS_ERROR, "请选择主题文件");
    	}
    	//解析主题和域名文件
    	final List<String> keywordList = new ArrayList<>();
    	final List<String> domainList = new ArrayList<>();
    	try {
    		String subFileEncoding = FileUtil.getEncoding(subFile.getInputStream());
    		logger.info("主题文件编码格式: " + subFileEncoding); 
			BufferedReader subReader = new BufferedReader(new InputStreamReader(subFile.getInputStream(), subFileEncoding));
			String keyword = null;
			while((keyword=subReader.readLine()) != null){
				keywordList.add(keyword.trim());
				System.out.println("输入关键词：" + keyword.trim());
			}
			if(domainFile != null){
				String domainFileEncoding = FileUtil.getEncoding(domainFile.getInputStream());
	    		logger.info("域名文件编码格式: " + domainFileEncoding); 
				BufferedReader domainReader = new BufferedReader(new InputStreamReader(domainFile.getInputStream(), domainFileEncoding));
				String domain = null;
				while((domain=domainReader.readLine()) != null){
					domainList.add(domain.trim());
					System.out.println("输入域名：" + domain.trim());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	if(keywordList.isEmpty()){
    		return buildResult(CODE_BUSINESS_ERROR, "主题文件中关键词为空");
    	}
    	//在子线程中运行爬虫
    	new Thread(new Runnable() {		
			@Override
			public void run() {
				hduCrawler.start(keywordList, depth, count, domainList, limitType);
			}
		}).start();
    	return buildResult(CODE_SUCCESS, "启动爬虫成功");
    }
    
    /**
     * 停止爬虫
     * @return
     */
    @RequestMapping("/stopSubCrawl")
    public Map stopSubCrawl(){
    	logger.info("收到爬虫结束指令");
    	if(HduCrawler.isStart){
    		hduCrawler.stop();
    	}
    	return buildResult(CODE_SUCCESS, "停止爬虫成功");
    }

    /**
     * 向前端返回监控状态信息
     * @param jobId 任务id,前端暂时写成1
     * @return status(0未完成1已完成) nowDepth(当前层数) tableData(监控信息)
     */
    @RequestMapping("/getMsg")
    public Map getMsg(Integer jobId){
    	logger.info("向前端返回监控状态信息");
        if(jobId == null){
            return buildResult(CODE_BUSINESS_ERROR, "任务id不能为空");
        }
        List<Map> jobMsgLs = jobMsgMapper.getJobMsg(jobId);
        if(jobMsgLs.isEmpty()){
        	return buildResult(CODE_BUSINESS_ERROR, "爬虫启动失败，数据库监控状态信息为空");
        }else if(MonitorExecute.dailyId != (int)jobMsgLs.get(0).get("dailyId")) {
        	return buildResult(CODE_BUSINESS_ERROR, "爬虫未启动");
		}else {
            Map<String, Object> data = new HashMap<>();
            JobDaily jobDaily = jobDailyMapper.getLastDailyInfo(jobId);
            if(jobDaily.getEndTime() == null){
            	data.put("status", 0);
            }else{
            	data.put("status", 1);
            }
            data.put("nowDepth", HduCrawler.nowDepth);
            data.put("tableData", jobMsgLs);
            return buildResult(data);
		}
    }
    
    /**
     * 向前端返回查询结果
     * @param input1 输入1
     * @param select1 选择类型1
     * @param relation1 关系1
     * @param input2 输入2
     * @param select2 选择类型2
     * @param relation2 关系2
     * @param input3 输入3
     * @param select3 选择类型3
     * @return
     */
    @RequestMapping("/getResult")
    public Map getResult(String input1, String select1, String relation1, String input2, String select2, 
    							String relation2, String input3, String select3){
    	logger.info("向前端返回查询结果");
    	if(StringUtils.isEmpty(input1) && StringUtils.isEmpty(input2) && StringUtils.isEmpty(input3)){
    		return buildResult(CODE_BUSINESS_ERROR, "搜索条件请至少输入一个");
    	}    	
    	List<Map> resultList = webPageDetailMapper.getResult(input1, select1, relation1, input2, select2, 
    																relation2, input3, select3);
    	return buildResult("resultList", resultList);
    }
}
