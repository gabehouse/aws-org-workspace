package com.gabe.animalia.ability.turtle;

import java.io.IOException;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Smack extends Action {
	private String indicatorMessage;

	public Smack(Critter subject, Targetable target,
			Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.SMACK);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + ","
				+ target.getSide();

		player.sendString("option,remove," + name);

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
		System.out.println(str);

		player
				.sendString("option,attack," + name + "," + str);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		dealStandardDamage(t, 0);
		return true;
	}

}
