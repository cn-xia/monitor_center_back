package org.hdu.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.mapper.base.BaseMapper;
import org.hdu.model.JobMsg;

public interface JobMsgMapper extends BaseMapper<JobMsg>{

    JobMsg selectLastJobMsg(@Param(value = "jobId") Integer jobId, @Param(value = "dailyId") Integer dailyId);
}