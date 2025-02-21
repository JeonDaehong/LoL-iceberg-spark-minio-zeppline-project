# 🚀 프로젝트 개요

<img src="https://github.com/user-attachments/assets/ec8cbdf3-1abb-4da5-808e-1bdeefe976a6" width="100%">

Apache Iceberg를 중심으로 데이터 적재, 관리, 분석까지의 전 과정을 실습하는 프로젝트입니다. 
본 프로젝트를 통해 Iceberg의 다양한 기능을 활용하고, Spark Streaming, Kafka, MinIO, Zeppelin 등의 기술과 통합하여 실무 수준의 데이터 아키텍처를 구현합니다.

<br>

## 🎯 프로젝트 목표

✅ **Apache Iceberg의 이해와 활용**  
✅ **Apache Iceberg의 ACID 트랜잭션 실습**  
✅ **Apache Iceberg의 Partition Evolution 적용 및 실습**  
✅ **Apache Iceberg의 Hidden Partitioning 활용**  
✅ **Apache Iceberg의 Copy-on-Write & Merge-on-Read 비교**  
✅ **Apache Iceberg의 Time Travel 및 Rollback 활용**  
✅ **Apache Iceberg의 Schema 변경 실습**  
✅ **Spark Streaming을 활용한 데이터 적재**  
✅ **Apache Zookeeper를 활용한 Kafka 분산 환경 관리**  
✅ **Apache Kafka를 통한 데이터 수집 및 Broker, Partition 이해**  
✅ **Apache Zeppelin을 이용한 데이터 시각화**  
✅ **Java Executor Service를 활용한 병렬 데이터 생성**  
✅ **MinIO를 통한 Object Storage 관리**  
✅ **Docker Image를 활용한 프로젝트 환경 구성**  

<br>

## 🏗️ 아키텍처

![아키텍처](https://github.com/user-attachments/assets/2a8bbe2d-8179-4385-8d49-83e11fff2644)

본 프로젝트는 **Apache Kafka → Spark Streaming → Apache Iceberg (MinIO) → Apache Zeppelin** 으로 이어지는 데이터 파이프라인을 구성합니다. 
데이터의 수집, 적재, 관리, 분석까지의 전 과정을 하나의 아키텍처에서 실습할 수 있도록 설계되었습니다.

<br>

## 🛠️ 사용 기술 및 버전

| 기술 스택 | 버전 |
|-----------|------|
| **Apache Iceberg** | 1.6.1 |
| **Apache Spark** | 3.4.4 |
| **Apache Zeppelin** | 0.11.2 |
| **Apache Kafka** | 7.3.2 |
| **Apache Zookeeper** | 7.3.2 |

<br>

## 🗂️ 데이터 스키마

![데이터 스키마](https://github.com/user-attachments/assets/e3782df9-03d3-4071-a67c-43d217ecc60d)

프로젝트에서 다룰 데이터는 Apache Iceberg 테이블에 적재되며, 스키마는 필요에 따라 변경 및 진화할 수 있습니다. 
Iceberg의 Schema Evolution 및 Partition Evolution 기능을 적극 활용할 예정입니다.

<br>

## 📌 실행 방법

1️⃣ **Docker 환경 세팅**
```sh
# Docker Compose 실행
docker-compose up -d
```

2️⃣ **데이터 적재 준비(Consume)**
```sh
# Spark Streaming 실행
sbt run
```

3️⃣ **데이터 생성 및 수집**
```sh
# 데이터 생성 jar 실행
./lol-event-generator-startup.sh 5 30
```
