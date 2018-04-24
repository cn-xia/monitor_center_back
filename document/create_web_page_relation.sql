drop table if exists web_page_relation;

/*==============================================================*/
/* Table: web_page_relation                                     */
/*==============================================================*/
create table web_page_relation
(
   id                   bigint(15) not null,
   url                  text,
   src_url              text,
   crawl_time           timestamp,
   update_time          timestamp,
   primary key (id)
);

alter table web_page_relation comment '网页关系表';
