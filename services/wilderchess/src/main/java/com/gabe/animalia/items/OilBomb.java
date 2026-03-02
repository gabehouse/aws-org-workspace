package com.gabe.animalia.items;



import java.io.IOException;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;

public class OilBomb extends Item{
	private TypesAndStuff taf = new TypesAndStuff();
	private String name = "Oil Bomb";
	private String description = name + "\nConsumes all burns dealing 25 \ndamage for each burn.";
	private String targetType = "targetless";
	private Targetable target;
	private Player player;
	private Player otherPlayer;
	private String imageName = "oilbomb.png";
	private boolean used = false;
	private int second;
	private Critter subject;
	private boolean isPerformable = true;

	public OilBomb(Player player, Player otherPlayer) {
		this.player = player;
		this.otherPlayer= otherPlayer;

	}


	public void actionLog() {


			player.sendString("actionlog," + player.getUsername() + " used " + name + ".");
			otherPlayer.sendString("actionlog," + player.getUsername()  + " used " + name + ".");


	}

	@Override
	public boolean perform() {
		if (!used && isPerformable) {
			actionLog();
			for (Critter f : otherPlayer.getCritters()) {

				for (int i = 0; i < f.getEffects().size(); i++) {
					if (taf.isBurn(f.getEffects().get(i))){
						Critter burnTarget = (Critter)f.getEffects().get(i).getTarget();
						burnTarget.setHealth(burnTarget.getHealth() - 25);
						f.getEffects().remove(i);

							player.sendString(
									"effecttt,remove," + burnTarget.getName() + ","
											+ burnTarget.getSide() + ","
											+ i);
							otherPlayer.sendString(
									"effecttt,remove," + burnTarget.getName() + ","
											+ burnTarget.getSide() + ","
											+ i);
							player.sendString(
									"health," + burnTarget.getName() + ","
											+ burnTarget.getSide() + ","
											+ burnTarget.getHealth() + "|");
							otherPlayer.sendString(
									"health," + burnTarget.getName() + ","
											+ burnTarget.getSide() + ","
											+ burnTarget.getHealth() + "|");

						i = i-1;
					}
				}

			}
			used = true;
		}
		return true;


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

//	@Override
//	public void setSubject(Critter subject) {
//
//		this.subject = subject;
//	}

}
