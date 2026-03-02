package com.gabe.animalia.critter;
import com.gabe.animalia.ability.donkey.Bray;
import com.gabe.animalia.ability.donkey.DonkeyBlues;
import com.gabe.animalia.ability.donkey.Invigorate;
import com.gabe.animalia.ability.donkey.Kick;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;


public class Donkey extends Critter{
	private static final int MAX_HEALTH = 130;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	private static Action action1 = new Bray(null, null, null, null);
	private static Action action2 = new DonkeyBlues(null, null, null, null);
	private static Action action3 = new Invigorate(null, null, null, null);
	private static Action action4 = new Kick(null, null, null, null);
	private static Action [] abilities = {action1, action2, action3, action4};
	private static String passiveName = "Field March";
	private static String passiveCritterDescription = "Restore 10 energy to \nthe team at the end of each\n turn while Donkey is benched.";
	private static String passiveSelectDescription = "Restore 10 energy to the team at the end of \neach turn while Donkey is benched.";
	int energy = 100;
	boolean selected = false;

	public Donkey (String name, Square spot, String side, Player owner, Player opponent) {
		super(name, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner, opponent, passiveCritterDescription, passiveName, passiveSelectDescription);
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
		for (Critter f : player.getCritters()) {
			if (!f.isBenched()) {
				f.setEnergy(f.getEnergy() + 10);
			}
		}
	}
}
