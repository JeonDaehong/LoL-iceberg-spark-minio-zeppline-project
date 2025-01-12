package lol.event.vo;

import lol.event.common.Player;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class TeamVo {

    private Set<Player> players = new HashSet<>();
    private int score;
    private String isWin;

}
