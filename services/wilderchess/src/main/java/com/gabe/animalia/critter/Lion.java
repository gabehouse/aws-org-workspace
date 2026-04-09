package com.gabe.animalia.critter;

import com.gabe.animalia.ability.lion.BenchCoach;
import com.gabe.animalia.ability.lion.Defend;
import com.gabe.animalia.ability.lion.LickWounds;
import com.gabe.animalia.ability.lion.Pounce;
import com.gabe.animalia.ability.lion.Slash;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;

public class Lion extends Critter {
	private static final int MAX_HEALTH = 120;
	int health = MAX_HEALTH;
	private static final int MAX_FATIGUE = 100;
	int energy = 100;
	boolean selected = false;
	private static Action action1 = new Slash(null, null, null, null);
	private static Action action2 = new LickWounds(null, null, null, null);
	private static Action action3 = new Pounce(null, null, null, null);
	private static Action action4 = new Defend(null, null, null, null);
	private static Action[] abilities = { action1, action2, action3, action4 };
	private static String passiveName = "Bench Coach";
	private static String passiveCritterDescription = "Increase allied fighters bonus damage by 10 while Lion is benched.";
	private static String passiveSelectDescription = "Increase allied fighters bonus damage by 10 \nwhile Lion is benched.";

	public Lion(String name, Square spot, String side, Player owner, Player opponent) {
		super(name, FighterType.LION, spot, MAX_HEALTH, MAX_FATIGUE, abilities, side, owner, opponent,
				passiveCritterDescription, passiveName, passiveSelectDescription);
	}

	@Override
	public void benchEffect(Player player, Player otherPlayer) {
		for (Critter f : player.getCritters()) {
			if (!f.equals(this)) {
				f.setBonusDmg(f.getBonusDmg() + 10);
				f.addEffect(new BenchCoach(this, f, player, otherPlayer));
			}
		}
	}

	@Override
	public void unbenchEffect(Player player, Player otherPlayer) {
		for (Critter f : player.getCritters()) {
			if (!f.equals(this)) {
				f.removeEffect(ActionEnum.BENCH_COACH);
			}
		}
		// for (Critter f : player.getCritters()) {
		// f.setBonusDmg(f.getBonusDmg() - 10);
		// }
	}

}
