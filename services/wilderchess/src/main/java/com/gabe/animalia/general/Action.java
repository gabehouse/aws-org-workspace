package com.gabe.animalia.general;

import java.io.IOException;

import com.gabe.animalia.ml.dtos.LoggableActionDTO;


/**
 * @author Gabe House
 * @version 6/7/2015
 * Parent class of all actions.
 */
public class Action implements Comparable<Action> {

	private int order = -1;
	private int displayOrder = -1;
	private boolean startPerformable = true;
	private boolean interrupted = false;
	private int priority = 0;
	public boolean isInterrupted() {
		return interrupted;
	}


	public void setInterrupted(boolean interrupted, Critter animating, Player player, Player otherPlayer) {
		this.interrupted = interrupted;
		if (animating != null) {

				player.sendString(
						"animate,cancel," + animating.getName() + "," + animating.getSide());
				otherPlayer.sendString(
						"animate,cancel," + animating.getName() + "," + animating.getSide());

					player.sendString(
							"actionlog," + this.getName()
									+ " was interrupted.");
					otherPlayer.sendString(
							"actionlog," + this.getName()
									+ " was interrupted.");


		}
	}

	public Action () {

	}

    public LoggableActionDTO toLoggableDTO() {
		LoggableActionDTO l = new LoggableActionDTO(getSubject().getName(), getType(), getName(), getTarget().getName(), getTimeCost(), getEnergyCost());
		return l;
	}

	public void animate(Player player, Player otherPlayer) {
		if (this.getType() == "item") return;
			player.sendString(
							"animate,action," + getTargetName() + "," + getTarget().getSide() + "," + getType() + "," + getTargetType() + "|");
					otherPlayer.sendString(
							"animate,action," + getTargetName() + "," + getTarget().getSide() + "," + getType() + "," + getTargetType() + "|");

	}

	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public Action getNew() {
		return null;
	}


	public boolean isStartPerformable() {
		return startPerformable;
	}


	public void setStartPerformable(boolean startPerformable) {
		this.startPerformable = startPerformable;
	}


	public String getStatusType() {
		return null;
	}

	public String getTargetType() {
		return null;
	}
	/**
	 * Performs the ability and sends updated health and energy of the critters to the clients.
	 * @return true is successfully performed
	 */
	public boolean perform() {
		return false;
	}
	public String getDescription() {
		return null;
	}
	public String getUsingName() {
		return null;
	}
	public String getType() {
		return null;
	}
	public String getTargetName() {
		return null;
	}

	public int getEnergyCost() {
		return 0;
	}

	public double getTimeCost() {
		return 0;
	}

	public String getName() {
		return null;
	}

	/**
	 * Removes action from action queue and removes its indicators.
	 */
	public void delete() {

	}


	/**
	 * @return true if the action can be performed
	 */
	public boolean performable() {
		return false;
	}


	/**
	 * Sends the clients the action indicators.
	 */
	public void displayAction() {

	}

	public Critter getSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSubject(Critter subject) {
		// TODO Auto-generated method stub

	}

	public Targetable getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setTarget(Targetable target) {
		// TODO Auto-generated method stub

	}

	public void setPlayer(Player player) {
		// TODO Auto-generated method stub

	}

	public void setOtherPlayer(Player otherPlayer) {
		// TODO Auto-generated method stub

	}

	public Player getOtherPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	public void init() {
		// TODO Auto-generated method stub

	}
	public String getIndicatorMessage() {
		return null;
	}

	public void displayOptions() {
		// TODO Auto-generated method stub

	}

	public void overTimeEffect(Critter critter) {
		// TODO Auto-generated method stub

	}

	public int getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setDuration(int duration) {
		// TODO Auto-generated method stub

	}

	public boolean isPerformable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPerformable(boolean performable) {
		// TODO Auto-generated method stub

	}

	public int getDamage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getEffects() {
		// TODO Auto-generated method stub
		return "";
	}


	public void endEffect(Critter critter) {
		// TODO Auto-generated method stub

	}


	public void initialEffect(Action ability) {
		// TODO Auto-generated method stub

	}


	public void setTimeCost(double d) {
		// TODO Auto-generated method stub

	}


	public void setEnergyCost(int energyCost) {
		// TODO Auto-generated method stub

	}


	public void setName(String string) {
		// TODO Auto-generated method stub

	}


	public void modify() {
		// TODO Auto-generated method stub

	}


	public void sendOptions() {
		// TODO Auto-generated method stub

	}


	public void setOrder(int order) {
		this.order = order;

	}
	public int getOrder() {
		return this.order;

	}


	public void showEffect() {
		// TODO Auto-generated method stub

	}


	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;

	}
	public int getDisplayOrder() {
		return this.displayOrder;

	}


	public boolean initable() {
		// TODO Auto-generated method stub
		return true;
	}


	public String getSelectDescription() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public int compareTo(Action other) {
		// TODO Auto-generated method stub
		return Integer.compare(other.getPriority(), this.priority);
	}


}
