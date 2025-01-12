package lol.event;

import lol.event.common.Champions;
import lol.event.common.Player;
import lol.event.util.GamePlayUtil;
import lol.event.util.RandomUtil;
import lol.event.util.kafkaLogSendUtil;
import lol.event.vo.PlayerRecord;
import lol.event.vo.TeamVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static lol.event.util.GamePlayUtil.calculateTeamKillSum;

@Slf4j
@RequiredArgsConstructor
public class Game implements Runnable {

    private final String sessionGameID;
    private final OffsetDateTime createGameDate;
    private final CountDownLatch mainLatch;

    private final Set<Integer> championSet = new HashSet<>();
    private final Set<String> ipSet = new HashSet<>();

    @Override
    public void run() {

        GamePlayUtil.clearGameResources(championSet, ipSet); // 게임 리소스 Clear

        int gamePlayTime = RandomUtil.getRandomPlayTime(); // 20분 ~ 1시간 사이 랜덤 시간 ( 초 단위 )

        // 1. 팀을 랜덤으로 나누기
        Set<Player> allPlayers = new HashSet<>(Arrays.asList(Player.values()));
        TeamVo blueTeam = new TeamVo();
        TeamVo redTeam = new TeamVo();

        // 2. Blue 팀과 Red 팀에 중복 없이 플레이어 배정
        while (blueTeam.getPlayers().size() < 5) {
            Player player = GamePlayUtil.getRandomPlayer(allPlayers);
            blueTeam.getPlayers().add(player);
            allPlayers.remove(player);
        }
        while (redTeam.getPlayers().size() < 5) {
            Player player = GamePlayUtil.getRandomPlayer(allPlayers);
            redTeam.getPlayers().add(player);
            allPlayers.remove(player);
        }

        // 3. 팀 점수 계산
        blueTeam.setScore(blueTeam.getPlayers().stream().mapToInt(Player::getTierScore).sum());
        redTeam.setScore(redTeam.getPlayers().stream().mapToInt(Player::getTierScore).sum());

        // 4. 승리 확률 계산 (Blue 팀과 Red 팀의 점수 차이를 기준으로)
        double blueWinProbability = (double) blueTeam.getScore() / (blueTeam.getScore() + redTeam.getScore());
        log.info("Blue Team Score: {} |  Red Team Score: {}", blueTeam.getScore() , redTeam.getScore() );

        // 5. 승리팀 결정
        String winningTeam = GamePlayUtil.decideWinner(blueWinProbability);
        blueTeam.setIsWin("Blue".equals(winningTeam) ? "Win" : "Lose");
        redTeam.setIsWin("Red".equals(winningTeam) ? "Win" : "Lose");
        log.info("Winning Team: {}", winningTeam);

        // 6. 각 플레이어들 정보 및 로그 입력 ( + 챔피언 정하기 )
        List<Player> allGamePlayers = new ArrayList<>();
        allGamePlayers.addAll(blueTeam.getPlayers());
        allGamePlayers.addAll(redTeam.getPlayers());

        // 모든 챔피언 목록 셔플링
        List<Champions> availableChampions = new ArrayList<>(Arrays.asList(Champions.values()));
        Collections.shuffle(availableChampions);

        CountDownLatch gameLatch = new CountDownLatch(Main.PLAYER_LIMIT);
        ExecutorService gameExecutor = Executors.newFixedThreadPool(Main.PLAYER_LIMIT);
        IntStream.range(0, Main.PLAYER_LIMIT).forEach(j -> {
            gameExecutor.execute(() -> {
                try {
                    // 플레이어에게 랜덤으로 챔피언 할당
                    Champions champion = availableChampions.remove(0); // 리스트에서 첫 번째 챔피언 제거
                    processPlayerLog(allGamePlayers.get(j), blueTeam, redTeam, winningTeam, champion, gamePlayTime, gameLatch);
                } finally {
                    gameLatch.countDown();
                }
            });
        });
        try {
            gameLatch.await(); // 모든 플레이어 작업 완료 대기
        } catch (InterruptedException e) {
            log.error("[ ERROR ] : {}", e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the thread
        } finally {
            shutdownExecutor(gameExecutor);
            mainLatch.countDown(); // 메인 래치 카운트 다운
        }
    }

    private void processPlayerLog(Player player, TeamVo blueTeam, TeamVo redTeam, String winningTeam, Champions champion, int gamePlayTime, CountDownLatch gameLatch) {
        boolean isPlayerInBlueTeam = blueTeam.getPlayers().contains(player);
        boolean isPlayerInWinningTeam = ("Blue".equals(winningTeam) && isPlayerInBlueTeam) ||
                ("Red".equals(winningTeam) && !isPlayerInBlueTeam);

        int kill, death;

        // 이긴 플레이어와 진 플레이어의 kill, death 설정
        if (isPlayerInWinningTeam) {
            death = RandomUtil.R.nextInt(15);
            kill = death + RandomUtil.R.nextInt(15);
        } else {
            death = RandomUtil.R.nextInt(30);
            kill = death - RandomUtil.R.nextInt(20);
            if ( death == 0 ) death += 3;
            if (kill < 0) kill = 0;
        }

        // 어시스트 설정
        int teamKillSum = calculateTeamKillSum(player, blueTeam.getPlayers(), redTeam.getPlayers());
        int maxAssist = Math.max(0, teamKillSum - kill - 1); // 음수 방지
        int assist = maxAssist > 0 ? RandomUtil.R.nextInt(maxAssist + 1) : 0; // 조건을 만족하는 assist 생성

        // 플레이어의 팀 정보 확인
        String playerTeam = blueTeam.getPlayers().contains(player) ? "Blue" : "Red";

        // 로그 전송
        kafkaLogSendUtil.sendLog(PlayerRecord.builder()
                        .gameSessionId(sessionGameID)
                        .uniqueId(player.getUniqueId())
                        .email(player.getEmail())
                        .password(player.getPassword())
                        .tier(player.getTier())
                        .sex(player.getSex())
                        .age(player.getAge())
                        .nickName(player.getNickName())
                        .team(playerTeam)
                        .ip(GamePlayUtil.getIpAddr(ipSet))
                        .champion(GamePlayUtil.getChampions(championSet))
                        .kill(kill)
                        .death(death)
                        .assist(assist)
                        .gamePlayTime(gamePlayTime)
                        .isWin(isPlayerInWinningTeam ? "Win" : "Lose")
                        .createGameDate(createGameDate)
                        .build(), gameLatch);
    }

    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn("Forcing shutdown of executor as tasks did not complete in time.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("[ ERROR ] during executor shutdown: {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
