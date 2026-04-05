package com.gabe.animalia.ability.lion;

import java.io.IOException;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class LickWounds extends Action {
	private String indicatorMessage;

	public LickWounds(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.LICK_WOUNDS);
	}

	@Override
	public void init() {
		target = subject;
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

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

		restoreHealth(t, this.healing);

		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

}
