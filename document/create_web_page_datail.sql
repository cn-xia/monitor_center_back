CREATE TABLE `web_page_detail` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT,
  `url` varchar(500) NOT NULL,
  `domain` varchar(200) DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `src` varchar(200) DEFAULT NULL,
  `create_time` timestamp NULL DEFAULT NULL,
  `author` varchar(200) DEFAULT NULL,
  `keyword` varchar(200) DEFAULT NULL,
  `content` mediumtext CHARACTER SET utf8mb4,
  `html` mediumtext,
  `view_num` int(10) DEFAULT NULL,
  `comment_num` int(10) DEFAULT NULL,
  `crawl_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `url_UNIQUE` (`url`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='Õ¯“≥œÍ«È±Ì'