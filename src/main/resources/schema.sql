
CREATE DATABASE IF NOT EXISTS automation_change_history;
CREATE DATABASE IF NOT EXISTS test_schema;

CREATE TABLE IF NOT EXISTS automation_change_history.change_history (
    id bigint primary key auto_increment,
    command_type varchar(64) not null,
    database_identifier varchar(255) not null,
    schema_name varchar(255) not null,
    table_name varchar(255) not null,
    doer varchar(255) not null,
    do_date_time datetime not null,
    change_content text not null
);