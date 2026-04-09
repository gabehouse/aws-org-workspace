package com.gabe.animalia.ability;

import java.util.Arrays;
import java.util.List;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Bench extends Action {
	private String indicatorMessage;
	private Critter unbenching = null;
	private Square transitionSpot = null;
	private String actionStr = "";
	private String direction = "";
	private boolean used = false;

	public Bench(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.BENCH);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getMoves().add(this);
		Square s = getTargetAsSquare();

		transitionSpot = subject.getTempSpot();
		for (Critter f : getPlayer().getCritters()) {
			if (f.getTempSpot().equals(target)) {
				unbenching = f;
			}
		}
		if (unbenching != null) {
			unbenching.getMoves().add(this);
			unbenching.setTempSpot(transitionSpot);
			unbenching.getIndicatedMoves().add(transitionSpot);

		} else if (!s.isPlannedMove()) {
			transitionSpot.setPlannedMove(false);
			s.setPlannedMove(true);
		}

		subject.setTempSpot(s);
		subject.getIndicatedMoves().add(s);
		getPlayer().getQueue().add(this);
		getPlayer().setAllottedTime(getPlayer().getAllottedTime() + timeCost);
		indicatorMessage = "indicate|move," + s.getName();
		if (unbenching != null) {
			indicatorMessage = "indicate|move,"
					+ unbenching.getTempSpot().getName() + "|move," + s.getName();

		}
	}

	@Override
	public void displayOptions() {
		if (getPlayer().getCritters().length > 1) {
			String str = "indicatespots,move,move," + target.getName() + "."
					+ target.getName() + "." + getPlayer().getSide() + ",";

			getPlayer().sendString(str);
			str = "";

		}

	}

	@Override
	public void displayAction() {
		Square s = getTargetAsSquare();
		if (subject.canMove()) {
			System.out.println("displayaction2 " + subject.getSpot().getName());
			if (subject.getSpot().compareSurrounding(target.getName())) {
				System.out.println("bench display action");

				getPlayer().sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + ","
								+ (timeCost * 1000));
				getOtherPlayer().sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + ","
								+ (timeCost * 1000));
				if (s.isOccupied()) {
					getPlayer().sendString(
							"move," + s.getCritter().getName() + ","
									+ subject.getSide() + ","
									+ subject.getSpot().getName() + ","
									+ (timeCost * 1000));
					getOtherPlayer().sendString(
							"move," + s.getCritter().getName() + ","
									+ subject.getSide() + ","
									+ subject.getSpot().getName() + ","
									+ (timeCost * 1000));
				}

			}
		}
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
	public CastResult customCheck() {
		if (subject.getSpot().compareSurrounding(target.getName())) {
			return CastResult.SUCCESS;
		}

		return CastResult.INVALID_TARGET_SPOT;
	}

	@Override
	public boolean customPerform() {
		findDirection();
		Square s = getTargetAsSquare();
		subject.onMove(subject.getOwner(), subject.getOpponent());
		if (s.isOccupied()) {
			unbenching = s.getCritter();
			this.logStr = subject.getLogName() + " was benched for "
					+ unbenching.getLogName() + ".";

			unbenching.setSpot(subject.getSpot());
			unbenching.setBenched(false);
			subject.getSpot().setCritter(unbenching);

			// Create a temporary "work set"
			List<List<Action>> allQueues = Arrays.asList(getPlayer().getQueue(), getOtherPlayer().getQueue());

			for (List<Action> queue : allQueues) {
				for (Action a : queue) {
					if (a.isTargeting(subject) && !a.isSelfTargeting()) {
						a.setTarget(unbenching);
					}
				}
			}

			unbenching.getIndicatedMoves().remove(unbenching.getSpot());
			unbenching.unbenchEffect(getPlayer(), getOtherPlayer());

		} else {
			subject.getSpot().setPlannedMove(false);
			subject.getSpot().setCritter(null);
			this.logStr = subject.getLogName() + " was benched.";
		}

		subject.setSpot(s);
		subject.benchEffect(getPlayer(), getOtherPlayer());
		s.setCritter(subject);
		subject.getIndicatedMoves().remove(s);
		s.setPlannedMove(true);
		subject.setBenched(true);
		used = true;
		return true;
	}

	@Override
	public void delete() {
		Square s = getTargetAsSquare();
		s.setPlannedMove(false);
		transitionSpot.setPlannedMove(false);
		Square subPrevSpot = transitionSpot;
		if (subject.getMoves().size() == 1) {
			subPrevSpot.setPlannedMove(true);
			subPrevSpot = subject.getSpot();
			subject.setTempSpot(subPrevSpot);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subPrevSpot.setPlannedMove(true);
			subPrevSpot = (Square) subject.getMoves().get(subject.getMoves().size() - 2).getTarget();
			subject.setTempSpot(subPrevSpot);
		}

		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(target);

		if (unbenching != null) {
			Square unbenchingPrevSpot = unbenching.getSpot();
			if (unbenching.getMoves().size() == 1) {
				unbenching.setTempSpot(unbenchingPrevSpot);
				unbenchingPrevSpot.setPlannedMove(true);
				unbenchingPrevSpot = unbenching.getSpot();
			} else if (this.equals(unbenching.getMoves().get(unbenching.getMoves().size() - 1))) {
				unbenching.setTempSpot(unbenchingPrevSpot);
				unbenchingPrevSpot.setPlannedMove(true);
				unbenchingPrevSpot = (Square) unbenching.getMoves().get(unbenching.getMoves().size() - 2).getTarget();
			}

			unbenching.getMoves().remove(this);
			unbenching.getIndicatedMoves().remove(transitionSpot);
		} else {

			s.setPlannedMove(false);
		}

	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();

		// Row 1: The Benched Critter
		sb.append(buildManifestRow(
				"MOVE", "MOVE", this.getType(), subject,
				"square", target.getName(), subject.getSide(), -1, -1,
				startTime, this.logStr, subject.getFullEffectSnapshot(), "none"));

		if (unbenching != null) {
			sb.append("|");
			// Row 2: The Swap Target
			sb.append(buildManifestRow(
					"MOVE", "MOVE", this.getType(), unbenching,
					"square", unbenching.getSpot().getName(), unbenching.getSide(), -1, -1,
					startTime, "none", unbenching.getFullEffectSnapshot(), "none"));
			if (unbenching.getFighterType() == FighterType.WOLF) {
				sb.append("|");
				// Row 2: The Swap Target
				sb.append(buildManifestRow(
						"STEALTH", // behavior
						getName(), // name
						this.getType(), // type
						unbenching, // The Critter moving
						"critter", // tarType
						unbenching.getName(), // tarName
						unbenching.getSide(), // tarSide
						unbenching.getHealth(), unbenching.getMaxHealth(), // tarHP, tarMaxHP
						startTime, // time
						unbenching.getLogName() + " melded on unbench.", // log
						unbenching.getFullEffectSnapshot(), // subBundle (update icons!)
						unbenching.getFullEffectSnapshot() // tarBundle
				));
			}
		}

		return sb.toString();
	}
}
