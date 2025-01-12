package lol.event.util;

import lol.event.common.Champions;
import lol.event.common.Player;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Slf4j
public class GamePlayUtil {

    public static Player getRandomPlayer(Set<Player> allPlayers) {
        Player[] playersArray = allPlayers.toArray(new Player[0]);
        return playersArray[RandomUtil.R.nextInt(playersArray.length)];
    }

    public static Player getRandomPlayer(Set<Player> blueTeam, Set<Player> redTeam) {
        Set<Player> allPlayers = new HashSet<>();
        allPlayers.addAll(blueTeam);
        allPlayers.addAll(redTeam);

        return getRandomPlayer(allPlayers);
    }

    public static String decideWinner(double blueWinProbability) {
        return RandomUtil.R.nextDouble() < blueWinProbability ? "Blue" : "Red";
    }

    public static int calculateTeamKillSum(Player player, Set<Player> blueTeam, Set<Player> redTeam) {
        Set<Player> team = blueTeam.contains(player) ? blueTeam : redTeam;

        int totalKills = 0;
        for (Player teammate : team) {
            totalKills += RandomUtil.R.nextInt(10); // 팀원의 킬 합
        }
        return totalKills;
    }

    /**
     * 챔피언을 지정합니다.
     * 이 메서드는 챔피언을 생성하고, 중복을 허용하지 않습니다.
     * @return Set<String> championSet 에 생성된 챔피언이 없을 경우 반환하고, 있을 시 반환하지 않습니다.
     */
    public static String getChampions(Set<Integer> championSet) {
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
    public static String getIpAddr(Set<String> ipSet) {
        while (true) {
            String ipAddr = "192.168."+  RandomUtil.R.nextInt(256)+ "." + RandomUtil.R.nextInt(256);
            if (!ipSet.contains(ipAddr)) {
                ipSet.add(ipAddr);
                return ipAddr;
            }
        }
    }

    /**
     * 게임 리소스를 초기화합니다.
     * 다음 게임 세션을 위해 챔피언 및 IP 중복 관리를 초기화합니다.
     */
    public static void clearGameResources(Set<Integer> championSet, Set<String> ipSet) {
        championSet.clear();
        ipSet.clear();
        log.info("Game resources (championSet, ipSet) cleared for the new session.");
    }
}

