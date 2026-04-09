package com.gabe.animalia.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.TargetTypeEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;

import net.bytebuddy.dynamic.TargetType;

/**
 * @author Gabe House
 * @version 6/7/2015
 *          Parent class of all actions.
 */
public class Action implements Comparable<Action> {

	protected int order = -1;
	protected int displayOrder = -1;

	protected boolean interrupted = false;
	protected int priority = 0;
	protected String behaviour = "none"; // e.g., "attack", "support", "move", "item"
	protected String logStr = "none";
	protected String effectImage = "none";
	protected String effectDescription = "none";
	protected int stacks = 0;
	protected Critter subject;
	protected boolean stackable = false;
	protected Player player, otherPlayer;
	protected int energyCost;
	protected double timeCost;
	protected Targetable target;
	protected StatusEnum statusType;
	protected int duration = 0;
	protected TargetTypeEnum targetType = TargetTypeEnum.NONE;
	protected String name = "unnamed";
	protected ActionCategoryEnum category = ActionCategoryEnum.NONE;
	protected int damage = 0;
	protected int block = 0;
	protected int healing = 0;
	protected int statusValue = 0;
	protected int energyRestore = 0;
	protected boolean performable = true;
	protected ActionEnum actionData;
	protected String description;
	protected StatusEnum status;
	protected String statusDescription;
	private CastResult checkResult;

	public Action(Critter subject, Targetable target, Player p1, Player p2, ActionEnum stats) {
		this.subject = subject;
		this.target = target;
		this.player = p1;
		this.otherPlayer = p2;

		// Map the Enum data to the class fields ONCE here
		this.actionData = stats;
		this.name = stats.name();
		this.category = stats.category;
		this.damage = stats.damage;
		this.healing = stats.healing;
		this.block = stats.block;
		this.timeCost = stats.time;
		this.energyCost = stats.energy;
		this.targetType = stats.targetType;
		this.duration = stats.duration;
		this.statusValue = stats.statusValue;
		this.energyRestore = stats.energyRestore;
		this.description = stats.description;
		this.status = stats.status;
		this.statusDescription = stats.statusDescription;
		this.stackable = stats.stackable;
	}

	public enum CastResult {
		SUCCESS,
		NOT_ENOUGH_ENERGY,
		NOT_ENOUGH_TIME,
		STUNNED,
		IMMOBILIZED,
		TARGET_DEAD,
		SUBJECT_DEAD,
		TARGET_BENCHED,
		SUBJECT_BENCHED,
		TARGET_STEALTHED,
		INVALID_SUBJECT_SPOT, // For your Charge ability!
		INVALID_TARGET_SPOT,
		CUSTOM_CHECK_FAILED,
		TARGET_IS_NULL,
		SPOT_OCCUPIED,
		IN_SHELL_STANCE,
		MISSED,
		TARGET_MUST_BE_ADJACENT,
		ALREADY_USED,
		UNKNOWN_FAILURE,
		SMOKE_BOMB_ACTIVE,
		SET_UNPERFORMABLE
	}

