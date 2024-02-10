package org.bot0ff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bot0ff.model.InfoResponse;
import org.bot0ff.entity.unit.UnitArmor;
import org.bot0ff.entity.Location;
import org.bot0ff.entity.Thing;
import org.bot0ff.entity.Unit;
import org.bot0ff.model.MainResponse;
import org.bot0ff.repository.LocationRepository;
import org.bot0ff.repository.ThingRepository;
import org.bot0ff.repository.UnitRepository;
import org.bot0ff.util.JsonProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Обрабатывает запросы для страниц:
 * - Информация об игроке (profile)
 * - Список вещей в инвентаре и взаимодействие с ними (profile/inventory)
 * - Распределение аттрибутов (profile/attribute)
 * - Уровень навыков (profile/skill)
 * - Список и выбор умений (profile/ability) */

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UnitRepository unitRepository;
    private final LocationRepository locationRepository;
    private final ThingRepository thingRepository;

    private final JsonProcessor jsonProcessor;

    //страница профиля
    @Transactional
    public String getUnitProfileState(String name) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalLocation = locationRepository.findById(player.getLocationId());
        if(optionalLocation.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Локация не найдена"));
            log.info("Не найдена location в БД по запросу locationId: {}", optionalPlayer.get().getLocationId());
            return response;
        }
        Location location = optionalLocation.get();

        return jsonProcessor
                .toJsonMain(new MainResponse(player, location, null));
    }

    //добавляет вещь в инвентарь с локации
    @Transactional
    public String takeLocationThing(String name, Long thingId) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalLocation = locationRepository.findById(player.getLocationId());
        if(optionalLocation.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Локация не найдена"));
            log.info("Не найдена location в БД по запросу locationId: {}", player.getLocationId());
            return response;
        }
        Location location = optionalLocation.get();

        var optionalThing = thingRepository.findById(thingId);
        if(optionalThing.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь не найдена"));
            log.info("Не найдена вещь в БД по запросу thingId: {}", thingId);
            return response;
        }
        Thing thing = optionalThing.get();

        //если у вещи есть владелец, отправляем уведомление
        if(thing.getOwnerId() != null) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("У вещи есть владелец"));
            log.info("Попытка забрать вещь, у которой есть владелец, thingId: {}", thingId);
            return response;
        }

        //сохраняем нового владельца и удаляем id вещи с локации, если привязана
        thing.setOwnerId(player.getId());
        thingRepository.save(thing);
        location.getThings().removeIf(th -> th.equals(thingId));
        locationRepository.save(location);

        return jsonProcessor
                .toJsonInfo(new InfoResponse("Вещь (" + thing.getName() + ") добавлена в инвентарь"));
    }

    //удаляет вещь из инвентаря
    @Transactional
    public String removeInventoryThing(String name, Long thingId) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalLocation = locationRepository.findById(player.getLocationId());
        if(optionalLocation.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Локация не найдена"));
            log.info("Не найдена location в БД по запросу locationId: {}", player.getLocationId());
            return response;
        }
        Location location = optionalLocation.get();

        var optionalThing = thingRepository.findById(thingId);
        if(optionalThing.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь не найдена"));
            log.info("Не найдена вещь в БД по запросу thingId: {}", thingId);
            return response;
        }
        Thing thing = optionalThing.get();

        if(thing.isUse()) {
            switch (thing.getApplyType()) {
                case ONE_HAND, TWO_HAND, BOW -> {
                    player.setWeapon(new UnitArmor());
                    thing.setUse(false);
                }
                case HEAD -> {
                    player.setHead(new UnitArmor());
                    thing.setUse(false);
                }
                case HAND -> {
                    player.setHand(new UnitArmor());
                    thing.setUse(false);
                }
                case BODY -> {
                    player.setBody(new UnitArmor());
                    thing.setUse(false);
                }
                case LEG -> {
                    player.setLeg(new UnitArmor());
                    thing.setUse(false);
                }
            }
        }

        //удаляем владельца у выбранной вещь и привязываем к локации
        thing.setUse(false);
        thing.setOwnerId(null);
        thingRepository.save(thing);
        location.getThings().add(thingId);
        locationRepository.save(location);

        return jsonProcessor
                .toJsonInfo(new InfoResponse("Вещь (" + thing.getName() + ") удалена из инвентаря"));
    }

    //надеть вещь из инвентаря
    @Transactional
    public String putOnInventoryThing(String name, Long thingId) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalThing = thingRepository.findById(thingId);
        if(optionalThing.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь не найдена"));
            log.info("Не найдена вещь в БД по запросу thingId: {}", thingId);
            return response;
        }
        Thing thing = optionalThing.get();

        if(thing.isUse()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь уже надета"));
            log.info("Попытка повторно надеть вещь, thingId: {}", thingId);
            return response;
        }

        switch (thing.getApplyType()) {
            case ONE_HAND, TWO_HAND, BOW -> {
                if(player.getWeapon().getId() != null) {
                    takeOffExistThing(player.getWeapon());
                }
                player.setWeapon(new UnitArmor(
                        thing.getId(),
                        thing.getName(),
                        thing.getApplyType().name(),
                        thing.getHp(),
                        thing.getMana(),
                        thing.getPhysDamage(),
                        thing.getMagDamage(),
                        thing.getMagDamageModifier(),
                        thing.getPhysDefense(),
                        thing.getMagDefense(),
                        thing.getPointAction(),
                        thing.getDistance(),
                        thing.getDuration()
                ));
                thing.setUse(true);
            }
            case HEAD -> {
                if(player.getHead().getId() != null) {
                    takeOffExistThing(player.getHead());
                }
                player.setHead(new UnitArmor(
                        thing.getId(),
                        thing.getName(),
                        thing.getApplyType().name(),
                        thing.getHp(),
                        thing.getMana(),
                        thing.getPhysDamage(),
                        thing.getMagDamage(),
                        thing.getMagDamageModifier(),
                        thing.getPhysDefense(),
                        thing.getMagDefense(),
                        thing.getPointAction(),
                        thing.getDistance(),
                        thing.getDuration()
                ));
                thing.setUse(true);
            }
            case HAND -> {
                if(player.getHand().getId() != null) {
                    takeOffExistThing(player.getHand());
                }
                player.setHand(new UnitArmor(
                        thing.getId(),
                        thing.getName(),
                        thing.getApplyType().name(),
                        thing.getHp(),
                        thing.getMana(),
                        thing.getPhysDamage(),
                        thing.getMagDamage(),
                        thing.getMagDamageModifier(),
                        thing.getPhysDefense(),
                        thing.getMagDefense(),
                        thing.getPointAction(),
                        thing.getDistance(),
                        thing.getDuration()
                ));
                thing.setUse(true);
            }
            case BODY -> {
                if(player.getBody().getId() != null) {
                    takeOffExistThing(player.getBody());
                }
                player.setBody(new UnitArmor(
                        thing.getId(),
                        thing.getName(),
                        thing.getApplyType().name(),
                        thing.getHp(),
                        thing.getMana(),
                        thing.getPhysDamage(),
                        thing.getMagDamage(),
                        thing.getMagDamageModifier(),
                        thing.getPhysDefense(),
                        thing.getMagDefense(),
                        thing.getPointAction(),
                        thing.getDistance(),
                        thing.getDuration()
                ));
                thing.setUse(true);
            }
            case LEG -> {
                if(player.getLeg().getId() != null) {
                    takeOffExistThing(player.getLeg());
                }
                player.setLeg(new UnitArmor(
                        thing.getId(),
                        thing.getName(),
                        thing.getApplyType().name(),
                        thing.getHp(),
                        thing.getMana(),
                        thing.getPhysDamage(),
                        thing.getMagDamage(),
                        thing.getMagDamageModifier(),
                        thing.getPhysDefense(),
                        thing.getMagDefense(),
                        thing.getPointAction(),
                        thing.getDistance(),
                        thing.getDuration()
                ));
                thing.setUse(true);
            }
        }

        unitRepository.save(player);
        thingRepository.save(thing);

        return jsonProcessor
                .toJson(new InfoResponse("Вещь (" + thing.getName() + ") надета"));
    }

    //снять надетую вещь
    @Transactional
    public String takeOffInventoryThing(String name, Long thingId) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalThing = thingRepository.findById(thingId);
        if(optionalThing.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь не найдена"));
            log.info("Не найдена вещь в БД по запросу thingId: {}", thingId);
            return response;
        }
        Thing thing = optionalThing.get();

        if(!thing.isUse()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Вещь не надета"));
            log.info("Попытка повторно снять вещь, thingId: {}", thingId);
            return response;
        }

        switch (thing.getApplyType()) {
            case ONE_HAND, TWO_HAND, BOW -> {
                player.setWeapon(new UnitArmor());
                thing.setDuration(player.getWeapon().getDuration());
                thing.setUse(false);
            }
            case HEAD -> {
                player.setHead(new UnitArmor());
                thing.setDuration(player.getHead().getDuration());
                thing.setUse(false);
            }
            case HAND -> {
                player.setHand(new UnitArmor());
                thing.setDuration(player.getHand().getDuration());
                thing.setUse(false);
            }
            case BODY -> {
                player.setBody(new UnitArmor());
                thing.setDuration(player.getBody().getDuration());
                thing.setUse(false);
            }
            case LEG -> {
                player.setLeg(new UnitArmor());
                thing.setDuration(player.getLeg().getDuration());
                thing.setUse(false);
            }
        }

        unitRepository.save(player);
        thingRepository.save(thing);

        return jsonProcessor
                .toJson(new InfoResponse("Вещь (" + thing.getName() + ") снята"));
    }

    //просмотр инвентаря
    @Transactional
    public String getInventoryThings(String name) {
        var optionalPlayer = unitRepository.findByName(name);
        if(optionalPlayer.isEmpty()) {
            var response = jsonProcessor
                    .toJsonInfo(new InfoResponse("Игрок не найден"));
            log.info("Не найден player в БД по запросу username: {}", name);
            return response;
        }
        Unit player = optionalPlayer.get();

        var optionalThing = thingRepository.findAllByOwnerId(player.getId());
        List<Thing> things = optionalThing.stream().toList();

        return jsonProcessor
                .toJson(things);
    }

    //снимает существующую вещь, перед надеванием другой
    public void takeOffExistThing(UnitArmor existThing) {
        var optionalExistThing = thingRepository.findById(existThing.getId());
        if(optionalExistThing.isEmpty()) {
            log.info("Ошибка при поиске надетой вещи, existThingId: {}", existThing.getId());
            return;
        }
        Thing thing = optionalExistThing.get();

        thing.setDuration(existThing.getDuration());
        thing.setUse(false);
        thingRepository.save(thing);
    }
}