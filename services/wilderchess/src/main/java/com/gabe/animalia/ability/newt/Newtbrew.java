package com.gabe.animalia.ability.newt;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;


public class Newtbrew extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Newtbrew";
	private String type = "support";
	private double timeCost = 2;
	private int energyCost = 20;
	private String effectDescription = "-50 energy/turn";
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1;
	private String statusType = "curse";
	private boolean performable = true;
	private TypesAndStuff tas = new TypesAndStuff();



	public Newtbrew(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
	public void displayOptions() {
		String str = "";
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

			player.sendString("option,support,"+ name + "," + str);

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
	public boolean perform() {

		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction()
				&& performable()) {
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			if (target.getSpot()
					.compareSurrounding(subject.getSpot().getName())) {
				target.getEffects().add(this);
				showEffect();
				target.setEnergy(target.getEnergy() + 40);

			}

				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				player.sendString(
						"energy," + target.getName() + "," + target.getSide()
								+ "," + target.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + target.getName() + "," + target.getSide()
								+ "," + target.getEnergy() + "|");


			return true;
		}
		return false;
	}

	public void actionLog() {


			player.sendString("actionlog," + subject.getName() + " administered a dose of " + name + " to " + target.getName()  +  ".");
			player.sendString("actionlog," + subject.getName() + " administered a dose of " + name + " to " + target.getName()  +  ".");


	}


	@Override
	public void endEffect(Critter critter) {
		target.setEnergy(target.getEnergy() - 50);

			player.sendString(
					"energy," + target.getName() + "," + target.getSide()
							+ "," + target.getEnergy() + "|");
			otherPlayer.sendString(
					"energy," + target.getName() + "," + target.getSide()
							+ "," + target.getEnergy() + "|");
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
					"effecttt,create,curse.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,curse.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


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
		this.target = (Critter)target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost
				+ "\nDescription: Increases the energy of \nan adjacent fighter by 40 then \nremoves 50 at the end of the turn. ";
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
		return new Newtbrew(null, null, null, null);

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
		return "curse.png";
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
