CREATE TABLE `job_info` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `job_id` int(10) DEFAULT NULL COMMENT '任务主键',
  `job_name` varchar(20) DEFAULT NULL COMMENT '任务名称',
  `job_desc` varchar(255) DEFAULT NULL COMMENT '任务描述，包括功能和服务器信息',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
  `appkey` varchar(6) DEFAULT NULL COMMENT '作为api接口的身份标识',
  `min_count` int(10) DEFAULT NULL COMMENT '最小数量',
  `latest_time` timestamp NULL DEFAULT NULL COMMENT '最晚时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `appkey` (`appkey`) USING BTREE,
  UNIQUE KEY `jobid` (`job_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='爬虫任务基本信息表';

