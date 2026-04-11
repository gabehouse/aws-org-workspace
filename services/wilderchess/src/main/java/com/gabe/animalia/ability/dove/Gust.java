package com.gabe.animalia.ability.dove;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Gust extends Action {

	private String indicatorMessage;
	Square otherTarget, otherOtherTarget;

	public Gust(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.GUST);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayOptions() {
		String str = "indicatespots,support," + name + ","
				+ "leftTopBack.00-10-13-03-00.left,"
				+ "leftTopFront.10-20-23-13-10.left,"
				+ "rightTopFront.30-40-43-33-30.right,"
				+ "rightTopBack.40-50-53-43-40.right,";

		player.sendString(str);

	}

	@Override
	public void init() {
		Square t = getTargetAsSquare();

		subject.getIndicatedSupports().add(t);
		player.getQueue().add(this);
		indicatorMessage = "indicate|supportspot," + t.getName();
		if (t.getOnLeft() != null) {
			if (t.getOnLeft().getOnLeft() != null) {
				otherTarget = t.getOnLeft();
				otherOtherTarget = t.getOnLeft().getOnLeft();
				subject.getIndicatedSupports().add(otherTarget);
				subject.getIndicatedSupports().add(otherOtherTarget);
				indicatorMessage += "|supportspot," + otherTarget.getName() +
						"|supportspot," + otherOtherTarget.getName();
			} else {
				otherTarget = t.getOnLeft();
				otherOtherTarget = t.getOnRight();
				subject.getIndicatedSupports().add(otherTarget);
				subject.getIndicatedSupports().add(otherOtherTarget);
				indicatorMessage += "|supportspot," + otherTarget.getName() +
						"|supportspot," + otherOtherTarget.getName();
			}
		} else {
			otherTarget = t.getOnRight();
			otherOtherTarget = t.getOnRight().getOnRight();
			subject.getIndicatedSupports().add(otherTarget);
			subject.getIndicatedSupports().add(t.getOnRight().getOnRight());
			indicatorMessage += "|supportspot," + otherTarget.getName() +
					"|supportspot," + otherOtherTarget.getName();
		}
		player.sendString("indicatespots,no moves");

	}

	@Override
	public boolean customPerform() {
		Square t = getTargetAsSquare();

		Square[] targets = { t, otherTarget, otherOtherTarget };
		for (Square s : targets) {
			if (s.getCritter() != null && s.getCritter().isAlive()) {
				Critter critter = s.getCritter();

				restoreHealth(critter, this.healing);

			}
		}
		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();
		double rowDelay = startTime;

		// Get all potential targets (Square objects)
		Square[] targets = { getTargetAsSquare(), otherTarget, otherOtherTarget };

		boolean firstRow = true; // This MUST be outside the loop

		for (Square s : targets) {
			if (s == null || s.getCritter() == null)
				continue;

			Critter toAnimate = s.getCritter();

			// If this isn't the very first row we've added, add a pipe separator
			if (sb.length() > 0) {
				sb.append("|");
			}

			rowDelay += 0.05; // Staggered animations

			// Use the EXACT same logic as your working normal heal, but inside the loop
			sb.append(buildManifestRow(
					"basic",
					this.getName(),
					this.getType(),
					this.getSubject(),
					"critter",
					toAnimate.getName(),
					toAnimate.getSide(),
					toAnimate.getHealth(),
					toAnimate.getMaxHealth(),
					rowDelay,
					firstRow ? this.getLogStr() : "none", // Log only once
					this.getSubject().getFullEffectSnapshot(),
					toAnimate.getFullEffectSnapshot()));

			firstRow = false; // Ensure next loops send "none" for log
		}

		return sb.toString();
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
		subject.getIndicatedSupports().remove(otherTarget);
		subject.getIndicatedSupports().remove(otherOtherTarget);

	}

}
