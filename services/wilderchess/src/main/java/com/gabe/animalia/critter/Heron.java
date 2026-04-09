package com.gabe.animalia.critter;

import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

public class Heron extends Critter {
	private static final int MAX_HEALTH = 130;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;

	private static Action[] abilities = {};
	private static String passiveName = "Field March";
	private static String passiveCritterDescription = "Restore 10 \nenergy to the team at the \nend of each turn while Donkey is \nbenched.";
	private static String passiveSelectDescription = "Restore 10 energy to the team at the end of \neach turn while Donkey is benched.";
	int energy = 100;
	boolean selected = false;
	final boolean comingSoon = true;

	public Heron(String name, Square spot, String side, Player owner,
			Player opponent) {
		super(name, FighterType.HERON, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner,
				opponent, passiveCritterDescription, passiveName,
				passiveSelectDescription);
	}

	@Override
	public final boolean isComingSoon() {
		return comingSoon;
	}
}
