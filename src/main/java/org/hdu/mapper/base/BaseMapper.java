package org.hdu.mapper.base;

import org.hdu.model.JobInfo;

public interface BaseMapper<Model> {

    int deleteByPrimaryKey(Integer id);

    int insert(Model record);

    int insertSelective(Model record);

    Model selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Model record);

    int updateByPrimaryKey(Model record);

}
