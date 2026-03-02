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

public class ThiefGloves extends Item{
	private TypesAndStuff taf = new TypesAndStuff();
	private String name = "Thief Gloves";
	private String description = name + "\nSteals the opponent's item if it \nis being used later this turn.";

	private String effectDescription = "Move enemy critters \n do random spots";
	private String targetType = "targetless";
	private Targetable target;
	private Player player;
	private Player otherPlayer;
	private String imageName = "thiefgloves.png";
	private int duration = 1;
	private boolean used = false;
	private int second;
	private boolean isPerformable = true;

	public ThiefGloves(Player player, Player otherPlayer) {
		this.player = player;
		this.otherPlayer= otherPlayer;

	}

	public void actionLog() {


			player.sendString("actionlog," + player.getUsername() + " used " + name + ".");
			otherPlayer.sendString("actionlog," + player.getUsername()  + " used " + name + ".");


	}

	@Override
	public boolean perform() {
		System.out.println("gloves perform1");
		if (!used && isPerformable) {
			System.out.println("gloves perform2");
			actionLog();
			int pos = -1;
			Random rng = new Random();
			Item toSteal = null;
			ArrayList<Item> itemsBeingUsed = new ArrayList<Item>();
			for (Item i : otherPlayer.getItemQueue()) {
				if (i.getSecond() >= second) {
					System.out.println("gloves perform3");
					itemsBeingUsed.add(i);
				}
			}
			if (!itemsBeingUsed.isEmpty()) {
				System.out.println("gloves perform4");
				int rnged = rng.nextInt(itemsBeingUsed.size());
				toSteal = itemsBeingUsed
						.get(rnged);
				toSteal.setPerformable(false);
				toSteal.setUsed(false);
				toSteal.setPlayer(player);
				toSteal.setOtherPlayer(otherPlayer);
				System.out.println(toSteal.getName());
				if (player.getItem1().getName().equals(name)) {
					player.setItem1(toSteal);
					pos = 0;
				} else if (player.getItem2().getName().equals(name)) {
					player.setItem2(toSteal);
					pos = 1;
				} else if (player.getItem3().getName().equals(name)) {
					player.setItem3(toSteal);
					pos = 2;
				}

					player.sendString(
							"inititems,|" + toSteal.getName() + ","
									+ toSteal.getDescription() + ","+ pos +","
									+ toSteal.getImageName() + "|");

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
