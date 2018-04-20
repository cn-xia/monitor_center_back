package org.hdu.controller;

import org.hdu.controller.base.BaseController;
import org.hdu.mapper.JobDailyMapper;
import org.hdu.mapper.JobInfoMapper;
import org.hdu.mapper.JobMsgMapper;
import org.hdu.util.CmdUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.io.File;
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

    /**
     * 根据前端输入的关键字，启动爬虫任务
     * @return
     */
    @RequestMapping("/startCrawl")
    public Map startCrawl(String keyword){
        System.out.println("启动爬虫");
        if(keyword == null){
            return buildResult(CODE_BUSINESS_ERROR, "关键字不能为空");
        }
        System.out.println("关键字：" + keyword);
        String result = CmdUtil.execCmd("java -jar SearchCrawler-1.0-SNAPSHOT.jar", new File("D:\\leqee\\git-workspace\\SearchCrawler\\target"));
        if(result.contains("成功")){
            return buildResult(CODE_SUCCESS, result);
        }else {
            return buildResult(CODE_BUSINESS_ERROR, result);
        }
    }

    /**
     * 向前端返回监控状态信息
     */
    @RequestMapping("/getMsg")
    public Map getMsg(Integer jobId){
        System.out.println("向前端返回监控状态信息");
        if(jobId == null){
            return buildResult(CODE_BUSINESS_ERROR, "任务id不能为空");
        }
        List<Map> jobMsgLs = jobMsgMapper.getJobMsg(jobId);
        return buildResult("tableData", jobMsgLs);
    }
}
