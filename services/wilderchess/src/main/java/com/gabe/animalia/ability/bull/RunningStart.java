package com.gabe.animalia.ability.bull;

import com.gabe.animalia.ability.turtle.Reflect;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class RunningStart extends Action {
	private String indicatorMessage;

	public RunningStart(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.RUNNING_START);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();
	}

	@Override
	public boolean customPerform() {
		subject.raiseBonusDmg(this.statusValue);
		((Critter) target).addEffect(this);
		return true;
	}

	@Override
	public void endEffect(Critter critter) {
		subject.lowerBonusDmg(this.statusValue);
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}
}
