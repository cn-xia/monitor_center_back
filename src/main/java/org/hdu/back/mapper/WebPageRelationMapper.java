package org.hdu.back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.WebPageRelation;

public interface WebPageRelationMapper extends BaseMapper<WebPageRelation>{

	void batchInsert(@Param(value="relationLs")List<WebPageRelation> relationLs);
    
}