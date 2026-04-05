package com.gabe.animalia.ability.bull;

import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Charge extends Action {
	private Square spot = null;
	private Square[] targets = new Square[2];
	private int damageToDeal = damage;
	private ArrayList<Targetable> toAnimateImpact = new ArrayList<Targetable>();
	private Square squareToHit;
	private Square otherSquareToHit;
	private String indicatorMessage = "";

	public Charge(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.CHARGE);
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
		String str = "indicatespots,attack," + name + ","
				+ otherPlayer.getSide() + "TopBack." + startX + "0-" + (startX + 2) + "0-" + (startX + 2) + "1-"
				+ startX + "1-" + startX + "0." + otherPlayer.getSide() + ","
				+ otherPlayer.getSide() + "MiddleBack." + startX + "1-" + (startX + 2) + "1-" + (startX + 2) + "2-"
				+ startX + "2-" + startX + "1." + otherPlayer.getSide() + ","
				+ otherPlayer.getSide() + "BottomBack." + startX + "2-" + (startX + 2) + "2-" + (startX + 2) + "3-"
				+ startX + "3-" + startX + "2." + otherPlayer.getSide() + ",";

		player.sendString(str);

	}

	@Override
	public void init() {
		squareToHit = getTargetAsSquare();
		subject.getMoves().add(this);
		subject.getIndicatedAttacks().add(target);
		spot = subject.getTempSpot().getInfront();
		subject.getIndicatedMoves().add(spot);
		spot.setPlannedMove(true);
		subject.getTempSpot().setPlannedMove(false);
		subject.setTempSpot(spot);

		player.getQueue().add(this);
		indicatorMessage = "indicate|attackspot," + squareToHit.getName();
		if (squareToHit.getInfront() != null) {
			otherSquareToHit = squareToHit.getInfront();
		} else {
			otherSquareToHit = squareToHit.getBehind();
		}
		subject.getIndicatedAttacks().add(otherSquareToHit);
		indicatorMessage += "|attackspot," + otherSquareToHit.getName() + "|move," + spot.getName();

		player.sendString("indicatespots,no moves");
		toAnimateImpact.add(squareToHit);
		toAnimateImpact.add(otherSquareToHit);

	}

	@Override
	public boolean customInitable() {
		if (subject.getTempSpot().getInfront() != null) {
			if (!subject.getTempSpot().getInfront().isPlannedMove()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public CastResult customCheck() {
		if (subject.getSpot().getInfront() != null && subject.getSpot().getInfront().getCritter() == null) {
			return CastResult.SUCCESS;
		}
		return CastResult.UNKNOWN_FAILURE;
	}

	@Override
	public boolean customPerform() {

		targets[0] = squareToHit;
		targets[1] = otherSquareToHit;
		int damageToDeal = this.damage + subject.getBonusDmg();
		for (Square t : targets) {
			if (t.getCritter() != null && t.getCritter().isAlive()) {
				Critter critter = t.getCritter();

				critter.setHealth(critter.getHealth() - damageToDeal);
				ArrayList<String> toRemoveNames = new ArrayList<String>();
				critter.clearEffectsByType(StatusEnum.BLOCK);

				spot.setCritter(subject);

				critter.onHit(subject.getOwner(), subject.getOpponent(), this);

			}
			subject.removeEffect(ActionEnum.RUNNING_START);
			subject.getSpot().setPlannedMove(false);
			subject.getSpot().setCritter(null);
			subject.setSpot(spot);
			subject.getIndicatedMoves().remove(spot);
			spot.setPlannedMove(true);
			subject.onMove(subject.getOwner(), subject.getOpponent());
		}

		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();
		Square toMove = subject.getSpot();
		// Row 1: The Benched Critter
		sb.append(buildManifestRow(
				"MOVE", "MOVE", this.getType(), subject,
				"square", toMove.getName(), toMove.getSide(), -1, -1,
				startTime, getLogStr(), subject.getFullEffectSnapshot(), "none"));

		if (targets[0] == null)
			return sb.toString();

		for (Square s : targets) {
			Critter target = s.getCritter();
			if (target != null) {
				sb.append("|");
				// Row 2: The Swap Target
				sb.append(buildManifestRow(
						"basic", this.getName(), this.getType(), subject,
						"critter", target.getName(), target.getSide(), target.getHealth(), target.getMaxHealth(),
						startTime, "none", subject.getFullEffectSnapshot(), target.getFullEffectSnapshot()));
			}
		}

		return sb.toString();
	}

	@Override
	public void delete() {
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			spot.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square) subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			spot.setPlannedMove(false);
		}
		subject.getIndicatedAttacks().remove(squareToHit);
		subject.getIndicatedAttacks().remove(otherSquareToHit);
		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(spot);
		for (Critter c : player.getCritters()) {
			if (c.getTempSpot().equals(spot)) {
				spot.setPlannedMove(true);
			}
		}
	}
}