	public final CastResult checkPerformable() {
		System.out.println("checkPerformable() " + this.name + " , this.performable = " + this.performable);
		if (!this.performable)
			return CastResult.SET_UNPERFORMABLE;
		// 1. Core Engine Requirements
		if (this.getType() == ActionCategoryEnum.ITEM)
			return customCheck();

		if (subject.getEnergy() - this.energyCost < 0)
			return CastResult.NOT_ENOUGH_ENERGY;
		if (subject.getOwner().getUsedTime() + this.timeCost > 10)
			return CastResult.NOT_ENOUGH_TIME;
		if (subject.isBenched() && this.actionData != ActionEnum.UNBENCH)
			return CastResult.SUBJECT_BENCHED;
		if (subject.hasStatus(StatusEnum.STUN))
			return CastResult.STUNNED;
		if (!subject.isAlive())
			return CastResult.SUBJECT_DEAD;
		if (target != null && target.getSide() != subject.getSide() && subject.hasEffect(ActionEnum.SMOKE_BOMB))
			return CastResult.SMOKE_BOMB_ACTIVE;

		// --- SHELL STANCE LOGIC START ---
		boolean hasShell = subject.hasEffect(ActionEnum.SHELL_STANCE);
		boolean isTryingToToggleShell = (this.actionData == ActionEnum.SHELL_STANCE);

		if (hasShell) {
			// If we are in the shell, but the move isn't Shell Stance, we are LOCKED.
			if (!isTryingToToggleShell) {
				return CastResult.IN_SHELL_STANCE;
			}
			// If we ARE in the shell and trying to use Shell Stance, that's allowed (to
			// exit)!
			// We let it pass through to customCheck() or SUCCESS.
		}
		// --- SHELL STANCE LOGIC END ---

		if (getTargetAsCritter() != null) {
			if (getTargetAsCritter().hasStatus(StatusEnum.STEALTH) && !getTargetAsCritter().equals(subject))
				return CastResult.TARGET_STEALTHED;
			if (!getTargetAsCritter().isAlive())
				return CastResult.TARGET_DEAD;
			if (getTargetAsCritter().isBenched() && this.actionData != ActionEnum.UNBENCH)
				return CastResult.TARGET_BENCHED;
		}

		return customCheck();
	}

	// Default: If no custom rules, it's a success
	protected CastResult customCheck() {
		return CastResult.SUCCESS;
	}

	protected boolean perform() {
		System.out.println("[PERFORM] Subject: " + (subject != null ? subject.getName() : "NULL") +
				" | Ability: " + this.name +
				" | Target: " + (target != null ? target.getName() : "NONE/NULL"));

		checkResult = checkPerformable();

		if (checkResult != Action.CastResult.SUCCESS) {
			// Log the failure to the console so you can see why the move "fizzled"
			System.out.println("[ACTION FAILED] " + this.getName() +
					" by " + (this.subject != null ? this.subject.getName() : "NULL") +
					" Reason: " + checkResult.name());

			// Optional: Add specific details for common failures
			if (checkResult == Action.CastResult.NOT_ENOUGH_ENERGY) {
				System.out.println(" -> Needs " + this.energyCost + ", has "
						+ (this.subject != null ? this.subject.getEnergy() : "NULL"));
			}
			this.logStr = (this.subject != null ? this.subject.getLogName() : "NULL") + " failed to use " + this.name
					+ ": " + checkResult.name();
			return false;
		}
		if (this.category == ActionCategoryEnum.ITEM) {
			((Item) this).setUsed(true);
			return customPerform();
		}

		subject.removeEffectByType(StatusEnum.CHANNELLING);
		// 2. THE REDIRECTION LOGIC
		if (this.category == ActionCategoryEnum.ATTACK && this.targetType == TargetTypeEnum.FIGHTER) {
			redirectTargetToFrontline();
		}

		if (this.getTargetAsCritter() != null) {
			if (this.getTargetAsCritter().hasEffect(ActionEnum.REFLECT)) {

				setTarget(getSubject());
			}
		}

		// 3. Pay the energy cost
		this.subject.spendEnergy(this.energyCost);
		// 4. Execute

		// 1. Initialize the log FIRST
		this.logStr = subject.getLogName() + " used " + this.name;

		// 2. Now run the custom logic (which appends to the log)
		boolean success = customPerform();

		if (success) {
			System.out.println("[FINAL LOG] " + this.logStr);
		}

		return success;

	}

	private void redirectTargetToFrontline() {
		Critter t = getTargetAsCritter();
		if (t == null)
			return;

		// Your logic simplified for the Parent context
		if (t.getSpot().getInfront() != null && !t.getSide().equals(subject.getSide())) {
			if (t.getSpot().getInfront().isOccupied()) {
				// Re-assign the target field in the parent so the child uses the front person
				this.target = t.getSpot().getInfront().getCritter();
			}
		}

	}

