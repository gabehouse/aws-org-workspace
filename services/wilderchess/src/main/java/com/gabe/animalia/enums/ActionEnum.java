package com.gabe.animalia.enums;

import java.util.HashMap;
import java.util.Map;

public enum ActionEnum {
        // Strategic Actions
        NONE(0, 0.0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.NONE, TargetTypeEnum.NONE, 0, 0, 0, "", ""),
        MOVE(1, 1.0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.MOVE, TargetTypeEnum.SQUARE, 0, 0, 0, "",
                        ""),
        BENCH(2, 2.0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.MOVE, TargetTypeEnum.SQUARE, 0, 0, 0, "",
                        ""),
        UNBENCH(3, 2.0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.MOVE, TargetTypeEnum.SQUARE, 0, 0, 0, "",
                        ""),

        // Attacks (Most target FIGHTER, some like Fireball target SQUARE)
        CHARGE(4, 3.0, 90, 40, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.SQUARE, 0, 0, 0,
                        "Attacks target row and remove blocks from enemy fighters in target row. Must be used from the back column and moves Bull to the front column.",
                        ""),
        HACK(5, 1.5, 25, 35, 0, 0, StatusEnum.CURSE, true, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 7, 0,
                        999,
                        "Attacks target fighter. Consecutively targeted hacks deal +{sv} damage.",
                        "Consecutively targeted hacks deal +{sv} damage."),
        SHIELD_STRIKE(6, 2.5, 30, 40, 0, 20, StatusEnum.BLOCK, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER,
                        0, 0,
                        1,
                        "Attacks target fighter and Blocks.", "+{b} defence"),
        TOSS(7, 2, 60, 40, 0, 0, StatusEnum.STUN, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SQUARE, 0, 0, 1,
                        "Stuns and moves fighter in target Square forward.", "Stunned."),
        KICK(12, 2.0, 20, 40, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 0, 0, 0,
                        "Attacks target fighter.",
                        ""),
        FIREBALL(17, 3.0, 40, 80, 0, 0, StatusEnum.BURN, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 5, 0,
                        3,
                        "Attacks target fighter. Burns for {sv}/t", "-{sv} HP/t"),
        IGNITE(18, 2.0, 30, 0, 0, 0, StatusEnum.BURN, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 25, 0,
                        4,
                        "Burns for {sv}/t", "-{sv} HP/t"),

        SLASH(24, 2.5, 30, 50, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 0, 0, 0,
                        "Attacks target fighter. Gain an additional morale point.", ""),
        SMACK(32, 3.0, 40, 70, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 0, 0, 0,
                        "Attacks target fighter.",
                        ""),
        STAB(38, 2.0, 30, 50, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 25, 0, 0,
                        "Attacks target fighter. + {sv} damage when used from the front column.", ""),
        TRANQUILIZER(47, 3, 40, 70, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.NONE, 0,
                        -20, 0,
                        "Attacks fighter in a random row and lowers their energy by {er}.", ""),
        SHADOW_LUNGE(48, 3, 40, 30, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ATTACK, TargetTypeEnum.FIGHTER, 30,
                        0, 0,
                        "Attacks target fighter. Deals + {sv} damage and is unblockable from stealth.", ""),

