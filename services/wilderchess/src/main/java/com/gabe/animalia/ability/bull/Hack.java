package com.gabe.animalia.ability.bull;

import java.util.Arrays;
import java.util.stream.Stream;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Hack extends Action {
	private String indicatorMessage;
	private int damageToDeal = 0;

	public Hack(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.HACK);
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

		t.addEffect(this);
		if (this.getStacks() == 1) {
			// Combine both arrays into one stream
			Stream.concat(Arrays.stream(player.getCritters()),
					Arrays.stream(otherPlayer.getCritters()))
					.filter(c -> !c.equals(t))
					.forEach(c -> c.removeEffect(this.actionData, getSubject().getId()));
		}

		int stackBonus = this.statusValue * (this.getStacks() - 1);
		dealStandardDamage(t, stackBonus);

		subject.removeEffect(ActionEnum.RUNNING_START);
		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedAttacks().remove(target);

	}

}
