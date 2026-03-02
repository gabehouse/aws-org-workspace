package com.gabe.animalia.ability.lion;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class LickWounds extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Lick Wounds";
	private String type = "support";
	private double timeCost = 3;
	private int energyCost = 10;
	private int heal = 40;
	private Player player;
	private Player otherPlayer;
	private boolean used;
	private String indicatorMessage;
	private boolean performable = true;
	private String description = name.replace("\n", "") + "\nHeal: " + heal + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription: Heals Lion.";
	private String selectDescription = name.replace("\n", "") + "\nHeal: " + heal + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription: Heals Lion.";

	public LickWounds(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = subject;
		this.player = player;
		this.otherPlayer = otherPlayer;



	}

	@Override
	public void init() {
		target = subject;
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

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
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			if (target.getHealth() + heal > target.getMaxHealth()) {
				target.setHealth(target.getMaxHealth());
			} else {
				target.setHealth(target.getHealth() + heal);
			}

				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				player.sendString("health," + target.getName() + "," + target.getSide() + "," + target.getHealth() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("health," + target.getName() + "," + target.getSide() + "," + target.getHealth() + "|");

			return true;
		}
		return false;
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
		return new LickWounds(null, null, null, null);
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
