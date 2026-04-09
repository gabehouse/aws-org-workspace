package com.gabe.animalia.ability.fox;

import com.gabe.animalia.critter.Fox;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Stoke extends Action {
	private String indicatorMessage;

	public Stoke(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.STOKE);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();
	}

	@Override
	public boolean customPerform() {
		subject.restoreEnergy(
				this.energyRestore);
		int stokeStacks = 0;
		if (subject.hasEffect(ActionEnum.STOKE)) {
			stokeStacks = subject.getEffect(ActionEnum.STOKE).getStacks();
		}
		if (stokeStacks < 3) {
			subject.addEffect(this);
		}

		return true;

	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
	}

}
