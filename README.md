# SpringDB

## 1- JDBC 이해

<br>

- **등장 배경**
    - 각각의 DB마다 커넥션 연결, SQL 전달, 결과 응답 방법이 다름
        - DB를 다른 종류의 DB로 변경 시 개발에 사용된 DB 코드도 함께 변경되어야 함
        - 개발자가 각각의 DB마다 사용법을 학습해야 함
    
    ---
    
- **JDBC 표준 인터페이스 기능**
    - java.sql.Connection: 연결
    - java.sql.Statement: SQL 내용
    - java.sql.ResultSet: SQL 요청 응답
    - 구현 → JDBC 드라이버 (MySQL, Oracle…)
    - DB 변경 시 드라이버만 변경하면 됨
    
    ---
    
    - **표준화의 한계**
        - 페이징 SQL은 각 DB마다 사용법이 다름
        - SQL은 해당 DB에 맞게 변경해야 함
    
    ---
    
- **최신 데이터 접근 기술**
    - SQL Mapper (MyBatis, Jdbc Template)
        - 장점
            - SQL 응답 결과를 객체로 편리하게 반환 및 반복 코드 제거
            - JDBC 편리하게 사용하도록 도와줌
        - 단점
            - 개발자가 직접 SQL을 작성해야 함
        
        ---
        
    - ORM (JPA, Hibernate)
        - 객체를 관계형 DB 테이블과 매핑 → SQL 작성 필요 X
        - 깊이 있는 학습이 필요 → 난이도 高
    
    ---
    
- **DB 연결**
    - DB에 연결하려면 JDBC가 제공하는 DriverManager.getConnection( )를 이용
        - DriveManager: DB 드라이버 관리
    - 라이브러리에 있는 DB 드라이버를 찾아서 실제 DB와 커넥션을 맺고 결과를 반환
    
    ---
    
- **데이터 등록, 수정, 삭제**
    - PreparedStatement: ‘?’를 통한 파라미터 바인딩 허용 → SQL Injection 공격 예방
    - con.prepareStatement(sql): db에 전달할 sql과 파라미터로 전달할 데이터를 준비
    - pstmt.executeUpdate( ): ‘statement’를 통해 db에 전달, int 반환(row 수)
        
        ---
        
    - **리소스 정리: finally 문**
        - 쿼리 실행이 끝난 후 리소스 정리( close 문 작성) 필요
        - 작성한 순서의 역순으로 리소스를 정리
        - 커넥션이 끊어지지 않고 계속 유지되는 문제 방지
    
    ---
    
- **데이터 조회**
    - rs=pstmt.executeQuery( ): 데이터 조회시 사용  → ResultSet에 담아서 반환
        - @RequestParam or @ModelAttribute
    - **ResultSet**
        - select 쿼리의 결과가 순서대로 들어감
        - 내부 커서를 이동해 다음 데이터를 조회 가능(최소 한번의 호출 필요)

    <hr><br>

## 2- 커넥션 풀과 데이터소스 이해

<br>

- **커넥션 풀 필요성**
    - 데이터베이스 커넥션을 획득할 때마다 DB 드라이버가 커넥션 객체를 생성함
    - 이는 과정도 복잡하고 시간도 많이 소모됨
    - 따라서 APP 시작 시점에 커넥션 풀을 필요한 만큼 생성함
    
    ---
    
- **커넥션 풀**
    - APP 로직에서는 커넥션 풀에 생성된 커넥션을 객체 참조로 가져다 씀
    - 커넥션을 사용하고 나면 종료하는 것이 아니라 그대로 커넥션 풀에 반환함
    - 동시에 여러 요청이 들어오면 여러 쓰레드에서 커넥션 풀의 커넥션을 가져감
    - 실무에서 항상 기본으로 사용, 대부분 hikariCP 사용
    
    ---
    
- **DataSource**
    - 기존 문제: DriverManger → 커넥션 풀 변경 시 코드를 수정해야 함
    - DataSource는 커넥션을 획득하는 방법을 추상화하는 인터페이스
    - 대부분의 커넥션 풀은 DataSource 인터페이스를 구현하여 이에 의존
    - 스프링은 DriverManager 또한 DataSource를 통해 사용할 수 있도록DriverManagerDataSource라는 구현 클래스를 제공함
    - DI + OCP 만족
    
    ---
    
- **설정과 사용의 분리 필요**
    - DriverManager는 커넥션을 획득할 때 마다 파라미터 전달 필요
    - DataSource는 처음 객체를 생성할 때만 필요 → 권장 O
    
    ---
    
- **커넥션 풀 생성작업**
    - 애플리케이션 속도에 영향을 주지 않기 위해 별도의 쓰레드에서 작동
        - 미리 생성 시 APP 실행 시간이 늦어짐
    - 대기 시간 (Thread.Sleep) 필요
 
  <hr><br>

## 3- 트랜잭션 이해

---

- **트랜잭션**
    - 하나의 거래를 안전하게 처리하도록 보장해주는 것
    - 하나라도 실패 시 되돌릴 수 있음(모두 성공하거나 모두 실패해야 함: 원자성)
    - **ACID**
        - 트랜잭션은 원자성, 일관성, 격리성, 지속성을 보장해야함
        - 격리성은 완벽한 보장이 어려워 트랜잭션 격리 수준 4단계로 나눔
            - READ COMMITTED를 거의 사용함
    
    ---
    
- **DB 연결 구조**
    - 클라이언트가  서버에 연결 요청 → 커넥션 생성 → 세션 생성 → 트랜잭션 시작, 종료 반복 → 커넥션 종료 → 세션 종료
    - 커넥션 풀에 의해 만들어진 커넥션의 개수 = 세션의 개수
    
    ---
    
- **트랜잭션 사용법**
    - COMMIT 호출 전까지는 임시로 데이터를 저장 → 다른 세션에서 확인 불가
    - ROLLBACK 호출 시 트랜잭션 시작 전으로 복구
    
    ---
    
- **수동커밋 설정**
    - 대부분 자동 커밋 모드가 기본으로 설정되어있음
    - 자동 커밋 모드 → 수동 커밋 모드 전환(설정): 트랜잭션의 시작
    - 수동 커밋 설정 시 commit 혹은 rollback 반드시 필요함
    - 한번 설정 시 해당 세션에서 계속 유지 & 중간에 변경 가능
    
    ---
    
- **DB 락**
    - 동시에 같은 데이터에 대한 수정 문제 방지: 원자성 깨짐 문제 방지
    - 락 획득 시 다른 세션은 락이 돌아올 때 까지 대기
    - Commit or Rollback 실행 시 락을 다음 순위 세션에 넘김 (획득/ 반납)
    - 락 대기 시간 초과 시 락 TIMEOUT 오류 발생(TIMEOUT 설정 가능)
    - 조회 & 락 획득: select for update 구문 사용
    
    ---
    
- **비즈니스 로직과 트랜잭션**
    - 트랜잭션은 비즈니스 로직이 있는 서비스 계층에서 시작해야함
        - 문제가 되는 부분을 함께 롤백해야 되기 때문
    - 트랜잭션을 사용하는 동안 같은 트랜잭션 유지 필요
        - 커넥션을 파라미터로 전달
        - 커넥션은 서비스 로직 종료시 닫음 (레포지토리에서 종료 X)
    - 트랜잭션 관리 로직과 실제 비즈니스 로직은 구분이 필요함
        
        ---
        
    - **문제점**
        - 직접 트랜잭션 관리 로직을 작성하는 것은 서비스 계층이 지저분해짐
        - 커넥션 유지 코드 변경의 어려움
     
  <hr><br>
