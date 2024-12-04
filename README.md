Spring Boot Project, gradle, 상품에 대한 캐시 기능, Redis와 MariaDB 이용, Redis의 Bloom Filter -> Redis Cache -> RDB(JPA MariaDB) 순서로 조회하도록 설계 

curl -X POST http://localhost:8080/api/products -H Content-Type: application/json -d {"name":"Test Product","price":10000,"description":"This is a test product"}   
curl -X GET http://localhost:8080/api/products/1   
curl -X DELETE http://localhost:8080/api/products/1   

프로젝트의 기능은 처음 저장 후에 bloom, redis cache, db에 다 저장을 잘 하고 추후에 조회시 bloom에 조회하려고 했을 때 존재하지 않으면 bloom의 특성으로 인해 조회 결과가 없다고 판단하고, bloom의 특성상 있다고 판단할 경우에 cache를 조회하지만 오탐이 있을 경우엔 DB를 조회하는 형태

1. 저장 (save) 시:
    * DB에 데이터 저장
    * Redis 캐시에 저장 (24시간 TTL)
    * Bloom Filter에 ID 추가
2. 조회 (findById) 시:
    * 첫 번째 단계: Bloom Filter 확인
        * Bloom Filter에 없다고 판단되면 → 확실히 데이터가 없음을 알 수 있어 바로 empty 반환
        * Bloom Filter에 있다고 판단되면 → 다음 단계로 진행 (false positive 가능성 있음)
    * 두 번째 단계: Redis Cache 확인
        * 캐시에서 찾으면 → 바로 반환
        * 캐시에서 못 찾으면 → 다음 단계로 진행
    * 세 번째 단계: DB 조회
        * DB에서 찾으면 → Redis에 다시 캐싱하고 Bloom Filter에도 추가
        * DB에서도 못 찾으면 → empty 반환
3. 삭제 (delete) 시:
    * DB에서 삭제
    * Redis 캐시에서 삭제
    * Bloom Filter는 삭제 연산을 지원하지 않아 그대로 유지 (false positive 가능성 증가)
이러한 구조의 장점은:
1. Bloom Filter를 통한 빠른 negative 판단
2. Redis 캐시를 통한 빠른 조회
3. DB 부하 감소
단, Bloom Filter의 특성상:
* false positive는 발생할 수 있음 (있다고 했는데 실제로는 없을 수 있음)
* false negative는 절대 발생하지 않음 (없다고 했으면 확실히 없음)
* 삭제 연산을 지원하지 않아 시간이 지날수록 false positive 비율이 증가할 수 있음
