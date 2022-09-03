create table genshin_info
(
    id        bigint auto_increment
        primary key,
    uid       varchar(20) null,
    qq_number varchar(25) null,
    nick_name varchar(50) null,
    cookie    text        null,
    push      int         null,
    deletes   int         null
);
