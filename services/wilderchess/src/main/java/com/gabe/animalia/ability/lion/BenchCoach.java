package com.gabe.animalia.ability.lion;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class BenchCoach extends Action {

    private String indicatorMessage;

    public BenchCoach(Critter subject, Targetable target, Player player, Player otherPlayer) {
        super(subject, target, player, otherPlayer, ActionEnum.BENCH_COACH);
    }

    @Override
    public String getIndicatorMessage() {
        return indicatorMessage;
    }

    @Override
    public void endEffect(Critter critter) {
        critter.lowerBonusDmg(this.statusValue);
    }

}
