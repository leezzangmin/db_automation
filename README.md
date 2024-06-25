
# AWS Multi-Account + MSA 기반 MySQL DBMS 운영 자동화 플랫폼 엔지니어링

[프로젝트가 설명된 포스팅 링크](https://leezzangmin.tistory.com/63)
****  


## DDL 수행 자동화

### 1. 컬럼 추가/삭제/변경, 인덱스 추가/삭제, 테이블 생성 등에 대한 컨벤션 검토와 실행을 자동으로 수행해줍니다.
    DB 엔지니어가 sustaining 성격의 업무들로 모든 리소스를 소비할 수는 없음
    단순 DDL 수행을 위해 하루에서 길게는 일주일까지 검토와 커뮤니케이션에 비용 소모
    개발자들이 직접 DDL을 안전하게 실행할 수 있게 되어 커뮤니케이션 비용 하락, 개발 속도 향상
    작업 중 DBMS 서버의 부하가 높아지면 자동으로 작업을 중단
    작업 전 테이블의 용량을 검사해서 기준 이상이면 실행하지 않음
    DDL 실행 중 메타데이터 락으로 인해 신규 세션이 block 되는 경우 자동 kill (https://leezzangmin.tistory.com/51) 
    DDL 실행 전 롱쿼리가 존재하면 실행하지 않음
    1대의 DBMS 당 global하게 동시에 실행되는 DDL 작업은 1개로 제한
    작업 가능 시간을 설정하여 그 외 시간에는 작업을 block (서비스 도메인에 따라 부하가 높은 시간으로 설정 가능)
    작업 시작, 종료, 실패 시 슬랙으로 알림
### 2. 클러스터 파라미터 그룹, 클러스터 생성 변수, 스키마 구조에 대한 표준을 정의하고 준수 여부를 주기적으로 검사해서 비표준값들을 slack으로 전송합니다.
    운영 중 마주치게 되는 비표준 설정값들로 인한 오버헤드를 사전에 파악하고 줄이기 위함
    클러스터 파라미터 그룹 변수(ex.`max_connetions`, `character_set_database` 등) 중 DB팀 표준과 다른 값이 있으면 알림
    클러스터 생성 변수(ex.`DeletionProtection`, `BackupRetentionPeriod` 등) 중 DB팀 표준과 다른 값이 있으면 알림
    인스턴스 생성 변수(ex. `AutoMinorVersionUpgrade`, 태그존재 여부 등) 중 DB팀 표준과 다른 값이 있으면 알림
    스키마 생성 표준(ex. 네이밍 컨벤션, COMMENT 필수 기입, CHARSET 및 COLLATE 등)에 어긋나는 테이블 및 컬럼이 있으면 알림
### 3. DBMS 목록 조회 & 각 DBMS의 상태 및 변경 히스토리를 조회할 수 있습니다.
    AWS SDK 를 사용하여 계정의 모든 DBMS 클러스터 목록을 조회
    클러스터의 버전, CPU, Memory, Storage, Connection, QPS 등의 상태 조회
    스키마, 테이블 목록 및 용량 등 조회
    테이블 스키마의 변경 히스토리 조회

### 4. PROD <-> STAGE 간 스키마 차이점을 자동으로 검사합니다. (table, view, function, procedure, trigger 등)
    사람의 손으로 운영하는 DB라서 이슈 대응 등의 긴급한 상황에서는 수동으로 스키마를 조작하게 됨
    이런 히스토리는 추적하기 어려워서 prod와 stage, dev 간의 스키마가 불일치한 상태로 서비스가 지속됨
    미리 파악하기 힘든 장애를 방지하기 위해 각 환경의 스키마 차이점을 검사
    object_name, 컬럼 및 제약조건 개수, charset, collation, comment, routine_definition, security_type, definer 등
    검사한 결과는 슬랙으로 전달되어 히스토리 관리가 가능


****  

## 시연 영상 및 샘플

[샘플](https://github.com/leezzangmin/db_automation/assets/64303390/da03e152-6d7e-4cb3-aadc-8c9a5cdc02c7)



### 3. DB 표준 검사 샘플

<details>  
<summary>DB 표준 목록</summary>
<div markdown="1">  

1. 클러스터 생성 표준값
   ```java
    public class ClusterCreationStandard {
    
      public final static Map<String, String> clusterCreationStandard = new HashMap<>();
    
      static {
          clusterCreationStandard.put("BackupRetentionPeriod", "7");
          clusterCreationStandard.put("MultiAZ", "true");
          clusterCreationStandard.put("DeletionProtection", "true");
          clusterCreationStandard.put("Engine", "aurora-mysql");
          clusterCreationStandard.put("EngineVersion", "8.0.mysql_aurora.3.03.1");
          clusterCreationStandard.put("Port", "3306");
          clusterCreationStandard.put("MasterUsername", "admin");
      }
    }
    ```

2. 인스턴스 생성 표준값
    ```java
    public class InstanceCreationStandard {
    
        public final static Map<String, String> instanceCreationStandard = new HashMap<>();
    
        static {
            instanceCreationStandard.put("AutoMinorVersionUpgrade", "false");
            instanceCreationStandard.put("DeletionProtection", "true");
            instanceCreationStandard.put("PerformanceInsightsEnabled", "true");
            instanceCreationStandard.put("EnabledCloudwatchLogsExports", "[slowquery]");
            instanceCreationStandard.put("TagList", "[]");
        }
    }
    ```

3. 파라미터 표준값
      ```java
      public class ParameterGroupStandard {
          public final static Map<String, String> standardParameters = new HashMap<>();
          
          static {
              standardParameters.put("max_connections", "10000");
              standardParameters.put("character_set_connection", "utf8mb4");
              standardParameters.put("character_set_database", "utf8mb4");
              standardParameters.put("character_set_filesystem", "utf8mb4");
              standardParameters.put("character_set_server", "utf8mb4");
              standardParameters.put("character_set_results", "utf8mb4");
              standardParameters.put("collation_connection", "utf8mb4_0900_ai_ci");
              standardParameters.put("collation_server", "utf8mb4_0900_ai_ci");
              standardParameters.put("slow_query_log", "1");
              standardParameters.put("time_zone", "UTC");
              standardParameters.put("transaction_isolation", "REPEATABLE-READ");
              standardParameters.put("performance_schema", "1");
          }
      }
      ```
</div>
</details>
<br>

<details>
<summary>표준 검사 동작 예시</summary>
<div markdown="1">
    
![스크린샷 2023-07-24 오후 6 38 46](https://github.com/leezzangmin/db_automation/assets/64303390/1bd077b4-25e1-4457-ad27-19255064b505)

</div>
</details>


[//]: # (****  )
[//]: # (## 아키텍처)

****  


## 사용 기술 및 도구
- Java 17 & SpringBoot 3 & Junit
- AWS SDK (for java)
- Slack API, SDK (interactivity + block kit)

## 실행 방법
- 환경변수에  설정
  - ENCRYPT_KEY, SLACK_TOKEN, SLACK_APP_SIGNING_SECRET, SLACK_DEFAULT_CHANNEL_ID, SLACK_VERIFICATION_TOKEN, SLACK_ADMIN_USER_IDS
- application.properties 파일에 inhouse용 DB 접속 정보 기재 필요
- 실행하는 환경에서 aws configure 사전 수행 필요
  - 해당 롤에 rds, secret manager, cloudwatch, performance insights 권한 필요
  - profile name이 겹치지 않게 여러 프로필 설정 가능
- Database credential 은 aws Secret Manager에 등록 필요
  - postfix 는 `-db-credential` 로 지정
- 클러스터 필수 설정 태그
  - env: [prod, stage, dev] (환경 명)
  - service: [user, shop, order] (서비스 명)
- profiles 설정 필요 (dev, stage, prod 등)
- schema.sql 스크립트 미리 수행
- performance_schema 활성화
- slack app 설정 및 https 도메인 활성화 필요
  - slash comamnd 등록 (SlackController.java -> databaseRequestCommand())
  - interactivity enable
  - callback url 설정
  - slack channel 에 bot add



<br>
<br>
<br>
<br>

## 영감
레인 캠벨과 채리티 메이저즈의 [Database Reliability Engineering: Designing and Operating Resilient Database Systems](O'Reilly, 2017)와 같은 책을 통해 기술 조직은 데이터베이스 엔지니어를 모든 데이터베이스의 단독 운영자가 아닌 비즈니스 성장의 주체로 보게 되었습니다. 한때 DBA의 기본 일상은 스키마 디자인 및 쿼리 최적화와 관련되어 있었지만, 이제는 개발자에게 이러한 기술을 가르치고 개발자가 자신의 스키마 변경사항을 안전하게 배포할 수 있도록 시스템을 관리하는 일을 담당합니다. - [실비아 보트로스 - MySQL 성능 최적화](O'Reilly, 2021) 중


[//]: # (- metadata 만 변경하는 작업은 즉시 실행&#40;리스트업 필요&#41;)

[//]: # (  - rename)

[//]: # (  - comment 수정)

[//]: # (- 개발자 메일계정으로 접근가능 클러스터 인증/인가)

[//]: # (- 예외처리 및 예외응답 일원화)

[//]: # (- 용량 크면 slack 으로 실행버튼을 정보랑 함께 전송해서&#40;스키마, 테이블, 커맨드타입 등&#41; pt-change-online shell 실행?)

[//]: # (- DDL 실행중 진행상황 Performance schema 통해서 확인.)

[//]: # (- 인덱스 &#40;col1, col2&#41; 있을때 &#40;col1&#41; 인덱스 신규생성 block 기능, pk 포함 인덱스 생성 방지)

[//]: # (- 작업 실패할 때 슬랙으로 알림 &#40;작업자, 작업시간, 작업내용, 작업커맨드, 에러메시지&#41;)
