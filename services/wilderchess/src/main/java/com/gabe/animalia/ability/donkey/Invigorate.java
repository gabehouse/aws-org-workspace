package com.gabe.animalia.ability.donkey;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Invigorate extends Action {

	private int bonusAttack = 15;
	private String indicatorMessage;

	public Invigorate(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.INVIGORATE);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		player.getQueue().add(this);
		indicatorMessage = "indicate|";
		for (Critter f : player.getCritters()) {
			subject.getIndicatedSupports().add(f);
			indicatorMessage += "support," + f.getName() + "," + f.getSide() + "|";
		}

	}

	@Override
	public boolean customPerform() {

		subject.getIndicatedSupports().add(target);

		for (Critter f : player.getCritters()) {
			f.raiseBonusDmg(bonusAttack);
			f.addEffect(
					new Invigorate(subject, f, player, otherPlayer));
		}

		return false;
	}

	@Override
	public void endEffect(Critter critter) {

		critter.lowerBonusDmg(bonusAttack);

	}

	@Override
	public void delete() {
		for (Critter f : player.getCritters()) {
			subject.getIndicatedSupports().remove(f);
		}
	}

}
