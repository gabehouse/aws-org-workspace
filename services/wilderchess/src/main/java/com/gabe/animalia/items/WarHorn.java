package com.gabe.animalia.items;

import java.util.ArrayList;
import java.util.Random;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

public class WarHorn extends Item {
	private String imageName = "warhorn.png";
	private Action interruptedAbility = null;

	public WarHorn(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.WAR_HORN);
	}

	@Override
	public boolean customPerform() {
		Random rng = new Random();
		ArrayList<Integer> chosen = new ArrayList<Integer>();
		if (getOtherPlayer().getPerformingAbility() != null) {
			interruptedAbility = getOtherPlayer().getPerformingAbility();
			interruptedAbility.setInterrupted(true, interruptedAbility.getSubject(), getPlayer(), getOtherPlayer());
			getOtherPlayer().getPerformingAbility().setPerformable(false);
		}

		for (Critter f : getOtherPlayer().getCritters()) {
			String str = "";
			if (f.isBenched()) {
				str = f.getSpot().getName();
			} else {

				int n = rng.nextInt(5);
				while (chosen.contains(n)) {
					n = rng.nextInt(5);
				}
				chosen.add(n);

				Square newSpot = getOtherPlayer().getTopBack();
				if (n == 0) {
					newSpot = getOtherPlayer().getTopBack();
				} else if (n == 1) {
					newSpot = getOtherPlayer().getTopFront();
				} else if (n == 2) {
					newSpot = getOtherPlayer().getMiddleBack();
				} else if (n == 3) {
					newSpot = getOtherPlayer().getMiddleFront();
				} else if (n == 4) {
					newSpot = getOtherPlayer().getBottomBack();
				} else if (n == 5) {
					newSpot = getOtherPlayer().getBottomFront();
				}
				f.getSpot().setPlannedMove(false);
				f.setSpot(newSpot);
				f.getSpot().setCritter(f);
				f.setTempSpot(f.getSpot());
				f.getSpot().setPlannedMove(true);
				str = newSpot.getName();

			}
		}

		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();

		// Row 1: The Benched Critter
		for (Critter f : getOtherPlayer().getCritters()) {
			sb.append(buildManifestRow(
					"MOVE", "MOVE", ActionCategoryEnum.MOVE, f,
					"square", f.getSpot().getName(), f.getSide(), -1, -1,
					startTime, "none", f.getFullEffectSnapshot(), "none"));
			sb.append("|");

		}

		return sb.toString();
	}

	@Override
	public String getImageName() {

		return imageName;
	}

}
