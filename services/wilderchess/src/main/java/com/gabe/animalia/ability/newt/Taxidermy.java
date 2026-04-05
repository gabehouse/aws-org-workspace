package com.gabe.animalia.ability.newt;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Taxidermy extends Action {
	private String indicatorMessage;

	public Taxidermy(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.TAXIDERMY);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		player.getQueue().add(this);
	}

	@Override
	public CastResult customCheck() {
		if (subject.getSpot().getInfront() == null || subject.getSpot().getInfront().getCritter() == null) {
			return CastResult.MISSED;
		}
		return CastResult.SUCCESS;
	}

	@Override
	public boolean customPerform() {

		Critter target = null;

		target = subject.getSpot().getInfront().getCritter();
		target.addEffect(this);

		return true;
	}

}
