### 카프카 네트워크 구축
 - docker network create kafka

### Topic 지우기
 - docker exec kafka-worker-01 rm -rf /tmp/kafka-logs/league-of-legend-*
 - docker exec kafka-worker-02 rm -rf /tmp/kafka-logs/league-of-legend-*
 - docker exec kafka-worker-03 rm -rf /tmp/kafka-logs/league-of-legend-*

### Topic 생성
 - docker exec kafka-worker-01 kafka-topics --create --topic league-of-legend --bootstrap-server kafka-worker-01:19092 --partitions 3 --replication-factor 3

### 도커 Stop -> Remove
 - docker stop $(docker ps -aq)
 - docker rm $(docker ps -aq)

### 도커 실행
 - docker-compose -f zookeeper-docker-compose.yml up -d
 - docker-compose -f kafka-docker-compose.yml up -d

### Jar 실행
 - java -jar lol-champion-event-generator-1.0-SNAPSHOT-all.jar 10 127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094

### 컨슈머 로그 확인
 - docker exec -it kafka-worker-01 kafka-console-consumer --bootstrap-server 127.0.0.1:19092 --topic league_of_legend --from-beginning




