CREATE DATABASE IF NOT EXISTS back_office;
CREATE DATABASE IF NOT EXISTS test_schema;

CREATE TABLE IF NOT EXISTS back_office.change_history (
    id bigint primary key auto_increment,
    command_type varchar(64) not null,
    database_identifier varchar(255) not null,
    schema_name varchar(255) not null,
    table_name varchar(255) not null,
    doer varchar(255) not null,
    do_date_time datetime not null,
    change_content text not null
);

CREATE TABLE IF NOT EXISTS back_office.schema_object (
    id bigint primary key auto_increment,
    schema_object_type varchar(64) not null,
    schema_object_name varchar(64) not null,
    database_name varchar(64) not null,
    service_name varchar(64) not null,
    encrypted_json_string mediumtext not null,
    unique key (schema_object_type, schema_object_name, database_name, service_name)
);

CREATE TABLE IF NOT EXISTS back_office.request_history (
    id bigint primary key auto_increment,
    request_doer varchar(64) not null comment '요청자 닉네임 ex) `august`',
    request_type varchar(64) not null comment '요청 타입 ex) `add index`',
    command text not null comment '요청 커맨드 ex) `create index...`',
    request_datetime datetime not null comment '요청 시간',
    perform_datetime datetime not null comment '실제 수행 시간(시작시간)',
    excution_duration double not null comment '소요 시간(초) ex) `12.403`(초)'

) comment '요청 내역(개발팀 DB요청 로그)';
