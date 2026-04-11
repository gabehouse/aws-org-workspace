package com.gabe.animalia.ability.dove;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Intervention extends Action {

	private String indicatorMessage;
	private int energyRestore;

	public Intervention(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.INTERVENTION);
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
	public CastResult customCheck() {
		if (subject.getEnergy() <= 30
				&& subject.getHealth() <= 30) {
			return CastResult.SUCCESS;
		}
		return CastResult.CUSTOM_CHECK_FAILED;
	}

	@Override
	public boolean customPerform() {
		subject.restoreEnergy(energyRestore);
		subject.raiseDefence(block);
		subject.addEffect(this);
		return true;

	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

	@Override
	public void endEffect(Critter critter) {
		subject.lowerDefence(block);
	}

}
