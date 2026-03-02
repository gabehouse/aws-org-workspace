package com.gabe.animalia.ability.fox;
import java.io.IOException;

import com.gabe.animalia.critter.Fox;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class Stoke extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Stoke";
	private String type = "support";
	private String statusType = "buff";
	private String effectDescription = "10 dmg/turn";
	private double timeCost = 4;
	private int energyCost = 0;
	private int damage = 0;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private int duration = 999;
	private String description = name
			+ "\nType: "
			+ this.type
			+ "\nEnergy: "
			+ energyCost
			+ "\nTime: "
			+ timeCost
			+ "\nDescription: Adds a stack of \nKindle up to a max of 3 and \nrestores 40 energy to Fox.\nEffect: Each stack increases the \ndamage of the next\nFireball by 20.";
	private String selectDescription = name
			+ "\nType: "
			+ this.type
			+ "\nEnergy: "
			+ energyCost
			+ "\nTime: "
			+ timeCost
			+ "\nDescription: Adds a stack of \nKindle up to a max of 3 and \nrestores 40 energy to Fox.\nEffect: Each stack increases the \ndamage of the next Cunning \nFireball by 20.";

	public Stoke(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
	public void displayAction() {

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
		if (subject.getEnergy() - energyCost >= 0  && subject.canUseAction() && performable()) {
			if (subject.isAffectedBy("Inferno")) {
				subject.removeEffect("Inferno");
			}
			subject.setEnergy(subject.getEnergy() - energyCost);
			subject.setEnergy(subject.getEnergy() + 40);
			if (subject.getEnergy() < 0) {
				subject.setEnergy(0);
			}
			if (((Fox)subject).getRechargeStacks() < 3) {
				((Fox)subject).setRechargeStacks(((Fox)subject).getRechargeStacks() + 1);
			}

			actionLog();

			if (subject.getEffect(this.name) != null) {
				subject.removeEffect(this.name);
			}
			effectDescription = "stacks: " + ((Fox)subject).getRechargeStacks() + "\nnext Cunning Fireball's \ndamage is increased by " + 20*((Fox)subject).getRechargeStacks() + ".";
			subject.getEffects().add(this);
			showEffect();


				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");


			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {


	}

	public void showEffect() {

			player.sendString(
					"effecttt,create,buff.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,buff.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


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

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
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
	public String getTargetName() {
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new Stoke(null, null, null, null);

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
	public boolean isPerformable() {
		return performable;
	}

	@Override
	public void setPerformable(boolean performable) {
		this.performable = performable;
	}
	@Override
	public int getDamage() {
		return this.damage;
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
