package org.hdu.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.mapper.base.BaseMapper;
import org.hdu.model.JobInfo;

public interface JobInfoMapper extends BaseMapper<JobInfo>{

    JobInfo selectByAppkey(@Param(value = "appkey") String appkey);

}