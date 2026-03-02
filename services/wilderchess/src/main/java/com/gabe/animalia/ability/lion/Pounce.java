package com.gabe.animalia.ability.lion;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class Pounce extends Action {
	private Critter subject;
	private Square target;
	private String targetType = "spot";
	private String name = "Pounce";
	private String type = "block";
	private double timeCost = 1;
	private int energyCost = 20;
	private int block = 15;
	private String effectDescription = "defence + 15";
	private Player player;
	private Player otherPlayer;
	private Square initialTempSpot;
	private String indicatorMessage;
	private int duration = 1;
	private String statusType = "block";
	private boolean performable = true;
	private String direction = "";
	private Square previousSpot;
	private String description = name.replace("\n", "") + "\nDefence: " + block + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription: Moves Lion to an \nadjacent spot and raises defence \nby " + block + ".";
	private String selectDescription = name.replace("\n", "") + "\nDefence: " + block + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription: Moves Lion to an \nadjacent spot and raises defence \nby " + block + ".";
	public Pounce(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Square)target;
		this.player = player;
		this.otherPlayer = otherPlayer;


	}

	@Override
	public void displayOptions() {
		subject.displayPossibleMoves(player, name);


	}

	@Override
	public void init() {
		if (subject.getTempSpot().compareSurrounding(target.getName())) {
			subject.getMoves().add(this);
			initialTempSpot = subject.getTempSpot();
			subject.getIndicatedBlocks().add(subject);
			subject.getTempSpot().setPlannedMove(false);
			player.getQueue().add(this);
			target.setPlannedMove(true);
			subject.setTempSpot(target);
			// subject.displayPossibleMoves(player);
			subject.getIndicatedMoves().add(target);

			indicatorMessage = "indicate|block," + subject.getName() + ","
					+ subject.getSide() + "|move," + target.getName();

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

	@Override
	public void displayAction() {





				player.sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + "," + (timeCost*1000));
				otherPlayer.sendString(
						"move," + getUsingName() + "," + subject.getSide()
								+ "," + getTargetName() + "," + (timeCost*1000));


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
	public boolean initable() {
		if (!target.isPlannedMove() && performable && subject.getEnergy() - energyCost >= 0
				&& subject.canUseAction() && subject.canMove()) {

			return true;
		}
		return false;

	}

	@Override
	public boolean performable() {
		if (!target.isOccupied() && performable && subject.getEnergy() - energyCost >= 0
				&& subject.canUseAction() && subject.canMove() && subject.getSpot().compareSurrounding(target.getName())) {

			return true;
		}
		return false;

	}

	public void actionLog() {


			player.sendString(
					"actionlog," + subject.getName() + " pounced " + direction
							+ ".");
			otherPlayer.sendString("actionlog," + subject.getName() + " pounced " + direction + ".");


	}

	@Override
	public boolean perform() {
		if (!target.isOccupied() && subject.getEnergy() - energyCost >= 0
				&& subject.canUseAction() && performable
				&& subject.getEnergy() - energyCost >= 0 && subject.canMove()
				&& subject.getSpot().compareSurrounding(target.getName())) {
			if (subject.getSpot().compareSurrounding(target.getName())) {
				findDirection();
				actionLog();
				subject.getSpot().setOccupied(false);
				subject.getSpot().setPlannedMove(false);
				subject.setSpot(target);
				target.setCritter(subject);
				subject.getIndicatedMoves().remove(target);
				target.setOccupied(true);
				target.setCritter(subject);
			}
			subject.setEnergy(subject.getEnergy() - energyCost);
			subject.setDefence(subject.getDefence() + block);
			subject.getEffects().add(this);
			showEffect();
			subject.onMove(subject.getOwner(), subject.getOpponent());
			subject.sendStats(player, otherPlayer);
			return true;
		}
		delete();
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {
	}

	@Override
	public void endEffect(Critter critter) {
		subject.setDefence(subject.getDefence() - block);


			player.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));


	}

	public void showEffect() {

			player.sendString(
					"effecttt,create,block.png," + name + "," + subject.getName()
							+ "," + subject.getSide() + "," + duration + "," + effectDescription
							+ "," + subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,block.png," + name + "," + subject.getName()
					+ "," + subject.getSide() + "," + duration + "," + effectDescription
					+ "," + subject.getEffects().indexOf(this));


	}

	@Override
	public String getStatusType() {
		return statusType;
	}

	@Override
	public void delete() {
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			target.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square)subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			target.setPlannedMove(false);
		}
		subject.getMoves().remove(this);
		subject.getIndicatedBlocks().remove(subject);
		subject.getIndicatedMoves().remove(target);

	}
	@Override
	public Player getPlayer() {
		return player;
	}
	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}
	@Override
	public Player getOtherPlayer() {
		return otherPlayer;
	}
	@Override
	public void setOtherPlayer(Player otherPlayer) {
		this.otherPlayer = otherPlayer;
	}
	@Override
	public Critter getSubject() {
		return subject;
	}
	@Override
	public void setSubject(Critter subject) {
		this.subject = subject;
	}
	@Override
	public Targetable getTarget() {
		return target;
	}
	@Override
	public void setTarget(Targetable target) {
		this.target = (Square)target;
	}
	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return description;
	}
	@Override
	public String getSelectDescription() {
		return selectDescription;
	}
	@Override
	public String getType() {
		return type;
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public String getTargetName() {
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new Pounce(null, null, null, null);
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
	public String getEffects() {
		return "block.png";
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
