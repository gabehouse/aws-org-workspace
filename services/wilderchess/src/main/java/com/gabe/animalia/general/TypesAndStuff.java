package com.gabe.animalia.general;

public class TypesAndStuff {
	public String [] allEffects = {"Fireball", "Ignite", "Meld", "Inferno", "Shell Stance", "Bray", "Toss", "Shell Stance", "Shield Strike", "Divine Intervention", "Pounce", "Defend", "Stout Shield"};
	public String [] burns = {"Fireball", "Ignite"};
	public String [] bleeds = {};
	public String [] poisons = {};
	public String [] moves = {"move", "Pounce", "Charge"};
	public String [] stealths = {"Meld"};
	public String [] channellings = {"Inferno"};
	public String [] suppresses = {"Shell Stance", "Bray", "Toss"};
	public String [] immobilizes = {""};
	public String [] blocks = {"Shell Stance", "Shield Strike", "Divine Intervention", "Pounce", "Defend", "Stout Shield"};




	public TypesAndStuff () {

	}
	public String [] getAllEffects () {
		return allEffects;
	}

	public boolean isSuppress(Action action) {
		for (String s : suppresses) {
			if (s.equals(action.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}
	public boolean isImmobilize(Action action) {
		for (String i : immobilizes) {
			if (i.equals(action.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}
	public boolean isBlock(Action action) {
		for (String i : blocks) {
			if (i.equals(action.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}
	public boolean isMove(Action action) {
		for (String m : moves) {
			if (m.equals(action.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;
	}

	public boolean isBurn(Action effect) {
		for (String b : burns) {
			if (b.equals(effect.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;
	}

	public boolean isBleed(Action effect) {
		for (String b : bleeds) {
			if (b.equals(effect.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}
	public boolean isPoison(Action effect) {
		for (String b : poisons) {
			if (b.equals(effect.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}

	public boolean isStealth(Action effect) {
		for (String b : stealths) {
			if (b.equals(effect.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}

	public boolean ischannelling(Action effect) {
		for (String b : channellings) {
			if (b.equals(effect.getName().replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}





}
