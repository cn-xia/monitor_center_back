CREATE TABLE `job_daily` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `job_id` int(10) DEFAULT NULL COMMENT '对应的任务',
  `start_time` timestamp NULL DEFAULT NULL COMMENT '当天任务开始时间',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '当天任务结束时间',
  `spend_time` varchar(10) DEFAULT NULL COMMENT '当日任务花费时间',
  `crawler_time` varchar(10) DEFAULT NULL COMMENT '当天爬虫阶段花费多少时间（"hh:mm:ss"）',
  `total_count` int(10) DEFAULT NULL COMMENT '当日该类型总爬取量',
  `total_sold` bigint(20) DEFAULT NULL COMMENT '当日总销售量',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '日报生成时间',
  `job_interval` int(4) DEFAULT NULL COMMENT 'msg的时间间隔，单位分钟',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='爬虫任务日报';

