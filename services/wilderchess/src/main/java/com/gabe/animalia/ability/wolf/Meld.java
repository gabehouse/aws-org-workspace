package com.gabe.animalia.ability.wolf;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Meld extends Action {
	private String indicatorMessage;

	public Meld(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.MELD);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + subject.getName() + "," + subject.getSide();
		target = subject;

	}

	@Override
	public boolean customPerform() {

		subject.addEffect(this);

		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		Critter t = getTargetAsCritter();

		StringBuilder sb = new StringBuilder();

		// 1. The Benched Critter moving to the Active Spot
		// We pass -1 for target HP because we are targeting a "square" (the active
		// spot)
		sb.append(buildManifestRow(
				"STEALTH", // behavior
				getName(), // name
				this.getType(), // type
				subject, // The Critter moving
				"critter", // tarType
				subject.getName(), // tarName
				subject.getSide(), // tarSide
				t.getHealth(), t.getMaxHealth(), // tarHP, tarMaxHP
				startTime, // time
				getLogStr(), // log
				subject.getFullEffectSnapshot(), // subBundle (update icons!)
				t.getFullEffectSnapshot() // tarBundle
		));

		return sb.toString();
	}

	@Override
	public String getLogStr() {
		return subject.getLogName() + " has stealthed.";
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

}
