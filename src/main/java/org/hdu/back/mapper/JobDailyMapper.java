package org.hdu.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.JobDaily;

public interface JobDailyMapper extends BaseMapper<JobDaily>{

    JobDaily getLastDailyInfo(@Param(value = "jobId") Integer jobId);
}