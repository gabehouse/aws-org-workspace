package com.gabe.animalia.critter;
import com.gabe.animalia.ability.fox.Fireball;
import com.gabe.animalia.ability.fox.Ignite;
import com.gabe.animalia.ability.fox.Inferno;
import com.gabe.animalia.ability.fox.Stoke;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;


public class Fox extends Critter{
	private static final int MAX_HEALTH = 90;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	int energy = 100;
	private int rechargeStacks = 0;
	boolean selected = false;
	private static Action action1 = new Fireball(null, null, null, null);
	private static Action action2 = new Ignite(null, null, null, null);
	private static Action action3 = new Stoke(null, null, null, null);
	private static Action action4 = new Inferno(null, null, null, null);
	private static Action [] abilities = {action1, action2, action3, action4};
	private static String passiveName = "Singe";
	private static String passiveCritterDescription = "Deal 10 damage \nto each enemy fighter on unbench.";
	private static String passiveSelectDescription = "Deal 10 damage to each enemy fighte r on \nunbench.";
	public Fox (String name, Square spot, String side, Player owner, Player opponent) {
		super(name, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner, opponent, passiveCritterDescription, passiveName, passiveSelectDescription);
	}

	@Override
	public void onHit(Player attacking, Player hit, Action action) {
		if (this.isAffectedBy("Inferno")) {
			this.removeEffect("Inferno");
		}
		super.onHit(attacking, hit, action);

	}

	@Override
	public void onMove(Player player, Player otherPlayer) {
		if (this.isAffectedBy("Inferno")) {
			this.removeEffect("Inferno");
		}
	}
	@Override
	public void onBlock(Player blocked, Player blocking) {
		super.onBlock(blocked, blocking);
	}
	public int getRechargeStacks() {
		return rechargeStacks;
	}

	public void setRechargeStacks(int rechargeStacks) {
		this.rechargeStacks = rechargeStacks;
	}
	@Override
	public void unbenchEffect(Player player, Player otherPlayer) {
		System.out.println("fox bench effect");
		for (Critter f : otherPlayer.getCritters()) {
			f.setHealth(f.getHealth() - 10);
		}
	}

}
