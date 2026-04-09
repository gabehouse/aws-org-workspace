package com.gabe.animalia.ability.dove;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class MercyoftheDove extends Action {

	private String indicatorMessage;
	private String effectDescription = "20 dmg/turn";
	private Critter toRevive = null;
	private boolean used = false;

	public MercyoftheDove(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.MERCY);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		player.getQueue().add(this);
		indicatorMessage = "";

	}

	@Override
	public String onTick(Critter critter) {
		critter.setHealth(critter.getHealth() - this.statusValue);
		return "Burn: " + this.statusValue + " HP";
	}

	@Override
	public boolean customPerform() {

		if (((Dove) subject).getGood() == 5 && subject.getOwner().getDeadCritters().size() >= 1) {

			Random rng = new Random();
			ArrayList<Square> possibleSpots = new ArrayList<Square>();
			for (Square s : subject.getOwner().getSquares()) {
				if (!s.isOccupied() && !s.getName().contains("Bench")) {
					possibleSpots.add(s);
				}

			}
			Square newSpot = possibleSpots.get(rng.nextInt(possibleSpots.size()));

			if (subject.getOwner().getDeadCritters().size() == 1) {
				toRevive = subject.getOwner().getDeadCritters().get(0);
			} else if (subject.getOwner().getDeadCritters().size() == 2) {
				toRevive = subject.getOwner().getDeadCritters().get(rng.nextInt(2));
			} else if (subject.getOwner().getDeadCritters().size() == 3) {
				toRevive = subject.getOwner().getDeadCritters().get(rng.nextInt(3));
			} else {
				used = true;
				return true;
			}
			toRevive.setSpot(newSpot);
			newSpot.setCritter(toRevive);
			toRevive.setTempSpot(newSpot);
			toRevive.setHealth(toRevive.getMaxHealth());
			toRevive.setEnergy(

					toRevive.getMaxEnergy());
			newSpot.setPlannedMove(true);
			toRevive.setAlive(true);
			player.getDeadCritters().remove(toRevive);
			Critter[] newCritters = new Critter[player.getCritters().length + 1];
			for (int i = 0; i < player.getCritters().length; i++) {
				newCritters[i] = player.getCritters()[i];
			}
			newCritters[player.getCritters().length] = toRevive;
			player.setCritters(newCritters);
			((Dove) subject).setGood(0);

			// toRevive.markDirty();
		} else if (((Dove) subject).getGood() == -5) {
			for (Critter f : otherPlayer.getCritters()) {
				MercyoftheDove m = new MercyoftheDove(f, target, player, otherPlayer);
				f.getEffects().add(m);
			}
			((Dove) subject).setGood(0);
			used = true;
		}
		((Dove) subject).markDirty();
		used = true;
		return true;
	}

	@Override
	public String getManifestData(double startTime) {
		StringBuilder sb = new StringBuilder();
		if (toRevive != null) {
			System.out.println("MERCY OF THE DOVE MANUIFST");
			sb.append(buildManifestRow(
					"basic", getName(), this.category, subject,
					"critter", toRevive.getName(), toRevive.getSide(), toRevive.getHealth(), toRevive.getMaxHealth(),
					startTime, getLogStr(), subject.getFullEffectSnapshot(), toRevive.getFullEffectSnapshot()));
			sb.append("|");
			sb.append(buildManifestRow(
					"MOVE", "MOVE", ActionCategoryEnum.MOVE, toRevive,
					"square", toRevive.getSpot().getName(), toRevive.getSide(), -1, -1,
					startTime, "none", "none", "none"));
		} else if (used) {
			for (Critter f : otherPlayer.getCritters()) {
				f.markDirty();
			}
		}

		return sb.toString();
	}

	@Override
	public String getLogStr() {
		return this.getName() + " revived " + toRevive.getName() + ".";
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
