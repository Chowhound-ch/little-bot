create table permit_detail
(
    id        bigint auto_increment
        primary key,
    qq_number varchar(25) not null,
    permit    int         not null,
    constraint permit_detail_id_uindex
        unique (id),
    constraint permit_detail_qq_number_uindex
        unique (qq_number)
);

