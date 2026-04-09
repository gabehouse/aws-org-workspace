package com.gabe.animalia.ability.wolf;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class ShadowLunge extends Action {
	private String indicatorMessage;
	private boolean stealthed = false;

	public ShadowLunge(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.SHADOW_LUNGE);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide();

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

		player.sendString("option,attack," + name + "," + str);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		int bonusDmg = 0;
		if (subject.hasStatus(StatusEnum.STEALTH)) {
			bonusDmg = this.statusValue;
			t.setHealth(t.getHealth() - (this.damage + bonusDmg));
		} else {
			dealStandardDamage(t, bonusDmg);
		}

		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
