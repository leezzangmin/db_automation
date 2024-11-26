CREATE DATABASE IF NOT EXISTS back_office;
CREATE DATABASE IF NOT EXISTS test_schema;

CREATE TABLE IF NOT EXISTS back_office.change_history (
    id bigint primary key auto_increment,
    command_type varchar(64) not null comment '요청 타입 ex) `add index`',
    database_identifier varchar(255) not null,
    schema_name varchar(255) not null,
    table_name varchar(255) not null,
    doer varchar(255) not null,
    do_date_time datetime not null,
    change_content_sql text not null
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
    command_type varchar(64) not null comment '요청 타입 ex) `add index`',
    command text not null comment '요청 커맨드 ex) `create index...`',
    database_identifier varchar(255) not null comment '대상 DB서버명',
    schema_name varchar(255) not null comment '대상 스키마명',
    table_name varchar(255) not null comment '대상 테이블명',
    request_datetime datetime not null comment '요청 시간',
    perform_datetime datetime not null comment '실제 수행 시간(시작시간)',
    execution_duration double not null comment '소요 시간(초) ex) `12.403`(초)'

) comment '요청 내역(개발팀 DB요청 로그)';

CREATE TABLE IF NOT EXISTS back_office.mysql_account(
    id bigint primary key auto_increment,
    service_name varchar(255) not null comment '서비스명 ex. groo, pe, udc',
    host varchar(255) not null comment '호스트 ex.10.100.0.0/255.255.255.0',
    user varchar(255) not null comment '계정명 ex.august'
);

CREATE TABLE IF NOT EXISTS back_office.mysql_account_privilege(
    id bigint primary key auto_increment,
    mysql_account_id bigint not null comment 'mysql account id',
    database_name varchar(255) not null comment '대상 database 이름 (mysql, information_schema, * 등)',
    object_name varchar(255) not null comment '대상 object(table,view,function,trigger,procedure 등) 이름',
    permission_type varchar(255) not null comment '권한 타입 (SELECT, INSERT, UPDATE, DELETE, ALL, USAGE 등)'
);

CREATE TABLE IF NOT EXISTS back_office.monitor_target_database(
    id bigint primary key auto_increment comment '아이디',
    account_id varchar(64) not null comment 'account_id AWS',
    environment varchar(64) not null comment '환경 (dev,stage,prod)',
    service_name varchar(64) not null comment '서비스명(도메인명)',
    database_type varchar(64) not null comment '데이터베이스 타입(cluster, instance, serverless, onprem 등',
    database_name varchar(255) not null comment '데이터베이스 identifier',
    database_driver varchar(255) not null comment '데이터베이스 드라이버 클래스명 ex(jdbc)',
    writer_endpoint varchar(255) not null comment '데이터베이스 write/read url',
    reader_endpoint varchar(255) not null comment '데이터베이스 read url',
    port int not null comment '데이터베이스 포트 ex.3306',
    username varchar(64) not null comment '모니터링 DB 유저 계정명',
    password varchar(64) not null comment '모니터링 DB 유저 패스워드',
    is_monitor_target tinyint not null comment '모니터링 대상 여부 yn',
    unique(account_id, environment, database_name)
) COMMENT '모니터링 대상 DB 정보';

CREATE TABLE IF NOT EXISTS back_office.slack_user(
    id              bigint primary key auto_increment comment '아이디',
    user_slack_id   varchar(32) not null comment '슬랙 아이디 ex.U04282C8DDX',
    user_slack_name varchar(64) not null comment '슬랙 유저명 ex.홍길동(DBA)',
    user_type       varchar(32) not null comment '슬랙 유저 타입 ex.admin, normal, ...',
    expire_datetime datetime    not null comment '유저 만료 시간',
    unique(user_slack_id)
) comment '슬랙 유저 정보';

CREATE TABLE IF NOT EXISTS back_office.slack_database_request(
    id bigint primary key auto_increment comment '아이디',
    monitor_target_database_id bigint not null comment '모니터링 대상 DB 아이디',
    slack_user_id bigint not null comment '요청 유저 슬랙 아이디',

    command_type varchar(64) not null comment '요청 타입 ex) `add index`',
    request_dto_class_type varchar(64) not null comment '요청 DTO 클래스 타입',
    request_dto mediumtext not null comment '요청 DTO',

    request_uuid char(36) not null comment '요청 메타데이터 uuid', -- todo binary(16)

    request_content text null comment '요청 내용 ex.SQL 등',

    request_description mediumtext null comment '요청 부가 설명 내용',

    request_datetime datetime not null comment '요청 시간',
    execute_datetime datetime not null comment '실행 예정 시간',
    is_complete tinyint not null comment '완료 여부',
    unique (request_uuid)
) comment '슬랙 DB 요청 정보';

CREATE TABLE IF NOT EXISTS back_office.slack_database_request_approval(
    id bigint primary key auto_increment comment '아이디',
    slack_database_request_id bigint not null comment '슬랙 DB 요청 정보 아이디',
    slack_user_id bigint not null comment '응답자 슬랙 아이디',
    response_type varchar(32) not null comment '응답 타입 ex.accept,deny,',
    response_reason text null comment '응답 사유 설명',
    response_datetime datetime not null comment '응답 시간',
    unique key (slack_database_request_id, slack_user_id)
) comment '슬랙 DB 요청 응답자';
