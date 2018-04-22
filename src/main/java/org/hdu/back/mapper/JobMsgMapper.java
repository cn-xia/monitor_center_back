package org.hdu.back.mapper;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.JobMsg;

import java.util.List;
import java.util.Map;

public interface JobMsgMapper extends BaseMapper<JobMsg>{

    JobMsg selectLastJobMsg(@Param(value = "jobId") Integer jobId, @Param(value = "dailyId") Integer dailyId);

    List<Map> getJobMsg(@Param(value = "jobId")Integer jobId);
}