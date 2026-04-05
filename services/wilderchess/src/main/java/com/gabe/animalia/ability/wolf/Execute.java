package com.gabe.animalia.ability.wolf;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Execute extends Action {
	private String indicatorMessage;

	public Execute(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.EXECUTE);

	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

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
				.sendString("option,support," + name + "," + str);

	}

	@Override
	public CastResult customCheck() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return CastResult.CUSTOM_CHECK_FAILED;

		if (subject.getSpot().getInfront() == null) {
			if ((double) t.getHealth() / (double) t.getMaxHealth() * 100 < 50) {
				// subject is in first row and target is below 50 hp
				return CastResult.SUCCESS;
			}

		}
		return CastResult.CUSTOM_CHECK_FAILED;
	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		t.setHealth(0);

		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
