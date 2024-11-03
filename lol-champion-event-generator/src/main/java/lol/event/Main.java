package lol.event;

import lol.event.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@Slf4j
public class Main {

    // Final Common Code
    static final Integer PLAYER_LIMIT = 10; // league of legend 는 1 Game 당, 10명의 Player 가 참여함.
    static final String TOPIC_NAME = "league_of_legend";

    // Variable Common Code
    static Integer USER_NUM = 0;
    static String BOOT_SERVER = null;

    public static void main(String[] args) {
        log.info("[ league of legend 가 실행됩니다. ]");

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

        // Common Code Setting
        USER_NUM = Integer.parseInt(args[0]);
        BOOT_SERVER = args[1];

        // Game Data Collection
        collectGameData();

    }

    public static void collectGameData() {

        int gameCount = USER_NUM / PLAYER_LIMIT;

        CountDownLatch latch = new CountDownLatch(gameCount);
        ExecutorService executor = Executors.newFixedThreadPool(gameCount);

        IntStream.range(0, gameCount).forEach(j -> {
            String sessionGameID = SessionUtil.getSessionGameID();
            OffsetDateTime createGameDate = OffsetDateTime.now(ZoneId.of("UTC"));
            executor.execute(() -> {
                new Game(sessionGameID, createGameDate, latch).run();
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("[ ERROR ] : " + e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the thread
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown as tasks did not complete in time.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("[ ERROR ] during shutdown: " + e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}