        // Support / Healing (Some target others, LICK_WOUNDS targets SELF)
        GUST(14, 2.0, 40, 0, 30, 20, StatusEnum.NONE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SQUARE,
                        0, 0,
                        0,
                        "Heals fighters in targeted column.", ""),
        HYMN(15, 2.0, 30, 40, 20, 0, StatusEnum.BUFF, true, ActionCategoryEnum.SUPPORT, TargetTypeEnum.FIGHTER, 6, 0,
                        999,
                        "Attacks target enemy fighters and heals allied. Dove becomes evil with each attack and good with each heal gaining +6/-6 heal/damage per Good.",
                        "+ {sv} per stack"),
        LICK_WOUNDS(22, 3.0, 10, 0, 40, 0, StatusEnum.NONE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SELF, 0,
                        0, 0,
                        "Heals Lion.",
                        ""),
        TAXIDERMY(28, 3.0, 30, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.NONE, 0, 0,
                        1,
                        "Prevent fighter infront of Newt from dying this turn.", "HP can't be reduced below 1."),
        SOOTHE(33, 4.5, 10, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.NONE, 0, 40, 0,
                        "Restore the energy of a critter behind Turtle by {er}", ""),
        MELD(36, 2.0, 30, 0, 0, 0, StatusEnum.STEALTH, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SELF, 0, 0, 2,
                        "Wolf Stealths. Actions targeted at Stealthed fighters won't cast",
                        "Actions targeted at Wolf won't perform"),
        BRAY(39, 2.5, 30, 0, 0, 0, StatusEnum.STUN, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.FIGHTER, 0, 0, 1,
                        "Stuns target fighter.", "Stunned."),
        DONKEY_BLUES(40, 1.5, 25, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.NONE, 0,
                        -15, 0,
                        "Reduces enemy fighters' energy by {sv}", ""),
        INVIGORATE(41, 2, 25, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.NONE, 15, 0,
                        2,
                        "Raises the team's bonus damage  by {sv}.", "+{sv} damage"),
        MERCY(42, 3, 40, 20, 0, 0, StatusEnum.BURN, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.NONE, 30, 0, 4,
                        "5 Good: revives a random teammate. 5 Evil: deals {sv} damage each turn for {dur} turns.",
                        "-{sv} HP/t"),
        NEWTBREW(43, 1.5, 20, 0, 0, 0, StatusEnum.CURSE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.FIGHTER, -50,
                        40, 1,
                        "Restores adjacent target energy by {er} and remove {sv} at the end of the turn.",
                        "Lowers energy by 50 on expiry."),
        STOKE(44, 3, 10, 0, 0, 0, StatusEnum.BUFF, true, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SELF, 20, 30, 3,
                        "Adds a stack of {t} up to a max of 3 and restores {er} energy to Fox.",
                        "Next Fireball's damage is increased by {sv} * stacks."),
        CONTAGION(45, 3.5, 50, 0, 0, 0, StatusEnum.CURSE, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.FIGHTER, 25,
                        -25, 2,
                        "Infects target with a disease that spreads to adjacent fighters on hit and ticks for -{sv} hp and -{er} energy per turn. ",
                        "-{sv} hp and -{er} energy per turn. Transfers to adjacent fighters on hit."),
        EXECUTE(46, 3, 50, 50, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.FIGHTER, 0, 0,
                        0,
                        "Kills a fighter below 50 health.", ""),
        INFERNO(19, 3.0, 70, 60, 0, 0, StatusEnum.CHANNELLING, false, ActionCategoryEnum.SUPPORT, TargetTypeEnum.SELF,
                        0, 0,
                        999,
                        "Fox begins channelling {t}, Attacking the first fighter in each row at the end of each round. Casting or getting hit breaks the effect.",
                        "-{sv} HP/t blockable to the first opposing fighter in each row. Using any actions or getting hit ends the effect."),

        // Block / Defend
        DEFEND(21, 1.5, 30, 0, 0, 30, StatusEnum.BLOCK, false, ActionCategoryEnum.BLOCK, TargetTypeEnum.SELF, 0, 0, 1,
                        "Block.",
                        "+{b} defence"),
        POUNCE(23, 1.0, 20, 0, 0, 15, StatusEnum.BLOCK, false, ActionCategoryEnum.BLOCK, TargetTypeEnum.SQUARE, 0, 0, 1,
                        "Moves Lion to an adjacent square and Blocks.", "+{b} defence"),
        SHELL_STANCE(30, 2.0, 20, 0, 0, 20, StatusEnum.BLOCK, false, ActionCategoryEnum.BLOCK, TargetTypeEnum.SELF, 0,
                        0, 999,
                        "Blocks and prevents actions by Turtle until recast.",
                        "+{b} defence. Turtle can't take action."),
        STOUT_SHIELD(34, 2.0, 30, 0, 0, 40, StatusEnum.BLOCK, false, ActionCategoryEnum.BLOCK, TargetTypeEnum.SELF, 0,
                        0, 1,
                        "Block.", "+{b} defence"),
        INTERVENTION(13, 2.0, 0, 0, 10, 50, StatusEnum.BLOCK, false, ActionCategoryEnum.BLOCK,
                        TargetTypeEnum.SELF, 0,
                        60, 1, "Raises defence and restores {er} energy if Dove has less than 30 health and 30 energy.",
                        "+{b} defence"),

