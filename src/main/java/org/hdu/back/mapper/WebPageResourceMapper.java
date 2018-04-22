package org.hdu.back.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.WebPageResource;

public interface WebPageResourceMapper extends BaseMapper<WebPageResource>{

	/**
	 * 批量插入网页资源
	 * @param resourceLs
	 */
	void batchInsert(@Param(value = "resourceLs") List<WebPageResource> resourceLs);
}