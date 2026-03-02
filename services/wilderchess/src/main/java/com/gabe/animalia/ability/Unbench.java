package com.gabe.animalia.ability;
import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;


public class Unbench extends Action {
	private Critter subject;
	private Square target;
	private String direction = "";
	private String targetType = "spot";
	private String name = "Unbench";
	private String type = "move";
	private double timeCost = 3;
	private int energyCost = 0;
	private Player player;
	private Player otherPlayer;
	private boolean used = false;
	private String indicatorMessage;
	private boolean performable = true;
	private Critter toSwapWith = null;
	private Square bench = null;
	private TypesAndStuff tas = new TypesAndStuff();
	private String actionStr = "";

	public Unbench(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		bench = subject.getTempSpot();
		if (target.isPlannedMove()) {
			for (Critter c : player.getCritters()) {
				if (c.getTempSpot().equals(target))
				toSwapWith = c;
			}
		}
		if (toSwapWith != null) {
			toSwapWith.getMoves().add(this);
			toSwapWith.setTempSpot(bench);
			toSwapWith.getIndicatedMoves().add(bench);

		} else if (!target.isPlannedMove()) {
			target.setPlannedMove(true);
			bench.setPlannedMove(false);
		}
		subject.setTempSpot(target);
		subject.getIndicatedMoves().add(target);
		player.getQueue().add(this);
		player.setAllottedTime(player.getAllottedTime() + timeCost);
		indicatorMessage = "indicate|move," + target.getName();
		if (toSwapWith != null) {
			indicatorMessage = "indicate|move,"
					+ toSwapWith.getTempSpot().getName() + "|move,"
					+ target.getName();

		}

	}

	@Override
	public boolean isStartPerformable() {
		if (performable && subject.getEnergy() - energyCost >= 0
				&& subject.getSpot().compareSurrounding(target.getName())
				&& subject.canMove()
				&& (!target.isOccupied() || target.getCritter().canMove())
				&& (target.isOccupied() || player.getDeadCritters().size() > 0)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean performable() {

		if (performable && subject.getEnergy() - energyCost >= 0
				&& subject.canMove()) {

			return true;
		}
		return false;
	}

	@Override
	public void displayOptions() {
		String str = "indicatespots,move,move," + target.getName() + "." + target.getName() + "." + player.getSide() + ",";

			player.sendString(str);
			str = "";



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
					if (target.isOccupied()) {
						player.sendString(
								"move," + target.getCritter().getName() + ","
										+ subject.getSide() + ","
										+ subject.getSpot().getName() + ","
										+ (timeCost * 1000));
						otherPlayer.sendString(
								"move," + target.getCritter().getName() + ","
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
	public boolean perform() {
		boolean stunnedToSwapWith = false;
		if (!used && performable()
				&& subject.getSpot().compareSurrounding(target.getName())
				&& subject.getEnergy() - energyCost >= 0) {
			findDirection();
			subject.onMove(subject.getOwner(), subject.getOpponent());
			if (target.isOccupied()) {
				actionStr = subject.getName() + " came off the bench for "
						+ target.getCritter().getName() + ".";
				if (target.getCritter().canMove()) {
					target.getCritter().setSpot(subject.getSpot());
					target.getCritter().setBenched(false);
					subject.getSpot().setCritter(target.getCritter());
					for (Action a : otherPlayer.getQueue()) {
						if (a.getTarget().equals(subject)) {
							a.setTarget(toSwapWith);
						}
					}
					for (Action a : player.getQueue()) {
						if (a.getTarget().equals(subject)) {
							a.setTarget(toSwapWith);
						}
					}
					toSwapWith.unstealth(otherPlayer);
					toSwapWith.setEnergy(toSwapWith.getEnergy() - energyCost);
					toSwapWith.getIndicatedMoves().remove(toSwapWith.getSpot());
					toSwapWith.benchEffect(player, otherPlayer);
				} else {
					stunnedToSwapWith = true;
				}
			} else {
				actionStr = subject.getName() + " came off the bench.";
				subject.getSpot().setOccupied(false);
				subject.getSpot().setPlannedMove(false);
			}
			if (!stunnedToSwapWith) {
				subject.setSpot(target);
				subject.unbenchEffect(player, otherPlayer);
				target.setCritter(subject);
				subject.getIndicatedMoves().remove(target);
				target.setPlannedMove(true);
				target.setOccupied(true);
				subject.setBenched(false);

				for (Critter f : player.getCritters()) {
					f.sendStats(player, otherPlayer);
				}
				for (Critter f : otherPlayer.getCritters()) {
					f.sendStats(player, otherPlayer);
				}
				actionLog();
				used = true;
				return true;
			}
		}
		delete();
		used = true;
		return false;
	}

	@Override
	public void delete() {
		target.setPlannedMove(false);
		bench.setPlannedMove(false);
		Square subPrevSpot = bench;
		if (subject.getMoves().size() == 1) {
			subPrevSpot = subject.getSpot();
			subject.setTempSpot(subPrevSpot);
			subPrevSpot.setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subPrevSpot = (Square)subject.getMoves().get(subject.getMoves().size() - 2).getTarget();
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
				toSwapWithPrevSpot = (Square)toSwapWith.getMoves().get(toSwapWith.getMoves().size() - 2).getTarget();
				toSwapWith.setTempSpot(toSwapWithPrevSpot);
				toSwapWithPrevSpot.setPlannedMove(true);
			}

			toSwapWith.getMoves().remove(this);
			toSwapWith.getIndicatedMoves().remove(bench);
		} else {

			target.setPlannedMove(false);
		}



			player.sendString(
					"energy," + subject.getName() + "," + subject.getSide()
							+ "," + subject.getEnergy() + "|");

	}

	public void actionLog() {


			player.sendString("actionlog," + actionStr);
			otherPlayer.sendString("actionlog," + actionStr);


	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return "Move \nMoves to an available adjacent square.";
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
		return name;
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
	@Override
	public Targetable getTarget(){
		return this.target;
	}
}
