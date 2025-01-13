package lol.event.util;

import lol.event.Main;
import lol.event.vo.PlayerRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class kafkaLogSendUtil {

    public static void sendLog(PlayerRecord player, CountDownLatch gameLatch) {

        // Properties 객체를 생성하여 Kafka Producer 설정을 저장
        Properties props = getProperties();

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            String eventLog = convertPlayerRecordToJson(player);

            log.info("eventLog :: {}", eventLog);

            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(eventLog);
                String jsonLog = String.valueOf(jsonObject);

                log.info("Kafka Producer Send Event Log : {}", jsonLog);
                producer.send(new ProducerRecord<>(Main.TOPIC_NAME, jsonLog));

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            gameLatch.countDown();
        }

    };

    private static String convertPlayerRecordToJson(PlayerRecord playerRecord) {
        return "{" +
                "\"gameID\": \"" + playerRecord.gameSessionId() + "\"," +
                "\"uniqueId\": \"" + playerRecord.uniqueId() + "\"," +
                "\"email\": \"" + playerRecord.email() + "\"," +
                "\"password\": \"" + playerRecord.password() + "\"," +
                "\"tier\": \"" + playerRecord.tier() + "\"," +
                "\"sex\": \"" + playerRecord.sex() + "\"," +
                "\"age\": \"" + playerRecord.age() + "\"," +
                "\"nickName\": \"" + playerRecord.nickName() + "\"," +
                "\"team\": \"" + playerRecord.team() + "\"," +
                "\"ip\": \"" + playerRecord.ip() + "\"," +
                "\"champion\": \"" + playerRecord.champion() + "\"," +
                "\"kill\": \"" + playerRecord.kill() + "\"," +
                "\"death\": \"" + playerRecord.death() + "\"," +
                "\"assist\": \"" + playerRecord.assist() + "\"," +
                "\"gamePlayTime\": \"" + playerRecord.gamePlayTime() + "\"," +
                "\"isWin\": \"" + playerRecord.isWin() + "\"," +
                "\"createGameDate\": \"" + playerRecord.createGameDate() + "\"" +
                "}";
    }

    private static Properties getProperties() {
        Properties props = new Properties();

        // Kafka 클러스터의 부트스트랩 서버 주소 설정
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Main.BOOT_SERVER);

        // 클라이언트 식별자 설정 (Kafka 에서 이 Producer 를 구분하기 위해 사용)
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "League_Of_Legend");

        // 메시지 키를 String 으로 직렬화하기 위한 설정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 메시지 값을 String 으로 직렬화하기 위한 설정
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return props;
    }

}
