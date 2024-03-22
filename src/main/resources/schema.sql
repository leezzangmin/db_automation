
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

CREATE TABLE IF NOT EXISTS back_office.schema (
    id bigint primary key auto_increment,
    schema_type varchar(64) not null,
    schema_name varchar(64) not null,
    service_name varchar(64) not null,
    encrypted_json_string mediumtext not null
);