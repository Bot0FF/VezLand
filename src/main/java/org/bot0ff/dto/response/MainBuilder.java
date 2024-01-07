package org.bot0ff.dto.response;

import lombok.Builder;
import lombok.Data;
import org.bot0ff.entity.Enemy;
import org.bot0ff.entity.Library;
import org.bot0ff.entity.Player;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class MainBuilder {
    private Player player;
    private List<Enemy> enemies;
    private List<Player> players;
    private List<Library> libraries;
    private String content;
    private boolean inFight;
    private HttpStatus status;
}