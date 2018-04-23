CREATE TABLE `job_msg` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `job_id` int(10) DEFAULT NULL COMMENT 'job_info的id',
  `crawler_count` int(10) DEFAULT NULL COMMENT '当前爬取到的数量',
  `inc_crawler_count` int(10) DEFAULT NULL COMMENT '比上一次状态增加的数量',
  `save_count` int(10) DEFAULT NULL COMMENT '入库的数量',
  `inc_save_count` int(10) DEFAULT NULL COMMENT '比上一次状态新增入库的数量',
  `cpu` varchar(10) DEFAULT NULL COMMENT 'cpu状态',
  `ram` varchar(10) DEFAULT NULL COMMENT '内存状态',
  `monitor_time` timestamp NULL DEFAULT NULL COMMENT '监控时间',
  `daily_id` int(10) DEFAULT NULL COMMENT '日报id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='爬虫任务的实时状态信息';

