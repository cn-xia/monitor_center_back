package org.hdu.back.mapper.base;

import org.hdu.back.model.JobInfo;

public interface BaseMapper<Model> {

    int deleteByPrimaryKey(Integer id);

    int insert(Model record);

    int insertSelective(Model record);

    Model selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Model record);

    int updateByPrimaryKey(Model record);

}
