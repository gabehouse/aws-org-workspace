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

public class WarHorn extends Item{
	private TypesAndStuff taf = new TypesAndStuff();
	private String name = "War Horn";
	private String description = name + "\nMoves your opponent's fighters \nto random spots.";
	private String targetType = "targetless";
	private Targetable target;
	private Player player;
	private Player otherPlayer;
	private String imageName = "warhorn.png";
	private boolean used = false;
	private int second;
	private boolean isPerformable = true;
	private Action interruptedAbility = null;

	public WarHorn(Player player, Player otherPlayer) {
		this.player = player;
		this.otherPlayer = otherPlayer;

	}

	public void actionLog() {


			player
					.sendString(
							"actionlog," + player.getUsername() + " used "
									+ name + ".");
			otherPlayer
					.sendString(
							"actionlog," + player.getUsername() + " used "
									+ name + ".");



	}

	@Override
	public boolean perform() {
		if (!used && isPerformable) {
			actionLog();
			Random rng = new Random();
			ArrayList<Integer> chosen = new ArrayList<Integer>();
			if (otherPlayer.getPerformingAbility() != null) {
				interruptedAbility = otherPlayer.getPerformingAbility();
				interruptedAbility.setInterrupted(true, interruptedAbility.getSubject(), player, otherPlayer);
				otherPlayer.getPerformingAbility().setPerformable(false);
			}

			for (Critter f : otherPlayer.getCritters()) {
				String str = "";
				if (f.isBenched()) {
					str = f.getSpot().getName();
				} else {


					int n = rng.nextInt(5);
					while (chosen.contains(n)) {
						n = rng.nextInt(5);
					}
					chosen.add(n);

					Square newSpot = otherPlayer.getTopBack();
					if (n == 0) {
						newSpot = otherPlayer.getTopBack();
					} else if (n == 1) {
						newSpot = otherPlayer.getTopFront();
					} else if (n == 2) {
						newSpot = otherPlayer.getMiddleBack();
					} else if (n == 3) {
						newSpot = otherPlayer.getMiddleFront();
					} else if (n == 4) {
						newSpot = otherPlayer.getBottomBack();
					} else if (n == 5) {
						newSpot = otherPlayer.getBottomFront();
					}
					f.getSpot().setOccupied(false);
					f.getSpot().setPlannedMove(false);
					f.setSpot(newSpot);
					f.getSpot().setOccupied(true);
					f.getSpot().setCritter(f);
					f.setTempSpot(f.getSpot());
					f.getSpot().setPlannedMove(true);
					str = newSpot.getName();

				}

					player.sendString(
							"animate,cancel," + f.getName() + "," + f.getSide());
					otherPlayer.sendString(
							"animate,cancel," + f.getName() + "," + f.getSide());
					player.sendString(
							"move," + f.getName() + "," + f.getSide() + ","
									+ str + "," + 1000);
					otherPlayer.sendString(
							"move," + f.getName() + "," + f.getSide() + ","
									+ str + "," + 1000);


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
