drop table if exists web_page_resource;

/*==============================================================*/
/* Table: web_page_resource                                     */
/*==============================================================*/
create table web_page_resource
(
   id                   bigint(15) not null,
   url                  text,
   resource_url         text,
   resource_type        smallint(2),
   crawl_time           timestamp,
   update_time          timestamp,
   primary key (id)
);

alter table web_page_resource comment '网页资源表';
