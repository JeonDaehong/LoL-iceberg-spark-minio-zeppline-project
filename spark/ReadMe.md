## spark-minio-iceberg-connect

1. 스파크 다운
wget https://dlcdn.apache.org/spark/spark-3.4.4/spark-3.4.4-bin-hadoop3.tgz
tar -xzf spark-3.4.4-bin-hadoop3.tgz -C /home/daehong/spark

2. 환경변수 설정
echo "export SPARK_HOME=/home/daehong/spark/spark-3.4.4-bin-hadoop3" >> ~/.bashrc
echo "export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin" >> ~/.bashrc
source ~/.bashrc

3. 아이스버그-스파크 연결 jar 다운
wget -P /home/daehong/spark/spark-3.4.4-bin-hadoop3/jars \
https://repo1.maven.org/maven2/org/apache/iceberg/iceberg-spark-runtime-3.4_2.12/1.6.1/iceberg-spark-runtime-3.4_2.12-1.6.1.jar

4. 하둡-aws jar 다운
wget -P /home/daehong/spark/spark-3.4.4-bin-hadoop3/jars \
https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/3.3.1/hadoop-aws-3.3.1.jar

5. aws java sdk 번들 jar 다운
wget -P /home/daehong/spark/spark-3.4.4-bin-hadoop3/jars \
https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk-bundle/1.11.1026/aws-java-sdk-bundle-1.11.1026.jar

6. spark-defaults-conf 에 카탈로그 및 미니오 정보 추가
 - spark history
spark.eventLog.enabled=true
spark.history.ui.port=28888
spark.eventLog.dir=s3a://league-of-legend-history/history
spark.history.fs.logDirectory=s3a://league-of-legend-history/history
spark.hadoop.fs.s3a.path.style.access=true

 - spark connect iceberg
spark.sql.extensions=org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions
spark.sql.catalog.ice_catalog=org.apache.iceberg.spark.SparkCatalog
spark.sql.catalog.ice_catalog.type=hadoop
spark.sql.catalog.ice_catalog.warehouse=s3a://ice-berg/kafka-test

 - spark connect minio
spark.hadoop.fs.s3a.endpoint=http://127.0.0.1:13579
spark.hadoop.fs.s3a.access.key=minioadmin
spark.hadoop.fs.s3a.secret.key=minioadmin
spark.hadoop.fs.s3a.path.style.access=true
spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem
spark.driver.extraJavaOptions=-Daws.region=us-east-1 -Daws.accessKeyId=minioadmin -Daws.secretAccessKey=minioadmin
spark.executor.extraJavaOptions=-Daws.region=us-east-1 -Daws.accessKeyId=minioadmin -Daws.secretAccessKey=minioadmin

 - etc
spark.metrics.executor.executorSource.enabled=true
spark.metrics.servlet.enabled=true
spark.ui.prometheus.enabled=true
spark.metrics.appStatusSource.enabled=true
spark.metrics.executorMetricsSource.enabled=true

 - compression
spark.sql.parquet.compression.codec=gzip

7.예시 Iceberg 테이블 생성
```scala
spark.sql("CREATE TABLE IF NOT EXISTS ice_catalog.ice_db.ice_table (id BIGINT, name STRING) USING iceberg")

// 임의의 데이터 생성
val data = Seq(
  (1, "Alice"),
  (2, "Bob"),
  (3, "Charlie")
)

// 데이터를 DataFrame으로 변환
val df = data.toDF("id", "name")

// Iceberg 테이블에 데이터 삽입
df.write
  .format("iceberg")
  .mode("append")
  .save("ice_catalog.ice_db.ice_table")

// Iceberg 테이블에서 데이터 조회
spark.sql("SELECT * FROM ice_catalog.ice_db.ice_table").show()
```
