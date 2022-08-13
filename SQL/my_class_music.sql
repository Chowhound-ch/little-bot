create table music
(
    id         bigint auto_increment,
    audio_name varchar(50)              not null,
    song_name  varchar(50)              not null,
    author     varchar(50) default '未知' not null,
    img_url    varchar(100)             null,
    time       int                      not null,
    tip        varchar(100)             null,
    md5        varchar(50)              not null
        primary key,
    constraint music_audio_name_uindex
        unique (audio_name),
    constraint music_id_uindex
        unique (id),
    constraint music_md5_uindex
        unique (md5)
);

