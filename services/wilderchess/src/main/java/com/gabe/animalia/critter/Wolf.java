package com.gabe.animalia.critter;
import java.io.IOException;

import com.gabe.animalia.ability.wolf.Execute;
import com.gabe.animalia.ability.wolf.Meld;
import com.gabe.animalia.ability.wolf.ShadowLunge;
import com.gabe.animalia.ability.wolf.Stab;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;


public class Wolf extends Critter{
	private static final int MAX_HEALTH = 110;
	private int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	private int energy = 100;
	boolean selected = false;
	private static Action action1 = new ShadowLunge(null, null, null, null);
	private static Action action2 = new Meld(null, null, null, null);
	private static Action action3 = new Stab(null, null, null, null);
	private static Action action4 = new Execute(null, null, null, null);
	private static Action [] abilities = {action1, action2, action3, action4};
	private static String passiveName = "Prowl";
	private static String passiveCritterDescription = "Use Meld on Unbench.";
	private static String passiveSelectDescription = "Use Meld on Unbench.";
	public Wolf (String name, Square spot, String side, Player owner, Player opponent) {
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
	public void unbenchEffect(Player player, Player otherPlayer){
		System.out.println("wolf unbench");
		Action meld = new Meld(this,this,player,otherPlayer);
		meld.perform();

			otherPlayer.sendString(
					"animate," + "stealth" + "," + getName() + ","
							+ getSide() +"," + 0 + "|");


		this.setEnergy(this.getEnergy() + meld.getEnergyCost());
	}
}
