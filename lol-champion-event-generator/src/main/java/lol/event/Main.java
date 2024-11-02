package lol.event;

import lol.event.util.DataFormatUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
public class Main {

    static final Integer PLAYER_LIMIT = 10; // league of legend 는 1 Game 당, 10명의 Player 가 참여함.
    static final String IDENTIFICATION_DATE = DataFormatUtil.getToday(); // 오늘 날짜 yyyyMMdd 형식으로 표현

    public static void main(String[] args) {
        log.info("[ league of legend Game 이 실행됩니다. ]");
        log.info("args {}", Arrays.toString(args));

        // args[0] = 플레이어 수 ( 10명 단위 )
        // args[1] = 부트스트랩 서버:포트
        if (args.length < 2) {
            log.info("올바르지 않은 사용으로 인하여, 프로그램을 종료합니다.");
            log.info("사용법: java Main <플레이어 수> <bootstrap 서버:포트> 입니다.");
            System.exit(0);
        }

        if (Integer.parseInt(args[0]) % PLAYER_LIMIT != 0) { // league of legend 는 1 Game 당, 10명의 Player 가 참여함.
            log.info("올바르지 않은 사용으로 인하여, 프로그램을 종료합니다.");
            log.info("한 게임당 플레이어는 10명입니다. 10의 배수를 입력해주시기 바랍니다.");
            System.exit(0);
        }

        collectGameData(Integer.parseInt(args[0]), args[1]);

    }

    public static void collectGameData(int userNum, String server) {

        int gameCount = userNum / PLAYER_LIMIT;

        CountDownLatch latch = new CountDownLatch(gameCount);
        ExecutorService executor = Executors.newFixedThreadPool(gameCount);

        IntStream.range(0, gameCount).forEach(j -> {
            String sessionGameID = Game.getSessionGameID();
            OffsetDateTime createGameDate = OffsetDateTime.now(ZoneId.of("UTC"));
            executor.execute(() -> {
                log.info(sessionGameID);
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("[ ERROR ] : " + e.getMessage());
        } finally {
            executor.shutdown();
        }
    }


}