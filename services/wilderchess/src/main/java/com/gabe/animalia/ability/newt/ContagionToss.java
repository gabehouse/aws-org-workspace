package com.gabe.animalia.ability.newt;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;


public class ContagionToss extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "critter";
	private String name = "Contagion Toss";
	private String type = "support";
	private double timeCost = 4;
	private int energyCost = 40;
	private String effectDescription = "-30 energy and health/turn. \nSpreads to adjacent fighters";
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1;
	private String statusType = "burn";
	private boolean performable = true;
	private TypesAndStuff tas = new TypesAndStuff();
	private boolean justSpread = false;



	public ContagionToss(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

			player.sendString("option,support,"+ name + "," + str);

	}


	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

			player.sendString("option,remove," + name);

	}
	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}
	@Override
	public boolean perform() {

		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction()
				&& performable()) {
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			target.getEffects().add(this);
			showEffect();


				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");


			return true;
		}
		return false;
	}

	public void actionLog() {


			player.sendString("actionlog," + subject.getName() + " administered a dose of " + name + " to " + target.getName()  +  ".");
			player.sendString("actionlog," + subject.getName() + " administered a dose of " + name + " to " + target.getName()  +  ".");


	}

	@Override
	public void overTimeEffect(Critter critter) {
		if (!justSpread) {
			String str = target.getName()
					+ " lost 30 health and energy to fever.";

			target.setHealth(target.getHealth() - 30);
			target.setEnergy(target.getEnergy() - 30);
			for (int i = 0; i < otherPlayer.getCritters().length; i++) {
				if (target.getSpot().compareSurrounding(
						otherPlayer.getCritters()[i].getSpot().getName())
						&& !otherPlayer.getCritters()[i].getSpot().getName()
								.contains("Bench") && !target.getSpot().getName().contains("Bench") && !otherPlayer.getCritters()[i].isAffectedBy(name)) {
					System.out.println(otherPlayer.getCritters()[i].getName());
					ContagionToss toSpread = new ContagionToss(subject,
							otherPlayer.getCritters()[i], player, otherPlayer);
					otherPlayer.getCritters()[i].getEffects().add(toSpread);
					toSpread.setDuration(toSpread.getDuration() + 1);
					str += " " + otherPlayer.getCritters()[i].getName() + " caught " + name + ".";
					for (int j = 0; j < otherPlayer.getCritters().length; j++) {
						if (otherPlayer.getCritters()[j].equals(target))
						if (j < i) {
							toSpread.setJustSpread(true);
						} else {
						}
					}
					toSpread.showEffect();
				}
			}

				player.sendString(
						"actionlog," + str);
				otherPlayer.sendString(
						"actionlog," + str);

		} else {
			this.justSpread = false;
		}

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
					"effecttt,create,burn.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,burn.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


	}



	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}


	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public boolean isJustSpread() {
		return justSpread;
	}
	public void setJustSpread(boolean justSpread) {
		this.justSpread = justSpread;
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
		this.target = (Critter)target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost
				+ "\nDuration: " + duration
				+ "\nEffect: Reduces health and energy \nby 30 each turn. Spreads to \nadjacent Critters.";
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
		return new ContagionToss(null, null, null, null);

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
		return "burn.png";
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
