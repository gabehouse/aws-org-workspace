package com.gabe.animalia.ability.donkey;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class DonkeyBlues extends Action {
	private String indicatorMessage;

	public DonkeyBlues(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.DONKEY_BLUES);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		player.getQueue().add(this);
		indicatorMessage = "indicate|";
		for (Critter f : otherPlayer.getCritters()) {
			subject.getIndicatedSupports().add(f);
			indicatorMessage += "support," + f.getName() + "," + f.getSide() + "|";
		}
	}

	@Override
	public boolean customPerform() {
		for (Critter f : otherPlayer.getCritters()) {
			f.spendEnergy(15);
			f.markDirty();
		}
		return true;
	}

	@Override
	public void delete() {
		for (Critter f : otherPlayer.getCritters()) {
			subject.getIndicatedSupports().remove(f);
		}

	}

}
