package com.gabe.animalia.ability.dove;

import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Hymn extends Action {

	private String indicatorMessage;

	public Hymn(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.HYMN);
	}

	@Override
	public void init() {
		for (Action a : subject.getEffects()) {
			a.initialEffect(this);
		}
		player.getQueue().add(this);
		if (target.getSide().equals(subject.getSide())) {
			subject.getIndicatedSupports().add(target);
			indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();
			this.category = ActionCategoryEnum.SUPPORT;
		} else {
			subject.getIndicatedAttacks().add(target);
			indicatorMessage = "indicate|attack," + target.getName() + "," + target.getSide();
			this.category = ActionCategoryEnum.ATTACK;
		}

		player.sendString("option,remove," + name);

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
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

		player.sendString("option,support," + name + "," + str);

		str = "";
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

		player.sendString("option,attack," + name + "," + str);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		int bonusDmg = -this.statusValue * ((Dove) subject).getGood();

		if (t.getSide().equals(subject.getSide())) {
			if (((Dove) subject).getGood() < 5) {
				((Dove) subject).setGood(((Dove) subject).getGood() + 1);
			}
			restoreHealth(t, this.healing - bonusDmg);

		} else {
			if (((Dove) subject).getGood() > -5) {
				((Dove) subject).setGood(((Dove) subject).getGood() - 1);
			}
			dealStandardDamage(t, bonusDmg);
		}
		if (((Dove) subject).getGood() > 0) {
			effectDescription = "good: " + ((Dove) subject).getGood()
					+ "\nHymn heal increased by "
					+ ((Dove) subject).getGood() * 3
					+ "\nHymn damage decreased by "
					+ ((Dove) subject).getGood() * 3;
		} else {
			effectDescription = "evil: "
					+ Math.abs(((Dove) subject).getGood())
					+ "\nHymn heal decreased by "
					+ Math.abs(((Dove) subject).getGood()) * 3
					+ "\nHymn damage increased by "
					+ Math.abs(((Dove) subject).getGood()) * 3;
		}

		Action prev = subject.getEffect(this.actionData, subject.getId());
		if (prev == null) {
			subject.addEffect(this);
		} else {

			prev.setStacks(Math.abs(((Dove) subject).getGood()));
			prev.setDuration(this.getDuration());
			if (prev.getStacks() == 0) {
				subject.removeEffect(this.actionData, prev.getSubject().getId());
			}
		}

		otherPlayer.sendString(
				"moveQueue,_remove");
		player.sendString(
				"moveQueue,_remove");

		player.sendString("moveQueue$" + player.iconString(player.getQueue()) + "$380");
		player.sendString(
				"moveQueue,_"
						+ otherPlayer.iconString(otherPlayer.getQueue()) + "$430");
		otherPlayer.sendString(
				"moveQueue,_"
						+ player.iconString(player.getQueue()) + "$430");
		otherPlayer.sendString(
				"moveQueue,_"
						+ otherPlayer.iconString(otherPlayer.getQueue()) + "$380");

		return true;
	}

	@Override
	public void delete() {
		if (this.category.equals(ActionCategoryEnum.SUPPORT)) {
			subject.getIndicatedSupports().remove(target);
		} else {
			subject.getIndicatedAttacks().remove(target);
		}

	}

}
