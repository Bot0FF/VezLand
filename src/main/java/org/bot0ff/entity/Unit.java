package org.bot0ff.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.bot0ff.entity.unit.UnitArmor;
import org.bot0ff.entity.unit.UnitEffect;
import org.bot0ff.entity.enums.Status;
import org.bot0ff.entity.enums.SubjectType;
import org.bot0ff.entity.unit.UnitSkill;
import org.bot0ff.util.converter.UnitJsonSubjectToArmorConverter;
import org.bot0ff.util.converter.UnitJsonSubjectToEffectConverter;
import org.bot0ff.util.converter.UnitJsonSubjectToSkillConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "unit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    //общее
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(value = EnumType.STRING)
    private SubjectType subjectType;

    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(name = "actionEnd")
    private boolean actionEnd;

    /** локация */
    @Column(name = "locationId")
    private Long locationId;

    /** основные характеристики */
    //здоровье
    @Column(name = "hp")
    private int hp;

    @Column(name = "mana")
    private int mana;

    //очки действия
    @Column(name = "pointAction")
    private int pointAction;

    //максимальные очки действия
    @Column(name = "maxPointAction")
    private int maxPointAction;

    /** аттрибуты */
    //сила
    @Column(name = "strength")
    private int strength;

    //интеллект
    @Column(name = "intelligence")
    private int intelligence;

    //ловкость
    @Column(name = "dexterity")
    private int dexterity;

    //выносливость
    @Column(name = "endurance")
    private int endurance;

    //удача
    @Column(name = "luck")
    private int luck;

    //свободные очки для распределения
    @Column(name = "bonusPoint")
    private int bonusPoint;

    /** навыки */
    @Convert(converter = UnitJsonSubjectToSkillConverter.class)
    @Column(name = "unitSkill")
    @JsonIgnore
    private UnitSkill unitSkill;

    /** список умений */
    //активные умения
    @Column(name = "currentAbility")
    @JsonIgnore
    private List<Long> currentAbility;

    //все умения
    @Column(name = "allAbility")
    @JsonIgnore
    private List<Long> allAbility;

    /** экипировка */
    //оружие
    @Convert(converter = UnitJsonSubjectToArmorConverter.class)
    @Column(name = "weapon", length = 1024)
    @JsonIgnore
    private UnitArmor weapon;

    //голова
    @Convert(converter = UnitJsonSubjectToArmorConverter.class)
    @Column(name = "head", length = 1024)
    @JsonIgnore
    private UnitArmor head;

    //руки
    @Convert(converter = UnitJsonSubjectToArmorConverter.class)
    @Column(name = "hand", length = 1024)
    @JsonIgnore
    private UnitArmor hand;

    //тело
    @Convert(converter = UnitJsonSubjectToArmorConverter.class)
    @Column(name = "body", length = 1024)
    @JsonIgnore
    private UnitArmor body;

    //ноги
    @Convert(converter = UnitJsonSubjectToArmorConverter.class)
    @Column(name = "leg", length = 1024)
    @JsonIgnore
    private UnitArmor leg;

    /** сражение */
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "fight")
    @JsonIgnore
    private Fight fight;

    @Column(name = "unitFightPosition")
    private Long unitFightPosition;

    @Convert(converter = UnitJsonSubjectToEffectConverter.class)
    @Column(name = "unitFightEffect", length = 1024)
    @JsonIgnore
    private List<UnitEffect> unitFightEffect;

    @Column(name = "teamNumber")
    private Long teamNumber;

    @Column(name = "abilityId")
    @JsonIgnore
    private Long abilityId;

    @Column(name = "targetId")
    @JsonIgnore
    private Long targetId;

    //максимальное здоровье:
    public int getMaxHp() {
        double maxHpModifier = (((strength * 10.0) / 100) + 0.30) + (((luck * 3.0) / 100) + 0.10) + (((endurance * 15.0) / 100) + 0.10);
        //устанавливаем модификаторы прибавки здоровья от каждого вида экипировки, начальное значение зависит о навыка
        int fullHp = (int) Math.round(maxHpModifier * 10);
        if(weapon != null && weapon.getDuration() > 0) {
            fullHp += weapon.getHp();
        }
        if(head != null && head.getDuration() > 0) {
            fullHp += head.getHp();
        }
        if(hand != null && hand.getDuration() > 0) {
            fullHp += hand.getHp();
        }
        if(body != null && body.getDuration() > 0) {
            fullHp += body.getHp();
        }
        if(leg != null && leg.getDuration() > 0) {
            fullHp += leg.getHp();
        }
        //прибавляем к максимальному здоровью эффекты боя, если есть
        if(unitFightEffect != null) {
            for (UnitEffect effect : unitFightEffect) {
                fullHp += effect.getEffectHp();
            }
        }
        return Math.max(fullHp, 0);
    }

    //максимальная мана
    public int getMaxMana() {
        double maxManaModifier = (((intelligence * 17.0) / 100) + 0.30) + (((luck * 3.0) / 100) + 0.10) + (((endurance * 5.0) / 100) + 0.10) + (((dexterity * 3.0) / 100) + 0.10);
        //устанавливаем модификаторы прибавки здоровья от каждого вида экипировки, начальное значение зависит о навыка
        int fullMana = (int) Math.round(maxManaModifier * 10);
        if(weapon != null && weapon.getDuration() > 0) {
            fullMana += weapon.getMana();
        }
        if(head != null && head.getDuration() > 0) {
            fullMana += head.getMana();
        }
        if(hand != null && hand.getDuration() > 0) {
            fullMana += hand.getMana();
        }
        if(body != null && body.getDuration() > 0) {
            fullMana += body.getMana();
        }
        if(leg != null && leg.getDuration() > 0) {
            fullMana += leg.getMana();
        }
        //прибавляем к максимальному здоровью эффекты боя, если есть
        if(unitFightEffect != null) {
            for (UnitEffect effect : unitFightEffect) {
                fullMana += effect.getEffectMana();
            }
        }
        return Math.max(fullMana, 0);
    }

    //полный физический урон
    public int getPhysDamage() {
        int fullPhysDamage = 0;
        if(weapon == null || weapon.getApplyType() == null) return 1;
        switch (weapon.getApplyType()) {
            case "ONE_HAND" -> {
                double physDamageModifier = (((strength * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getOneHand()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "TWO_HAND" -> {
                double physDamageModifier = (((strength * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getTwoHand()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "BOW" -> {
                double physDamageModifier = (((dexterity * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getBow()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "FIRE" -> {
                double physDamageModifier = (((intelligence * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getFire()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "WATER" -> {
                double physDamageModifier = (((intelligence * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getWater()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "LAND" -> {
                double physDamageModifier = (((intelligence * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getLand()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
            case "AIR" -> {
                double physDamageModifier = (((intelligence * 1.0) / 100) + 1) + (((luck * 1.0) / 100) + 0.10);
                physDamageModifier += (getSkillLevel(unitSkill.getAir()) * 1.0 / 100);
                fullPhysDamage = (int) Math.round(physDamageModifier * (weapon.getPhysDamage() + 1));
            }
        }
        //прибавляем к базовому урону эффекты боя, если есть
        if(unitFightEffect != null) {
            for (UnitEffect effect : unitFightEffect) {
                fullPhysDamage += effect.getEffectPhysDamage();
            }
        }
        return Math.max(fullPhysDamage, 0);
    }

    //модификатор усиления магического умения
    public double getMagModifier() {
        double magDamageModifier = ((intelligence * 1.0) / 100) + 1 + (((luck * 1.0) / 100) + 0.10);
        if(magDamageModifier < 0) return 0.01;
        return magDamageModifier;
    }

    //физическая защита
    public int getPhysDefense() {
        double defenseModifier = (((strength * 15.0) / 100) + 0.30) + (((luck * 5.0) / 100) + 0.10) + (((endurance * 5.0) / 100) + 0.10) + (((dexterity * 2.0) / 100) + 0.10);
        //устанавливаем модификаторы прибавки здоровья от каждого вида экипировки, начальное значение зависит о навыка
        int fullDefense = (int) Math.round(defenseModifier * 10);
        if(weapon != null && weapon.getDuration() > 0) {
            fullDefense += weapon.getPhysDefense();
        }
        if(head != null && head.getDuration() > 0) {
            fullDefense += head.getPhysDefense();
        }
        if(hand != null && hand.getDuration() > 0) {
            fullDefense += hand.getPhysDefense();
        }
        if(body != null && body.getDuration() > 0) {
            fullDefense += body.getPhysDefense();
        }
        if(leg != null && leg.getDuration() > 0) {
            fullDefense += leg.getPhysDefense();
        }
        //прибавляем к максимальному здоровью эффекты боя, если есть
        if(unitFightEffect != null) {
            for (UnitEffect effect : unitFightEffect) {
                fullDefense += effect.getDurationEffectPhysDefense();
            }
        }
        return Math.max(fullDefense, 0);
    }

    //магическая защита
    public int getMagDefense() {
        double defenseModifier = (((intelligence * 15.0) / 100) + 0.30) + (((luck * 5.0) / 100) + 0.10) + (((endurance * 5.0) / 100) + 0.10) + (((dexterity * 2.0) / 100) + 0.10);
        //устанавливаем модификаторы прибавки здоровья от каждого вида экипировки, начальное значение зависит о навыка
        int fullDefense = (int) Math.round(defenseModifier * 10);
        if(weapon != null && weapon.getDuration() > 0) {
            fullDefense += weapon.getMagDefense();
        }
        if(head != null && head.getDuration() > 0) {
            fullDefense += head.getMagDefense();
        }
        if(hand != null && hand.getDuration() > 0) {
            fullDefense += hand.getMagDefense();
        }
        if(body != null && body.getDuration() > 0) {
            fullDefense += body.getMagDefense();
        }
        if(leg != null && leg.getDuration() > 0) {
            fullDefense += leg.getMagDefense();
        }
        //прибавляем к максимальному здоровью эффекты боя, если есть
        if(unitFightEffect != null) {
            for (UnitEffect effect : unitFightEffect) {
                fullDefense += effect.getEffectMagDefense();
            }
        }
        return Math.max(fullDefense, 0);
    }

    //инициатива
    public int getInitiative() {
        int initiativeModifier = (int) (((luck * 10.0) / 100) + 1.30);
        int resultInitiative = getRNum30(initiativeModifier);
        return Math.max(resultInitiative, 0);
    }

    //скорость регенерации
    public int getRegeneration() {
        double regenerationModifier = (((endurance * 10.0) / 100) + 0.30) + (getSkillLevel(unitSkill.getRegeneration()) * 1.0 / 100);
        return (int) Math.round(regenerationModifier);
    }

    //скорость восстановления маны
    public int getMeditation() {
        double meditationModifier = (((intelligence * 10.0) / 100) + 0.30) + (getSkillLevel(unitSkill.getMeditation()) * 1.0 / 100);
        return (int) Math.round(meditationModifier);
    }

    //шанс блока
    public double getChanceBlock() {
        return (int) (1 + (getSkillLevel(unitSkill.getBlock()) * 1.0 / 100));
    }

    //шанс уклонения
    public double getChanceEvade() {
        return (int) (1 + (getSkillLevel(unitSkill.getEvade()) * 1.0 / 100));
    }

    private int getSkillLevel(int skill) {
        int level = 1;
        List<Integer> levelList = new ArrayList<>(List.of(1, 1000, 2, 5000, 3, 10000));
        Collections.sort(levelList);
        for(Integer levelExp: levelList) {
            skill -= levelExp;
            if(skill < 0) break;
            level++;
        }
        return level;
    }

    //рандом +-30% от числа
    private int getRNum30(int num) {
        int min = (int) Math.round(num * 0.70);
        int max = (int) Math.round(num * 1.30);
        return new RandomDataGenerator().nextInt(min, max);
    }
}
