CREATE TABLE `web_page_relation` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT,
  `url` varchar(500) DEFAULT NULL,
  `src_url` varchar(1000) DEFAULT NULL,
  `crawl_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='ÍøÒ³¹ØÏµ±í'