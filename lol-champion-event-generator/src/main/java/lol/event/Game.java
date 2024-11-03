package lol.event;

import lol.event.util.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class Game implements Runnable {

    private final String sessionGameID;
    private final OffsetDateTime createGameDate;
    private final CountDownLatch mainLatch;

    static Set<String> ipSet = new HashSet<>();
    static Set<String> accSet = new HashSet<>();
    static Set<Integer> championSet = new HashSet<>();

    @Override
    public void run() {
        int gamePlayTime = RandomUtil.getRandomPlayTime(); // 20분 ~ 1시간 사이 랜덤 시간 ( 초 단위 )

        CountDownLatch gameLatch = new CountDownLatch(Main.PLAYER_LIMIT);
        ExecutorService gameExecutor = Executors.newFixedThreadPool(Main.PLAYER_LIMIT);

        IntStream.range(0, Main.PLAYER_LIMIT).forEach(j -> {
            String ipAddr = getIpAddr();
            String account = getAccount();
            String champion = getChampions();

            gameExecutor.execute(() -> {
                new Play(gameLatch, sessionGameID, createGameDate, ipAddr, account, champion, gamePlayTime).run();
            });
            championSet.clear();
        });
        try {
            gameLatch.await();
        } catch (InterruptedException e) {
            log.error("[ ERROR ] : " + e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the thread
        } finally {
            gameExecutor.shutdown();
            try {
                if (!gameExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    log.warn("Forcing shutdown of player executor as tasks did not complete in time.");
                    gameExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("[ ERROR ] during player executor shutdown: " + e.getMessage());
                gameExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            mainLatch.countDown(); // 모든 플레이어 실행 후 메인 래치 카운트 다운
        }
    }

    /**
     * 챔피언을 지정합니다.
     * 이 메서드는 챔피언을 생성하고, 중복을 허용하지 않습니다.
     * @return Set<String> championSet 에 생성된 챔피언이 없을 경우 반환하고, 있을 시 반환하지 않습니다.
     */
    private static String getChampions() {
        while (true) {
            int championStatus = RandomUtil.R.nextInt(Champions.values().length);
            for (Champions champion : Champions.values()) {
                if (champion.getValue() == championStatus && !championSet.contains(
                        championStatus)) {
                    championSet.add(championStatus);
                    return champion.getName();
                }
            }
        }
    }

    /**
     * 새로운 IP를 생성합니다.
     * 이 메서드는 플레이어의 IP 주소를 생성하고, 중복을 허용하지 않습니다.
     * @return Set<String> ipSet에 생성된 IP가 없을 시 반환하고, 있을 시 반환하지 않습니다.
     */
    private static String getIpAddr() {
        while (true) {
            String ipAddr = "192.168."+  RandomUtil.R.nextInt(256)+ "." + RandomUtil.R.nextInt(256);
            if (!ipSet.contains(ipAddr)) {
                ipSet.add(ipAddr);
                return ipAddr;
            }
        }
    }

    /**
     * 새로운 계정을 생성합니다.
     * 이 메서드는 플레이어의 수의 맞게 계정을 생성합니다. 중복된 계정은 생성하지 않습니다.
     * @return Set<String> accSet 에 해당 account 가 없을 시 반환하고, 있을 시 반환하지 않습니다.
     */
    private static String getAccount() {
        while (true) {
            String account = RandomUtil.getRandomNickname();
            if (!accSet.contains(account)) {
                accSet.add(account);
                return account;
            }
        }
    }
}
