package com.gabe.animalia.general;
import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.ability.Move;
import com.gabe.animalia.ml.dtos.FighterStateDTO;


/**
 * @author Gabe House
 * @version 6/7/2015
 * Parent class to all critters.
 */
public class Critter extends Targetable {
	private boolean alive = true;
	private String name;
	private Square spot;
	private Square tempSpot;
	private ArrayList<Action> effects = new ArrayList<Action>();
	private ArrayList<Action> moves = new ArrayList<Action>();
	private ArrayList<Targetable> indicatedMoves = new ArrayList<Targetable>();
	private ArrayList<Targetable> indicatedAttacks = new ArrayList<Targetable>();
	private ArrayList<Targetable> indicatedSupports = new ArrayList<Targetable>();
	private ArrayList<Targetable> indicatedBlocks = new ArrayList<Targetable>();
	private int maxHealth;
	private int maxEnergy;
	private int health;
	private int defence = 0;
	private String passiveCritterDescription;
	private String passiveSelectDescription;
	private String passiveName;
	private boolean resting = true;
	private Action [] abilities;
	private String side;
	private Player owner;
	private Player opponent;
	private int bonusDmg = 0;
	private int energy = 100;
	private boolean benched = false;
	private int hackStacks = 0;
	private boolean plasterved = false;
	// 0 through 7 for the 8 fighters in game
	private String id;



	private boolean moved = false;

	/**
	 * @param name
	 *            name of the critter
	 * @param spot
	 *            spot the critter begins on
	 * @param maxHealth
	 *            maximum health of the critter
	 * @param maxEnergy
	 *            maximum energy limit of the critter
	 * @param abilities
	 *            the critter's abilities
	 * @param side
	 *            the side the critter is on, left or right
	 */
	public Critter(String name, Square spot, int maxHealth, int maxEnergy,
			Action[] abilities, String side, Player owner, Player opponent,
			String passiveCritterDescription, String passiveName, String passiveSelectDescription) {
		this.name = name;
		this.spot = spot;
		this.spot.setOccupied(true);
		this.spot.setPlannedMove(true);
		this.tempSpot = spot;
		this.maxHealth = maxHealth;
		this.maxEnergy = maxEnergy;
		this.health = maxHealth;
		this.abilities = abilities;
		this.side = side;
		this.owner = owner;
		this.opponent = opponent;
		this.passiveSelectDescription = passiveSelectDescription;
		this.passiveCritterDescription = passiveCritterDescription;
		this.passiveName = passiveName;
	}

	public FighterStateDTO toFighterStateDTO() {
		FighterStateDTO fdto = new FighterStateDTO(id, name, health, maxHealth, energy, spot.getName(), bonusDmg);
		return fdto;
	}

	// public Action botChooseAction() {
	// 	Action a;
	// 	return a;
	// }
	/**
	 * Causes all over time effects on a critter to do what
	 * it should do.
	 */
	public void triggerEffects() {

		Action [] effectArr = new Action [effects.size()];
		effectArr = effects.toArray(effectArr);
		for (Action a : effectArr) {
			System.out.println("trigger effect " + a.getName() + ": " + a.getDuration());
			if (a.getDuration() > 0) {
				a.overTimeEffect(this);

			}
		}
	}
	public double move(Square spot, double timeUsed) {
		Move move = new Move(this, spot, owner, opponent);
		if (timeUsed + move.getTimeCost() > 10) return 0;
		if (getPossibleMoves().contains(spot)) {

			move.init();
			return move.getTimeCost();
		} else  {
			if (timeUsed + move.getTimeCost()*2 > 10) return 0;
			for (Square s : getPossibleMoves()) {
				if (!s.isPlannedMove() && !spot.isPlannedMove() && s.getSurroundingSquares().contains(spot)) {
					Move move1 = new Move(this, s, owner, opponent);
					move1.init();
					move.init();
					return move.getTimeCost() * 2;
				}
			}
		}
		return 0;
	}

	public Critter getInfront() {
		Square infrontSpot = this.getTempSpot().getInfront();
		if (infrontSpot != null ) {
			return infrontSpot.getCritter();
		} else {
			return null;
		}
	}
	/**
	 * Lowers the duration of all effects on the critter.
	 */
	public void effectDuration() {
		Action [] effectArr = new Action [effects.size()];
		effectArr = effects.toArray(effectArr);
		for (Action a : effectArr) {
			if (a.getDuration() > 0) {
				a.setDuration(a.getDuration() - 1);
			}
			if (a.getDuration() == 0){
				a.endEffect(this);
				effects.remove(a);
			}
		}
	}

