package lol.event.vo;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record PlayerRecord(String gameSessionId, String uniqueId, String email, String password, String tier, String sex, int age,
                           String nickName, String team, String ip, String champion, int kill, int death, int assist,
                           int gamePlayTime, String isWin, OffsetDateTime createGameDate) {

}
