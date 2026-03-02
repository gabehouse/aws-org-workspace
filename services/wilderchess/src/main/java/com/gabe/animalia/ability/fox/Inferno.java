package com.gabe.animalia.ability.fox;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class Inferno extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Inferno";
	private String type = "attack";
	private double timeCost = 3;
	private int energyCost = 70;
	private int damage = 70;
	private String effectDescription = "channeling";
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private String statusType = "channeling";
	private int duration = 999;
	private boolean performable = true;


	public Inferno(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		this.target = subject;
		player.getQueue().add(this);
		indicatorMessage = "indicate|";
//		for (Critter f : otherPlayer.getCritters()) {
//			subject.getIndicatedAttacks().add(f);
//			indicatorMessage += "attack," + f.getName() + "," +f.getSide() + "|";
//		}
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
			subject.getEffects().add(this);
		    showEffect();



			subject.setEnergy(subject.getEnergy() - energyCost);


				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");


			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {

		for (Critter f : otherPlayer.getCritters()) {
			Critter toHit = f;

			if (f.getSpot().getInfront() != null || f.isBenched()) {
				if (f.getSpot().getInfront().isOccupied() || f.isBenched()) {
					continue;
				}
			}
			int damageToDeal = damage + subject.getBonusDmg();
			if (damageToDeal < 0) {
				damageToDeal = 0;
			}
			if (toHit.getDefence() == 0) {
				toHit.onHit(subject.getOwner(), subject.getOpponent(), this);
			} else {
				toHit.onBlock(subject.getOwner(), subject.getOpponent());
			}
			toHit.setHealth(toHit.getHealth() - damageToDeal);

				player.sendString("actionlog," + toHit.getName() + " lost " + damageToDeal + " health to the inferno.");
				otherPlayer.sendString("actionlog," + toHit.getName() + " lost " + damageToDeal + " health to the inferno.");



		}

		String healthStr = "";
		for (Critter f : otherPlayer.getCritters()) {
			healthStr += "health," + f.getName() + "," + f.getSide() + "," + f.getHealth() + "|";

		}



			player.sendString(healthStr);
			otherPlayer.sendString(healthStr);





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

	public void showEffect() {

			player.sendString(
					"effecttt,create,channeling.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,channeling.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


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
		return name.replace("\n", "") + "\nDamage: " + damage + "\nType: " + this.type  +  "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription: Deals unblockable \ndamage to the first opposing \nfighter in each row at the end of \neach turn. Using any actions or \ngetting hit ends the effect.";
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
		return new Inferno(null, null, null, null);
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
		return "channeling.png";
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
