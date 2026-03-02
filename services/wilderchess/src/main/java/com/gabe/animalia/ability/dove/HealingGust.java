package com.gabe.animalia.ability.dove;
import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class HealingGust extends Action {
	private Critter subject;
	private Square target;
	private String targetType = "spot";
	private String name = "Healing Gust";
	private String type = "support";
	private String statusType = "heal";
	private String effectDescription = "heal 30 to a column";
	private double timeCost = 2;
	private int energyCost = 40;
	private int heal = 30;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private Square otherTarget;
	private Square otherOtherTarget;
	private ArrayList<Targetable> toAnimateImpact = new ArrayList<>();



	public HealingGust(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Square)target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	//	toAnimateImpact = new ArrayList<Targetable>();
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayOptions() {
		String str = "indicatespots,support," + name + ","
				+ "leftTopBack.00-10-13-03-00.left,"
				+ "leftTopFront.10-20-23-13-10.left,"
				+ "rightTopFront.30-40-43-33-30.right,"
				+ "rightTopBack.40-50-53-43-40.right,";

			player.sendString(str);

	}

	@Override
	public void animate(Player player, Player otherPlayer) {
		String str = "";
		for (Targetable t : toAnimateImpact) {
			str += "animate,action," + t.getName() + "," + t.getSide() + "," + getType() + "," + getTargetType() + "|";
		}
		player.sendString(str);
		otherPlayer.sendString(str);
		// if (target.getCritter() != null) {
		// 	Critter tempTarget = target.getCritter();
		// 	System.out.println(target.getSide());
		// 	player.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// 	otherPlayer.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// }
		// if (otherTarget.getCritter() != null) {
		// 				System.out.println(otherTarget.getSide());
		// 	Critter tempTarget = otherTarget.getCritter();
		// 	player.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// 	otherPlayer.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// }
		// if (otherOtherTarget.getCritter() != null) {
		// 	Critter tempTarget = otherOtherTarget.getCritter();
		// 	player.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// 	otherPlayer.sendString(
		// 			"animate,support," + tempTarget.getName() + "," + tempTarget.getSide() + ","
		// 					+ "|");
		// }
	}


	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|supportspot," + target.getName() ;
		if (target.getOnLeft() != null) {
			if (target.getOnLeft().getOnLeft() != null) {
				otherTarget = target.getOnLeft();
				otherOtherTarget = target.getOnLeft().getOnLeft();
				subject.getIndicatedSupports().add(otherTarget);
				subject.getIndicatedSupports().add(otherOtherTarget);
				indicatorMessage += "|supportspot," + otherTarget.getName() +
									"|supportspot," + otherOtherTarget.getName();
			} else {
				otherTarget = target.getOnLeft();
				otherOtherTarget = target.getOnRight();
				subject.getIndicatedSupports().add(otherTarget);
				subject.getIndicatedSupports().add(otherOtherTarget);
				indicatorMessage += "|supportspot," + otherTarget.getName()+
						"|supportspot," + otherOtherTarget.getName();
			}
		} else {
			otherTarget = target.getOnRight();
			otherOtherTarget = target.getOnRight().getOnRight();
			subject.getIndicatedSupports().add(otherTarget);
			subject.getIndicatedSupports().add(target.getOnRight().getOnRight());
			indicatorMessage += "|supportspot," + otherTarget.getName() +
					"|supportspot," + otherOtherTarget.getName();
		}
			toAnimateImpact.add(target);
			toAnimateImpact.add(otherTarget);
			toAnimateImpact.add(otherOtherTarget);
			player.sendString("indicatespots,no moves");

	}


	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}


	public void actionLog() {


			if (target.getName().equals("leftTopFront") || target.getName().equals("leftMiddleFront") || target.getName().equals("leftBottomFront") ||
				target.getName().equals("rightTopFront") || target.getName().equals("rightMiddleFront") || target.getName().equals("rightBottomFront")) {
				player.sendString("actionlog," + subject.getName() + " used " + name + " on the front column.");
				otherPlayer.sendString("actionlog," + subject.getName() + " used " + name + " on the front column.");
			} else {
				player.sendString("actionlog," + subject.getName() + " used " + name + " on the back column.");
				otherPlayer.sendString("actionlog," + subject.getName() + " used " + name + " on the back column.");
			}


	}

	@Override
	public boolean perform() {


		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && performable()) {
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			Square[] targets = {target, otherTarget, otherOtherTarget};
			for (Square t : targets) {
				if (t.getCritter() != null && t.getCritter().isAlive()) {
					Critter critter = t.getCritter();

					critter.setHealth(critter.getHealth() + heal);
					if (critter.getHealth() > critter.getMaxHealth()) {
						critter.setHealth(critter.getMaxHealth());
					}

						player.sendString("health," + critter.getName() + "," + critter.getSide() + "," + critter.getHealth() + "|");
						otherPlayer.sendString("health," + critter.getName() + "," + critter.getSide() + "," + critter.getHealth() + "|");

				}
			}

				player.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");

			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {

	}

	@Override
	public void endEffect(Critter critter) {

	}

	public void showEffect() {


	}

	@Override
	public void displayAction() {

	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
		subject.getIndicatedSupports().remove(otherTarget);
		subject.getIndicatedSupports().remove(otherOtherTarget);

	}


//	@Override
//	public int getDuration() {
//		return duration;
//	}
//
//	@Override
//	public void setDuration(int duration) {
//		this.duration = duration;
//	}

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
	public Square getTarget() {
		return target;
	}
	@Override
	public void setTarget(Targetable target) {
		this.target = (Square)target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name
				+ "\nHeal: "
				+ heal + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost + "\nDescription: Heals fighters on \ntargeted column.";
	}

	@Override
	public String getSelectDescription() {
		return name
				+ "\nHeal: "
				+ heal + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost + "\nDescription: Heals fighters on \ntargeted column.";
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
		return new HealingGust(null, null, null, null);

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
