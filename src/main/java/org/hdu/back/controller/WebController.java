package org.hdu.back.controller;

import org.hdu.back.controller.base.BaseController;
import org.hdu.back.mapper.JobDailyMapper;
import org.hdu.back.mapper.JobInfoMapper;
import org.hdu.back.mapper.JobMsgMapper;
import org.hdu.back.mapper.WebPageDetailMapper;
import org.hdu.back.model.JobDaily;
import org.hdu.back.util.CmdUtil;
import org.hdu.crawler.crawler.HduCrawler;
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
        System.out.println("收到关键字爬虫请求");
        if(StringUtils.isEmpty(keyword)){
            return buildResult(CODE_BUSINESS_ERROR, "关键字不能为空");
        }
        System.out.println("关键字：" + keyword);
        String result = CmdUtil.execCmd("java -jar SearchCrawler-1.0-SNAPSHOT.jar", new File("./crawler"));
        if(result.contains("成功")){
            return buildResult(CODE_SUCCESS, result);
        }else {
            return buildResult(CODE_BUSINESS_ERROR, result);
        }
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
        System.out.println("收到主题爬虫请求");
    	if(StringUtils.isEmpty(subFile)){
    		return buildResult(CODE_BUSINESS_ERROR, "请选择主题文件");
    	}
    	//解析主题和域名文件
    	final List<String> keywordList = new ArrayList<>();
    	final List<String> domainList = new ArrayList<>();
    	try {
			BufferedReader subReader = new BufferedReader(new InputStreamReader(subFile.getInputStream()));
			String subject = null;
			if((subject=subReader.readLine()) != null){
				keywordList.add(subject.trim());
			}
			if(domainFile != null){
				BufferedReader domainReader = new BufferedReader(new InputStreamReader(domainFile.getInputStream()));
				String domain = null;
				if((domain=domainReader.readLine()) != null){
					domainList.add(domain.trim());
				}
			}
		} catch (IOException e) {
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
     * 根据前端输入的主题信息，启动爬虫任务
     * @param subFile 主题文件
     * @param depth 层数
     * @param count 总量
     * @param domainFile 域名文件
     * @param limitType init仅限初始 all限全部
     * @return
     */
    @RequestMapping("/startSubCrawl")
    public Map stopSubCrawl(){
    	hduCrawler.stop();
    	return buildResult(CODE_SUCCESS, "停止爬虫成功");
    }

    /**
     * 向前端返回监控状态信息
     * @param jobId 任务id,前端暂时写成1
     */
    @RequestMapping("/getMsg")
    public Map getMsg(Integer jobId){
        System.out.println("向前端返回监控状态信息");
        if(jobId == null){
            return buildResult(CODE_BUSINESS_ERROR, "任务id不能为空");
        }
        List<Map> jobMsgLs = jobMsgMapper.getJobMsg(jobId);
        if(jobMsgLs.isEmpty()){
        	return buildResult(CODE_BUSINESS_ERROR, "爬虫启动失败，数据库监控状态信息为空");
        }else {
            Map<String, Object> data = new HashMap<>();
            JobDaily jobDaily = jobDailyMapper.getLastDailyInfo(jobId);
            if(jobDaily.getEndTime() == null){
            	data.put("status", 0);
            }else{
            	data.put("status", 1);
            }
            data.put("tableData", jobMsgLs);
            return buildResult(data);
		}
    }
    
    @RequestMapping("/getResult")
    public Map getResult(String domain, Boolean relation1, String keyword1, String relation2, String keyword2){
    	if(StringUtils.isEmpty(domain) && StringUtils.isEmpty(keyword1) && StringUtils.isEmpty(keyword2)){
    		return buildResult(CODE_BUSINESS_ERROR, "搜索条件请至少选择一个");
    	}
    	List<Map> resultList = webPageDetailMapper.getResult(domain, relation1, keyword1, relation2, keyword2);
    	return buildResult("resultList", resultList);
    }
}
