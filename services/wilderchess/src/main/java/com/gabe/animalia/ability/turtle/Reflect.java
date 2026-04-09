package com.gabe.animalia.ability.turtle;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Reflect extends Action {
	private String indicatorMessage;

	public Reflect(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.REFLECT);
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
	public boolean perform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		t.addEffect(this);
		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
