package com.gabe.animalia.ability.fox;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Fireball extends Action {

	private String indicatorMessage;

	public Fireball(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.FIREBALL);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayOptions() {
		String str = "";
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				System.out.println(f.isBenched());
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}
		System.out.println(str);

		player.sendString("option,attack," + name + "," + str);

	}

	@Override
	public void init() {
		subject.getIndicatedAttacks().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide();

		player.sendString("option,remove," + name);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;
		int stokeStacks = 0;
		if (subject.hasEffect(ActionEnum.STOKE)) {
			stokeStacks = subject.getEffect(ActionEnum.STOKE).getStacks();
		}
		dealStandardDamage(t, 20 * stokeStacks);
		subject.removeEffect(ActionEnum.STOKE);
		t.addEffect(this);
		return true;
	}

	@Override
	public String onTick(Critter critter) {
		critter.setHealth(critter.getHealth() - this.statusValue);
		return "Burn: -" + this.statusValue + " HP";
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
