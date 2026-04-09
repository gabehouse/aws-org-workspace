package com.gabe.animalia.ability.newt;

import java.io.IOException;
import java.util.Random;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;

public class Tranquilizer extends Action {
	private String indicatorMessage;
	private String actionStr = "";

	public Tranquilizer(Critter subject, Targetable target, Player player, Player otherPlayer) {
		super(subject, target, player, otherPlayer, ActionEnum.TRANQUILIZER);
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;
	}

	@Override
	public void init() {
		target = subject;
		player.getQueue().add(this);
	}

	@Override
	public boolean customPerform() {

		Random r = new Random();
		int row = r.nextInt(3);
		System.out.println("row = " + row);
		Square s = null;
		if (row == 0) {
			s = otherPlayer.identifySquare(otherPlayer.getSide() + "TopFront");
		} else if (row == 1) {
			s = otherPlayer.identifySquare(otherPlayer.getSide() + "MiddleFront");

		} else if (row == 2) {
			s = otherPlayer.identifySquare(otherPlayer.getSide() + "BottomFront");
		}
		if (s.getCritter() != null) {
			target = s.getCritter();
		} else {
			if (s.getBehind().getCritter() != null) {
				target = s.getBehind().getCritter();
			} else {
				this.logStr += "Random row was empty.";
				target = null;
			}
		}
		if (target != null) {
			Critter t = (Critter) target;
			dealStandardDamage(t, 0);
			t.restoreEnergy(this.energyRestore);

		}
		return true;

	}

}
