package org.bot0ff.rest;

import lombok.RequiredArgsConstructor;
import org.bot0ff.service.fight.FightService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/fight")
@RequiredArgsConstructor
public class FightController {
    private final FightService fightService;

    //начать сражение с выбранным противником
    @GetMapping("/attack")
    public ResponseEntity<?> actionAttack(@RequestParam Long id) {
        var response = fightService.getStartFightUserVsEnemy("admin", id);
        return ResponseEntity.ok(response);
    }

    //текущее состояние сражения
    @GetMapping("/refresh")
    public ResponseEntity<?> actionFight() {
        var response = fightService.getRefreshCurrentRound("admin");
        return ResponseEntity.ok(response);
    }

    //физическая атака по выбранному enemy
    @GetMapping("/phys")
    public ResponseEntity<?> actionFight(@RequestParam Long targetId) {
        var response = fightService.getPhysAttackUserVsEnemy("admin", targetId);
        return ResponseEntity.ok(response);
    }
}
