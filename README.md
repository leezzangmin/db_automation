# db_automation
db 운영 자동화 백오피스 프로젝트입니다.  
컬럼 추가/삭제/변경, 인덱스 추가/삭제, 테이블 생성 등에 대한 컨벤션 검토와 실행을 자동으로 수행해줍니다.  

varchar 확장

- DB 엔지니어가 sustaining 성격의 업무들로 모든 리소스를 소비할 수는 없습니다.
- 단순 DDL 수행을 위해 하루에서 길게는 일주일까지 검토와 커뮤니케이션에 비용을 쏟아야합니다.
- 개발자들이 직접 DDL을 안전하게 실행할 수 있게 되어 커뮤니케이션 비용 하락, 개발 속도 향상





## TODO
- metadata lock detection AOP
- metadata 만 변경하는 작업은 즉시 실행(리스트업 필요)
  - rename
  - comment 수정
  - 
- 롱쿼리 있으면 pause
- 개발자 메일계정으로 접근가능 클러스터 인증/인가
- 표준검사
- timeout ?ㅇ?
- 스키마 용량, 정보, 마스킹 툴
- 예외처리 및 예외응답 일원화
- DB pw 환경변수화? - parameter store 에서 fetch



## knowledge
- create table 은 METADATA LOCK 없음
- add column 은 metadata lock 발생 (동일테이블에 한해)