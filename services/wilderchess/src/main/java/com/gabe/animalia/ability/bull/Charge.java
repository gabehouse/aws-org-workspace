package com.gabe.animalia.ability.bull;

import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;

public class Charge extends Action{
	private Critter subject;
	private Square target;
	private String targetType = "spot";
	private String name = "Charge";
	private String type = "attack";
	private String statusType = "";
	private double timeCost = 3;
	private int energyCost = 50;
	private int damage = 50;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private Square otherTarget;
	private TypesAndStuff taf = new TypesAndStuff();
	private Square spot = null;
	private Square [] targets = new Square[2];
	private int damageToDeal = damage;
	private ArrayList<Targetable> toAnimateImpact = new ArrayList<Targetable>();



	public Charge(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Square)target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	}

	@Override
	public void animate(Player player, Player otherPlayer) {
		String str = "";
		for (Targetable t : toAnimateImpact) {
			str += "animate,action," + t.getName() + "," + t.getSide() + "," + getType() + "," + getTargetType() + "|";
		}
		player.sendString(str);
		otherPlayer.sendString(str);
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
				+ otherPlayer.getSide() + "TopBack." + startX + "0-" + (startX + 2) + "0-" + (startX + 2) + "1-" + startX + "1-" + startX + "0." + otherPlayer.getSide() + ","
				+ otherPlayer.getSide() + "MiddleBack." + startX + "1-" + (startX + 2) + "1-" + (startX + 2) + "2-" + startX + "2-" + startX + "1." + otherPlayer.getSide() + ","
				+ otherPlayer.getSide() + "BottomBack." + startX + "2-" + (startX + 2) + "2-" + (startX + 2) + "3-" + startX + "3-" + startX + "2." + otherPlayer.getSide() + ",";

			player.sendString(str);

	}

	@Override
	public void init() {
		subject.getMoves().add(this);
		subject.getIndicatedAttacks().add(target);
		spot = subject.getTempSpot().getInfront();
		subject.getIndicatedMoves().add(spot);
		spot.setPlannedMove(true);
		subject.getTempSpot().setPlannedMove(false);
		subject.setTempSpot(spot);

		player.getQueue().add(this);
		indicatorMessage = "indicate|attackspot," + target.getName();
		if (target.getInfront() != null) {
			otherTarget = target.getInfront();
		} else {
			otherTarget = target.getBehind();
		}
		subject.getIndicatedAttacks().add(otherTarget);
		indicatorMessage += "|attackspot," + otherTarget.getName() + "|move," + spot.getName();


			player.sendString("indicatespots,no moves");
		toAnimateImpact.add(target);
		toAnimateImpact.add(otherTarget);

	}

	@Override
	public boolean initable() {
		if (subject.getTempSpot().getInfront() != null) {
			if (!subject.getTempSpot().getInfront().isPlannedMove()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}

	public void actionLog() {
		String str = "";

		if (target.getName().contains("Top")) {
			str += "actionlog," + subject.getName() + " used " + name
					+ " on the top row. ";
		} else if (target.getName().contains("Middle")) {
			str += "actionlog," + subject.getName() + " used " + name
					+ " on the middle column. ";
		} else {
			str += "actionlog," + subject.getName() + " used " + name
					+ " on the bottom column. ";
		}
		if (targets[0].getCritter() == null && targets[1].getCritter() == null) {
			str += "No one was hit.";
		} else if (targets[1].getCritter() == null) {
			str += targets[0].getCritter().getName() + " lost " + damageToDeal + " health.";
		} else if (targets[0].getCritter() == null) {
			str += targets[1].getCritter().getName() + " lost " + damageToDeal + " health.";
		} else {
			str += targets[0].getCritter().getName() + " and " + targets[1].getCritter().getName()
					+ " lost " + damageToDeal + " health.";
		}
		//str += "\n";

			player.sendString(str);
			otherPlayer.sendString(str);


	}

	@Override
	public boolean perform() {


		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {

			subject.setEnergy(subject.getEnergy() - energyCost);
			targets[0] = target;
			targets[1] = otherTarget;
			damageToDeal = damage + subject.getBonusDmg();
			for (Square t : targets) {
				if (t.getCritter() != null && t.getCritter().isAlive()) {
					Critter critter = t.getCritter();

					critter.setHealth(critter.getHealth() - damageToDeal);
					ArrayList<String> toRemoveNames = new ArrayList<String>();
					for (Action a : critter.getEffects()) {
						if (taf.isBlock(a)){
							toRemoveNames.add(a.getName());
						}
					}
					for (int i = 0; i < toRemoveNames.size(); i++) {
						System.out.println(toRemoveNames.get(i));
						critter.removeEffect(toRemoveNames.get(i));
					}

					subject.onMove(subject.getOwner(), subject.getOpponent());
					subject.getSpot().setOccupied(false);
					subject.getSpot().setPlannedMove(false);
					subject.setSpot(spot);
					subject.getIndicatedMoves().remove(spot);
					spot.setPlannedMove(true);
					spot.setOccupied(true);
					spot.setCritter(subject);

					critter.onHit(subject.getOwner(), subject.getOpponent(), this);
					subject.removeEffect("Running Start");

						player.sendString("health," + critter.getName() + "," + critter.getSide() + "," + critter.getHealth() + "|");
						otherPlayer.sendString("health," + critter.getName() + "," + critter.getSide() + "," + critter.getHealth() + "|");

				}
			}
			actionLog();

				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

			return true;
		}
		return false;
	}
	@Override
	public void displayAction() {
		if (subject.canMove()) {
			if (subject.getSpot().compareSurrounding(spot.getName())) {

					player.sendString(
							"move," + getUsingName() + "," + subject.getSide()
									+ "," + spot.getName() + ","
									+ (timeCost * 1000));
					otherPlayer.sendString(
							"move," + getUsingName() + "," + subject.getSide()
									+ "," + spot.getName() + ","
									+ (timeCost * 1000));

			}
		}
	}
	@Override
	public void overTimeEffect(Critter critter) {

	}

	@Override
	public void endEffect(Critter critter) {

	}

	public void showEffect() {


	}

	@Override
	public void delete() {
		if (subject.getMoves().size() == 1) {
			subject.setTempSpot(subject.getSpot());
			spot.setPlannedMove(false);
			subject.getSpot().setPlannedMove(true);
		} else if (this.equals(subject.getMoves().get(subject.getMoves().size() - 1))) {
			subject.setTempSpot((Square)subject.getMoves().get(subject.getMoves().size() - 2).getTarget());
			spot.setPlannedMove(false);
		}
		subject.getIndicatedAttacks().remove(target);
		subject.getIndicatedAttacks().remove(otherTarget);
		subject.getMoves().remove(this);
		subject.getIndicatedMoves().remove(spot);
		for (Critter c : player.getCritters()) {
			if (c.getTempSpot().equals(spot)) {
				spot.setPlannedMove(true);
			}
		}
	}

	@Override
	public String getStatusType() {
		return statusType;
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
	public Square getTarget() {
		return spot;
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
		return name
				+ "\nDamage: "
				+ damage + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost + "\nDescription: Deals damage and \nremoves blocks from enemy \nfighters in a targetted row. Must \nbe used from the back column \nand moves Bull to the front column.";
	}

	@Override
	public String getSelectDescription() {
		return name
				+ "\nDamage: "
				+ damage + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost + "\nDescription: Deals damage and \nremoves blocks from enemy \nfighters in a targetted row. Must \nbe used from the back column \nand moves Bull to the front \ncolumn.";
	}


	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTargetName() {
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new Charge(null, null, null, null);

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


}
