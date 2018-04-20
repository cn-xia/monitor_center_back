package org.hdu.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.mapper.base.BaseMapper;
import org.hdu.model.JobDaily;

public interface JobDailyMapper extends BaseMapper<JobDaily>{

    JobDaily getLastDailyInfo(@Param(value = "jobId") Integer jobId);
}