package org.bot0ff.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot0ff.model.InfoResponse;
import org.bot0ff.model.auth.SettingRequest;
import org.bot0ff.entity.Unit;
import org.bot0ff.repository.LocationRepository;
import org.bot0ff.repository.UnitRepository;
import org.bot0ff.repository.UserRepository;
import org.bot0ff.service.MainService;
import org.bot0ff.util.Constants;
import org.bot0ff.util.JsonProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SettingController {
    private final MainService mainService;
    private final UserRepository userRepository;
    private final UnitRepository unitRepository;
    private final LocationRepository locationRepository;
    private final JsonProcessor jsonProcessor;

    //настройка нового пользователя
    @PostMapping("/start/setting")
    public ResponseEntity<?> settingUser(@RequestBody SettingRequest settingRequest) {
        String username = "user";
        var user = userRepository.findByUsername(username).orElse(null);
        if(user == null) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", username);
            return ResponseEntity.ok(response);
        }
        var locationId = Long.parseLong("" + Constants.START_POS_X + Constants.START_POS_Y);
        var location = locationRepository.findById(locationId).orElse(null);
        if(location == null) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Локация не найдена"));
            log.info("Не найдена location в БД по запросу locationId: {}", locationId);
            return ResponseEntity.ok(response);
        }
        var unit = new Unit();
        var response = mainService.getUnitState(username);
        return ResponseEntity.ok(response);
    }
}
