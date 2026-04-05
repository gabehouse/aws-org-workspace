package com.gabe.animalia.ability.lion;

import java.io.IOException;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Defend extends Action {

	private String indicatorMessage;

	public Defend(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.DEFEND);
	}

	@Override
	public void init() {
		subject.getIndicatedBlocks().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|block," + subject.getName() + "," + subject.getSide();
		target = subject;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		t.addEffect(this);
		t.raiseDefence(block);
		return true;
	}

	@Override
	public void endEffect(Critter critter) {
		subject.lowerDefence(block);
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(target);

	}

}