	protected void dealStandardDamage(Critter t, int bonusFlatDmg) {
		// 1. Calculate Damage using Enum field 'this.damage'
		int totalDmg = (this.damage + subject.getBonusDmg() + bonusFlatDmg) - t.getDefence();
		totalDmg = Math.max(0, totalDmg);

		// 2. Trigger World Events
		if (t.getDefence() == 0) {
			t.onHit(subject.getOwner(), subject.getOpponent(), this);
		} else {
			t.onBlock(subject.getOwner(), subject.getOpponent());
		}

		// 3. Apply Damage
		t.setHealth(t.getHealth() - totalDmg);

		t.markDirty();
		// 4. Record for Manifest & Console
		String hitDetail = ". " + this.name + " hit " + t.getName() + " for " + totalDmg + " damage!";
		this.logStr += hitDetail;
		System.out.println("[BATTLE LOG UPDATE] " + hitDetail.trim());
	}

	protected void restoreHealth(Critter t, int amount) {
		int actualHeal = Math.min(amount, t.getMaxHealth() - t.getHealth());

		t.setHealth(t.getHealth() + actualHeal);
		t.markDirty();

		String healDetail = ". " + this.name + " healed " + t.getName() + " for " + actualHeal + " HP!";
		this.logStr += healDetail;

		System.out.println("[BATTLE LOG UPDATE] " + healDetail.trim());
	}

	// Inside Action.java
	protected Critter getTargetAsCritter() {
		if (this.target instanceof Critter) {
			return (Critter) this.target;
		}
		return null;
	}

	protected Square getTargetAsSquare() {
		if (this.target instanceof Square) {
			return (Square) this.target;
		}
		return null;
	}

	// Default: If no custom rules, it's a success
	protected boolean customPerform() {
		return true;
	}

	public boolean isInterrupted() {
		return interrupted;
	}

	public void setContext(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	}

	public void setInterrupted(boolean interrupted, Critter animating, Player player, Player otherPlayer) {
		this.interrupted = interrupted;
	}

	public String getTargetPos(Targetable target) {
		if (target == null || target.getType() == null) {
			return "Self";
		}

		String rawName = "";

		if (target.getType().equals("spot")) {
			// target is the Spot itself
			rawName = target.getName();
		} else if (target.getType().equals("critter")) {
			// target is a Critter, so we ask for its current Spot
			if (((Critter) target).getSpot() != null) {
				rawName = ((Critter) target).getSpot().getName();
			} else {
				return "Err: target should always have a spot"; // Or "None" depending on your logic for unplaced
																// critters
			}
		} else {
			return "None";
		}

		// Convert "leftMiddleBack" -> "MiddleBack"
		return rawName.replace("left", "").replace("right", "");
	}

	public LoggableActionDTO toLoggableDTO() {
		// 1. Get the full Enum object using your existing fromName helper
		// This gives us access to all the combat stats (damage, heal, etc.)
		ActionEnum actionData = ActionEnum.fromName(this.getName());

		// 2. Resolve Target details safely
		int tId = getTargetId();
		String tPos = getTargetPos(getTarget());
		String tName = getTargetName();

		// 3. Construct the DTO using the enriched constructor
		// Note: We use the Enum directly now to populate the DTO's internal stats
		return new LoggableActionDTO(
				getSubject().getId(),
				getSubject().getFighterType().id, // subjectType ID (e.g. 1 for Wolf)
				getSubject().getName(),

				actionData, // <--- Pass the whole Enum instead of individual strings

				tId,
				tPos,
				tName);
	}

