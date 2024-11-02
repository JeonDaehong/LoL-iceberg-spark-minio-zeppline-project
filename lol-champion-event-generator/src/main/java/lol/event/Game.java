package lol.event;

import java.util.UUID;

public class Game {

    /**
     * 이 메서드는 생성된 Game 에 대한 고유함을 보장하기 위한 아이디를 생성합니다.
     * @return 생성된 Game 을 식별하는 고유한 ID인 SessionGameID 반환합니다.
     */
    public static String getSessionGameID() {
        return String.valueOf("Game_" + UUID.randomUUID());
    }
}
