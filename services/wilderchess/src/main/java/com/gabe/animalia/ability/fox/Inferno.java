package com.gabe.animalia.ability.fox;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Inferno extends Action {
	private String indicatorMessage;

	public Inferno(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.INFERNO);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		this.target = subject;
		player.getQueue().add(this);
		indicatorMessage = "indicate|";
		// for (Critter f : otherPlayer.getCritters()) {
		// subject.getIndicatedAttacks().add(f);
		// indicatorMessage += "attack," + f.getName() + "," +f.getSide() + "|";
		// }
	}

	@Override
	public boolean customPerform() {
		subject.addEffect(this);
		return true;
	}

	@Override
	public String onTick(Critter critter) {
		// String log = "";
		for (Critter f : otherPlayer.getCritters()) {

			if ((f.getSpot().getInfront() != null && f.getSpot().getInfront().isOccupied()) || f.isBenched()) {

				continue;

			}
			dealStandardDamage(f, 0);

		}
		return "Channelling: " + damage + subject.getBonusDmg() + " HP";
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
