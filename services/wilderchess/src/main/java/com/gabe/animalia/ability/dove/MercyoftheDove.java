package com.gabe.animalia.ability.dove;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class MercyoftheDove extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Mercy of the Dove";
	private String type = "support";
	private double timeCost = 4;
	private int energyCost = 25;
	private int damage = 0;
	private Player player;
	private Player otherPlayer;
	private boolean used;
	private String indicatorMessage;
	private boolean performable = true;
	private int damageToDeal;
	private int duration = 5;
	private String effectDescription = "20 dmg/turn";
	private String statusType = "burn";


	public MercyoftheDove(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		player.getQueue().add(this);
		indicatorMessage = "";

	}

	@Override
	public void displayAction() {

	}

	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable && subject.canUseAction() && Math.abs(((Dove)subject).getGood()) == 5) {
			return true;
		}
		return false;

	}

	public void actionLog(Critter toRevive) {
		if (((Dove)subject).getGood() == 5) {

				player.sendString("actionlog," + subject.getName() + "'s miracle revived " + toRevive.getName() + ".");
				otherPlayer.sendString("actionlog," + subject.getName() + "'s miracle revived " + toRevive.getName() + ".");

		} else if (((Dove)subject).getGood() == -5) {

				player.sendString("actionlog," + subject.getName() + "'s miracle placed a curse on \n" + subject.getName() + "'s enemies.");
				otherPlayer.sendString("actionlog," + subject.getName() + "'s miracle placed a curse on \n" + subject.getName() + "'s enemies.");

		} else {

				player.sendString("actionlog," + subject.getName() + "'s miracle failed.");
				otherPlayer.sendString("actionlog," + subject.getName() + "'s miracle failed.");

		}


	}

	@Override
	public boolean perform() {

		if (subject.getEnergy() - energyCost >= 0) {
			if (performable()) {
			Critter toRevive = null;
			Random rng = new Random();
			subject.setEnergy(subject.getEnergy() - energyCost);
			ArrayList<Square> possibleSpots = new ArrayList<Square>();
			for (Square s : subject.getOwner().getSquares()) {
				if (!s.isOccupied() && !s.getName().contains("Bench")) {
					possibleSpots.add(s);
				}

			}
			Square newSpot = possibleSpots.get(rng.nextInt(possibleSpots.size()));
			if (((Dove)subject).getGood() == 5 && subject.getOwner().getDeadCritters().size() >= 1) {
				if (subject.getOwner().getDeadCritters().size() == 1) {
					toRevive = subject.getOwner().getDeadCritters().get(0);
				} else if (subject.getOwner().getDeadCritters().size() == 2) {
					toRevive = subject.getOwner().getDeadCritters().get(rng.nextInt(2));
				} else if (subject.getOwner().getDeadCritters().size() == 3) {
					toRevive = subject.getOwner().getDeadCritters().get(rng.nextInt(3));
				} else {
					return true;
				}
				toRevive.setSpot(newSpot);
				newSpot.setOccupied(true);
				newSpot.setCritter(toRevive);
				toRevive.setTempSpot(newSpot);
				toRevive.setHealth(toRevive.getMaxHealth());
				toRevive.setEnergy(toRevive.getMaxEnergy());
				newSpot.setPlannedMove(true);
				toRevive.setAlive(true);
				player.getDeadCritters().remove(toRevive);
				Critter [] newCritters = new Critter [player.getCritters().length + 1];
				for (int i = 0; i < player.getCritters().length; i++) {
					newCritters[i] = player.getCritters()[i];
				}
				newCritters[player.getCritters().length] = toRevive;
				player.setCritters(newCritters);

					player.sendString("revive," + toRevive.getName() + "," + toRevive.getSpot() + "," + toRevive.getSide());
					otherPlayer.sendString("revive," + toRevive.getName() + "," + toRevive.getSpot() + "," + toRevive.getSide());
					player.sendString("health," + toRevive.getName() + "," + toRevive.getSide() + "," + toRevive.getHealth() + "|");
					otherPlayer.sendString("health," + toRevive.getName() + "," + toRevive.getSide() + "," + toRevive.getHealth() + "|");
					player.sendString("energy," + toRevive.getName() + "," + toRevive.getSide() + "," + toRevive.getEnergy() + "|");
					otherPlayer.sendString("energy," + toRevive.getName() + "," + toRevive.getSide() + "," + toRevive.getEnergy() + "|");

			} else {
				for (Critter f : otherPlayer.getCritters()) {
					MercyoftheDove m = new MercyoftheDove(f, target, player, otherPlayer);
					f.getEffects().add(m);
					m.showEffect();
				}
			}
			subject.setEnergy(subject.getEnergy() - energyCost);
			actionLog(toRevive);
			((Dove)subject).setGood(0);
			subject.removeEffect("Spiritual Hymn of The Scales");



				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

			return true;
		}
			subject.setEnergy(subject.getEnergy() - energyCost);

				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {
		System.out.println("overTimeEffect miracle");
		critter.setHealth(critter.getHealth() - 20);



			player.sendString(
					"health," + critter.getName() + "," + critter.getSide() + ","
							+ critter.getHealth() + "|");
			otherPlayer.sendString(
					"health," + critter.getName() + "," + critter.getSide() + ","
							+ critter.getHealth() + "|");



			player.sendString("actionlog," + critter.getName() + " lost 20 health to burn.");
			otherPlayer.sendString("actionlog," + critter.getName() + " lost 20 health to burn.");


	}

	public void showEffect() {



				player.sendString(
						"effecttt,create,burn.png," + name + ","
								+ subject.getName() + "," + subject.getSide()
								+ "," + duration + "," + effectDescription
								+ "," + subject.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,create,burn.png," + name + ","
								+ subject.getName() + "," + subject.getSide()
								+ "," + duration + "," + effectDescription
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
		return name
				+ "\nDamage: "
				+ damage
				+ "\nType: "
				+ this.type
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nDescription: \n5 Good: revive a random \nteammate. \n5 Evil: deal 100 damage over 5 \nturns to each enemy fighter.";
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
		return new MercyoftheDove(null, null, null, null);

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
	public String getEffects() {
		return "burn.png";
	}
	@Override
	public String getStatusType() {
		return statusType;
	}
	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}
	@Override
	public int getDuration() {
		return duration;
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
