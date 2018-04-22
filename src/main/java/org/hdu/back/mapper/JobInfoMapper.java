package org.hdu.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.JobInfo;

public interface JobInfoMapper extends BaseMapper<JobInfo>{

    JobInfo selectByAppkey(@Param(value = "appkey") String appkey);

}