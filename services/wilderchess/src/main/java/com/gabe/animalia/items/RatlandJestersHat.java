package com.gabe.animalia.items;



import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;

public class RatlandJestersHat extends Item{
	private TypesAndStuff taf = new TypesAndStuff();
	private String name = "Ratland Jester's Hat";

	private String description = name + "\nAll enemy attacks are directed \nto your fighter with the most \nhealth.";
	private String targetType = "targetless";
	private Targetable target;
	private Player player;
	private Player otherPlayer;
	private String imageName = "jestershat.png";
	private int duration = 1;
	private boolean used = false;
	private int second;
	private boolean isPerformable = true;

	public RatlandJestersHat(Player player, Player otherPlayer) {
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
			ArrayList<Critter> mostHealth = new ArrayList<Critter>();
			Critter temp = null;
			Critter newTarget = null;
			int maxHealth = 0;
			Random rng = new Random();

			for (Critter f : player.getCritters()) {
				if (f.getHealth() > maxHealth) {
					maxHealth = f.getHealth();
					if (temp != null) {
						mostHealth.remove(temp);
					}
					temp = f;
					mostHealth.add(temp);
				} else if (f.getHealth() == maxHealth) {
					mostHealth.add(f);
				}
			}
			if (mostHealth.size() > 1) {
				newTarget = mostHealth.get(rng.nextInt(mostHealth.size()-1));
			} else {
				newTarget = mostHealth.get(0);
			}

			for (Action a : otherPlayer.getQueue()) {
				if (a.getType().equals("attack")) {
					a.setTarget(newTarget);
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
}
