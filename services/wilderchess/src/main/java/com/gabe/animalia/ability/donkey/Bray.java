package com.gabe.animalia.ability.donkey;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Bray extends Action {
	private String indicatorMessage;

	public Bray(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.BRAY);
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
		for (Critter f : otherPlayer.getCritters()) {
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
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		t.addEffect(this);

		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}
}
