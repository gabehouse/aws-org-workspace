package com.gabe.animalia.ability.turtle;
import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;


public class ShellStance extends Action {
	private Critter subject;
	private Critter target;
	private String targetType = "targetless";
	private String name = "Shell Stance";
	private String type = "block";
	private String effectDescription = "defence + 20";
	private double timeCost = 2;
	private int energyCost = 20;
	private int block = 20;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private int duration = 1000;
	private String statusType = "block,immobilize";
	private boolean performable = true;

	public ShellStance(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = subject;
		this.player = player;
		this.otherPlayer = otherPlayer;


	}


	@Override
	public void modify() {
		for (Action a : subject.getEffects()) {
			a.initialEffect(this);
		}
	}

	@Override
	public void init() {
		subject.getIndicatedBlocks().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|block," + subject.getName() + "," + subject.getSide();
		target = subject;
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
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}

	public void actionLog() {


			if (!subject.isAffectedBy(name)) {
				player.sendString("actionlog," + subject.getName() + " started hiding.");
				otherPlayer.sendString("actionlog," + subject.getName() + " started hiding.");
			} else {
				player.sendString("actionlog," + subject.getName() + " stopped hiding.");
				otherPlayer.sendString("actionlog," + subject.getName() + " stopped hiding.");
			}


	}

	@Override
	public boolean perform() {
		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction() && subject.canUseAction() && performable()) {
			actionLog();
			subject.setEnergy(subject.getEnergy() - energyCost);
			if (!subject.isAffectedBy(name)) {


				target.setDefence(target.getDefence() + block);


				target.getEffects().add(this);
				showEffect();
			} else {
				target.removeEffect(name);
				subject.setDefence(subject.getDefence() - block);


			}

			subject.sendStats(player, otherPlayer);
			return true;
		}
		return false;
	}

	@Override
	public void overTimeEffect(Critter critter) {

	}




	@Override
	public void endEffect(Critter critter) {
		subject.setDefence(subject.getDefence() - block);

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
					"effecttt,create,block.png," + name + "," + target.getName()
							+ "," + target.getSide() + "," + duration + "," + effectDescription
							+ "," + target.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,create,block.png," + name + "," + target.getName()
					+ "," + target.getSide() + "," + duration + "," + effectDescription
					+ "," + target.getEffects().indexOf(this));


	}

	@Override
	public String getStatusType() {
		return statusType;
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(target);

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
		return name.replace("\n", "")
				+ "\nDefence: "
				+ block
				+ "\nType: "
				+ this.type
				+ "\nEnergy: "
				+ energyCost
				+ "\nTime: "
				+ timeCost
				+ "\nEffect: Increases Turtle's \ndefence and disables the rest of \nTurtle's actions while activated. \nUse again to deactivate.";
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
		return new ShellStance(null, null, null, null);
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
	public void setTimeCost(double timeCost) {
		this.timeCost = timeCost;
	}

	@Override
	public void setEnergyCost(int energyCost) {
		this.energyCost = energyCost;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
