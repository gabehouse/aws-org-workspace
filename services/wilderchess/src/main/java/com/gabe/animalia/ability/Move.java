package com.gabe.animalia.ability;
import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class Move extends Action {
	private Critter subject;
	private Square target;
	private String direction = "";
	private String targetType = "spot";
	private String iconDescription = "move";
	private String type = "move";
	private double timeCost = 2;
	private int energyCost = 10;
	private Player player;
	private Player otherPlayer;
	private boolean used = false;
	private String indicatorMessage;
	private boolean performable = true;
	private Square previousSpot = null;

	public Move(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Square)target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}


	@Override
	public void init() {
		subject.getMoves().add(this);
		System.out.println("MOVE INIT, added " + this + " to " + subject + " moves, size = " + subject.getMoves().size());
		System.out.println(target.getCritter());
		subject.getTempSpot().setPlannedMove(false);
		subject.getIndicatedMoves().add(target);
		subject.setTempSpot(target);
		target.setPlannedMove(true);
		player.setAllottedTime(player.getAllottedTime() + timeCost);
		player.getQueue().add(this);
		indicatorMessage = "indicate|move," + target.getName();

			player.sendString("indicatespots,no moves");
		System.out.println("moveinit");

	}

	@Override
	public boolean performable() {

		if (!target.isOccupied() && performable
				&& subject.getEnergy() - energyCost >= 0
				&& subject.canUseAction() && subject.canMove()) {

			return true;
		}
		return false;
	}

	@Override
	public void displayAction() {
		if (subject.canMove()) {
			if (subject.getSpot().compareSurrounding(target.getName())) {

					player.sendString(
							"move," + getUsingName() + "," + subject.getSide()
									+ "," + getTargetName() + ","
									+ (timeCost * 1000));
					otherPlayer.sendString(
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
	public boolean perform() {
		if (!target.isOccupied() && !used && performable()
				&& subject.getSpot().compareSurrounding(target.getName())
				&& subject.getEnergy() - energyCost >= 0) {
			findDirection();
			subject.onMove(subject.getOwner(), subject.getOpponent());
			subject.getSpot().setOccupied(false);
			subject.getSpot().setPlannedMove(false);
			subject.setSpot(target);
			subject.getIndicatedMoves().remove(target);
			target.setPlannedMove(true);
			target.setOccupied(true);
			target.setCritter(subject);
			subject.setEnergy(subject.getEnergy() - energyCost);

				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");

			actionLog();
			used = true;
			System.out.println("move perform ended");
			return true;
		}

		delete();
		used = true;
		return false;

	}
	@Override
	public void delete() {
		System.out.println("move delete, subject moves size =  " + subject.getMoves().size());
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			target.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square)subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			target.setPlannedMove(false);
		}
		System.out.println("moves before delete count :" + subject.getMoves().size() + ", " + subject.getSpot());
		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(target);
		subject.setMoved(false);
		for (Critter c : player.getCritters()) {
			if (c.getTempSpot().equals(target)) {
				target.setPlannedMove(true);
			}
		}


			player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

	}

	public void actionLog() {


			player.sendString("actionlog," + subject.getName() + " moved " + direction + ".");
			otherPlayer.sendString("actionlog," + subject.getName() + " moved " + direction + ".");


	}

	@Override
	public Targetable getTarget() {
		return this.target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return "Move \nMoves to an available adjacent \nsquare.";
	}
	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTargetName() {
		return target.toString();
	}

	@Override
	public Critter getSubject() {
		return subject;
	}

	@Override
	public String getUsingName() {
		return subject.getName();
	}

	@Override
	public double getTimeCost() {
		return timeCost;
	}

	@Override
	public int getEnergyCost() {
		return energyCost;
	}

	@Override
	public String getName() {
		return iconDescription;
	}
	@Override
	public boolean isPerformable() {
		return performable;
	}
	@Override
	public void setPerformable(boolean performable) {
		this.performable = performable;
	}

	@Override
	public void setTimeCost(double timeCost) {
		this.timeCost = timeCost;
	}

	@Override
	public void setEnergyCost(int energyCost) {
		this.energyCost = energyCost;
	}
}
