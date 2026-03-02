package com.gabe.animalia.ability.bull;

import java.io.IOException;






import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class ShieldStrike extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Shield Strike";
	private String type = "attack";
	private double timeCost = 2.5;
	private int energyCost = 30;
	private int block = 20;
	private int damage = 40;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private int damageToDeal = 0;
	private int duration = 1;
	private String effectDescription = "defence + " + block;
	private String statusType = "block";


	public ShieldStrike(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Critter)target;
		this.player = player;
		this.otherPlayer = otherPlayer;



	}

	@Override
	public void animate(Player player, Player otherPlayer) {
		player.sendString(
					"animate,attack," + getTargetName() + "," + getTarget().getSide() + ","
							+ "|");
			otherPlayer.sendString(
					"animate,attack," + getTargetName() + "," + getTarget().getSide() + ","
							+ "|");
		player.sendString(
					"animate,block," + subject.getName() + "," + subject.getSide() + ","
							+ "|");
			otherPlayer.sendString(
					"animate,block," + subject.getName() + "," + subject.getSide() + ","
							+ "|");


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
		subject.getIndicatedBlocks().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide() + "|block," + subject.getName() + "," + subject.getSide();

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


			player.sendString("actionlog," + subject.getName() + " dealt " + damageToDeal + " to " + target.getName() + " with "  + name + ".");
			otherPlayer.sendString("actionlog," + subject.getName() + " dealt " + damageToDeal + " to " + target.getName() + " with "  + name + ".");


	}


	@Override
	public boolean perform() {
		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {
			if (target.getSpot().getInfront() != null && !target.getSide().equals(subject.getSide())) {
				if (target.getSpot().getInfront().isOccupied()) {
					this.target = target.getSpot().getInfront().getCritter();

				}
			}
			subject.setDefence(subject.getDefence() + block);
			subject.setEnergy(subject.getEnergy() - energyCost);
			subject.getEffects().add(this);
			damageToDeal = damage + subject.getBonusDmg() - target.getDefence();
			if (target.getDefence() == 0) {
				target.onHit(subject.getOwner(), subject.getOpponent(), this);
			} else {
				target.onBlock(subject.getOwner(), subject.getOpponent());
			}
			if (damageToDeal < 0) {
				damageToDeal = 0;
			}
			showEffect();
			actionLog();
			target.setHealth(target.getHealth() - damageToDeal);
			subject.removeEffect("Running Start");

				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				player.sendString("health," + target.getName() + "," + target.getSide() + "," + target.getHealth() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("health," + target.getName() + "," + target.getSide() + "," + target.getHealth() + "|");

			return true;
		}
		return false;
	}
	@Override
	public void overTimeEffect(Critter critter) {
	}

	@Override
	public void endEffect(Critter critter) {
		System.out.println("wags");
		subject.setDefence(subject.getDefence() - block);

			player.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + subject.getName() + ","
							+ subject.getSide() + ","
							+ subject.getEffects().indexOf(this));


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
	public String getStatusType() {
		return statusType;
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(subject);
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
		return new ShieldStrike(null, null, null, null);
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
	public String getDescription() {
		return name.replace("\n", "") + "\nDamage: " + damage + "\nType: "
				+ this.type + "\nDefence: " + this.block + "\nEnergy: " + energyCost + "\nTime: " + timeCost;
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
