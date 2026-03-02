package com.gabe.animalia.critter;

import com.gabe.animalia.ability.turtle.Reflect;
import com.gabe.animalia.ability.turtle.ShellStance;
import com.gabe.animalia.ability.turtle.Smack;
import com.gabe.animalia.ability.turtle.Soothe;
import com.gabe.animalia.ability.turtle.StoutShield;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;


public class Turtle extends Critter{
	private static final int MAX_HEALTH = 150;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	int energy = 100;
	boolean selected = false;
	private static Action action1 = new StoutShield(null, null, null, null);
	private static Action action2 = new ShellStance(null, null, null, null);
	private static Action action3 = new Smack(null, null, null, null);
	private static Action action4 = new Soothe(null, null, null, null);
	private static Action [] abilities = {action1, action2, action3, action4};
	private static String passiveName = "Reflect";
	private static String passiveCritterDescription = "Deal all damage \ndirected to Turtle on unbench \nback to the attacking fighters.";
	private static String passiveSelectDescription = "Deal all damage directed to Turtle on unbench \nback to the attacking fighters.";

	public Turtle (String name, Square spot, String side, Player owner, Player opponent) {
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
		Reflect passive = new Reflect(this, this, player, otherPlayer);
		passive.perform();
	}
}