	/**
	 * Checks if the critter is able to perform actions
	 * @return true if the figher is okay and false if he is not
	 */
	public boolean canUseAction() {
		for (Action a : getEffects()) {
			for (String s : a.getStatusType().split(",")) {
				if (s.equals("suppress")) {
					return false;
				}
			}
		}

		return true;

	}
	public boolean canMove() {
		for (Action a : getEffects()) {
			for (String s : a.getStatusType().split(",")) {
				if (s.equals("immobilize") || s.equals("suppress")) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * Critters have their own onHit method which has an effect
	 * when the critter is hit by a direct attack
	 * @return
	 */
	public void onHit(Player attacking, Player hit, Action action) {
		if (attacking.getMorale() < 10) {
			attacking.setMorale(attacking.getMorale() + 1);
			hit.setMorale(hit.getMorale() - 1);
		}
		for (int i = 0; i < this.getEffects().size(); i++) {
			if (this.getEffects().get(i).getStatusType().equals("channeling")) {
				this.removeEffect(this.getEffects().get(i).getName());
				i--;
			}
		}

			attacking.sendString("morale," + attacking.getMorale() + "," + attacking.getSide());
			hit.sendString("morale," + attacking.getMorale() + "," + attacking.getSide());

	}


	/**
	 * Critters have their own onHit method which has an effect
	 * when the critter moves
	 */
	public void onMove(Player player, Player otherPlayer) {

	}

	public void onBlock(Player blocked, Player blocking) {
		if (blocking.getMorale() < 10) {
			blocked.setMorale(blocked.getMorale() - 1);
			blocking.setMorale(blocking.getMorale() + 1);
		}
			blocked.sendString("morale," + blocked.getMorale() + "," + blocked.getSide());
			blocking.sendString("morale," + blocked.getMorale() + "," + blocked.getSide());
	}

	public void onAction(Player player, Player otherPlayer, Action action) {
		if (player.identifyCritter("Bull", player.getSide()) != null) {
			Critter bull = player.identifyCritter("Bull", player.getSide());
			if (bull.isAffectedBy("Running Start")) {
				if (!action.getSubject().equals(bull)) {
					bull.removeEffect("Running Start");
				}
			}
		}
		// System.out.println("aaaa" + action.getTargetType() + ", " + ((Critter)action.getTarget()).getSpot().getName());
		// if (action.getTargetType() == "critter" && ((Critter)action.getTarget()).getSpot().getName().contains("bench")) {
		// 	System.out.println("aaaaaaaaa");
		// 	action.setPerformable(false);
		// }

		if (action.getTargetType().equals("critter")) {
			Critter target = (Critter)action.getTarget();
			for (int i = 0; i < target.getEffects().size(); i++) {
				if (target.getEffects().get(i).getStatusType()
						.equals("invisible")&& !player.getSide().equals(target.getSide())
						&& action.performable()) {
					action.setPerformable(false);
				}
			}

		}
		for (int i = 0; i < this.getEffects().size(); i++) {
			if (this.getEffects().get(i).getStatusType().equals("channeling") && action.performable()) {
				this.removeEffect(this.getEffects().get(i).getName());
				i--;
			}
		}
		if (action.getTargetType().equals("critter") && action.getType().equals("attack")) {
			Critter target = (Critter)action.getTarget();
			if (target.isAffectedBy("Turtle Passive")) {
				action.setTarget(action.getSubject());
			}
		}

	}


	public String getIndicationStr() {
		String str = "";
		if (this.getIndicatedMoves().size() > 0) {
			for (int i = 0; i < this.getIndicatedMoves().size(); i++) {
				Targetable target = null;
				str += "move," + this.getIndicatedMoves().get(i).getName()
						+ "|";
			}
		}
		if (this.getIndicatedBlocks().size() > 0) {
			for (int i = 0; i < this.getIndicatedBlocks().size(); i++) {

				Targetable target = null;
				str += "block," + this.getIndicatedBlocks().get(i).getName()
						+ "," + this.getIndicatedBlocks().get(i).getSide()
						+ "|";
			}

		}
		if (this.getIndicatedSupports().size() > 0) {
			for (int i = 0; i < this.getIndicatedSupports().size(); i++) {
				Targetable target = null;
				if (this.getIndicatedSupports().get(i).getName().length() < 10) {
					str += "support,"
							+ this.getIndicatedSupports().get(i).getName()
							+ ","
							+ this.getIndicatedSupports().get(i).getSide()
							+ "|";
				} else {
					str += "supportspot,"
							+ this.getIndicatedSupports().get(i).getName()
							+ ","
							+ this.getIndicatedSupports().get(i).getSide()
							+ "|";
				}
			}
		}
		if (this.getIndicatedAttacks().size() > 0) {
			for (int i = 0; i < this.getIndicatedAttacks().size(); i++) {
				Targetable target = null;
				if (this.getIndicatedAttacks().get(i).getName().length() < 10) {
					str += "attack,"
							+ this.getIndicatedAttacks().get(i).getName()
							+ ","
							+ this.getIndicatedAttacks().get(i).getSide()
							+ "|";
				} else {
					str += "attackspot,"
							+ this.getIndicatedAttacks().get(i).getName()
							+ ","
							+ this.getIndicatedAttacks().get(i).getSide()
							+ "|";
				}

			}
		}
		return str;

	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getSide() {
		return side;
	}

	/**
	 * @param abilityName name of the ability
	 * @return true if the critter is affected by the ability, false if not
	 */
	public boolean isAffectedBy(String abilityName) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getName().replace("\n", "").equals(abilityName.replace("\n", ""))) {
				return true;
			}
		}
		return false;

	}

	public Action getEffect(String abilityName) {
		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getName().replace("\n", "").equals(abilityName.replace("\n", ""))) {
				return effects.get(i);
			}
		}
		return null;
	}

	/**
	 * @param abilityName name of the ability
	 * @return true if ability is successfully removed, false if not
	 */
	public boolean removeEffect(String abilityName) {

		for (int i = 0; i < effects.size(); i++) {
			if (effects.get(i).getName().replace("\n", "").equals(abilityName.replace("\n", ""))) {
				effects.get(i).endEffect(this);
				effects.remove(i);

				return true;
			}
		}
		return false;

	}


	public ArrayList<Action> getEffects() {
		return effects;
	}

	public Square getTempSpot() {
		return tempSpot;
	}
	public void setTempSpot(Square tempSpot) {
		this.tempSpot = tempSpot;
	}

	public void setSide(String side) {
		this.side = side;
	}

	public ArrayList<Targetable> getIndicatedAttacks() {
		return indicatedAttacks;
	}
	public ArrayList<Targetable> getIndicatedSupports() {
		return indicatedSupports;
	}
	public ArrayList<Targetable> getIndicatedBlocks() {
		return indicatedBlocks;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public int getMaxEnergy() {
		return maxEnergy;
	}

	public void setMaxEnergy(int maxEnergy) {
		this.maxEnergy = maxEnergy;
	}

	public int getBonusDmg() {
		return bonusDmg;
	}


	public void setBonusDmg(int bonusDmg) {
		this.bonusDmg = bonusDmg;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		System.out.println("sethealth " + health);
		this.health = health;
	}

	public int getEnergy() {
		return energy;
	}

	public void setEnergy(int energy) {
		if (energy > maxEnergy) {
			this.energy = maxEnergy;
		} else if (energy < 0) {
			this.energy = 0;
		} else {
			this.energy = energy;
		}
	}

	@Override
	public String getName() {
		return name;
	}
	public Square getSpot() {
		return spot;
	}
	public void setSpot(Square spot) {
		System.out.println("setspot spot name = " + spot.getName());
		this.spot = spot;
		this.spot.setOccupied(true);
		this.spot.setCritter(this);
	}

	public boolean hasMoved() {
		return moved;
	}

	public void setMoved(boolean moved) {
		this.moved = moved;
	}
	public ArrayList<Targetable> getIndicatedMoves() {
		return indicatedMoves;
	}

	public boolean isAlive() {
		return alive;
	}
	public boolean canBench() {

		if (tempSpot.getSurroundingSquare("leftBench") != null || tempSpot.getSurroundingSquare("rightBench") != null) return true;
		return false;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	public void sendStats(Player player, Player otherPlayer) {
			String str = "critterupdate," + this.getName() + ","
					+ this.getSide() + "," + this.getHealth() + ","
					+ this.getEnergy() + "," + this.getBonusDmg() + ","
					+ this.getDefence();

			player.sendString(str);
			otherPlayer.sendString(str);

	}

	public ArrayList<Square> getPossibleMoves() {
		ArrayList<Square> possibleMoves = new ArrayList<>();

		for (Square s : tempSpot.getSurroundingSquares()) {
			if (!s.isPlannedMove()) {
				possibleMoves.add(s);
			}
		}
		return possibleMoves;
	}

	public void displayPossibleMoves(Player player, String name) {
		if (player.isBot()) return;
		String str = "indicatespots,move," + name + ",";
		String tlc = "";
		if (!((player.getSelectedCritter().getTempSpot().getName().contains("Bench") && player.getCritters().length == 4) || (name.equals("bench") && player.getCritters().length < 2))) {
			if (getTempSpot().getInfront() != null) {
				if (!getTempSpot().getInfront().isPlannedMove()) {
					str += getTempSpot().getInfront().getName() + ".";
					tlc = getTempSpot().getInfront().getTopLeftCoord();
					String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
							+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
					str += coordstr + "." + player.getSide() + ",";
				}
			}
			if (getTempSpot().getBehind() != null) {
				if (!(getTempSpot().getBehind().getName().contains("Bench") && player
						.getCritters().length < 2)) {
					if (!getTempSpot().getBehind().isPlannedMove() || getTempSpot().getBehind().getName().contains("Bench")) {
						str += getTempSpot().getBehind().getName() + ".";
						tlc = getTempSpot().getBehind().getTopLeftCoord();
						String coordstr = tlc + "-"
								+ (char) (tlc.charAt(0) + 1) + tlc.charAt(1)
								+ "-" + (char) (tlc.charAt(0) + 1)
								+ (char) (tlc.charAt(1) + 1) + "-"
								+ tlc.charAt(0) + (char) (tlc.charAt(1) + 1)
								+ "-" + tlc;
						str += coordstr + "." + player.getSide() + ",";
					}
				}
			}
			if (getTempSpot().getOnLeft() != null) {
				if (!getTempSpot().getOnLeft().isPlannedMove()) {
					str += getTempSpot().getOnLeft().getName() + ".";
					tlc = getTempSpot().getOnLeft().getTopLeftCoord();
					String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
							+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
					str += coordstr + "." + player.getSide() + ",";
				}
			}
			if (getTempSpot().getOnRight() != null) {
				if (!getTempSpot().getOnRight().isPlannedMove()) {
					str += getTempSpot().getOnRight().getName() + ".";
					tlc = getTempSpot().getOnRight().getTopLeftCoord();
					String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
							+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
							+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
					str += coordstr + "." + player.getSide() + ",";
				}
			}

			System.out.println("coordstr = " + str);


				player.sendString("indicatespots,no moves");
				player.sendString(str);
				str = "";
		}

	}

	/**
	 * @param name name of the ability
	 * @return action that is identified by the name of the ability
	 */
	public Action identifyAbility(String name) {
		for (Action a : abilities) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Sends the information of the abilities of the selected critter to the client.
	 * @param player player to send the information to
	 * @param otherPlayer the player that didn't click
	 */
	public void displayAbilities(Player player, Player otherPlayer) {
		String playerStr = "";

		int i = 0;
		for (Action a : abilities) {
			a.setSubject(player.getSelectedCritter());
			a.modify();
			if (side.equals(player.getSide()) && player.getSelectedCritter().isAlive()) {
				playerStr += "clickable" + "," + a.getTimeCost() + ","
						+ a.getEnergyCost() + "," + a.getName() + ","
						+ a.getDescription() + "," + i + "," + a.getDamage() +","+ a.getType() +","+ a.getEffects()+"|";
			} else {
				playerStr += "notclickable" + "," + a.getTimeCost() + ","
						+ a.getEnergyCost() + "," + a.getName() + ","
						+ a.getDescription() + "," + i + "," +  a.getDamage() + "," + a.getType() + ","+ a.getEffects() + "|";
			}
			i++;
		}

			player.sendString(playerStr);
	}
	public void setDefence(int defence) {
		this.defence = defence;

	}
	public int getDefence() {
		return defence;
	}


	/**
	 * Tells the client which critter should animate
	 *
	 * @param player
	 *            one player to send the string to
	 * @param otherPlayer
	 *            the other player to send the strign to
	 * @param time
	 *            how long the animation should last
	 */

	 @Override
	public String getType() {
		return "critter";
	}

	public void animate(Player player, Player otherPlayer, double time, Action action) {


		if (this.canUseAction()) {


				player.sendString(
						"animate,basic," + getName() + "," + getSide() + ","
								+ time + "|");
				otherPlayer.sendString(
						"animate,basic," + getName() + "," + getSide() + ","
								+ time + "|");

		}
		// if (action.getSubject().canUseAction()) {
		// 	if (action.getType() == "support") {
		// 		player.sendString(
		// 					"animate,support," + action.getTargetName() + "," + action.getTarget().getSide() + ","
		// 							+ time + "|");
		// 			otherPlayer.sendString(
		// 					"animate,support," + action.getTargetName() + "," + action.getTarget().getSide() + ","
		// 							+ time + "|");

		// 	} else if (action.getType() == "attack") {
		// 						player.sendString(
		// 					"animate,attack," + action.getTargetName() + "," + action.getTarget().getSide() + ","
		// 							+ time + "|");
		// 			otherPlayer.sendString(
		// 					"animate,attack," + action.getTargetName() + "," +action.getTarget().getSide() + ","
		// 							+ time + "|");
		// 	} else if (action.getType() == "block") {
		// 		player.sendString(
		// 					"animate,block," + action.getTargetName() + "," + action.getTarget().getSide() + ","
		// 							+ time + "|");
		// 			otherPlayer.sendString(
		// 					"animate,block," + action.getTargetName() + "," + action.getTarget().getSide() + ","
		// 							+ time + "|");

		// 	}
		// }

	}


	/**
	 * Removes stealth from critter and tells the client to make the critter
	 * visible.
	 *
	 * @param otherPlayer
	 *            player which should now see the critter again
	 */
	public void unstealth(Player otherPlayer) {
		for (int i = 0; i < effects.size(); i++) {
			for (String s : effects.get(i).getStatusType().split(",")) {
				if (s.equals("invisible")) {
					effects.get(i).endEffect(this);
					effects.remove(effects.get(i));
				}
			}

		}

	}
	public Action[] getAbilities() {
		return abilities;
	}
	public Player getOwner() {
		return owner;
	}
	public Player getOpponent() {
		return opponent;
	}


	public boolean isResting() {
		return resting;
	}


	public void setResting(boolean resting) {
		this.resting = resting;
	}


	public void setBenched(boolean b) {
		this.benched = b;

	}
	public boolean isBenched() {
		return this.benched;
	}
	public ArrayList<Action> getMoves() {
		return this.moves;
	}


	public String getPassiveCritterDescription() {
		return passiveCritterDescription;
	}

	public String getPassiveSelectDescription() {
		return passiveSelectDescription;
	}

	public String getPassiveName() {
		return passiveName;
	}

	public void displayUnbenchOptions(Player player) {
		System.out.println("displayunbenchoptions");
		String str = "indicatespots,move," + name + ",";
		String tlc = "";
		if (getTempSpot().getInfront() != null) {
			if (getTempSpot().getInfront().isPlannedMove()
					|| !player.getDeadCritters().isEmpty()) {
				str += getTempSpot().getInfront().getName() + ".";
				tlc = getTempSpot().getInfront().getTopLeftCoord();
				String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
						+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
				str += coordstr + "." + player.getSide() + ",";
			}
		}
		if (getTempSpot().getOnLeft() != null) {
			if (getTempSpot().getOnLeft().isPlannedMove()
					|| !player.getDeadCritters().isEmpty()) {
				str += getTempSpot().getOnLeft().getName() + ".";
				tlc = getTempSpot().getOnLeft().getTopLeftCoord();
				String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
						+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
				str += coordstr + "." + player.getSide() + ",";
			}
		}
		if (getTempSpot().getOnRight() != null) {
			if (getTempSpot().getOnRight().isPlannedMove()
					|| !player.getDeadCritters().isEmpty()) {
				str += getTempSpot().getOnRight().getName() + ".";
				tlc = getTempSpot().getOnRight().getTopLeftCoord();
				String coordstr = tlc + "-" + (char) (tlc.charAt(0) + 1)
						+ tlc.charAt(1) + "-" + (char) (tlc.charAt(0) + 1)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc.charAt(0)
						+ (char) (tlc.charAt(1) + 1) + "-" + tlc;
				str += coordstr + "." + player.getSide() + ",";
			}
		}

		System.out.println("coordstr = " + str);


			player.sendString("indicatespots,no moves");
			player.sendString(str);
			str = "";
	}


	public boolean isComingSoon() {
		return false;
	}


	public int getHackStacks() {
		// TODO Auto-generated method stub
		return hackStacks;
	}

	public void setHackStacks(int hackStacks) {
		// TODO Auto-generated method stub
		this.hackStacks = hackStacks;
	}

	public boolean isPlasterved() {
		return plasterved;
	}


	public void setPlasterved(boolean plasterved) {
		this.plasterved = plasterved;
	}


}
