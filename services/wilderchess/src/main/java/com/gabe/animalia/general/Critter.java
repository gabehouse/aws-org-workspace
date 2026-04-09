package com.gabe.animalia.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import com.gabe.animalia.ability.Move;
import com.gabe.animalia.ability.newt.Contagion;
import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.FighterType;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.enums.TargetTypeEnum;
import com.gabe.animalia.ml.dtos.FighterStateDTO;

/**
 * @author Gabe House
 * @version 6/7/2015
 *          Parent class to all critters.
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
	private Action[] abilities;
	private String side;
	private Player owner;
	private Player opponent;
	private int bonusDmg = 0;
	private int energy = 100;
	private boolean benched = false;
	private int hackStacks = 0;
	private boolean plasterved = false;

	// lets know needs visual update
	private boolean isDirty = false;

	public void markDirty() {
		this.isDirty = true;
	}

	public boolean isDirty() {
		return this.isDirty;
	}

	public void clearDirty() {
		this.isDirty = false;
	}

	public boolean hasBlock() {
		for (Action a : abilities) {
			if (a.getType() == ActionCategoryEnum.BLOCK) {
				return true;
			}
		}
		return false;
	}

	// pass to dto
	private FighterType fighterType;
	// 0 through 7 for the 8 fighters in game
	private int id;

	private boolean moved = false;
	private String currentTurnLog;

	/**
	 * @param name
	 *                  name of the critter
	 * @param spot
	 *                  spot the critter begins on
	 * @param maxHealth
	 *                  maximum health of the critter
	 * @param maxEnergy
	 *                  maximum energy limit of the critter
	 * @param abilities
	 *                  the critter's abilities
	 * @param side
	 *                  the side the critter is on, left or right
	 */
	public Critter(String name, FighterType fighterType, Square spot, int maxHealth, int maxEnergy,
			Action[] abilities, String side, Player owner, Player opponent,
			String passiveCritterDescription, String passiveName, String passiveSelectDescription) {
		this.name = name;
		this.spot = spot;
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
		this.fighterType = fighterType;
	}

	public FighterStateDTO toFighterStateDTO() {
		return new FighterStateDTO(
				this.id,
				this.fighterType,
				this.health,
				this.maxHealth,
				this.energy,
				this.spot != null ? this.spot.getBoardIndex() : -1, // Use the 0-6 index here
				this.bonusDmg,
				this.isAlive(),
				!this.hasStatus(StatusEnum.STUN));
	}

	public String getPositionKey() {
		if (this.spot == null)
			return "None";
		// Converts "leftTopFront" or "rightTopFront" into just "TopFront"
		return this.spot.getName()
				.replace("left", "")
				.replace("right", "");
	}

	// public Action botChooseAction() {
	// Action a;
	// return a;
	// }
	/**
	 * Causes all over time effects on a critter to do what
	 * it should do.
	 */

	public double move(Square spot, double timeUsed) {
		Move move = new Move(this, spot, owner, opponent);
		if (timeUsed + move.getTimeCost() > 10)
			return 0;
		if (getPossibleMoves().contains(spot)) {

			move.init();
			return move.getTimeCost();
		} else {
			if (timeUsed + move.getTimeCost() * 2 > 10)
				return 0;
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
		if (infrontSpot != null) {
			return infrontSpot.getCritter();
		} else {
			return null;
		}
	}

	/**
	 * Checks if the critter is able to perform actions
	 *
	 * @return true if the figher is okay and false if he is not
	 */
	public boolean canUseAction() {
		if (this.hasStatus(StatusEnum.STUN))
			return false;

		return true;

	}

	public boolean canMove() {
		if (this.hasStatus(StatusEnum.ROOT))
			return false;
		return true;

	}

	/**
	 * Critters have their own onHit method which has an effect
	 * when the critter is hit by a direct attack
	 *
	 * @return
	 */
	public void onHit(Player attacking, Player hit, Action action) {
		if (attacking.getMorale() < 10) {
			attacking.setMorale(attacking.getMorale() + 1);
			hit.setMorale(hit.getMorale() - 1);
		}
		removeEffectByType(StatusEnum.CHANNELLING);

		if (this.hasEffect(ActionEnum.CONTAGION)) {
			spreadContagion(attacking, hit);
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

	public void unstealth() {
		// This safely removes all effects that match the condition
		clearEffectsByType(StatusEnum.STEALTH);

	}

	public String onAction(Player player, Player otherPlayer, Action action) {
		System.out.println("\n>>> [ON_ACTION] Player: " + action.getPlayer().getSide() +
				" | Action: " + (action != null ? action.getName() : "NULL") +
				" | Target: "
				+ (action != null && action.getTarget() != null ? action.getTarget().getName() : "NONE"));
		String manifest = "";
		if (player.identifyCritter("Bull", player.getSide()) != null) {
			Critter bull = player.identifyCritter("Bull", player.getSide());
			if (bull.hasEffect(ActionEnum.RUNNING_START)) {
				if (!action.getSubject().equals(bull)) {
					bull.removeEffect(ActionEnum.RUNNING_START);
				}
			}

		}

		if (action.getSubject().hasStatus(StatusEnum.STEALTH)) {
			if (action.getActionData() != ActionEnum.UNBENCH && action.getStatus() != StatusEnum.STEALTH) {
				unstealth();
				manifest += action.buildManifestRow(
						"UNSTEALTH", // behavior
						"UNSTEALTH", // name
						action.getType(), // type
						action.getSubject(), // The Critter moving
						"critter", // tarType
						action.getSubject().getName(), // tarName
						action.getSubject().getSide(), // tarSide
						action.getSubject().getHealth(), action.getSubject().getMaxHealth(), // tarHP,
																								// tarMaxHP
						player.getUsedTime() + action.getTimeCost(), // time
						action.getSubject().getLogName() + " has unstealthed.", // log
						action.getSubject().getFullEffectSnapshot(), // subBundle (update icons!)
						action.getSubject().getFullEffectSnapshot() // tarBundle
				);
			}
		}

		return manifest;
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

	public int getPosition() {
		// Call the mapping method instead of returning the raw 100+ ID
		return this.getSpot().getBoardIndex();
	}

	/**
	 * Captures the current state of the Critter (HP, Energy, Icons)
	 * without advancing status effect durations.
	 */
	public String generateSystemSnapshot(double currentTime) {
		// 1. Get the current log
		String log = this.getCurrentTurnLog();

		// 2. FIX: Ensure it is never an empty string
		if (log == null || log.trim().isEmpty() || log.equals("")) {
			log = "none";
		}

		// 3. Create the update object
		Tick update = new Tick(this, log);

		// 4. Reset the log after capturing it so it doesn't repeat
		this.currentTurnLog = "none";

		return update.getManifestData(currentTime);
	}

	public void restoreEnergy(
			int energy) {
		this.setEnergy(this.getEnergy() + energy);
	}

	public String processEndOfTurn(double currentTime) {
		StringBuilder turnManifest = new StringBuilder();

		if (this.isBenched()) {
			this.restoreEnergy(
					40);
		} else {
			this.restoreEnergy(
					20);
		}

		this.triggerEffects();

		String tickSnapshot = this.generateSystemSnapshot(currentTime);
		if (!tickSnapshot.equals("none")) {
			turnManifest.append(tickSnapshot);
		}
		// Capture the animations from expiring effects
		String durationEvents = this.effectDuration(currentTime);
		if (!durationEvents.isEmpty()) {
			turnManifest.append(durationEvents);
		}

		return turnManifest.toString();
	}

	public void benchedEffect(Player player, Player otherPlayer) {
		// Default is do nothing, override in critters that have bench effects
	}

	public void triggerEffects() {

		StringBuilder summary = new StringBuilder();

		// 1. Use a safe iterator to avoid ConcurrentModification
		Iterator<Action> it = effects.iterator();
		while (it.hasNext()) {
			Action e = it.next();
			String msg = e.onTick(this);

			if (msg != null && !msg.isEmpty() && !msg.equals("none")) {
				if (summary.length() > 0)
					summary.append(" ;; ");
				summary.append(msg);
			}

			// 2. Check for death IMMEDIATELY after each tick
			if (!this.isAlive()) {
				effects.clear(); // Clear everything immediately
				this.markDirty(); // Force a UI refresh
				break;
			}
		}

		// 3. SECONDARY SAFETY: If they are dead, they MUST have 0 effects
		if (!this.isAlive() && !effects.isEmpty()) {
			effects.clear();
			this.markDirty();
		}

		String finalResult = summary.toString().trim();
		this.currentTurnLog = finalResult.isEmpty() ? "none" : finalResult;
	}

	public String effectDuration(double time) {
		StringBuilder sb = new StringBuilder();
		// Using an Iterator is safer than your current Array copy approach
		Iterator<Action> it = effects.iterator();

		while (it.hasNext()) {
			Action a = it.next();

			if (a.getDuration() > 0) {
				a.setDuration(a.getDuration() - 1);
			}

			if (a.getDuration() == 0) {
				// CHECK FOR SPECIAL ANIMATIONS HERE
				if (a.getStatus() == StatusEnum.STEALTH) {
					// Format: ANIM, targetId, type, time
					sb.append(a.buildManifestRow(
							"UNSTEALTH", // behavior
							"UNSTEALTH", // name
							a.getType(), // type
							a.getSubject(), // The Critter moving
							"critter", // tarType
							a.getSubject().getName(), // tarName
							a.getSubject().getSide(), // tarSide
							a.getSubject().getHealth(), a.getSubject().getMaxHealth(), // tarHP,
																						// tarMaxHP
							10, // time
							a.getSubject().getLogName() + " has unstealthed.", // log
							a.getSubject().getFullEffectSnapshot(), // subBundle (update icons!)
							a.getSubject().getFullEffectSnapshot() // tarBundle
					));
				}

				a.endEffect(this);
				it.remove();
				this.markDirty();
			}
		}
		return sb.toString();
	}

	public String getFullEffectSnapshot() {
		if (effects == null || effects.isEmpty())
			return "none";

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < effects.size(); i++) {
			Action e = effects.get(i);

			// Use ^ instead of :
			sb.append(e.getEffectImage()).append("^") // parts[0]
					.append(e.getStatus()).append("^") // parts[1]
					.append(e.getStatusInfo()).append("^") // parts[2]
					.append(e.getName()).append("^") // parts[3]
					.append(e.getDuration()).append("^") // parts[4]
					.append(e.getStacks()); // parts[5]

			if (i < effects.size() - 1)
				sb.append(";");
		}
		return sb.toString();
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public FighterType getFighterType() {
		return fighterType;
	}

	public void setFighterType(FighterType fighterType) {
		this.fighterType = fighterType;
	}

	@Override
	public String getSide() {
		return side;
	}

	private void spreadContagion(Player attacking, Player hit) {
		for (Critter victim : hit.getCritters()) {
			if (victim == null || !victim.isAlive() || victim.equals(this) || victim.isBenched())
				continue;

			// Check adjacency between the hit unit and its teammates
			if (this.getSpot().compareSurrounding(victim.getSpot().getName())) {
				if (!victim.hasEffect(ActionEnum.CONTAGION)) {
					// Infect the neighbor!
					Contagion spread = new Contagion(null, victim, attacking, hit);
					spread.setDuration(3); // Give it a longer life since it needs a hit to spread
					victim.addEffect(spread);
					victim.markDirty();

					System.out.println("Contagion splashed from " + this.getName() + " to " + victim.getName());
				}
			}
		}
	}

	/**
	 * Finds an active effect based on its ActionEnum type.
	 *
	 * @param type     The Enum to look for (e.g., ActionEnum.POISON)
	 * @param sourceId The ID of the critter who applied it, or -1 for any source.
	 * @return The Action object if found, otherwise null.
	 */
	public Action getEffect(ActionEnum type, int sourceId) {
		if (effects == null || type == null)
			return null;

		for (Action e : effects) {
			// Direct Enum comparison: No more .replace("\n", "") hacks!
			if (e.getActionData() == type) {

				// Source check: Only proceed if sourceId matches or is -1
				if (sourceId == -1 || (e.getSubject() != null && e.getSubject().getId() == sourceId)) {
					return e;
				}
			}
		}
		return null;
	}

	// Overload for the "Generic" search (no source check)
	public Action getEffect(ActionEnum type) {
		return getEffect(type, -1);
	}

	// Version 3: Find by Status Enum (Recommended)
	public Action getEffectByStatus(StatusEnum status) {
		if (effects == null || status == null)
			return null;

		for (Action e : effects) {
			// Direct Enum comparison is faster and safer than String matching
			if (e.getStatus() == status) {
				return e;
			}
		}
		return null;
	}

	// Helper for quick boolean checks
	public boolean hasStatus(StatusEnum status) {
		return getEffectByStatus(status) != null;
	}

	public boolean hasEffect(ActionEnum data) {
		if (data == null)
			return false;
		return getEffect(data) != null;
	}

	public void addEffect(Action a) {
		Action prev = null;

		if (a.stackable) {
			for (Action e : this.getEffects()) {
				// FIX: Compare the existing effect (e) name to the new effect (a) name
				// Also ensure both were cast by the same subject/source
				if (e.getName().equals(a.getName()) &&
						e.getSubject().getId() == a.getSubject().getId()) {

					prev = e;
					prev.setStacks(prev.getStacks() + 1);
					prev.setDuration(a.getDuration()); // Refresh duration

					// Sync the local variable 'a' stacks if you need it for immediate logic
					a.setStacks(prev.getStacks());
					break;
				}
			}
		}

		if (prev == null) {
			a.setStacks(1);
			effects.add(a);
		}

		this.markDirty(); // Ensure frontend knows a new icon or number appeared
	}

	// Version 1b: Remove by Enum only (ignores source)
	public boolean removeEffect(ActionEnum actionType) {
		return removeEffect(actionType, -1);
	}

	// Version 1: The New "Master" Method using Enum
	public boolean removeEffect(ActionEnum actionType, int sourceId) {
		if (actionType == null)
			return false;

		for (int i = 0; i < effects.size(); i++) {
			Action e = effects.get(i);

			// 1. Compare Enums directly (Safe and Fast)
			if (e.getActionData() == actionType) {

				// 2. Check Source ID
				if (sourceId == -1 || (e.getSubject() != null && e.getSubject().getId() == sourceId)) {
					e.endEffect(this);
					effects.remove(i);
					this.markDirty();
					return true;
				}
			}
		}
		return false;
	}

	// Version 3: Remove by Status Type (e.g., "burn", "poison")
	public boolean removeEffectByType(StatusEnum statusType) {
		for (int i = 0; i < effects.size(); i++) {
			Action e = effects.get(i);

			// Match the status type (using .equals for safety!)
			if (statusType.equals(e.getStatus())) {
				e.endEffect(this);
				effects.remove(i);
				this.markDirty();
				return true; // Stop after removing the FIRST match
			}
		}
		return false; // No effect of that type found
	}

	public void clearEffectsByType(StatusEnum statusType) {
		// This handles the loop, the removal, and the logic in one go
		if (effects.removeIf(e -> statusType.equals(e.getStatus()))) {
			this.markDirty();
		}
	}

	public void clearEffectsByName(String name) {
		// This handles the loop, the removal, and the logic in one go
		if (effects.removeIf(e -> name.equals(e.getName()))) {
			this.markDirty();
		}
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

	public void raiseBonusDmg(int bonusDmg) {
		this.bonusDmg += bonusDmg;
	}

	public void lowerBonusDmg(int bonusDmg) {
		this.bonusDmg -= bonusDmg;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int hp) {

		this.health = hp;
		if (hp <= 0) {
			if (getEffect(ActionEnum.TAXIDERMY) != null) {
				this.health = 1;
			} else {
				this.health = 0;
				this.setAlive(false);
				this.getEffects().clear();
			}
		}
		if (hp > maxHealth) {
			this.health = maxHealth;
		}
	}

	public void heal(
			int hp) {
		setHealth(this.getHealth() + hp);

	}

	public int getEnergy() {
		return energy;
	}

	public void spendEnergy(int energy) {
		this.setEnergy(this.energy - energy);
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

	public String getLogName() {
		return (this.side == "left" ? "P1" : "P2") + "." + getName();
	}

	public Square getSpot() {
		return spot;
	}

	public void setSpot(Square spot) {
		this.spot = spot;
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

		if (tempSpot.getSurroundingSquare("leftBench") != null || tempSpot.getSurroundingSquare("rightBench") != null)
			return true;
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
		if (player.isBot())
			return;
		String str = "indicatespots,move," + name + ",";
		String tlc = "";

		for (Square s : player.getSquares()) {
			System.out.println(s.getName() + ", " + s.isOccupied() + ", " + s.isPlannedMove());
		}

		if (!((player.getSelectedCritter().getTempSpot().getName().contains("Bench")
				&& player.getCritters().length == 4) || (name.equals("bench") && player.getCritters().length < 2))) {
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
					if (!getTempSpot().getBehind().isPlannedMove()
							|| getTempSpot().getBehind().getName().contains("Bench")) {
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
	 *
	 * @param player      player to send the information to
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
						+ a.getInfo() + "," + i + "," + a.getDamage() + "," + a.getType() + ","
						+ a.getEffectImage() + "|";
			} else {
				playerStr += "notclickable" + "," + a.getTimeCost() + ","
						+ a.getEnergyCost() + "," + a.getName() + ","
						+ a.getInfo() + "," + i + "," + a.getDamage() + "," + a.getType() + ","
						+ a.getEffectImage() + "|";
			}
			i++;
		}

		player.sendString(playerStr);
	}

	public void setDefence(int defence) {
		this.defence = defence;
	}

	public void raiseDefence(int defence) {
		setDefence(this.defence + defence);
	}

	public void lowerDefence(int defence) {
		setDefence(this.defence - defence);
	}

	public int getDefence() {
		return defence;
	}

	/**
	 * Tells the client which critter should animate
	 *
	 * @param player
	 *                    one player to send the string to
	 * @param otherPlayer
	 *                    the other player to send the strign to
	 * @param time
	 *                    how long the animation should last
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
		// if (action.getType() == "support") {
		// player.sendString(
		// "animate,support," + action.getTargetName() + "," +
		// action.getTarget().getSide() + ","
		// + time + "|");
		// otherPlayer.sendString(
		// "animate,support," + action.getTargetName() + "," +
		// action.getTarget().getSide() + ","
		// + time + "|");

		// } else if (action.getType() == "attack") {
		// player.sendString(
		// "animate,attack," + action.getTargetName() + "," +
		// action.getTarget().getSide() + ","
		// + time + "|");
		// otherPlayer.sendString(
		// "animate,attack," + action.getTargetName() + ","
		// +action.getTarget().getSide() + ","
		// + time + "|");
		// } else if (action.getType() == "block") {
		// player.sendString(
		// "animate,block," + action.getTargetName() + "," +
		// action.getTarget().getSide() + ","
		// + time + "|");
		// otherPlayer.sendString(
		// "animate,block," + action.getTargetName() + "," +
		// action.getTarget().getSide() + ","
		// + time + "|");

		// }
		// }

	}

	/**
	 * Removes stealth from critter and tells the client to make the critter
	 * visible.
	 *
	 * @param otherPlayer
	 *                    player which should now see the critter again
	 */
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
		return getSpot().getName().contains("Bench");
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

	public String getCurrentTurnLog() {
		return currentTurnLog;
	}

	public void setCurrentTurnLog(String currentTurnLog) {
		this.currentTurnLog = currentTurnLog;
	}

}
