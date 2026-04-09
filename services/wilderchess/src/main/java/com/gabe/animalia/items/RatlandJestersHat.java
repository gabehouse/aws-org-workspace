package com.gabe.animalia.items;

import java.util.ArrayList;
import java.util.Random;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;

public class RatlandJestersHat extends Item {

	private String imageName = "jestershat.png";

	public RatlandJestersHat(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.COXCOMB);
	}

	@Override
	public boolean customPerform() {
		ArrayList<Critter> mostHealth = new ArrayList<Critter>();
		Critter temp = null;
		Critter newTarget = null;
		int maxHealth = 0;
		Random rng = new Random();

		for (Critter f : getPlayer().getCritters()) {
			if (f.isBenched())
				continue;
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
			newTarget = mostHealth.get(rng.nextInt(mostHealth.size() - 1));
		} else {
			newTarget = mostHealth.get(0);
		}
		newTarget.addEffect(this);
		for (Action a : getOtherPlayer().getQueue()) {
			if (a.getType() == ActionCategoryEnum.ATTACK) {

				a.setTarget(newTarget);
			}
		}

		return true;
	}

	@Override
	public String getImageName() {

		return imageName;
	}
}
