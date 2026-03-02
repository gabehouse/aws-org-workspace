package com.gabe.animalia.ability.wolf;

import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;


public class Meld extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Meld";
	private String type = "support";
	private String statusType = "invisible";
	private double timeCost = 2;
	private int energyCost = 30;
	private int damage = 0;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 2;
	private String effectDescription = "untargetable";
	private boolean performable = true;


	public Meld(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = subject;
		this.player = player;
		this.otherPlayer = otherPlayer;



	}

	@Override
	public String getStatusType() {
		return statusType;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + subject.getName() + "," + subject.getSide();
		target = subject;

	}


	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;

		}
		return false;

	}

	public void actionLog() {


			player.sendString("actionlog," + subject.getName() + " used " + name + ".");
			otherPlayer.sendString("actionlog," + subject.getName() + " used " + name + ".");


	}

	@Override
	public boolean perform() {

		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {
			System.out.println("meld perform");
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			subject.getEffects().add(this);
			showEffect();
			displayAction();

				player.sendString(
						"energy," + subject.getName() + ","
								+ subject.getSide() + ","
								+ subject.getEnergy() + "|");

				otherPlayer.sendString(
						"energy," + subject.getName() + ","
								+ subject.getSide() + ","
								+ subject.getEnergy() + "|");


			return true;
		}

		return false;
	}

	@Override
	public void overTimeEffect(Critter fighter) {
	}

	@Override
	public void endEffect(Critter fighter) {

				player.sendString(
						"effecttt,remove," + target.getName() + ","
								+ target.getSide() + ","
								+ target.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,remove," + target.getName() + ","
								+ target.getSide() + ","
								+ target.getEffects().indexOf(this));
				otherPlayer.sendString(
						"animate,unstealth," + target.getName() + ","
								+ target.getSide() + "|");
				player.sendString(
						"animate,unstealth," + target.getName() + ","
								+ target.getSide() + "|");

	}

	@Override
	public void showEffect() {
		System.out.println("effecttt,create,buff.png," + name + "," + subject.getName()
				+ "," + subject.getSide() + "," + duration + "," + effectDescription
				+ "," + subject.getEffects().indexOf(this));

			player.sendString(
					"effecttt,create,buff.png," + name + "," + subject.getName()
							+ "," + subject.getSide() + "," + duration + "," + effectDescription
							+ "," + subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,buff.png," + name + "," + subject.getName()
					+ "," + subject.getSide() + "," + duration + "," + effectDescription
					+ "," + subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"animate,stealth," + target.getName() + ","
							+ target.getSide() + "|");
			player.sendString(
					"animate,stealth," + target.getName() + ","
							+ target.getSide() + "|");


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
		return name
				+ "\nType: "
				+ this.type
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nEffect: Wolf becomes untargetable \nfor two turns. Actions break the \nmeld.";
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
		return new Meld(null, null, null, null);

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
		return "buff.png";
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
