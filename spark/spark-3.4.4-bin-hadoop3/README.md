# How to Download and Set Up Apache Spark 3.4.4

## 1. Download Apache Spark 3.4.4  
Go to [Apache Spark Archive](https://archive.apache.org/dist/spark/) and download version **3.4.4**.

## 2. Install Spark in the Docker-Compose Directory  
Extract and install Spark in the location specified in your **docker-compose** setup.

## 3. Replace `spark-defaults.conf`  
Replace the existing configuration file with the one provided in the Git repository (`conf/spark-defaults.conf`).

## 4. Download Required Dependencies from Maven  
Download the following JAR files from the **Maven repository**:  

- `aws-java-sdk-bundle-1.11.1026.jar`  
- `hadoop-aws-3.3.1.jar`  
- `iceberg-spark-runtime-3.4_2.12-1.6.1.jar`  

## 5. That's it!  
Not too difficult, right? 🚀