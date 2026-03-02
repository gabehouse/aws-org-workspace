package com.gabe.animalia.ability;
import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class Bench extends Action {
	private Critter subject;
	private Square target;
	private String direction = "";
	private String targetType = "spot";
	private String iconDescription = "Bench";
	private String type = "move";
	private double timeCost = 3;
	private int energyCost = 0;
	private Player player;
	private Player otherPlayer;
	private boolean used = false;
	private String indicatorMessage;
	private boolean performable = true;
	private Critter unbenching = null;
	private Square transitionSpot = null;
	private String actionStr = "";

	public Bench(Critter subject, Targetable target, Player player, Player otherPlayer) {
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

		transitionSpot = subject.getTempSpot();
		for (Critter f : player.getCritters()) {
			if (f.getTempSpot().equals(target)) {
				unbenching = f;
			}
		}
		if (unbenching != null) {
			unbenching.getMoves().add(this);
			unbenching.setTempSpot(transitionSpot);
			unbenching.getIndicatedMoves().add(transitionSpot);

		} else if (!target.isPlannedMove()) {
			transitionSpot.setPlannedMove(false);
			target.setPlannedMove(true);
		}

		subject.setTempSpot(target);
		subject.getIndicatedMoves().add(target);
		player.getQueue().add(this);
		player.setAllottedTime(player.getAllottedTime() + timeCost);
		indicatorMessage = "indicate|move," + target.getName();
		if (unbenching != null) {
			indicatorMessage = "indicate|move,"
					+ unbenching.getTempSpot().getName() + "|move," + target.getName();

		}
//		System.out.println("indi msg = " + indicatorMessage);
//
//			player.sendString("indicatespots,no moves");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

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
		if (player.getCritters().length > 1) {
			String str = "indicatespots,move,move," + target.getName() + "."
					+ target.getName() + "." + player.getSide() + ",";

				player.sendString(str);
				str = "";

		}

	}

	@Override
	public void displayAction() {

		if (subject.canMove()) {
			System.out.println("displayaction2 " + subject.getSpot().getName());
			if (subject.getSpot().compareSurrounding(target.getName())) {
				System.out.println("bench display action");

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
		if (performable()
				&& subject.getSpot().compareSurrounding(target.getName())
				&& subject.getEnergy() - energyCost >= 0) {
			findDirection();

			subject.onMove(subject.getOwner(), subject.getOpponent());
			if (target.isOccupied()) {
				unbenching = target.getCritter();
				actionStr = subject.getName() + " was benched for "
						+ unbenching.getName() + ".";

				unbenching.setSpot(subject.getSpot());
				unbenching.setBenched(false);
				subject.getSpot().setCritter(unbenching);
				for (Action a : otherPlayer.getQueue()) {
					if (a.getTarget().equals(subject)) {
						a.setTarget(unbenching);
					}
				}
				for (Action a : player.getQueue()) {
					System.out.println("changes "+ a.getName() + " target on bench, from " + a.getTarget().getName() + " to " +  unbenching);
					if (a.getTarget().equals(subject)) {
						a.setTarget(unbenching);
					}
				}

				unbenching.getIndicatedMoves().remove(unbenching.getSpot());
				unbenching.unbenchEffect(player, otherPlayer);

			} else {
				subject.getSpot().setOccupied(false);
				subject.getSpot().setPlannedMove(false);
				actionStr = subject.getName() + " was benched.";
			}

			subject.setSpot(target);
			subject.benchEffect(player, otherPlayer);
			target.setCritter(subject);
			subject.getIndicatedMoves().remove(target);
			target.setPlannedMove(true);
			target.setOccupied(true);
			subject.setBenched(true);
			subject.setEnergy(subject.getEnergy() - energyCost);

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
		delete();
		used = true;
		return false;
	}
	@Override
	public void delete() {
		target.setPlannedMove(false);
		transitionSpot.setPlannedMove(false);
		Square subPrevSpot = transitionSpot;
		if (subject.getMoves().size() == 1) {
			subPrevSpot.setPlannedMove(true);
			subPrevSpot = subject.getSpot();
			subject.setTempSpot(subPrevSpot);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subPrevSpot.setPlannedMove(true);
			subPrevSpot = (Square)subject.getMoves().get(subject.getMoves().size() - 2).getTarget();
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
				unbenchingPrevSpot = (Square)unbenching.getMoves().get(unbenching.getMoves().size() - 2).getTarget();
			}

			unbenching.getMoves().remove(this);
			unbenching.getIndicatedMoves().remove(transitionSpot);
		} else {

			target.setPlannedMove(false);
		}



//		if (unbenching != null) {
//			unbenching.setTempSpot(target);
//			target.setPlannedMove(true);
//
//			for (Action a : otherPlayer.getQueue()) {
//				if (a.getTarget().equals(subject)) {
//					a.setTarget(unbenching);
//				}
//			}
//			for (Action a : player.getQueue()) {
//				if (a.getTarget().equals(subject)) {
//					a.setTarget(unbenching);
//				}
//			}
//			unbenching.getIndicatedMoves().remove(prevSpot);
//		} else {
//			prevSpot.setPlannedMove(true);
//			target.setPlannedMove(false);
//		}
//
//		subject.getMoves().remove(this);
//
//		subject.getIndicatedMoves().remove(target);
	//	target.setCritter(null);


			player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

	}

	public void actionLog() {



			player.sendString("actionlog," + actionStr);
			otherPlayer.sendString("actionlog," + actionStr);



	}

	@Override
	public boolean isStartPerformable() {
		if (performable && subject.getEnergy() - energyCost >= 0
				&& subject.canMove()
				&& subject.getSpot().compareSurrounding(target.getName())
				&& (!target.isOccupied() || target.getCritter().canMove())
				&& (target.isOccupied() || player.getDeadCritters().size() > 0)) {
			return true;
		} else {
			return false;
		}
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
	public Targetable getTarget(){
		return this.target;
	}

	@Override
	public void setEnergyCost(int energyCost) {
		this.energyCost = energyCost;
	}
}
