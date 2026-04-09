package com.gabe.animalia.ability;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Move extends Action {
	private String direction = "";
	private boolean used = false;
	private String indicatorMessage;
	private Square previousSpot = null;

	public Move(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.MOVE);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		Square s = getTargetAsSquare();
		subject.getMoves().add(this);
		System.out
				.println("MOVE INIT, added " + this + " to " + subject + " moves, size = " + subject.getMoves().size());
		System.out.println(s.getCritter());
		subject.getTempSpot().setPlannedMove(false);
		subject.getIndicatedMoves().add(target);
		subject.setTempSpot(s);
		s.setPlannedMove(true);
		getPlayer().setAllottedTime(getPlayer().getAllottedTime() + timeCost);
		getPlayer().getQueue().add(this);
		indicatorMessage = "indicate|move," + s.getName();

		getPlayer().sendString("indicatespots,no moves");
		System.out.println("moveinit");

	}

	@Override
	public void displayAction() {
		if (subject.canMove()) {
			if (subject.getSpot().compareSurrounding(target.getName())) {

				getPlayer().sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + ","
								+ (timeCost * 1000));
				getOtherPlayer().sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + ","
								+ (timeCost * 1000));

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
	public boolean customPerform() {
		Square s = getTargetAsSquare();
		findDirection();
		System.out.println("moved from " + subject.getSpot().getName() + " to " + s.getName());
		subject.onMove(subject.getOwner(), subject.getOpponent());
		subject.getSpot().setPlannedMove(false);
		subject.getSpot().setCritter(null);
		subject.setSpot(s);
		subject.getIndicatedMoves().remove(s);
		s.setPlannedMove(true);
		s.setCritter(subject);
		return true;

	}

	@Override
	public void delete() {
		Square s = getTargetAsSquare();
		System.out.println("move delete, subject moves size =  " + subject.getMoves().size());
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			s.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square) subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			s.setPlannedMove(false);
		}
		System.out.println("moves before delete count :" + subject.getMoves().size() + ", " + subject.getSpot());
		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(target);
		subject.setMoved(false);
		for (Critter c : getPlayer().getCritters()) {
			if (c.getTempSpot().equals(target)) {
				s.setPlannedMove(true);
			}
		}
	}
}
