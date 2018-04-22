package org.hdu.back.mapper;

import org.hdu.back.model.JobException;

public interface JobExceptionMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(JobException record);

    int insertSelective(JobException record);

    JobException selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(JobException record);

    int updateByPrimaryKey(JobException record);
}