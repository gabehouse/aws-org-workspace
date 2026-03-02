package com.gabe.animalia.ability.donkey;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class DonkeyBlues extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Donkey Blues";
	private String type = "support";
	private String statusType = "none";
	private double timeCost = 2;
	private int energyCost = 40;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 0;
	private boolean performable = true;
	String selectDescription = name
			+ "\nType: "
			+ this.type
			+ "\nEnergy: "
			+ energyCost
			+ "\nTime: "
			+ timeCost
			+ "\nDescription: Decreases the energy of \nopposing fighters by 15.";
	String description = name
			+ "\nType: "
			+ this.type
			+ "\nEnergy: "
			+ energyCost
			+ "\nTime: "
			+ timeCost
			+ "\nDescription: Decreases the energy \nof opposing fighters by 15.";



	public DonkeyBlues(Critter subject, Targetable target, Player player, Player otherPlayer) {
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
		indicatorMessage = "indicate|";
		for (Critter f : otherPlayer.getCritters()) {
			subject.getIndicatedSupports().add(f);
			indicatorMessage += "support," + f.getName() + "," +f.getSide() + "|";
		}





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
			subject.setEnergy(subject.getEnergy() - energyCost);

			for (Critter f : otherPlayer.getCritters()) {
					f.setEnergy(f.getEnergy() - 15);
					if (f.getEnergy() > 100) {
						f.setEnergy(100);
					}

						player.sendString("energy," + f.getName() + ","+ f.getSide() + ","+ f.getEnergy() + "|");
						otherPlayer.sendString("energy," + f.getName() + "," + f.getSide() + "," + f.getEnergy() + "|");





			}


				player.sendString("energy," + subject.getName() + ","+ subject.getSide() + ","+ subject.getEnergy() + "|");
				otherPlayer.sendString("energy," + subject.getName() + "," + subject.getSide() + "," + subject.getEnergy() + "|");



			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {

	}

	@Override
	public void displayAction() {

	}

	@Override
	public void delete() {
		for (Critter f : otherPlayer.getCritters()) {
		subject.getIndicatedSupports().remove(f);
		}

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
	public String getType() {
		return type;
	}

	@Override
	public String getTargetName() {
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new DonkeyBlues(null, null, null, null);

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
	@Override
	public String getSelectDescription() {
		return selectDescription;
	}


}