	public String buildManifestRow(
			String behavior, String name, ActionCategoryEnum type,
			Critter sub,
			String tarType, String tarName, String tarSide, int tHP, int tMaxHP,
			double time, String log, String sBundle, String tBundle) {

		String finalLog = log;
		if (checkResult != null && checkResult != CastResult.SUCCESS) {
			// If it failed, we append the failure reason so JS knows to stop animations
			// We use a separator or just check for the word "Failed" in JS
			finalLog = "Failed: " + log;
		}

		// Using a shared format string prevents typos between the two blocks
		String rowFormat = "action,%s,%s,%s,%s,%s,%d,%d,%d,%d,%s,%s,%s,%d,%d,%.2f,%s,%s,%s";

		if (type == ActionCategoryEnum.ITEM) {
			return String.format(rowFormat,
					behavior, name, type.name(), // 1, 2, 3
					"none", "none", // 4, 5 (Subject Info)
					-1, -1, -1, -1, // 6, 7, 8, 9 (Subject HP/Energy)
					tarType, tarName, tarSide, // 10, 11, 12 (Target Info)
					tHP, tMaxHP, // 13, 14
					time, finalLog, sBundle, tBundle // 15, 16, 17, 18
			);
		}

		return String.format(rowFormat,
				behavior, name, type.name(), // 1, 2, 3
				sub.getName(), sub.getSide(), // 4, 5
				sub.getHealth(), sub.getMaxHealth(), // 6, 7
				sub.getEnergy(), sub.getMaxEnergy(), // 8, 9
				tarType, tarName, tarSide, // 10, 11, 12
				tHP, tMaxHP, // 13, 14
				time, finalLog, sBundle, tBundle // 15, 16, 17, 18
		);
	}

	public String getManifestData(double startTime) {
		String tarType = "none";
		String tarName = "none";
		String tarSide = "none";
		int tarHP = -1;
		int tarMaxHP = -1;
		String subBundle = (getType() == ActionCategoryEnum.ITEM) ? "none" : this.getSubject().getFullEffectSnapshot();
		String tarBundle = "none";

		if (this.getTarget() instanceof Critter) {
			tarBundle = ((Critter) this.getTarget()).getFullEffectSnapshot();
		}

		if (this.getTarget() != null) {
			tarName = this.getTarget().getName();
			tarSide = this.getTarget().getSide();

			if (this.getTarget() instanceof Critter) {
				Critter c = (Critter) this.getTarget();
				tarType = "critter";
				tarHP = c.getHealth();
				tarMaxHP = c.getMaxHealth();
			} else {
				tarType = "square";
			}
		}

		return buildManifestRow(
				this.getBehaviour(), this.getName(), this.getType(),
				this.getSubject(),
				tarType, tarName, tarSide, tarHP, tarMaxHP,
				startTime, this.getLogStr(), subBundle, tarBundle);
	}

	public String getLogStr() {
		return logStr;
	}

