package com.gabe.animalia.critter;


import com.gabe.animalia.ability.newt.ContagionToss;
import com.gabe.animalia.ability.newt.Newtbrew;
import com.gabe.animalia.ability.newt.Taxidermy;
import com.gabe.animalia.ability.newt.Tranquilizer;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

public class Newt extends Critter {
	private static final int MAX_HEALTH = 100;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	private static Action[] abilities = {new Tranquilizer(null, null, null, null), new Newtbrew(null, null, null, null), new ContagionToss(null, null, null, null), new Taxidermy(null, null, null, null)};
	private static String passiveName = "Home cooking";
	private static String passiveCritterDescription = "Use Newtbrew \non Newt on Unbench.";
	private static String passiveSelectDescription = "Use Newtbrew on Newt on \nUnbench.";
	int energy = 100;
	boolean selected = false;
	final boolean comingSoon = false;

	public Newt(String name, Square spot, String side, Player owner,
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
		Taxidermy p = new Taxidermy(this,this,player,otherPlayer);
		this.getEffects().add(p);
		p.showEffect();
		this.setPlasterved(true);
	}
	@Override
	public final boolean isComingSoon() {
		return comingSoon;
	}
}
