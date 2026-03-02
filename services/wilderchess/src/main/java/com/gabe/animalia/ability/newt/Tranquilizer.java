package com.gabe.animalia.ability.newt;

import java.io.IOException;
import java.util.Random;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Tranquilizer extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Tranquilizer";
	private String statusType = "none";
	private String type = "attack";
	private double timeCost = 2;
	private int energyCost = 30;
	private int damage = 30;
	private int energyReduce = 40;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private int damageToDeal = 0;
	private String actionStr = "";
	private Targetable toAnimateImpact;


	public Tranquilizer(Critter subject, Targetable target, Player player, Player otherPlayer) {
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


			player.sendString("actionlog," + actionStr);
			otherPlayer.sendString("actionlog," + actionStr);


	}

	@Override
	public void animate(Player player, Player otherPlayer) {
		player.sendString("animate,action," + toAnimateImpact.getName() + "," + toAnimateImpact.getSide() + "," + getType() + "," + toAnimateImpact.getType() + "|");
		otherPlayer.sendString("animate,action," + toAnimateImpact.getName() + "," + toAnimateImpact.getSide() + "," + getType() + "," + toAnimateImpact.getType() + "|");
	}

	@Override
	public boolean perform() {
		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {
			Random r = new Random();
			int row = r.nextInt(3);
			System.out.println("row = " + row);
			Square s = null;
			if (row == 0) {
				s = otherPlayer.identifySquare(otherPlayer.getSide() + "TopFront");
			} else if (row == 1) {
				s = otherPlayer.identifySquare(otherPlayer.getSide() + "MiddleFront");

			} else if (row == 2) {
				s = otherPlayer.identifySquare(otherPlayer.getSide() + "BottomFront");
			}
			System.out.println("wot " + s.getName() + ", " + s.getBehind().getName() + s.getCritter() + ", " + s.getBehind().getCritter());
			if (s.getCritter() != null) {
				target = s.getCritter();
			} else {
				if (s.getBehind().getCritter() != null) {
					target = s.getBehind().getCritter();
				} else {
					actionStr = name + " missed.";
					target = null;
				}
			}
			toAnimateImpact = target != null ? target : s;
			subject.setEnergy(subject.getEnergy() - energyCost);
			if (target != null) {
				damageToDeal = damage + subject.getBonusDmg()
						- target.getDefence();
				if (target.getDefence() == 0) {
					target.onHit(subject.getOwner(), subject.getOpponent(),
							this);
					target.setEnergy(target.getEnergy() - energyReduce);
				} else {
					target.onBlock(subject.getOwner(), subject.getOpponent());
				}
				if (damageToDeal < 0) {
					damageToDeal = 0;
				}
				actionStr = subject.getName() + " dealt " + damageToDeal + " to " + target.getName() + " with "  + name + ".";


				target.setHealth(target.getHealth() - damageToDeal);
			}
			actionLog();

				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				if (target != null) {
					player.sendString(
							"health," + target.getName() + ","
									+ target.getSide() + ","
									+ target.getHealth() + "|");
					otherPlayer.sendString(
							"health," + target.getName() + ","
									+ target.getSide() + ","
									+ target.getHealth() + "|");
					player.sendString(
							"energy," + target.getName() + ","
									+ target.getSide() + ","
									+ target.getEnergy() + "|");
					otherPlayer.sendString(
							"energy," + target.getName() + ","
									+ target.getSide() + ","
									+ target.getEnergy() + "|");
				}

			return true;
		}
		return false;
	}

	@Override
	public void delete() {

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
		if (target == null) {
			return subject;
		}
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
	public String getTargetName() {
		if (target == null) {
			return subject.getName();
		}
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new Tranquilizer(null, null, null, null);
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
	public String getDescription() {
		return name.replace("\n", "") + "\nDamage: " + damage + "\nType: "
				+ this.type  + "\nEnergy: " + energyCost + "\nTime: " + timeCost + "\nDescription:  Hits the closest enemy \nfighter in a random row. Reduces \nenergy of the target by " + energyReduce + " on \nsuccessful hit.";
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
