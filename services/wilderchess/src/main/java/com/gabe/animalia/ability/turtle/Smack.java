package com.gabe.animalia.ability.turtle;

import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Smack extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Smack";
	private String type = "attack";
	private double timeCost = 3;
	private int energyCost = 40;
	private int damage = 70;
	private Player player;
	private Player otherPlayer;
	private boolean used;
	private String indicatorMessage;
	private boolean performable = true;
	private int damageToDeal;
	private String description = name.replace("\n", "") + "\nDamage: " + damage + "\nType: "
			+ this.type + "\nEnergy: " + energyCost + "\nTime: " + timeCost;
	private String selectDescription = name.replace("\n", "") + "\nDamage: " + damage + "\nType: "
			+ this.type + "\nEnergy: " + energyCost + "\nTime: " + timeCost;

	public Smack(Critter subject, Targetable target,
			Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Critter) target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + ","
				+ target.getSide();

			player.sendString("option,remove," + name);


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

			player
					.sendString("option,attack," + name + "," + str);

	}

	@Override
	public void displayAction() {

	}

	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0
				&& !subject.isAffectedBy("Defence Stance") && performable) {
			return true;
		}
		return false;

	}

	public void actionLog() {


			player
					.sendString(
							"actionlog," + subject.getName() + " dealt "
									+ damageToDeal + " to " + target.getName()
									+ " with " + name + ".");
			otherPlayer
					.sendString(
							"actionlog," + subject.getName() + " dealt "
									+ damageToDeal + " to " + target.getName()
									+ " with " + name + ".");


	}

	@Override
	public boolean perform() {
		if (subject.getEnergy() - energyCost >= 0
				&& !subject.isAffectedBy("Defence Stance")
				&& subject.canUseAction() && performable()) {
			if (target.getSpot().getInfront() != null
					&& !target.getSide().equals(subject.getSide())) {
				if (target.getSpot().getInfront().isOccupied()) {
					this.target = target.getSpot().getInfront().getCritter();

				}
			}
			if (target.getDefence() == 0) {
				target.onHit(subject.getOwner(), subject.getOpponent(), this);
			} else {
				target.onBlock(subject.getOwner(), subject.getOpponent());
			}

			subject.setEnergy(subject.getEnergy() - energyCost);
			damageToDeal = damage + subject.getBonusDmg() - target.getDefence();
			if (damageToDeal < 0) {
				damageToDeal = 0;
			}
			actionLog();
			target.setHealth(target.getHealth() - damageToDeal);

				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				player.sendString(
						"health," + target.getName() + "," + target.getSide()
								+ "," + target.getHealth() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"health," + target.getName() + "," + target.getSide()
								+ "," + target.getHealth() + "|");

			return true;
		}
		return false;
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
		this.target = (Critter) target;
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
		return new Smack(null, null, null, null);

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
	public int getDamage() {
		return this.damage;
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
