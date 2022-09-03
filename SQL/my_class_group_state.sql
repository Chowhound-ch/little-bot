create table group_state
(
    id           bigint auto_increment
        primary key,
    group_number varchar(25)   not null,
    state        int default 0 null,
    constraint group_state_group_number_uindex
        unique (group_number),
    constraint group_state_id_uindex
        unique (id)
);

