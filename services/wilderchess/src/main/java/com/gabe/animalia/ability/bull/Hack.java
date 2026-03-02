package com.gabe.animalia.ability.bull;

import java.io.IOException;

import com.gabe.animalia.critter.Fox;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Hack extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Hack";
	private String type = "attack";
	private String statusType = "curse";
	private String effectDescription = "";
	private double timeCost = 1.5;
	private int energyCost = 25;
	private int damage = 35;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 999;
	private boolean performable = true;
	private int damageToDeal = 0;

	public Hack(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
				System.out.println(f.isBenched());
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		System.out.println(str);

			player.sendString("option,attack," + name + "," + str);

	}


	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide();

			player.sendString("option,remove," + name);


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
		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction()
				&& performable()) {
			if (target.getSpot().getInfront() != null
					&& !target.getSide().equals(subject.getSide())) {
				if (target.getSpot().getInfront().isOccupied()) {

					this.target = target.getSpot().getInfront().getCritter();

				}
			}

			if (target.getHackStacks() == 0) {
				for (Critter c : otherPlayer.getCritters()) {
					c.setHackStacks(0);
					c.removeEffect(this.name);
				}
				for (Critter c : player.getCritters()) {
					c.setHackStacks(0);
					c.removeEffect(this.name);
				}
			}
			if (target.getEffect(this.name) != null) {
				target.removeEffect(this.name);
			}
			target.setHackStacks(target.getHackStacks() + 1);


			effectDescription = "stacks: " + target.getHackStacks()
					+ "\nnext Hack's damage \nis increased by " + 7
					* target.getHackStacks() + ".";

			target.getEffects().add(this);

			showEffect();

			if (target.getDefence() == 0) {
				target.onHit(subject.getOwner(), subject.getOpponent(), this);


			} else {
				target.onBlock(subject.getOwner(), subject.getOpponent());
			}

			subject.setEnergy(subject.getEnergy() - energyCost);
			damageToDeal = damage + subject.getBonusDmg() - target.getDefence()
					+ 7 * (target.getHackStacks() - 1);
			if (damageToDeal < 0) {
				damageToDeal = 0;
			}
			actionLog();
			target.setHealth(target.getHealth() - damageToDeal);
			subject.removeEffect("Running Start");

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
	public void overTimeEffect(Critter critter) {

	}

	public void showEffect() {

			player.sendString(
					"effecttt,create,curse.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + ","
							+ effectDescription + ","
							+ target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,curse.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + ","
							+ effectDescription + ","
							+ target.getEffects().indexOf(this));


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
		return name + "\nDamage: " + damage + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nEffect: Consecutively targeted hacks \ndeal 7 extra damage.";
	}
	@Override
	public String getSelectDescription() {
		return name + "\nDamage: " + damage + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nEffect: Consecutively targeted hacks \ndeal 7 extra damage.";
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
		return new Hack(null, null, null, null);

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