	public void setLogStr(String logStr) {
		this.logStr = logStr;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Action getNew() {
		try {
			// This looks for the constructor (Critter, Targetable, Player, Player)
			return this.getClass()
					.getDeclaredConstructor(Critter.class, Targetable.class, Player.class, Player.class)
					.newInstance(null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getEffectBundle() {
		return "";
	}

	public StatusEnum getStatus() {
		return this.status;
	}

	public TargetTypeEnum getTargetType() {
		return targetType;

	}

	/**
	 * Performs the ability and sends updated health and energy of the critters to
	 * the clients.
	 *
	 * @return true is successfully performed
	 */

	public String getInfo() {
		StringBuilder sb = new StringBuilder();
		boolean isItem = (actionData.category == ActionCategoryEnum.ITEM);

		// 1. Header (Name) - Always shows
		sb.append(this.name.toUpperCase()).append("\n\n");

		if (!isItem) {
			// 2. Combat Line (DMG, HEAL, BLK) - Abilities Only
			List<String> combat = new ArrayList<>();
			if (actionData.damage > 0)
				combat.add("DAMAGE: " + actionData.damage);
			if (actionData.healing > 0)
				combat.add("HEAL: " + actionData.healing);
			if (actionData.block > 0)
				combat.add("BLOCK: " + actionData.block);

			if (!combat.isEmpty()) {
				sb.append(String.join("  •  ", combat)).append("\n\n");
			}
		}

		// 3. Description - Always shows
		// Using getFormattedDescription() handles your {sv} and {b} placeholders
		sb.append("DESCRIPTION: " + getFormattedDescription()).append("\n\n");

		if (!isItem) {
			// 4. Resource Line (NRG, TIME) - Abilities Only
			List<String> costs = new ArrayList<>();
			if (actionData.energy > 0)
				costs.add("ENERGY: " + actionData.energy);
			if (actionData.time > 0)
				costs.add("TIME: " + actionData.time + "s");

			if (!costs.isEmpty()) {
				sb.append(String.join("  •  ", costs)).append("\n\n");
			}

			// 5. Duration Footer (Optional)
			if (actionData.duration > 0) {
				sb.append("DURATION: ").append(formatDuration(actionData.duration));
			}
		}

		return sb.toString().replace(",", ";").trim();
	}

	private String formatDuration(int d) {
		if (d >= 900)
			return "Permanent";
		return d + " turns";
	}

	private String getFormattedDescription() {
		// 1. Get the raw "Flavor Text" from the Enum
		String text = this.actionData.description;
		if (text == null || text.isEmpty())
			return "No effect description.";

		// 2. Replace the standard placeholders
		return text
				.replace("{t}", String.valueOf(this.name))
				.replace("{d}", String.valueOf(this.damage))
				.replace("{h}", String.valueOf(this.actionData.healing))
				.replace("{b}", String.valueOf(this.actionData.block))
				.replace("{sv}", String.valueOf(this.actionData.statusValue))
				// Math.abs turns -15 into 15 for a cleaner "Drains 15 energy" sentence
				.replace("{er}", String.valueOf(Math.abs(this.actionData.energyRestore)))
				.replace("{dur}", String.valueOf(this.actionData.duration))
				// 3. Custom Hook for those "weird" variables (v1, v2)
				.replace("{v1}", String.valueOf(getCustomV1()))
				.replace("{v2}", String.valueOf(getCustomV2()));
	}

	// Default hooks that child classes can override
	public int getCustomV1() {
		return 0;
	}

	public int getCustomV2() {
		return 0;
	}

	public String getDescription() {
		return this.description;
	}

	public String getUsingName() {
		return this.getSubject().getName();
	}

	public ActionCategoryEnum getType() {
		return this.category;
	}

	public String getTargetName() {
		if (getTarget() == null)
			return null;
		return getTarget().getName();
	}

	public int getTargetId() {
		if (getTarget() == null)
			return -1;
		return this.target.getId();
	}

	public int getEnergyCost() {
		return this.energyCost;
	}

	public double getTimeCost() {
		return this.timeCost;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Removes action from action queue and removes its indicators.
	 */
	public void delete() {
		if (ActionCategoryEnum.ATTACK == category) {
			subject.getIndicatedAttacks().remove(target);
		} else if (ActionCategoryEnum.BLOCK == category) {
			subject.getIndicatedBlocks().remove(target);
		} else if (ActionCategoryEnum.SUPPORT == category) {
			subject.getIndicatedSupports().remove(target);
		}

	}

	/**
	 * Sends the clients the action indicators.
	 */
	public void displayAction() {

	}

	public String getBehaviour() {
		return "basic";
	}

	public String getEffectImage() {

		if (this.status == null)
			return "none";

		// Standardizing the string comparison for safety
		String type = this.status.name().toLowerCase().trim();

		if (type.equals("burn")) {
			return "burn.png";
		} else if (type.equals("block")) {
			return "block.png";
		} else if (type.equals("buff")) {
			return "buff.png";
		} else if (type.equals("channelling")) {
			return "channelling.png";
		} else if (type.equals("curse")) {
			return "curse.png";
		} else if (type.equals("stun")) {
			return "stun.png";
		} else if (type.equals("stealth")) {
			return "buff.png";
		}

		return "none";
	}

	public boolean isTargeting(Critter c) {
		if (getTarget() == null)
			return false;
		return getTarget().equals(c);
	}

	public boolean isSelfTargeting() {
		return getTarget().equals(getSubject());
	}

	public boolean getStackable() {
		return this.stackable;
	}

	public void setStackable(boolean stackable) {
		this.stackable = stackable;
	}

	public void setEffectImage(String effectImage) {
		this.effectImage = effectImage;
	}

	public String getStatusDescription() {
		return this.statusDescription;
	}

	public String getStatusInfo() {
		StringBuilder sb = new StringBuilder();

		// 1. Header (The Move that caused it)
		sb.append("SOURCE: ").append(this.getName().toUpperCase()).append("\n\n");

		// 2. Category / Type
		// If it's a StatusEnum.NONE, you might want to skip or call it "Generic"
		String typeLabel = (this.getStatus() != null) ? this.getStatus().name() : "GENERAL";
		sb.append("TYPE: ").append(typeLabel).append("\n\n");

		// 3. The Core Mechanic (The "Meat" of the tooltip)
		// This uses your bracket-replacer for {sv}, {b}, etc.
		sb.append("EFFECT: ").append(getFormattedDescription()).append("\n\n");

		// 4. Time-based Metadata
		if (this.getDuration() >= 900) {
			sb.append("DURATION: PERMANENT");
		} else {
			sb.append("DURATION: ").append(this.getDuration());
		}

		if (this.getStacks() > 1) {
			sb.append("\n\nSTACKS: ").append(this.getStacks());
		}

		// Safety: Semicolons are our friend, commas are our enemy (CSV-wise)
		return sb.toString().replace(",", ";").trim();
	}

	public void setEffectDescription(String effectDescription) {
		this.effectDescription = effectDescription;
	}

	public int getStacks() {
		return stacks;
	}

	public void setStacks(int stacks) {
		this.stacks = stacks;
	}

	public void setBehaviour(String behaviour) {
		this.behaviour = behaviour;
	}

	public ActionEnum getActionData() {
		return actionData;
	}

	public void setActionData(ActionEnum actionData) {
		this.actionData = actionData;
	}

	public Critter getSubject() {
		// TODO Auto-generated method stub
		return subject;
	}

	public void setSubject(Critter subject) {
		this.subject = subject;

	}

	public Targetable getTarget() {
		// TODO Auto-generated method stub
		return this.target;
	}

	public void setTarget(Targetable target) {
		// TODO Auto-generated method stub
		this.target = target;
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player getOtherPlayer() {
		return this.otherPlayer;
	}

	public void setOtherPlayer(Player otherPlayer) {
		this.otherPlayer = otherPlayer;
	}

	public void init() {
		// TODO Auto-generated method stub

	}

	public String getIndicatorMessage() {
		return null;
	}

	public void displayOptions() {
		// TODO Auto-generated method stub

	}

	public String onTick(Critter critter) {
		// TODO Auto-generated method stub
		return "none";
	}

	public int getDuration() {
		// TODO Auto-generated method stub
		return this.duration;
	}

	public void setDuration(int duration) {
		// TODO Auto-generated method stub
		this.duration = duration;
	}

	public boolean isPerformable() {

		return this.performable;
	}

	public void setPerformable(boolean performable) {
		System.out.println("Setting " + this.getName() + " unperformable.");
		this.performable = performable;

	}

	public int getDamage() {
		// TODO Auto-generated method stub
		return this.damage;
	}

	public void endEffect(Critter critter) {
		// TODO Auto-generated method stub

	}

	public void initialEffect(Action ability) {
		// TODO Auto-generated method stub

	}

	public void setTimeCost(double d) {
		// TODO Auto-generated method stub

	}

	public void setEnergyCost(int energyCost) {
		// TODO Auto-generated method stub

	}

	public void setName(String string) {
		// TODO Auto-generated method stub

	}

	public void modify() {
		// TODO Auto-generated method stub

	}

	public void sendOptions() {
		// TODO Auto-generated method stub

	}

	public void setOrder(int order) {
		this.order = order;

	}

	public int getOrder() {
		return this.order;

	}

	public void showEffect() {
		// TODO Auto-generated method stub

	}

	public void setDisplayOrder(int displayOrder) {
		this.displayOrder = displayOrder;

	}

	public int getDisplayOrder() {
		return this.displayOrder;

	}

	public boolean initable() {
		return customInitable();
	}

	public boolean customInitable() {
		return true;
	}

	public String getSelectDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(Action other) {
		// TODO Auto-generated method stub
		return Integer.compare(other.getPriority(), this.priority);
	}

}
