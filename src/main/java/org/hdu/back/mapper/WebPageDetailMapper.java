package org.hdu.back.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.hdu.back.mapper.base.BaseMapper;
import org.hdu.back.model.WebPageDetail;

public interface WebPageDetailMapper extends BaseMapper<WebPageDetail>{


    /*List<Map> getResult(@Param(value="domain")String domain, @Param(value="relation1")Boolean relation1, 
    					@Param(value="keyword1")String keyword1, @Param(value="relation2")String relation2,
    					@Param(value="keyword2")String keyword2);
*/
	List<Map> getResult(@Param(value="input1")String input1, @Param(value="select1")String select1, 
							@Param(value="relation1")String relation1, @Param(value="input2")String input2, 
							@Param(value="select2")String select2,	@Param(value="relation2")String relation2, 
							@Param(value="input3")String input3, @Param(value="select3")String select3);
    
}