package com.gabe.animalia.ability.turtle;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class ShellStance extends Action {
	private String indicatorMessage;

	public ShellStance(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.SHELL_STANCE);
	}

	@Override
	public void modify() {
		for (Action a : subject.getEffects()) {
			a.initialEffect(this);
		}
	}

	@Override
	public void init() {
		subject.getIndicatedBlocks().add(subject);
		player.getQueue().add(this);
		indicatorMessage = "indicate|block," + subject.getName() + "," + subject.getSide();
		target = subject;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		if (!subject.hasEffect(this.actionData)) {

			t.raiseDefence(block);

			t.addEffect(this);
			showEffect();
		} else {
			t.removeEffect(this.actionData);
			subject.lowerDefence(block);

		}

		subject.sendStats(player, otherPlayer);
		return true;
	}

	@Override
	public void delete() {
		subject.getIndicatedBlocks().remove(subject);

	}

}
