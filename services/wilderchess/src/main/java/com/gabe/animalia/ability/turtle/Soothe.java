package com.gabe.animalia.ability.turtle;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Soothe extends Action {
	private boolean used;
	private String indicatorMessage;
	private Critter restoreTo = null;

	public Soothe(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.SOOTHE);

	}

	@Override
	public void init() {
		if (initable()) {

			target = subject.getTempSpot().getBehind();
			indicatorMessage = "indicate|supportspot," + target.getName();
			subject.getIndicatedSupports().add(target);
			player.getQueue().add(this);
		}
	}

	@Override
	public boolean customInitable() {
		if (subject != null &&
				subject.getTempSpot() != null &&
				subject.getTempSpot().getBehind() != null &&
				subject.getTempSpot().getBehind().getName() != null &&
				!subject.getTempSpot().getBehind().getName().contains("Bench")) {
			return true;
		}
		return false;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public CastResult customCheck() {
		if (subject != null &&
				subject.getSpot() != null &&
				subject.getSpot().getBehind() != null &&
				subject.getSpot().getBehind().getName() != null &&
				!subject.getSpot().getBehind().getName().contains("Bench") &&
				subject.getSpot().getBehind().getCritter() != null) {
			return CastResult.SUCCESS;
		}
		return CastResult.CUSTOM_CHECK_FAILED;

	}

	@Override
	public boolean customPerform() {

		Square t = (Square) subject.getSpot().getBehind();

		restoreTo = t.getCritter();
		restoreTo.restoreEnergy(this.energyRestore);
		return true;

	}

	@Override
	public String getManifestData(double startTime) {
		if (restoreTo == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(buildManifestRow(
				"basic", getName(), this.getType(), subject,
				"critter", restoreTo.getName(), restoreTo.getSide(), restoreTo.getHealth(), restoreTo.getMaxHealth(),
				startTime, getLogStr(), subject.getFullEffectSnapshot(), "none"));
		restoreTo.markDirty();

		return sb.toString();
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

}
