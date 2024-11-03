package lol.event;

import lol.event.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

@Slf4j
@RequiredArgsConstructor
public class Play implements Runnable {

    private final CountDownLatch gameLatch;
    private final String sessionGameID;
    private final OffsetDateTime createGameDate;
    private final String ipAddr;
    private final String account;
    private final String champion;
    private final int gamePlayTime;

    @SuppressWarnings("BusyWait") // While 안에서 Thread.sleep(); 을 쓸 때, 경고 차단
    @Override
    public void run() {
        log.info("{} 님이 소환사의 협곡에 참여하였습니다. 챔피언 :: {} --> ( ip :: {} / account :: {} / sessionGameId :: {} / gamePlayTime :: {}"
                ,account, champion, ipAddr, account, sessionGameID, gamePlayTime);

        long MINIMUM_SLEEP_TIME = 50;
        long MAXIMUM_SLEEP_TIME = 60 * 300;

        // Properties 객체를 생성하여 Kafka Producer 설정을 저장
        Properties props = new Properties();

        // Kafka 클러스터의 부트스트랩 서버 주소 설정
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Main.BOOT_SERVER);

        // 클라이언트 식별자 설정 (Kafka 에서 이 Producer 를 구분하기 위해 사용)
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "League_Of_Legend");

        // 메시지 키를 String 으로 직렬화하기 위한 설정
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 메시지 값을 String 으로 직렬화하기 위한 설정
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 설정된 Properties 객체를 바탕으로 KafkaProducer 인스턴스 생성
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {

            long startTime = System.currentTimeMillis();
            int itemCount = 0;
            Integer deathCount = 0;

            while (isDuration(startTime)) {
                long sleepTime =
                        MINIMUM_SLEEP_TIME + Double.valueOf(RandomUtil.R.nextDouble() * (MAXIMUM_SLEEP_TIME))
                                .longValue();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String method = getMethod();
                OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneId.of("UTC"));

                long endTime = System.currentTimeMillis();
                String runTimeSeconds = String.valueOf((endTime - startTime) / 1000);

                Integer x_direction = getMouseX();
                Integer y_direction = getMouseY();
                String key = getPlayerInputKey();
                Integer status = getStatus();

                if (method.equals("/wait")) {
                    sendLog(sessionGameID, createGameDate, ipAddr, account, champion, method,
                            offsetDateTime, 0, 0, "0", status, deathCount, runTimeSeconds, producer);
                } else if (RandomUtil.R.nextDouble() > 0.97 && itemCount < 6) {
                    itemCount += 1;
                    sendLog(sessionGameID, createGameDate, ipAddr, account, champion, "/buyItem",
                            offsetDateTime, x_direction, y_direction, key, status, deathCount,
                            runTimeSeconds, producer);
                } else if (status == 1) {
                    sendLog(sessionGameID, createGameDate, ipAddr, account, champion, method,
                            offsetDateTime, x_direction, y_direction, key, status, deathCount,
                            runTimeSeconds, producer);
                    status = 0;
                    deathCount += 1;
                } else {
                    sendLog(sessionGameID, createGameDate, ipAddr, account, champion, method,
                            offsetDateTime, x_direction, y_direction, key, status, deathCount,
                            runTimeSeconds, producer);
                }
            }
            log.info(
                    "Stopping log generator (ipAddr=" + ipAddr + ", account=" + account + ", sessionID="
                            + sessionGameID + ", champion=" + champion + ", durationSeconds="
                            + gamePlayTime);

            gameLatch.countDown();
        }
    }

    /**
     * 플레이어의 이벤트를 생성하는 메서드입니다.
     *
     * @param sessionGameID  RoomID
     * @param createGameDate Room 생성시간
     * @param ipAddr         플레이어의 IP
     * @param account        플레이어의 계정명
     * @param champion       플레이어의 챔피언(=캐릭터)
     * @param method         플레이어가 발생시킨 메서드
     * @param offsetDateTime 로그가 생성된 시간
     * @param x_direction    마우스 X값
     * @param y_direction    마우스 Y값
     * @param key            플레이어가 입력한 키
     * @param status         플레이어의 상태
     * @param deathCount     플레이어의 죽은 횟수
     * @param finalTime      Room이 진행 된 현재 시간
     * @param producer       Kafka Producer
     */
    private void sendLog(String sessionGameID, OffsetDateTime createGameDate, String ipAddr,
                                String account, String champion, String method, OffsetDateTime offsetDateTime,
                                Integer x_direction, Integer y_direction, String key, Integer status, Integer deathCount,
                                String finalTime, KafkaProducer<String, String> producer) {

        String eventLog = "{" +
                "\"gameID\": \"" + sessionGameID + "\"," +
                "\"createGameDate\": \"" + createGameDate + "\"," +
                "\"ip\": \"" + ipAddr + "\"," +
                "\"account\": \"" + account + "\"," +
                "\"champion\": \"" + champion + "\"," +
                "\"method\": \"" + method + "\"," +
                "\"datetime\": \"" + offsetDateTime + "\"," +
                "\"x\": \"" + x_direction + "\"," +
                "\"y\": \"" + y_direction + "\"," +
                "\"input_key\": \"" + key + "\"," +
                "\"status\": \"" + status + "\"," +
                "\"deathCount\": \"" + deathCount + "\"," +
                "\"inGame_time\": \"" + finalTime + "\"" +
                "}";

        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(eventLog);
            String jsonLog = String.valueOf(jsonObject);

            producer.send(new ProducerRecord<>(Main.TOPIC_NAME, jsonLog));

            log.info("Event Log : {}", jsonLog);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 주어진 시간으로부터 경과하지 않았는지 확인하는 메서드입니다.
     * @param startTime 시작 시간
     * @return 현재 시간이 설정된 기간 내에 있으면 true, 그렇지 않으면 false 를 반환합니다.
     */
    private boolean isDuration(long startTime) {
        return System.currentTimeMillis() - startTime < gamePlayTime * 1000L;
    }

    /**
     * 플레이어가 발생시킨 행동을 출력하는 메서드입니다.
     * @return 15%의 확률로 wait 을 반환하고 85%의 확률로 move 를 반환합니다
     */
    private String getMethod() {
        if (RandomUtil.R.nextDouble() > 0.85) {
            return "/wait";
        } else {
            return "/move";
        }
    }

    /**
     * 플레이어의 마우스 X 좌표값을 생성히는 메서드입니다.
     * @return 플레이어는 99%로 -400 ~ 400의 X값을 반환하고 1%로 -1250 ~ 1250의 X값을 반환합니다.
     */
    private int getMouseX() {
        if (RandomUtil.R.nextDouble() > 0.99) {
            return RandomUtil.R.nextInt(1250 - (-1250) + 1) + (-1250);
        } else {
            return RandomUtil.R.nextInt(400 - (-400) + 1) + (-400);
        }
    }

    /**
     * 플레이어의 마우스 Y 좌표값을 생성히는 메서드입니다.
     * @return 플레이어는 99%로 -400 ~ 400의 Y값을 반환하고 1%로 -1250 ~ 1250의 X값을 반환합니다.
     */
    private int getMouseY() {
        if (RandomUtil.R.nextDouble() > 0.99) {
            return RandomUtil.R.nextInt(1250 - (-1250) + 1) + (-1250);
        } else {
            return RandomUtil.R.nextInt(400 - (-400) + 1) + (-400);
        }
    }

    /**
     * 사용자가 입력한 키를 생성하는 메서드입니다.
     * @return 3%로 "alt", "tab", "alt+tab", "esc", "shift", "enter"를 반환하고 20%로 d f null 그리고 그 외엔 q w
     * e r space b 를 반환합니다
     */
    private String getPlayerInputKey() {
        if (RandomUtil.R.nextDouble() > 0.97) {
            String[] arrKey = new String[]{"alt", "tab", "alt+tab", "esc", "shift", "enter"};
            int num = RandomUtil.R.nextInt(arrKey.length);
            return arrKey[num];
        } else if (RandomUtil.R.nextDouble() > 0.80) {
            String[] arrKey = new String[]{"d", "f", "null"};
            int num = RandomUtil.R.nextInt(arrKey.length);
            return arrKey[num];
        } else {
            String[] arrKey = new String[]{"q", "w", "e", "r", "space", "b"};
            int num = RandomUtil.R.nextInt(arrKey.length);
            return arrKey[num];
        }
    }

    /**
     * 사용자의 상태를 생성하는 메서드입니다. 상태는 플레이어의 생존 여부를 의미하고, status = 0일 경우 사망 status = 1일 경우 생존
     * @return 3%로 1을 반환하고, 그 외는 0을 반환합니다.
     */
    private int getStatus() {
        if (RandomUtil.R.nextDouble() > 0.97) {
            return 1;
        } else {
            return 0;
        }
    }
}
