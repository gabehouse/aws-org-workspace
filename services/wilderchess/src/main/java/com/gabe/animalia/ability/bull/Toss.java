package com.gabe.animalia.ability.bull;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Toss extends Action {

	private String indicatorMessage;
	private Critter hitFighter = null;

	public Toss(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.TOSS);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;

	}

	@Override
	public void displayOptions() {
		int startX = 0;
		if (otherPlayer.getSide().equals("right")) {
			startX = 3;
		}
		String str = "indicatespots,support," + name + ",";
		if (otherPlayer.getSide().equals("right")) {
			str += otherPlayer.getSide() + "TopFront." + startX + "0-"
					+ (startX + 1) + "0-" + (startX + 1) + "1-" + startX + "1-"
					+ startX + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleFront." + startX + "1-"
					+ (startX + 1) + "1-" + (startX + 1) + "2-" + startX + "2-"
					+ startX + "1." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "BottomFront." + startX + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "3-" + startX + "3-"
					+ startX + "2." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "TopBack." + (startX + 1) + "0-"
					+ (startX + 2) + "0-" + (startX + 2) + "1-" + (startX + 1)
					+ "1-" + (startX + 1) + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleBack." + (startX + 1)
					+ "1-" + (startX + 2) + "1-" + (startX + 2) + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "1."
					+ otherPlayer.getSide() + "," + otherPlayer.getSide()
					+ "BottomBack." + (startX + 1) + "2-" + (startX + 2) + "2-"
					+ (startX + 2) + "3-" + (startX + 1) + "3-" + (startX + 1)
					+ "2." + otherPlayer.getSide() + ",";
		} else {
			str += otherPlayer.getSide() + "TopBack." + startX + "0-"
					+ (startX + 1) + "0-" + (startX + 1) + "1-" + startX + "1-"
					+ startX + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleBack." + startX + "1-"
					+ (startX + 1) + "1-" + (startX + 1) + "2-" + startX + "2-"
					+ startX + "1." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "BottomBack." + startX + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "3-" + startX + "3-"
					+ startX + "2." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "TopFront." + (startX + 1) + "0-"
					+ (startX + 2) + "0-" + (startX + 2) + "1-" + (startX + 1)
					+ "1-" + (startX + 1) + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleFront." + (startX + 1)
					+ "1-" + (startX + 2) + "1-" + (startX + 2) + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "1."
					+ otherPlayer.getSide() + "," + otherPlayer.getSide()
					+ "BottomFront." + (startX + 1) + "2-" + (startX + 2) + "2-"
					+ (startX + 2) + "3-" + (startX + 1) + "3-" + (startX + 1)
					+ "2." + otherPlayer.getSide() + ",";
		}

		player.sendString(str);

	}

	@Override
	public void init() {

		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|supportspot," + target.getName();

		player.sendString("indicatespots,no moves");

	}

	@Override
	public boolean customPerform() {
		Square t = getTargetAsSquare();
		if (t == null)
			return false;

		hitFighter = t.getCritter();

		if (hitFighter != null) {
			hitFighter.addEffect(this);
			// move critter in the target spot forward if possible
			if (t.getInfront() != null
					&& !t.getInfront().isOccupied()) {
				hitFighter.getSpot().setCritter(null);
				hitFighter.setSpot(t.getInfront());
				t.getInfront().setCritter(hitFighter);
			}
			hitFighter.setHealth(hitFighter.getHealth() - this.damage + subject.getBonusDmg());
			subject.removeEffect(ActionEnum.RUNNING_START);
		}
		return true;
	}

	@Override
	public String getManifestData(double startTime) {

		StringBuilder sb = new StringBuilder();

		// Row 2: The Swap Target
		sb.append(buildManifestRow(
				"basic", this.getName(), this.getType(), subject,
				"square", target.getName(), target.getSide(), -1,
				-1,
				startTime, getLogStr(), subject.getFullEffectSnapshot(), "none"));

		if (hitFighter == null)
			return sb.toString();

		sb.append("|");
		sb.append(buildManifestRow(
				"MOVE", "MOVE", this.getType(), hitFighter,
				"square", hitFighter.getSpot().getName(), hitFighter.getSide(), hitFighter.getHealth(),
				hitFighter.getMaxHealth(),
				startTime, "none", hitFighter.getFullEffectSnapshot(), hitFighter.getFullEffectSnapshot()));

		// Row 1: The Benched Critter

		return sb.toString();
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
	}

}
