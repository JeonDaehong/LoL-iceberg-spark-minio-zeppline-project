import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{StructType, StringType, IntegerType, TimestampType}
import org.apache.spark.sql.functions.{from_json, col}

object KafkaSparkApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("KafkaSparkApp")
      .master("local[*]")
      .getOrCreate()

    val hadoopConf = spark.sparkContext.hadoopConfiguration
    hadoopConf.set("fs.s3a.access.key", "minioadmin")
    hadoopConf.set("fs.s3a.secret.key", "minioadmin")
    hadoopConf.set("fs.s3a.endpoint", "http://localhost:13579")
    hadoopConf.set("fs.s3a.path.style.access", "true")
    hadoopConf.set("fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    hadoopConf.set("fs.s3a.connection.ssl.enabled", "false")

    val kafkaBrokers = "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094"

    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", "league-of-legend")
      .option("startingOffsets", "latest")
      .option("failOnDataLoss", "false")
      .load()

    import spark.implicits._
    val jsonSchema = new StructType()
      .add("gameID", StringType)
      .add("method", StringType)
      .add("ip", StringType)
      .add("input_key", StringType)
      .add("deathCount", StringType)
      .add("inGame_time", StringType)
      .add("datetime", StringType)
      .add("x", StringType)
      .add("y", StringType)
      .add("createGameDate", StringType)
      .add("account", StringType)
      .add("champion", StringType)
      .add("status", StringType)

    val messages = df.withColumn("jsonString", col("value").cast(StringType))
    messages.printSchema()

    val parsedMessages = messages.select(from_json($"jsonString", jsonSchema).as("data")).select("data.*").coalesce(1)

    val minioOutputPath = "s3a://ice-berg/test"

    val query = parsedMessages.writeStream
      .outputMode("append")
      .format("parquet")
      .option("checkpointLocation", "s3a://ice-berg/test")
      .option("maxRecordsPerFile", 10000)
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start(minioOutputPath)

    query.awaitTermination()
  }
}

