package com.gabe.animalia.ability.newt;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Newtbrew extends Action {
	private String indicatorMessage;
	private Critter t;

	public Newtbrew(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.NEWTBREW);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayOptions() {
		String str = "";
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

		player.sendString("option,support," + name + "," + str);

	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

		player.sendString("option,remove," + name);

	}

	@Override
	public CastResult customCheck() {
		t = getTargetAsCritter();
		if (t == null)
			return CastResult.TARGET_IS_NULL;
		if (!t.getSpot()
				.compareSurrounding(subject.getSpot().getName())) {
			return CastResult.TARGET_MUST_BE_ADJACENT;
		}
		return CastResult.SUCCESS;
	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		if (t.getSpot()
				.compareSurrounding(subject.getSpot().getName())) {
			t.addEffect(this);
			t.restoreEnergy(
					40);

			return true;
		}
		return false;
	}

	@Override
	public void endEffect(Critter critter) {
		getTargetAsCritter().spendEnergy(50);
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

}