        // Passives (Usually NONE as they aren't "cast")
        RUNNING_START(8, 0, 0, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.PASSIVE, TargetTypeEnum.SELF, 20, 0,
                        1,
                        "First attack after unbenching has increased damage.", "+{sv} damage on next attack."),
        BENCH_COACH(25, 0, 0, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.PASSIVE, TargetTypeEnum.NONE, 5, 0,
                        999,
                        "Raise team's damage by {sv}.", "+{sv} damage."),
        REFLECT(31, 0, 0, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.PASSIVE, TargetTypeEnum.SELF, 0, 0, 1,
                        "Reflect attacks back at attackers on unbenching.", "Attacks are reflected back."),

        // Items
        CRIMBERRY_PACK(49, 0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 0,
                        "Remove burns from your team.", ""),
        OIL_BOMB(50, 0, 0, 25, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 0,
                        "Consumes enemy burns for {d} damage per burn.", ""),
        COXCOMB(51, 0, 0, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 1,
                        "Redirect enemy attacks toward the fighter with the most health.",
                        "Attacks are redirected to this fighter."),
        SMOKE_BOMB(52, 0, 0, 0, 0, 0, StatusEnum.BUFF, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 1,
                        "Give your team stealth until the end of turn.",
                        "Actioned targeted at this fighter won't performn."),
        THIEF_GLOVES(53, 0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 0,
                        "Cancel and steal an enemy item being used this turn.", ""),
        WAR_HORN(54, 0, 0, 0, 0, 0, StatusEnum.NONE, false, ActionCategoryEnum.ITEM, TargetTypeEnum.NONE, 0, 0, 0,
                        "Move enemy team to random squares.", "");

        public final int id;
        public final double time;
        public final int energy;
        public final int damage;
        public final int healing;
        public final int block;
        public final StatusEnum status;
        public final ActionCategoryEnum category;
        public final TargetTypeEnum targetType; // New Field
        public final int statusValue;
        public final int energyRestore;
        public final int duration;
        public final String description;
        public final String statusDescription;
        public final boolean stackable;

        ActionEnum(int id, double t, int e, int d, int h, int b, StatusEnum s, boolean stackable,
                        ActionCategoryEnum cat,
                        TargetTypeEnum tt,
                        int statusValue, int energyRestore, int duration, String description,
                        String statusDescription) {
                this.id = id;
                this.time = t;
                this.energy = e;
                this.damage = d;
                this.healing = h;
                this.block = b;
                this.status = s;
                this.stackable = stackable;
                this.category = cat;
                this.targetType = tt;
                this.statusValue = statusValue;
                this.energyRestore = energyRestore;
                this.duration = duration;
                this.description = description;
                this.statusDescription = statusDescription;
        }

        private static final Map<Integer, ActionEnum> BY_ID = new HashMap<>();
        static {
                for (ActionEnum e : values())
                        BY_ID.put(e.id, e);
        }

        public static ActionEnum fromId(int id) {
                return BY_ID.getOrDefault(id, NONE);
        }

        public static ActionEnum fromName(String name) {
                if (name == null)
                        return NONE;

                // Convert "Shield Strike" to "SHIELD_STRIKE"
                String lookup = name.toUpperCase().replace(" ", "_");

                try {
                        return ActionEnum.valueOf(lookup);
                } catch (IllegalArgumentException e) {
                        return NONE; // Default if name isn't found
                }
        }
}
