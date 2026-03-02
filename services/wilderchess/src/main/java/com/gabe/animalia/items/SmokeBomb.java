package com.gabe.animalia.items;



import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;

public class SmokeBomb extends Item{
	private TypesAndStuff taf = new TypesAndStuff();
	private String name = "Smoke Bomb";
	private String description = name +"\nCauses all abilities targeted at \nopposing fighters to have no \neffect.";
	private String effectDescription = "All targetted abilities miss";
	private String targetType = "targetless";
	private String statusType = "untargetable";
	private Targetable target;
	private Player player;
	private Player otherPlayer;
	private String imageName = "smokebomb.png";
	private int duration = 1;
	private boolean used = false;
	private int second;
	private boolean isPerformable = true;

	public SmokeBomb(Player player, Player otherPlayer) {
		this.player = player;
		this.otherPlayer= otherPlayer;

	}
	@Override
	public String getStatusType() {
		return statusType;
	}

	public void actionLog() {


			player.sendString("actionlog," + player.getUsername() + " used " + name + ".");
			otherPlayer.sendString("actionlog," + player.getUsername()  + " used " + name + ".");


	}

	@Override
	public boolean perform() {
		if (!used && isPerformable) {
			displayAction();
			actionLog();
			if (!player.getQueue().isEmpty()) {
				for (int i = 0; i < player.getQueue().size(); i++) {
					if (player.getQueue().get(i).getTarget() != null) {
						if (player.getQueue().get(i).getTarget().getSide() != null) {
							if (player.getQueue().get(i).getTarget().getSide()
									.equals(otherPlayer.getSide())) {
								player.getQueue().get(i).setPerformable(false);
							}
						}
					}
				}
			}
			if (!player.getQueue().isEmpty()) {
				for (int i = 0; i < otherPlayer.getQueue().size(); i++) {
					if (otherPlayer.getQueue().get(i).getTarget() != null) {
						if (otherPlayer.getQueue().get(i).getTarget().getSide() != null) {
							if (otherPlayer.getQueue().get(i).getTarget()
									.getSide().equals(player.getSide())) {
								otherPlayer.getQueue().get(i)
										.setPerformable(false);
							}
						}
					}
				}
			}
			for (Critter c : player.getCritters()) {
				c.getEffects().add(this);
			}
			for (Critter c : otherPlayer.getCritters()) {
				c.getEffects().add(this);
			}
			showEffect();
			used = true;
		}
		return true;
	}

	@Override
	public void showEffect() {
		for (Critter c : player.getCritters()) {


				player.sendString(
						"effecttt,create,buff.png," + name + "," + c.getName()
								+ "," + c.getSide() + "," + duration + ","
								+ effectDescription + ","
								+ c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,create,buff.png," + name + "," + c.getName()
								+ "," + c.getSide() + "," + duration + ","
								+ effectDescription + ","
								+ c.getEffects().indexOf(this));

		}
		for (Critter c : otherPlayer.getCritters()) {


				player.sendString(
						"effecttt,create,buff.png," + name + "," + c.getName()
								+ "," + c.getSide() + "," + duration + ","
								+ effectDescription + ","
								+ c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,create,buff.png," + name + "," + c.getName()
								+ "," + c.getSide() + "," + duration + ","
								+ effectDescription + ","
								+ c.getEffects().indexOf(this));

		}

	}

	@Override
	public void endEffect(Critter fighter) {


			for (Critter c : otherPlayer.getCritters()) {
				player.sendString(
						"effecttt,remove," + c.getName() + "," + c.getSide()
								+ "," + c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,remove," + c.getName() + "," + c.getSide()
								+ "," + c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"animate,unstealth," + c.getName() + "," + c.getSide()
								+ "|");
				player.sendString(
						"animate,unstealth," + c.getName() + "," + c.getSide()
								+ "|");
			}
			for (Critter c : player.getCritters()) {
				player.sendString(
						"effecttt,remove," + c.getName() + "," + c.getSide()
								+ "," + c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,remove," + c.getName() + "," + c.getSide()
								+ "," + c.getEffects().indexOf(this));
				otherPlayer.sendString(
						"animate,unstealth," + c.getName() + "," + c.getSide()
								+ "|");
				player.sendString(
						"animate,unstealth," + c.getName() + "," + c.getSide()
								+ "|");
			}

	}

	@Override
	public void displayAction() {
		System.out.println("smokebomb displayaction");
		for (Critter c : player.getCritters()) {

				otherPlayer.sendString(
						"animate," + "stealth" + "," + c.getName() + ","
								+ c.getSide() + "," + 0 + "|");
				player.sendString(
						"animate," + "stealth" + "," + c.getName() + ","
								+ c.getSide() + "," + 0 + "|");


		}
		for (Critter c : otherPlayer.getCritters()) {

				otherPlayer.sendString(
						"animate," + "stealth" + "," + c.getName() + ","
								+ c.getSide() + "," + 0 + "|");
				player.sendString(
						"animate," + "stealth" + "," + c.getName() + ","
								+ c.getSide() + "," + 0 + "|");


		}

	}

	@Override
	public int getSecond() {
		return second;
	}

	@Override
	public void setSecond(int second) {
		this.second = second;
	}

	@Override
	public String getName() {
		return name;
	}
	@Override
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String getDescription() {
		return description;
	}
	@Override
	public void setDescription(String description) {
		this.description = description;
	}
	@Override
	public String getTargetType() {
		return targetType;
	}
	@Override
	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}
	@Override
	public Targetable getTarget() {
		return target;
	}
	@Override
	public void setTarget(Targetable target) {
		this.target = target;
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
	public boolean isUsed() {
		return used;
	}
	@Override
	public void setUsed(boolean used) {
		this.used = used;
	}
	@Override
	public String getImageName() {

		return imageName;
	}
	@Override
	public boolean isPerformable() {
		return isPerformable;
	}
	@Override
	public void setPerformable(boolean isPerformable) {
		this.isPerformable = isPerformable;
	}
	@Override
	public String getType() {
		return "item";
	}

}
