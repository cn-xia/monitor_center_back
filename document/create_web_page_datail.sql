drop table if exists web_page_detail;

/*==============================================================*/
/* Table: web_page_detail                                       */
/*==============================================================*/
create table web_page_detail
(
   id                   bigint(15) not null,
   url_md5              char(32),
   url                  varchar(2000),
   domain               varchar(200),
   title                varchar(200),
   src                  varchar(200),
   create_time          timestamp,
   author               varchar(200),
   keyword              varchar(200),
   tags                 varchar(200),
   content              mediumtext,
   html                 mediumtext,
   view_num             int(10),
   comment_num          int(10),
   crawl_time           timestamp,
   update_time          timestamp,
   primary key (id)
);

alter table web_page_detail comment '网页详情表';
