package com.gabe.animalia.critter;

import java.util.Random;

import com.gabe.animalia.ability.dove.Intervention;
import com.gabe.animalia.ability.dove.Gust;
import com.gabe.animalia.ability.dove.Hymn;
import com.gabe.animalia.ability.dove.MercyoftheDove;
import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Dove extends Critter {
	private static final int MAX_HEALTH = 100;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	int energy = 100;
	boolean selected = false;
	private int good = 0;
	private static Action action1 = new Hymn(null, null, null, null);
	private static Action action2 = new MercyoftheDove(null, null, null, null);
	private static Action action3 = new Gust(null, null, null, null);
	private static Action action4 = new Intervention(null, null, null, null);
	private static Action[] abilities = { action1, action2, action3, action4 };
	private static String passiveName = "Seraphic Embrace";
	private static String passiveCritterDescription = "Heal 40 to the fighter Dove is switching in for.";
	private static String passiveSelectDescription = "Heal 40 to the fighter Dove is switching in for.";
	private static Player owner;
	private static Player opponent;

	public Dove(String name, Square spot, String side, Player owner, Player opponent) {
		super(name, FighterType.DOVE, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner, opponent,
				passiveCritterDescription, passiveName, passiveSelectDescription);
		this.owner = owner;
		this.opponent = opponent;
	}

	public int getGood() {
		return good;
	}

	public void setGood(int good) {
		this.good = good;
	}

	@Override
	public void unbenchEffect(Player player, Player otherPlayer) {
		for (Critter f : player.getCritters()) {
			if (f.getSpot().getName().contains("Bench")) {
				f.setHealth(f.getHealth() + 40);
				if (f.getHealth() > f.getMaxHealth()) {
					f.setHealth(f.getMaxHealth());
				}
			}
		}
	}
	// @Override
	// public Action botChooseAction(int timeLeft) {
	// Random r = new Random();
	// Action a = null;
	// ArrayList<Action> possibleActions = new ArrayList<Action>();
	// ArrayList<Integer> possibleActionWeights = new ArrayList<Integer>();
	// boolean isATeammateDead = false;

	// for (Critter c : owner.getCritters()) {
	// if (!c.isAlive()) {
	// isATeammateDead = true;
	// break;
	// }
	// }
	// if (energy >= 30 && health <= 30 && action2.getTimeCost() <= timeLeft) {
	// a = action4.getNew();
	// a.setPriority(10);
	// return a;
	// }
	// if (energy >= 25 && action2.getTimeCost() <= timeLeft && good >= 5 &&
	// isATeammateDead) {
	// a = action2.getNew();
	// a.setPriority(9);
	// return a;
	// }
	// else if (energy >= 25 && action2.getTimeCost() < timeLeft && good <= -5) {
	// a = action2.getNew();
	// a.setPriority(9);
	// return a;
	// }

	// }
}
