package com.gabe.animalia.ability.turtle;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class StoutShield extends Action {
	private String indicatorMessage;

	public StoutShield(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.STOUT_SHIELD);
	}

	@Override
	public void init() {
		target = subject;
		subject.getIndicatedBlocks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|block," + subject.getName() + "," + subject.getSide();
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public boolean customPerform() {

		subject.raiseDefence(this.block);
		subject.addEffect(this);
		return true;

	}

	@Override
	public void endEffect(Critter critter) {
		subject.lowerDefence(this.block);
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(target);

	}

	@Override
	public String getName() {
		return name;
	}
}
