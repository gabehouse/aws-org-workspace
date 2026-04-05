package com.gabe.animalia.ability;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Unbench extends Action {
	private boolean used = false;
	private String indicatorMessage;
	private Critter toSwapWith = null;
	private Square bench = null;
	private String actionStr = "";
	private String direction = "";

	public Unbench(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.UNBENCH);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		Square s = getTargetAsSquare();
		if (timeCost + getPlayer().getAllottedTime() <= 10) {
			subject.getMoves().add(this);
			bench = subject.getTempSpot();
			if (s.isPlannedMove()) {
				for (Critter c : getPlayer().getCritters()) {
					if (c.getTempSpot().equals(s))
						toSwapWith = c;
				}
			}
			if (toSwapWith != null) {
				toSwapWith.getMoves().add(this);
				toSwapWith.setTempSpot(bench);
				toSwapWith.getIndicatedMoves().add(bench);

			} else if (!s.isPlannedMove()) {
				s.setPlannedMove(true);
				bench.setPlannedMove(false);
			}
			subject.setTempSpot(s);
			subject.getIndicatedMoves().add(s);
			getPlayer().getQueue().add(this);
			getPlayer().setAllottedTime(getPlayer().getAllottedTime() + timeCost);
			indicatorMessage = "indicate|move," + s.getName();
			if (toSwapWith != null) {
				indicatorMessage = "indicate|move,"
						+ toSwapWith.getTempSpot().getName() + "|move,"
						+ s.getName();

			}
		}

	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();

		// 1. The Benched Critter moving to the Active Spot
		// We pass -1 for target HP because we are targeting a "square" (the active
		// spot)
		sb.append(buildManifestRow(
				"MOVE", // behavior
				"MOVE", // name
				this.getType(), // type
				subject, // The Critter moving
				"square", // tarType
				target.getName(), // tarName
				subject.getSide(), // tarSide
				-1, -1, // tarHP, tarMaxHP
				startTime, // time
				"none", // log
				subject.getFullEffectSnapshot(), // subBundle (update icons!)
				"none" // tarBundle
		));

		if (subject.getFighterType() == FighterType.WOLF) {
			sb.append("|");
			// Row 2: The Swap Target
			sb.append(buildManifestRow(
					"STEALTH", // behavior
					getName(), // name
					this.getType(), // type
					subject, // The Critter moving
					"critter", // tarType
					subject.getName(), // tarName
					subject.getSide(), // tarSide
					subject.getHealth(), subject.getMaxHealth(), // tarHP, tarMaxHP
					startTime, // time
					subject.getLogName() + " melded on unbench.", // log
					subject.getFullEffectSnapshot(), // subBundle (update icons!)
					subject.getFullEffectSnapshot() // tarBundle
			));
		}

		// 2. The Critter getting swapped out to the bench (if any)
		if (toSwapWith != null) {
			sb.append("|");
			sb.append(buildManifestRow(
					"MOVE",
					"MOVE",
					this.getType(),
					toSwapWith,
					"square",
					bench.getName(), // The Bench Square
					toSwapWith.getSide(),
					-1, -1,
					startTime,
					"none",
					toSwapWith.getFullEffectSnapshot(),
					"none"));
		}

		return sb.toString();
	}

	// @Override
	// public boolean isStartPerformable() {
	// if (performable && subject.getEnergy() - energyCost >= 0
	// && subject.getSpot().compareSurrounding(target.getName())
	// && subject.canMove()
	// && (!target.isOccupied() || target.getCritter().canMove())
	// && (target.isOccupied() || getPlayer().getDeadCritters().size() > 0)) {
	// return true;
	// } else {
	// return false;
	// }
	// }

	@Override
	public void displayOptions() {
		String str = "indicatespots,move,move," + target.getName() + "." + target.getName() + "."
				+ getPlayer().getSide()
				+ ",";

		getPlayer().sendString(str);
		str = "";

	}

	@Override
	public void displayAction() {
		Square s = getTargetAsSquare();
		if (subject.canMove()) {
			if (subject.getSpot().compareSurrounding(s.getName())) {

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
		return CastResult.CUSTOM_CHECK_FAILED;
	}

	@Override
	public boolean customPerform() {
		boolean stunnedToSwapWith = false;
		Square s = getTargetAsSquare();
		findDirection();
		subject.onMove(subject.getOwner(), subject.getOpponent());
		if (s.isOccupied()) {
			toSwapWith = s.getCritter();
			if (s.getCritter().canMove()) {
				s.getCritter().setSpot(subject.getSpot());
				s.getCritter().setBenched(false);
				subject.getSpot().setCritter(s.getCritter());
				for (Action a : getOtherPlayer().getQueue()) {
					if (a.getTarget() != null && a.getTarget().equals(toSwapWith)) {
						a.setTarget(subject);
					}
				}
				for (Action a : getPlayer().getQueue()) {
					if (a.getTarget() != null && a.getTarget().equals(toSwapWith)) {
						a.setTarget(subject);
					}
				}
				toSwapWith.setEnergy(

						toSwapWith.getEnergy() - energyCost);
				toSwapWith.getIndicatedMoves().remove(toSwapWith.getSpot());
				toSwapWith.benchEffect(getPlayer(), getOtherPlayer());
			} else {
				stunnedToSwapWith = true;
			}
		} else {
			subject.getSpot().setPlannedMove(false);
		}
		if (!stunnedToSwapWith) {
			subject.setSpot(s);
			subject.unbenchEffect(getPlayer(), getOtherPlayer());
			s.setCritter(subject);
			subject.getIndicatedMoves().remove(s);
			s.setPlannedMove(true);
			subject.setBenched(false);

			for (Critter f : getPlayer().getCritters()) {
				f.sendStats(getPlayer(), getOtherPlayer());
			}
			for (Critter f : getOtherPlayer().getCritters()) {
				f.sendStats(getPlayer(), getOtherPlayer());
			}
			used = true;
			return true;
		}

		delete();
		used = true;
		return true;

	}

	@Override
	public void delete() {
		Square s = getTargetAsSquare();
		s.setPlannedMove(false);
		bench.setPlannedMove(false);
		Square subPrevSpot = bench;
		if (subject.getMoves().size() == 1) {
			subPrevSpot = subject.getSpot();
			subject.setTempSpot(subPrevSpot);
			subPrevSpot.setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subPrevSpot = (Square) subject.getMoves().get(subject.getMoves().size() - 2).getTarget();
			subject.setTempSpot(subPrevSpot);
			subPrevSpot.setPlannedMove(true);
		}

		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(target);

		if (toSwapWith != null) {
			Square toSwapWithPrevSpot = toSwapWith.getSpot();
			if (toSwapWith.getMoves().size() == 1) {
				toSwapWithPrevSpot = toSwapWith.getSpot();
				toSwapWith.setTempSpot(toSwapWithPrevSpot);
				toSwapWithPrevSpot.setPlannedMove(true);
			} else if (this.equals(toSwapWith.getMoves().get(toSwapWith.getMoves().size() - 1))) {
				toSwapWithPrevSpot = (Square) toSwapWith.getMoves().get(toSwapWith.getMoves().size() - 2).getTarget();
				toSwapWith.setTempSpot(toSwapWithPrevSpot);
				toSwapWithPrevSpot.setPlannedMove(true);
			}

			toSwapWith.getMoves().remove(this);
			toSwapWith.getIndicatedMoves().remove(bench);
		} else {

			s.setPlannedMove(false);
		}
	}
}
