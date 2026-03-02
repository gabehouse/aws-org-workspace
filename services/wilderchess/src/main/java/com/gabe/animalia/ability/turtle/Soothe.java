package com.gabe.animalia.ability.turtle;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;


public class Soothe extends Action {
	private Critter subject;
	private Square target;
	private String targetType = "targetless";
	private String name = "Soothe";
	private String type = "support";
	private double timeCost = 4;
	private int energyCost = 10;
	private int energyRestore = 100;
	private Player player;
	private Player otherPlayer;
	private boolean used;
	private String indicatorMessage;
	private boolean performable = true;

	public Soothe(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		if (subject != null) {
			this.target = subject.getSpot().getBehind();
		} else {
			this.target = (Square) target;
		}
		this.player = player;
		this.otherPlayer = otherPlayer;
		if (this.target != null)
		System.out.println("SOOTHE CONSTRUCTURE, target = " + this.target.getName());

	}
	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		if (subject.getSpot().getBehind() != null)
			target = subject.getSpot().getBehind();
		player.getQueue().add(this);
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
		if (subject.getEnergy() - energyCost >= 0 && !subject.isAffectedBy("Defence Stance") && subject.canUseAction() && performable) {
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
		if (subject.getEnergy() - energyCost >= 0
				&& !subject.isAffectedBy("Defence Stance")  && subject.canUseAction() && performable()) {
			actionLog();
			if (subject.getSpot().getBehind() != null) {
				if (subject.getSpot().getBehind().isOccupied()) {
					System.out.println("soothe, behind spot = " +  subject.getSpot().getBehind().getName());
					subject.setEnergy(subject.getEnergy() - energyCost);
					target = subject.getSpot().getBehind();
					target.getCritter().setEnergy(
							target
							.getCritter()
							.getEnergy() + energyRestore);

						player.sendString(
								"energy," + subject.getName() + ","
										+ subject.getSide() + ","
										+ subject.getEnergy() + "|");
						player.sendString(
								"energy," + target.getCritter().getName()
										+ "," + target.getCritter().getSide()
										+ ","
										+ target.getCritter().getEnergy()
										+ "|");
						otherPlayer.sendString(
								"energy," + subject.getName() + ","
										+ subject.getSide() + ","
										+ subject.getEnergy() + "|");
						otherPlayer.sendString(
								"energy," + target.getCritter().getName()
										+ "," + target.getCritter().getSide()
										+ ","
										+ target.getCritter().getEnergy()
										+ "|");

					return true;

				}
			}

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
	public Targetable getTarget() {
		return target;
	}

	@Override
	public void setTarget(Targetable target) {
		System.out.print("SOOTHE SET TARGET from " + this.getTarget().getName() + " to " + target.getName());
		this.target = (Square) target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name.replace("\n", "")
				+ "\nEnergy Restore: "
				+ energyRestore
				+ "\nType: "
				+ this.type
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nDescription: Restores the energy \nof a fighter behind Turtle.";
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
		return new Soothe(null, null, null, null);
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
