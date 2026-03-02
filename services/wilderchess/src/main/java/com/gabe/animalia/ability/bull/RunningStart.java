package com.gabe.animalia.ability.bull;

import java.io.IOException;

import com.gabe.animalia.ability.turtle.Reflect;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class RunningStart extends Action{
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Running Start";
	private String type = "passive";
	private String statusType = "buff";
	private int bonusAttack = 30;
	private String effectDescription = "attack + " + bonusAttack;
	private double timeCost = 0;
	private int energyCost = 0;
	private int damage = 0;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1;
	private boolean performable = true;
	private int damageToDeal = 0;

	public RunningStart(Critter subject, Targetable target, Player player, Player otherPlayer) {
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

	}
	@Override
	public boolean perform() {
		subject.setBonusDmg(subject.getBonusDmg() + 30);
		target.getEffects().add(this);
		showEffect();
		return true;
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
		subject.setBonusDmg(subject.getBonusDmg() - 30);

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
		subject.getIndicatedAttacks().remove(target);

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
		return name + "\nEffect: Burns for 15 damage over \n3 turns. \nDamage: " + damage + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost;
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
		return new Reflect(null, null, null, null);

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
