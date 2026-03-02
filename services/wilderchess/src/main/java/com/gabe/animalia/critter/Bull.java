package com.gabe.animalia.critter;

import com.gabe.animalia.ability.bull.Charge;
import com.gabe.animalia.ability.bull.Hack;
import com.gabe.animalia.ability.bull.RunningStart;
import com.gabe.animalia.ability.bull.ShieldStrike;
import com.gabe.animalia.ability.bull.Toss;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

public class Bull extends Critter {
	private static final int MAX_HEALTH = 170;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;

	private static Action[] abilities = {new ShieldStrike(null, null, null, null), new Hack(null, null, null, null), new Charge(null, null, null, null), new Toss(null, null, null, null)};
	private static String passiveName = "Running Start";
	private static String passiveCritterDescription = "Bull's attacks deal \n30 bonus damage if used immediately \nafter unbenching.";
	private static String passiveSelectDescription = "Bull's attacks deal 30 bonus damage if used \nimmediately after unbenching.";
	int energy = 100;
	boolean selected = false;
	final boolean comingSoon = false;

	public Bull(String name, Square spot, String side, Player owner,
			Player opponent) {
		super(name, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner,
				opponent, passiveCritterDescription, passiveName,
				passiveSelectDescription);
	}

	@Override
	public void onHit(Player attacking, Player hit, Action action) {
		super.onHit(attacking, hit, action);

	}

	@Override
	public void onAction(Player player, Player otherPlayer, Action action) {

		super.onHit(player, otherPlayer, action);

	}

	@Override
	public void onMove(Player player, Player otherPlayer) {

	}

	@Override
	public void onBlock(Player blocked, Player blocking) {
		super.onBlock(blocked, blocking);
	}

	@Override
	public void benchedEffect(Player player, Player otherPlayer) {

	}
	@Override
	public void unbenchEffect(Player player, Player otherPlayer) {
		RunningStart passive = new RunningStart(this, this, player, otherPlayer);
		passive.perform();

	}
	@Override
	public final boolean isComingSoon() {
		return comingSoon;
	}
}
