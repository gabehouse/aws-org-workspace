package com.gabe.animalia.ability.fox;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class Ignite extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Ignite";
	private String type = "attack";
	private String statusType = "burn";

	private double timeCost = 2;
	private int energyCost = 30;

	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 4;
  private boolean performable = true;
  private int dpt = 25;
  private String effectDescription = dpt + " dmg/turn";



	public Ignite(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Critter)target;
		this.player = player;
		this.otherPlayer = otherPlayer;



	}


	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

			player.sendString("option,remove," + name);


	}


	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}

	@Override
	public void displayOptions() {
		String str = "";
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		System.out.println(str);

			player.sendString("option,support," + name + "," + str);

	}

	public void actionLog() {


			player.sendString("actionlog," + subject.getName() + " used " + name + " on " + target.getName() + ".");
			otherPlayer.sendString("actionlog," + subject.getName() + " used " + name + " on " + target.getName() + ".");


	}

	@Override
	public boolean perform() {


		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {
			if (target.getSpot().getInfront() != null && !target.getSide().equals(subject.getSide())) {
				if (target.getSpot().getInfront().isOccupied()) {

					this.target = target.getSpot().getInfront().getCritter();

				}
			}
			actionLog();
			if (subject.isAffectedBy("Inferno")) {
				subject.removeEffect("Inferno");
			}
			subject.setEnergy(subject.getEnergy() - energyCost);
			target.getEffects().add(this);
			showEffect();


				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {
		critter.setHealth(critter.getHealth() - dpt);

			player.sendString(
					"health," + target.getName() + "," + target.getSide() + ","
							+ target.getHealth() + "|");
			otherPlayer.sendString(
					"health," + target.getName() + "," + target.getSide() + ","
							+ target.getHealth() + "|");



			player.sendString("actionlog," + target.getName() + " lost " + dpt + " health to burn.");
			otherPlayer.sendString("actionlog," + target.getName() + " lost " + dpt + " health to burn.");


	}

	@Override
	public void endEffect(Critter critter) {

			player.sendString(
					"effecttt,remove," + target.getName() + ","
							+ target.getSide() + ","
							+ target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + target.getName() + ","
							+ target.getSide() + ","
							+ target.getEffects().indexOf(this));

	}

	public void showEffect() {

			player.sendString(
					"effecttt,create,burn.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,burn.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


	}

	@Override
	public void displayAction() {

	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

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
	public Critter getTarget() {
		return target;
	}

	@Override
	public void setTarget(Targetable target) {
		this.target = (Critter) target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost
				+ "\nEffect: Burns for 40 damage over \n4 turns. ";

	}

	@Override
	public String getSelectDescription() {
		return name + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost
				+ "\nEffect: Burns for 40 damage over \n4 turns. ";

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
		return new Ignite(null, null, null, null);

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
		return "burn.png";
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
