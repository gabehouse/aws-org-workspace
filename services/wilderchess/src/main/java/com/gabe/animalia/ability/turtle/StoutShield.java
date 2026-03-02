package com.gabe.animalia.ability.turtle;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class StoutShield extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Stout Shield";
	private String type = "block";
	private String effectDescription = "defence + 40";
	private double timeCost = 2;
	private int energyCost = 30;
	private int block = 40;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1;
	private boolean performable = true;
	private String statusType = "block";

	public StoutShield(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = subject;
		this.player = player;
		this.otherPlayer = otherPlayer;


	}
	@Override
	public void init() {
		target = subject;
		subject.getIndicatedBlocks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|block," + subject.getName() + "," + subject.getSide();
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayAction() {

	}
	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && !subject.isAffectedBy("Defence Stance") && performable) {
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
		if (subject.getEnergy() - energyCost >= 0 && !subject.isAffectedBy("Defence Stance") && subject.canUseAction() && performable()) {
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			subject.setDefence(subject.getDefence() + block);
			subject.getEffects().add(this);
			showEffect();
			subject.sendStats(player, otherPlayer);
			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {
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
	public void endEffect(Critter critter) {
		subject.setDefence(subject.getDefence() - block);

			player.sendString(
					"effecttt,remove," + target.getName() + ","
							+ target.getSide() + ","
							+ target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + target.getName() + ","
							+ target.getSide() + ","
							+ target.getEffects().indexOf(this));

	}

	@Override
	public String getStatusType() {
		return statusType;
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(target);

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
		return name.replace("\n", "") + "\nDefence: " + block + "\nType: "
				+ this.type + "\nEnergy: " + energyCost + "\nTime: " + timeCost;
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
		return new StoutShield(null, null, null, null);
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
