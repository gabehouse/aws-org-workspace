package com.gabe.animalia.ability.lion;

import java.io.IOException;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Pounce extends Action {
	private String effectDescription = "defence + 15";
	private Square initialTempSpot;
	private String indicatorMessage;

	private String direction = "";

	public Pounce(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.POUNCE);
	}

	@Override
	public void displayOptions() {
		subject.displayPossibleMoves(player, name);

	}

	@Override
	public void init() {
		Square t = getTargetAsSquare();
		if (subject.getTempSpot().compareSurrounding(t.getName())) {

			subject.getMoves().add(this);
			initialTempSpot = subject.getTempSpot();
			subject.getIndicatedBlocks().add(subject);
			subject.getTempSpot().setPlannedMove(false);
			player.getQueue().add(this);
			t.setPlannedMove(true);
			subject.setTempSpot(t);
			// subject.displayPossibleMoves(player);
			subject.getIndicatedMoves().add(t);

			indicatorMessage = "indicate|block," + subject.getName() + ","
					+ subject.getSide() + "|move," + t.getName();

			if (subject.hasMoved()) {

				player.sendString("indicatespots,no moves");
				player.sendString("possiblemoves, ");

			} else {
				subject.displayPossibleMoves(player, name);
			}
		}
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	public void findDirection() {

		if (subject.getSpot().getOnLeft() != null) {
			if (subject.getSpot().getOnLeft().equals(target)) {
				direction = "up";
			}
		}
		if (subject.getSpot().getOnRight() != null) {
			if (subject.getSpot().getOnRight().equals(target)) {
				direction = "down";
			}
		}
		if (subject.getSpot().getInfront() != null) {
			if (subject.getSpot().getInfront().equals(target)) {
				direction = "forward";
			}
		}
		if (subject.getSpot().getBehind() != null) {
			if (subject.getSpot().getBehind().equals(target)) {
				direction = "backward";
			}
		}
	}

	@Override
	public boolean customInitable() {
		if (getTargetAsSquare() != null
				&& !getTargetAsSquare().isPlannedMove()) {

			return true;
		}
		return false;

	}

	@Override
	public CastResult customCheck() {
		Square s = getTargetAsSquare();
		if (s == null) {
			return CastResult.TARGET_IS_NULL;
		}
		if (s.isOccupied()) {
			return CastResult.SPOT_OCCUPIED;
		}
		if (!subject.getSpot().compareSurrounding(s.getName())) {
			return CastResult.INVALID_TARGET_SPOT;
		}
		return CastResult.SUCCESS;

	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();

		// Row 1: The Benched Critter
		sb.append(buildManifestRow(
				"MOVE", "MOVE", this.getType(), subject,
				"square", target.getName(), subject.getSide(), subject.getHealth(), subject.getMaxHealth(),
				startTime, this.logStr, "none", "none"));
		sb.append("|");
		sb.append(buildManifestRow(
				"basic", this.getName(), this.getType(), subject,
				"critter", subject.getName(), subject.getSide(), subject.getHealth(), subject.getMaxHealth(),
				startTime, "none", this.getSubject().getFullEffectSnapshot(), "none"));

		return sb.toString();
	}

	@Override
	public boolean customPerform() {
		Square t = getTargetAsSquare();
		subject.onMove(subject.getOwner(), subject.getOpponent());
		findDirection();
		subject.getSpot().setPlannedMove(false);
		subject.getSpot().setCritter(null);
		subject.setSpot(t);
		t.setCritter(subject);
		t.setPlannedMove(true);
		subject.getIndicatedMoves().remove(t);

		subject.raiseDefence(block);
		subject.addEffect(this);
		return true;

	}

	@Override
	public void endEffect(Critter critter) {
		subject.lowerDefence(block);
	}

	@Override
	public void delete() {
		Square t = getTargetAsSquare();
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			t.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square) subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			t.setPlannedMove(false);
		}
		subject.getMoves().remove(this);
		subject.getIndicatedBlocks().remove(subject);
		subject.getIndicatedMoves().remove(t);

	}

}
