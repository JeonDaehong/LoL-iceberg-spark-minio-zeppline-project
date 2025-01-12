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
    public static final Integer PLAYER_LIMIT = 10; // league of legend 는 1 Game 당, 10명의 Player 가 참여함.
    public static final String TOPIC_NAME = "league-of-legend";

    // Variable Common Code
    public static Integer GAME_COUNT = 0;
    public static String BOOT_SERVER = null;

    public static void main(String[] args) {
        log.info("[ league of legend 가 실행됩니다. ]");

        // args[0] = 게임 수
        // args[1] = 부트스트랩 서버:포트
        if (args.length < 2) {
            log.info("올바르지 않은 사용으로 인하여, 프로그램을 종료합니다.");
            log.info("사용법: java Main <게임 수> <bootstrap 서버:포트> 입니다.");
            System.exit(0);
        }

        if (Integer.parseInt(args[0]) < 1) { // 최소 1 Game 이상은 입력해야 함.
            log.info("올바르지 않은 사용으로 인하여, 프로그램을 종료합니다.");
            log.info("최소 한 게임 이상은 입력해주시기 바랍니다.");
            System.exit(0);
        }

        // Common Code Setting
        GAME_COUNT = Integer.parseInt(args[0]);
        BOOT_SERVER = args[1];

        // Game Data Collection
        collectGameData();

    }

    public static void collectGameData() {

        CountDownLatch latch = new CountDownLatch(GAME_COUNT);
        ExecutorService executor = Executors.newFixedThreadPool(GAME_COUNT);

        IntStream.range(0, GAME_COUNT).forEach(j -> {
            String sessionGameID = SessionUtil.getSessionGameID();
            OffsetDateTime createGameDate = OffsetDateTime.now(ZoneId.of("UTC"));
            executor.execute(() -> {
                new Game(sessionGameID, createGameDate, latch).run();
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("[ ERROR ] : {}", e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the thread
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown as tasks did not complete in time.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("[ ERROR ] during shutdown: {}", e.getMessage());
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}