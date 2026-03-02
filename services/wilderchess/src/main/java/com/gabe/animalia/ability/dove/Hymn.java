package com.gabe.animalia.ability.dove;
import java.io.IOException;

import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class Hymn extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Hymn";
	private String type = "support";
	private double timeCost = 1.5;
	private String statusType = "buff";
	private int energyCost = 30;
	private int damage = 20;
	private int heal = 20;
	private Player player;
	private Player otherPlayer;
	private boolean used;
	private String indicatorMessage;
	private boolean performable = true;
	private int damageToDeal = 0;
	private String effectDescription = "";
	private int duration = 999;

	public Hymn(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Critter)target;
		this.player = player;
		this.otherPlayer = otherPlayer;


	}
	@Override
	public void init() {
		for (Action a : subject.getEffects()) {
			a.initialEffect(this);
		}
		player.getQueue().add(this);
		if (target.getSide().equals(subject.getSide())) {
			subject.getIndicatedSupports().add(target);
			indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();
			type = "support";
		} else {
			subject.getIndicatedAttacks().add(target);
			indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide();
			type = "attack";
		}

			player.sendString("option,remove," + name);

	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void initialEffect(Action ability) {

	}

	@Override
	public void displayAction() {

	}

	@Override
	public void displayOptions() {
		String str = "";
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

			player.sendString("option,support," + name + "," + str);

		str = "";
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

			player.sendString("option,attack," + name + "," + str);


	}
	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}

	public void actionLog() {
		if (subject.getSide().equals(target.getSide())) {


				player.sendString("actionlog," + subject.getName() + " healed " + target.getName() + " for " + heal + " with " + name + ".");
				otherPlayer.sendString("actionlog," + subject.getName() + " healed "  + target.getName() + " for " + heal  + " with "  + name + ".");

		} else {

				player.sendString("actionlog," + subject.getName() + " dealt " + damageToDeal + " to " + target.getName() + " with "  + name + ".");
				otherPlayer.sendString("actionlog," + subject.getName() + " dealt " + damageToDeal + " to " + target.getName() + " with "  + name + ".");

		}


	}


	@Override
	public boolean perform() {
		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable() && target.isAlive()) {

			damage += -3*((Dove)subject).getGood();
			heal += 3*((Dove)subject).getGood();

			subject.setEnergy(subject.getEnergy() - energyCost);
			if (target.getSide().equals(subject.getSide())) {
				if (((Dove)subject).getGood() < 5) {
					((Dove)subject).setGood(((Dove)subject).getGood() + 1);
				}
				if (target.getHealth() + heal > target.getMaxHealth()) {
					target.setHealth(target.getMaxHealth());
				} else {
					target.setHealth(target.getHealth() + heal);
				}
			} else {
				if (((Dove) subject).getGood() > -5) {
					((Dove) subject).setGood(((Dove) subject).getGood() - 1);
				}
				if (target.getSpot().getInfront() != null) {
					if (target.getSpot().getInfront().isOccupied()) {
						this.target = target.getSpot().getInfront()
								.getCritter();
					}
				}
				if (target.getDefence() == 0) {
					target.onHit(subject.getOwner(), subject.getOpponent(), this);
				} else {
					target.onBlock(subject.getOwner(), subject.getOpponent());
				}

				damageToDeal = damage + subject.getBonusDmg()
						- target.getDefence();
				if (damageToDeal < 0) {
					damageToDeal = 0;
				}
				target.setHealth(target.getHealth() - damageToDeal);
			}
			if (((Dove) subject).getGood() > 0) {
				effectDescription = "good: " + ((Dove) subject).getGood()
						+ "\nHymn heal increased by "
						+ ((Dove) subject).getGood() * 3
						+ "\nHymn damage decreased by "
						+ ((Dove) subject).getGood() * 3;
			} else {
				effectDescription = "evil: "
						+ Math.abs(((Dove) subject).getGood())
						+ "\nHymn heal decreased by "
						+ Math.abs(((Dove) subject).getGood()) * 3
						+ "\nHymn damage increased by "
						+ Math.abs(((Dove) subject).getGood()) * 3;
			}

			actionLog();
			if (subject.getEffect(this.name) != null) {
				subject.removeEffect(this.name);
			}
			if (((Dove)subject).getGood() == 0) {
				subject.removeEffect(this.name);
			} else {
				subject.getEffects().add(this);
				showEffect();
			}


				otherPlayer.sendString(
						"moveQueue,_remove");
				player.sendString(
						"moveQueue,_remove");

				player.sendString(
						"moveQueue,_"
								+ player.iconString(player.getQueue()) + "_380");
				player.sendString(
						"moveQueue,_"
								+ otherPlayer.iconString(otherPlayer.getQueue()) + "_430");
				otherPlayer.sendString(
						"moveQueue,_"
								+ player.iconString(player.getQueue())+ "_430");
				otherPlayer.sendString(
						"moveQueue,_"
								+ otherPlayer.iconString(otherPlayer.getQueue()) + "_380");

				player.sendString(
						"energy," + subject.getName() + ","
								+ subject.getSide() + ","
								+ subject.getEnergy() + "|");
				player.sendString(
						"health," + target.getName() + "," + target.getSide()
								+ "," + target.getHealth() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + ","
								+ subject.getSide() + ","
								+ subject.getEnergy() + "|");
				otherPlayer.sendString(
						"health," + target.getName() + "," + target.getSide()
								+ "," + target.getHealth() + "|");

			return true;
		}
		return false;
	}

	public void showEffect() {

			player.sendString(
					"effecttt,create,buff.png," + name + "," + subject.getName()
							+ "," + subject.getSide() + "," + duration + "," + effectDescription
							+ "," + subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,buff.png," + name + "," + subject.getName()
					+ "," + subject.getSide() + "," + duration + "," + effectDescription
					+ "," + subject.getEffects().indexOf(this));




	}

	@Override
	public void endEffect(Critter critter) {

			player.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));


	}


	@Override
	public void delete() {
		if (type.equals("support")) {
			subject.getIndicatedSupports().remove(target);
		} else {
			subject.getIndicatedAttacks().remove(target);
		}

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
		return name.replace("\n", "")
				+ "\nHeal: "
				+ heal
				+ " Damage: "
				+ damage
				+ "\nType: support/attack"
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nDescription: Deals damage to \nopponents and heals teammates. \nEffect: Dove becomes evil with \neach attack and good with each \nheal.";

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
		return new Hymn(null, null, null, null);
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
	public String getStatusType() {
		return statusType;
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
	public int getDuration() {
		return duration;
	}

	@Override
	public void setPerformable(boolean performable) {
		this.performable = performable;
	}

	public void setEffectDescription(String effectDescription) {
		this.effectDescription = effectDescription;
	}

	@Override
	public void setTimeCost(double timeCost) {
		this.timeCost = timeCost;
	}

	@Override
	public void setEnergyCost(int energyCost) {
		this.energyCost = energyCost;
	}
	@Override
	public String getEffects() {
		return "buff.png";
	}

}
