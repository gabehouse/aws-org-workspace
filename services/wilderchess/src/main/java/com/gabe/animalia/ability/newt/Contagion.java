package com.gabe.animalia.ability.newt;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Targetable;

public class Contagion extends Action {
	private String indicatorMessage;
	private boolean justSpread = false;

	public Contagion(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.CONTAGION);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void displayOptions() {
		String str = "";
		for (Critter f : otherPlayer.getCritters()) {
			if (!f.isBenched()) {
				str += f.getName() + "," + f.getSide() + ",";
			}
		}

		player.sendString("option,support," + name + "," + str);

	}

	@Override
	public void init() {
		subject.getIndicatedSupports().add(target);
		player.getQueue().add(this);
		indicatorMessage = "indicate|support," + target.getName() + "," + target.getSide();

		player.sendString("option,remove," + name);

	}

	@Override
	public boolean customPerform() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return false;

		t.addEffect(this);
		return true;
	}

	@Override
	public String onTick(Critter critter) {
		Critter host = getTargetAsCritter();
		if (host == null || !host.isAlive())
			return "";

		// 2. Damage/Energy
		host.setHealth(host.getHealth() - 30);
		host.spendEnergy(30);

		return host.getName() + " lost 30 health and energy to fever.";
	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);

	}

	public boolean isJustSpread() {
		return justSpread;
	}

	public void setJustSpread(boolean justSpread) {
		this.justSpread = justSpread;
	}

}
