package com.gabe.animalia.ability.dove;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class DivineIntervention extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Divine Intervention";
	private String type = "support";
	private String statusType = "none";
	private double timeCost = 2;
	private int energyCost = 0;
	private int heal = 10;
	private int energyRestore = 50;
	private int block = 50;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1;
	private boolean performable = true;
	private String effectDescription = "defence + " + block;

	public DivineIntervention(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		target = subject;
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();
	}


	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable && subject.getEnergy() < 30 && subject.getHealth() <= 30) {
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
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyRestore);
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

	@Override
	public void displayAction() {

	}

	public void showEffect() {


			player.sendString(
					"effecttt,create,block.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,block.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

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
		return name
				+ "\nType: "
				+ this.type
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nDescription: Raises defence by "
				+ block
				+ " \n and restores 50 energy if Dove \nhas less than 30 health and 30 \nenergy.";
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
		return new DivineIntervention(null, null, null, null);

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
