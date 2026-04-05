package com.gabe.animalia.ability.bull;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class ShieldStrike extends Action {
	private String indicatorMessage;

	public ShieldStrike(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.SHIELD_STRIKE);
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
		System.out.println(str);

		player.sendString("option,attack," + name + "," + str);

	}

	@Override
	public void init() {
		if (target == null)
			return;
		subject.getIndicatedAttacks().add(target);
		subject.getIndicatedBlocks().add(subject);
		player.getQueue().add(this);

		indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide() + "|block,"
				+ subject.getName() + "," + subject.getSide();

		player.sendString("option,remove," + name);

	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(subject);
		subject.getIndicatedAttacks().remove(target);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		subject.addEffect(this);
		subject.raiseDefence(this.block);

		dealStandardDamage(t, 0);

		subject.removeEffect(ActionEnum.RUNNING_START);
		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		Critter t = getTargetAsCritter();

		StringBuilder sb = new StringBuilder();

		// Row 1: The Benched Critter
		sb.append(buildManifestRow(
				"basic", this.getName(), this.category, subject,
				"critter", t.getName(), t.getSide(), t.getHealth(), t.getMaxHealth(),
				startTime, "none", "none", "none"));
		sb.append("|");
		sb.append(buildManifestRow(
				"basic", this.getName(), ActionCategoryEnum.BLOCK, subject,
				"critter", subject.getName(), subject.getSide(), subject.getHealth(), subject.getMaxHealth(),
				startTime, getLogStr(), this.getSubject().getFullEffectSnapshot(), "none"));

		return sb.toString();
	}

	@Override
	public void endEffect(Critter critter) {
		subject.setDefence(subject.getDefence() - block);
	}

}
