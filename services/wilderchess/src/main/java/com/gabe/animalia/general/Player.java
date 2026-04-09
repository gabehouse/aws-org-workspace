package com.gabe.animalia.general;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Random;
import java.util.stream.Collectors;

import com.gabe.animalia.items.CrimberryPack;
import com.gabe.animalia.items.WarHorn;
import com.gabe.animalia.ml.dtos.FighterStateDTO;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.dtos.PlayerStateDTO;
import com.gabe.animalia.ml.game.GameFeaturizer;
import com.gabe.animalia.ml.game.PlayerActionInput;
import com.gabe.animalia.ml.server.Inference;

import com.gabe.animalia.items.OilBomb;
import com.gabe.animalia.items.RatlandJestersHat;
import com.gabe.animalia.items.ThiefGloves;
import com.gabe.animalia.items.SmokeBomb;
import com.gabe.animalia.ability.Bench;
import com.gabe.animalia.ability.Move;
import com.gabe.animalia.ability.Unbench;
import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.TargetTypeEnum;

public class Player {
	private double maxTime = 10;
	private double usedTime;
	private int morale = 5;
	private double allottedTime;
	private RemoteEndpoint endpoint;
	private Square topBack, topFront, middleBack, middleFront, bottomBack, bottomFront, bench;
	private Square[] squares = new Square[7];
	private String side;
	private boolean readied;
	private boolean usingMove;
	private boolean benchActionUsed = false;
	private String lastIndicatedStr;
	private Action selectedAbility;
	private Critter selectedCritter;
	private Critter[] critters;
	private Critter[] totalCritters;

	private ArrayList<Item> queuedItems = new ArrayList<Item>();
	private ArrayList<Action> queue = new ArrayList<Action>();
	private ArrayList<Item> totalItems = new ArrayList<Item>();
	private ArrayList<Critter> deadCritters = new ArrayList<Critter>();
	private Session session;
	private Action performingAbility = null;
	private Item item1, item2, item3;

	private Item[] items = new Item[3];
	private String username;
	private int playerCompletedActionCount = 0;
	private int combinedCompletedActionCount = 0;
	private boolean isBot = false;
	private int id;

	private int turnStartHealthSum;
	private int turnEndHealthSum;
	private int turnStartEnergySum;
	private int turnEndEnergySum;
	private Game game = null;

	private Inference inference;
	private GameFeaturizer featurizer;

	public Player(Square topBack, Square topFront, Square middleBack,
			Square middleFront, Square bottomBack, Square bottomFront, Square bench, Game game, String side) {
		// player's squares
		this.topBack = topBack;
		this.topFront = topFront;
		this.middleBack = middleBack;
		this.middleFront = middleFront;
		this.bottomBack = bottomBack;
		this.bottomFront = bottomFront;
		this.bench = bench;
		squares[0] = this.topBack;
		squares[1] = this.topFront;
		squares[2] = this.middleBack;
		squares[3] = this.middleFront;
		squares[4] = this.bottomBack;
		squares[5] = this.bottomFront;
		squares[6] = this.bench;
		// items the player can choose from
		totalItems.add(new CrimberryPack(null, null));
		totalItems.add(new WarHorn(null, null));
		totalItems.add(new OilBomb(null, null));
		totalItems.add(new RatlandJestersHat(null, null));
		totalItems.add(new ThiefGloves(null, null));
		totalItems.add(new SmokeBomb(null, null));

		this.game = game;

		setSide(side);

	}

	public Square pickRandomSpot() {
		Random random = new Random();
		return squares[random.nextInt(6)];
	}

	public Critter pickRandomCritter() {
		// check at least one is alive
		Random random = new Random();
		ArrayList<Critter> livingCritters = new ArrayList<Critter>();
		for (int i = 0; i < critters.length; i++) {
			if (critters[i].isAlive() && !critters[i].isBenched()) {
				livingCritters.add(critters[i]);
			}
		}
		if (livingCritters.isEmpty())
			return null;
		Critter c = livingCritters.get(random.nextInt(livingCritters.size()));
		return c;
	}

	public Action pickRandomAction(Critter c) {
		Random random = new Random();

		Action a = c.getAbilities()[random.nextInt(4)];
		return a;
	}

	public Action pickRandomAction(Critter c, ActionCategoryEnum preferredCategory) {
		Random random = new Random();
		Action[] abilities = c.getAbilities();

		// Filter for the specific type (e.g., ATTACK)
		List<Action> filtered = Arrays.stream(abilities)
				.filter(a -> a.getType().equals(preferredCategory))
				.collect(Collectors.toList());

		if (!filtered.isEmpty()) {
			return filtered.get(random.nextInt(filtered.size()));
		}

		// Fallback: If no ability of that type exists, pick any of the 4
		return abilities[random.nextInt(4)];
	}

	public void randomFillActionQueueEasy(Player otherPlayer) {
		Random random = new Random();
		double totalTime = 0;
		while (totalTime < 7) {
			Critter c = pickRandomCritter();
			if (c.isBenched())
				continue;
			if (random.nextInt(7) == 1) {
				// move randomly
				ArrayList<Square> possibleMoves = c.getPossibleMoves();
				if (possibleMoves.size() > 0) {
					Square toMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
					Action a;
					if (toMove.getName().contains("Bench")) {
						a = new Bench(c, this.bench, this, otherPlayer);
					} else {
						a = new Move(c, toMove, this, otherPlayer);
					}
					a.init();
					totalTime += a.getTimeCost();
					continue;
				}
			}
			Action a = pickRandomAction(c);

			int tries = 0;
			while ((totalTime + a.getTimeCost() > 10 || a.getEnergyCost() > c.getEnergy()) && tries < 6) {
				c = pickRandomCritter();
				a = pickRandomAction(c);
				tries += 1;
			}
			if (totalTime + a.getTimeCost() > 10)
				break;
			if (a.getEnergyCost() > c.getEnergy())
				break;
			// bench if below 30 mana and have enough time and mana left and in back row
			if (c.getEnergy() < 30 && c.getEnergy() >= 10 && totalTime + a.getTimeCost() <= 9 && c.canBench()
					&& this.getCritters().length > 1) {
				Bench bench = new Bench(c, this.bench, this, otherPlayer);
				bench.init();
				totalTime += bench.getTimeCost();
				continue;
			}
			Action ability = a.getNew();
			ability.setPlayer(this);
			ability.setSubject(c);
			ability.setOtherPlayer(otherPlayer);
			if (ability.getTargetType().equals(TargetTypeEnum.FIGHTER)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {
					ability.setTarget(otherPlayer.pickRandomCritter());
				} else {
					ability.setTarget(pickRandomCritter());
				}
			} else if (ability.getTargetType().equals(TargetTypeEnum.SQUARE)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {
					ability.setTarget(otherPlayer.pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.SUPPORT)) {
					ability.setTarget(pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.BLOCK)) {
					ability.setTarget(pickRandomSpot());
				}
			}
			if (ability.initable()) {
				System.out.println("RANDOMFILL EASY INIT ABILITY " + ability + " subject = " + ability.getSubject()
						+ " target = " + ability.getTarget());
				ability.init();
				totalTime += ability.getTimeCost();
			}
		}

	}

	private float evaluateState(Player otherPlayer, List<Action> candidateActions, Inference inference,
			GameFeaturizer featurizer) {
		// Convert current actions to DTOs
		try {
			List<LoggableActionDTO> dtos = candidateActions.stream()
					.map(Action::toLoggableDTO)
					.collect(Collectors.toList());

			List<PlayerActionInput> actionInputs = List.of(new PlayerActionInput(this.id, dtos));

			// Featurize
			float[] features = featurizer.featurizeForInference(
					this.game.getTurnNumber(),
					this.toStateDto(),
					otherPlayer.toStateDto(),
					actionInputs);

			// Predict
			try {
				return inference.predict(features).winProbability;
			} catch (Exception e) {
				return 0.5f;
			}
		} catch (NullPointerException e) {
			System.err.println("!!! AI CRASH DETECTED !!!");
			e.printStackTrace(); // <--- THIS WILL FINALLY SHOW THE LINE NUMBER
			throw e;
		}
	}

	public void aiFillActionQueue(Player otherPlayer) {
		System.out.println("AI FILL ACTION QUEUE");
		List<List<Action>> candidateTurns = new ArrayList<>();

		// 1. Generate 20 random "potential" turns
		for (int i = 0; i < 100; i++) {
			candidateTurns.add(generatePotentialTurnMedium(otherPlayer));
		}

		// 2. Evaluate each turn using the passed-in params
		List<Action> bestTurn = null;
		float highestWinProb = -1.0f;

		for (List<Action> turn : candidateTurns) {
			float currentProb = evaluateState(otherPlayer, turn, inference, featurizer);
			if (currentProb > highestWinProb) {
				highestWinProb = currentProb;
				bestTurn = turn;
			}
		}

		// 3. Commit the winner
		if (bestTurn != null) {
			int time = 0;
			for (Action a : bestTurn) {
				if (a.initable() && a.getTimeCost() + time <= 10) {
					a.init(); // This actually puts them in the real queue
					time += a.getTimeCost();
				}
			}
		}
	}

	public List<Action> generatePotentialTurnEasy(Player otherPlayer) {
		Random random = new Random();
		double totalTime = 0;
		List<Action> potentialActions = new ArrayList<>();
		while (totalTime < 7) {
			Critter c = pickRandomCritter();
			if (c.isBenched())
				continue;
			if (random.nextInt(7) == 1) {
				// move randomly
				ArrayList<Square> possibleMoves = c.getPossibleMoves();
				if (possibleMoves.size() > 0) {
					Square toMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
					Action a;
					if (toMove.getName().contains("Bench")) {
						a = new Bench(c, this.bench, this, otherPlayer);
					} else {
						a = new Move(c, toMove, this, otherPlayer);
					}
					potentialActions.add(a);
					totalTime += a.getTimeCost();
					a.setSubject(c);
					continue;
				}
			}
			Action a = pickRandomAction(c);

			int tries = 0;
			while ((totalTime + a.getTimeCost() > 10 || a.getEnergyCost() > c.getEnergy()) && tries < 6) {
				c = pickRandomCritter();
				a = pickRandomAction(c);
				tries += 1;
			}
			if (totalTime + a.getTimeCost() > 10)
				break;
			if (a.getEnergyCost() > c.getEnergy())
				break;
			// bench if below 30 mana and have enough time and mana left and in back row
			if (c.getEnergy() < 30 && c.getEnergy() >= 10 && totalTime + a.getTimeCost() <= 9 && c.canBench()
					&& this.getCritters().length > 1) {
				Bench bench = new Bench(c, this.bench, this, otherPlayer);
				potentialActions.add(bench);
				totalTime += bench.getTimeCost();
				bench.setSubject(c);
				continue;
			}
			Action ability = a.getNew();
			ability.setPlayer(this);
			ability.setSubject(c);
			ability.setOtherPlayer(otherPlayer);
			if (ability.getTargetType().equals(TargetTypeEnum.FIGHTER)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {
					ability.setTarget(otherPlayer.pickRandomCritter());
				} else {
					ability.setTarget(pickRandomCritter());
				}
			} else if (ability.getTargetType().equals(TargetTypeEnum.SQUARE)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {
					ability.setTarget(otherPlayer.pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.SUPPORT)) {
					ability.setTarget(pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.BLOCK)) {
					ability.setTarget(pickRandomSpot());
				}
			}
			if (ability.initable()) {
				potentialActions.add(ability);
				totalTime += ability.getTimeCost();
			}
		}
		// System.out.println("returning potential actions: " + potentialActions);
		return potentialActions;
	}

	public List<Action> generatePotentialTurnMedium(Player otherPlayer) {
		Random random = new Random();
		double totalTime = 0;
		List<Action> potentialActions = new ArrayList<>();

		if (getDeadCritters().size() >= 1 && getBench().getCritter() != null) {
			Critter benched = getBench().getCritter();
			ArrayList<Square> possibleMoves = benched.getPossibleMoves();
			if (possibleMoves.size() > 0) {
				// if (!s.isPlannedMove()) {
				// backRow.remove(s);
				// }

				Square toMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
				System.out.println("unbench fill queue medium " + toMove.getName());
				Unbench unbenchTemplate = new Unbench(getBench().getCritter(), toMove, this, otherPlayer);
				Unbench unbenchAction = (Unbench) unbenchTemplate.getNew();
				unbenchAction.setPlayer(this);
				unbenchAction.setOtherPlayer(otherPlayer);
				unbenchAction.setSubject(getBench().getCritter());
				unbenchAction.setTarget(toMove);
				if (unbenchAction.initable()) {
					potentialActions.add(unbenchAction);
					totalTime += unbenchAction.getTimeCost();
				}

			}
		}
		Critter tank = null;
		// check if tank on field
		for (Critter c : critters) {
			if (c.hasBlock() && !c.isBenched()) {
				tank = c;
				System.out.println("Tank found randomfillmedium");
				break;
			}
		}
		if (tank != null && random.nextInt(2) == 1 && !tank.isBenched()) {
			System.out.println("attempt to move tank " + tank);
			// 1/2 chance for tank to move infront of lowest hp fighter

			// get lowest hp critter
			int leastHP = 1000;
			Critter leastHPCritter = null;
			for (Critter oc : getCritters()) {
				if (oc.equals(tank))
					continue;
				if (oc.isBenched())
					continue;
				if (oc.getHealth() < leastHP) {
					leastHP = oc.getHealth();
					leastHPCritter = oc;
				}
			}

			// if theres an empty spot infront of the lowest hp critter, move c to that spot
			if (leastHPCritter != null && leastHPCritter.getInfront() == null
					&& leastHPCritter.getTempSpot().getInfront() != null) {
				System.out.println("movetank2");
				Square spotToMove = leastHPCritter.getTempSpot().getInfront();
				totalTime += tank.move(spotToMove, 0);

			}

		}
		// block

		if (random.nextInt(2) == 1 && tank != null) {
			Action blockTemplate = pickRandomAction(tank, ActionCategoryEnum.BLOCK);
			Action block = blockTemplate.getNew();

			block.setPlayer(this);
			block.setSubject(tank);
			block.setTarget(tank);
			if (block.getActionData() == ActionEnum.POUNCE) {
				if (tank.getPossibleMoves().size() > 0) {
					block.setTarget(tank.getPossibleMoves().get(random.nextInt(tank.getPossibleMoves().size())));
				}
			}
			block.setOtherPlayer(otherPlayer);
			if (block.initable()) {
				potentialActions.add(block);
				totalTime += block.getTimeCost();
			}

		}
		boolean benchUsed = false;
		while (totalTime < 8) {
			Critter c = pickRandomCritter();
			if (c.isBenched())
				continue;
			if (c.getEnergy() < 30) {
				if (random.nextInt(2) == 0) {
					continue;
				}
			}

			int possibleMoveCount = c.getPossibleMoves().size();
			if (possibleMoveCount > 0 && random.nextInt(13) == 1) {
				// random move
				ArrayList<Square> possibleMoves = c.getPossibleMoves();
				Square toMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
				Action a;
				if (toMove.getName().contains("Bench")) {
					a = new Bench(c, this.bench, this, otherPlayer);
					a = a.getNew();
				} else {
					a = new Move(c, toMove, this, otherPlayer);
					a = a.getNew();
				}
				a.setPlayer(this);
				a.setOtherPlayer(otherPlayer);
				a.setSubject(c);
				a.setTarget(toMove);
				if (a.initable()) {
					potentialActions.add(a);
					totalTime += a.getTimeCost();
				}
				continue;
			}
			Action a;
			if (random.nextInt(2) == 0) {
				a = pickRandomAction(c, ActionCategoryEnum.ATTACK);
			} else {
				a = pickRandomAction(c);
			}

			int tries = 0;
			while ((totalTime + a.getTimeCost() > 10 || a.getEnergyCost() > c.getEnergy()) && tries < 10) {

				c = pickRandomCritter();
				if (random.nextInt(2) == 1) {
					a = pickRandomAction(c, ActionCategoryEnum.ATTACK);
				} else {
					a = pickRandomAction(c);
				}

				tries += 1;
			}
			if (totalTime + a.getTimeCost() > 10)
				break;
			if (a.getEnergyCost() > c.getEnergy())
				break;
			// bench if below 30 mana and have enough time and mana left and in back row
			Bench benchTemplate = new Bench(c, this.bench, this, otherPlayer);
			if (c.getEnergy() <= 30 && c.getEnergy() >= benchTemplate.getEnergyCost()
					&& totalTime + benchTemplate.getTimeCost() <= 10
					&& c.canBench()
					&& this.getCritters().length > 1 && !benchUsed) {
				Bench benchAction = (Bench) benchTemplate.getNew();
				benchAction.setPlayer(this);
				benchAction.setOtherPlayer(otherPlayer);
				benchAction.setSubject(c);
				benchAction.setTarget(this.bench);
				if (benchAction.initable()) {
					potentialActions.add(benchAction);
					benchUsed = true;
					totalTime += benchAction.getTimeCost();
				}
				continue;
			}
			Action ability = a.getNew();
			ability.setPlayer(this);
			ability.setSubject(c);
			ability.setOtherPlayer(otherPlayer);
			if (ability.getTargetType().equals(TargetTypeEnum.FIGHTER)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {

					// 1/3 chance to target their non-tanks that aren't protected
					if (random.nextInt(2) == 1) {
						Critter toAttack = null;
						for (int i = 1; i < otherPlayer.getCritters().length; i++) {
							Critter oc = otherPlayer.getCritters()[i];

							if (oc.isBenched())
								continue;
							if (oc.getInfront() != null)
								continue;
							if (!oc.isAlive())
								continue;

							toAttack = oc;
							break;
						}

						if (toAttack == null) {
							toAttack = otherPlayer.pickRandomCritter();
						}
						ability.setTarget(toAttack);
					}

					// 1/3 chance to target their lowest hp enemy
					else if (random.nextInt(10) < 8) {
						int leastHP = 1000;
						Critter leastHPCritter = null;
						for (Critter oc : otherPlayer.getCritters()) {
							if (oc.isBenched())
								continue;
							if (!oc.isAlive())
								continue;
							if (oc.getHealth() < leastHP) {
								leastHP = oc.getHealth();
								leastHPCritter = oc;
							}
						}
						ability.setTarget(leastHPCritter);
					} else {
						ability.setTarget(otherPlayer.pickRandomCritter());
					}

				} else {
					ability.setTarget(pickRandomCritter());
				}
			} else if (ability.getTargetType().equals(TargetTypeEnum.SQUARE)) {
				if (ability.getType().equals(ActionCategoryEnum.ATTACK)) {
					ability.setTarget(otherPlayer.pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.SUPPORT)) {
					ability.setTarget(pickRandomCritter().getSpot());
				} else if (ability.getType().equals(ActionCategoryEnum.BLOCK)) {
					ability.setTarget(pickRandomSpot());
				}
			}
			if (ability.initable()) {
				potentialActions.add(ability);
				totalTime += ability.getTimeCost();
			}
		}
		// System.out.println("returning potential actions: " + potentialActions);
		return potentialActions;
	}

	public void randomFillActionQueueMedium(Player otherPlayer) {
		List<Action> bestTurn = generatePotentialTurnMedium(otherPlayer);
		int time = 0;
		for (Action a : bestTurn) {
			if (a.initable() && a.getTimeCost() + time <= 10) {
				a.init(); // This actually puts them in the real queue
				time += a.getTimeCost();
			}
		}
	}

	public void fillActionQueue(Player otherPlayer) {
		System.out.println("RANDOMFILLACTIONQUEUE");
		aiFillActionQueue(otherPlayer);
	}

	private void randomFillActionQueue(Player otherPlayer) {
		System.out.println("RANDOMFILLACTIONQUEUE");
		randomFillActionQueueMedium(otherPlayer);
	}

	public void sendString(String str) {
		if (endpoint != null) {
			try {
				// System.out.println("sendstring " + str);
				endpoint.sendString(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param queue action queue to be turned into a string
	 * @return string to be sent to client in order to show icons
	 *         Takes the action queue and creates a string with the details
	 *         needed to create the visual representations in the client
	 */
	public String iconString(ArrayList<Action> queue) {
		String str = "";
		for (int i = 0; i < queue.size(); i++) {
			Action a = queue.get(i);

			// 1. Build the first 5 standard tokens
			str += a.getTimeCost() + "/"
					+ a.getEnergyCost() + "/"
					+ a.getUsingName() + "/"
					+ a.getTargetName() + "/"
					+ a.getName() + "/";

			// 2. Handle the Type and extra fields based on Category
			// Use .name().toLowerCase() so MOVE becomes "move"
			String typeLower = a.getType().name().toLowerCase();

			if (a.getType().equals(ActionCategoryEnum.ATTACK)
					|| a.getType().equals(ActionCategoryEnum.SUPPORT)
					|| a.getType().equals(ActionCategoryEnum.BLOCK)) {

				str += typeLower + "/"
						+ (a.getTarget() != null ? a.getTarget().getSide() : "none") + "/"
						+ a.getIndicatorMessage() + "/"
						+ a.getInfo().replace("/", "|") // Use getInfo() for the tooltip!
						+ "$";

			} else if (a.getType().equals(ActionCategoryEnum.MOVE)) {
				str += typeLower + "/"
						+ "unimportant" + "/"
						+ a.getIndicatorMessage() + "/"
						+ a.getInfo().replace("/", "|")
						+ "$";
			}
		}
		return str;
	}

	public String queueToString(ArrayList<Action> queue) {
		// Start with a leading underscore if your JS expects bigTokens[0] to be a
		// header
		StringBuilder sb = new StringBuilder("queue");

		for (Action a : queue) {
			sb.append("$"); // Big tokens are separated by underscores

			// Convert Enum to lowercase string: "ATTACK" -> "attack"
			String typeStr = (a.getType() != null) ? a.getType().name().toLowerCase() : "none";

			// Small tokens are separated by slashes /
			sb.append(typeStr).append("/")
					.append((int) a.getTimeCost()).append("/") // Ensure this matches icon constructor
					.append(a.getUsingName()).append("/")
					.append(a.getTargetName()).append("/")
					.append(a.getInfo().replace("/", "-")); // Replace slashes in info so they don't break the split

			// Add any other small tokens your 'new icon(...)' constructor requires
			// (smallTokens[5...8])
		}

		return sb.toString();
	}

	public Item identifyItem(String name, int pos) {
		// // items[0] = item1;
		// // items[1] = item2;
		// // items[2] = item3;

		// // --- Diagnostic Printline ---
		// System.out.println("--- Current Inventory State ---");
		// for (int i = 0; i < items.length; i++) {
		// String itemInfo = (items[i] == null) ? "null" : items[i].toString() + " [" +
		// items[i].getName() + "]";
		// System.out.println("Slot " + i + ": " + itemInfo);
		// }
		// System.out.println("Requesting Name: " + name + " | Requesting Pos: " + pos);
		// // ----------------------------

		// for (Item item : items) {
		// if (item != null && name.toLowerCase().equals(item.getName().toLowerCase()))
		// {
		// System.out.println("Match found in loop! Returning: " + item);
		// return item;
		// }
		// }

		// System.out.println("No loop match. Returning by direct index: " +
		// items[pos]);
		return items[pos];
	}

	public Item getItem1() {
		return item1;
	}

	public void setItem1(Item item1) {
		this.item1 = item1;
		items[0] = item1;
	}

	public Item getItem2() {
		return item2;

	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public void setItem2(Item item2) {
		items[1] = item2;
		this.item2 = item2;
	}

	public Item getItem3() {
		return item3;
	}

	public void setItem3(Item item3) {
		items[2] = item3;
		this.item3 = item3;
	}

	public ArrayList<Item> getItemQueue() {
		return queuedItems;
	}

	public ArrayList<Action> getQueue() {
		return queue;
	}

	public void setMoveQueue(ArrayList<Action> moveQueue) {
		this.queue = moveQueue;
	}

	public double getAllottedTime() {
		return allottedTime;
	}

	public void setAllottedTime(double allottedTime) {
		System.out.println("allotted time2 = " + allottedTime);
		this.allottedTime = allottedTime;
	}

	public Action getSelectedAbility() {
		return selectedAbility;
	}

	public void setSelectedAbility(Action selectedAbility) {
		this.selectedAbility = selectedAbility;
	}

	public boolean isUsingMove() {
		return usingMove;
	}

	public void setUsingMove(boolean usingMove) {
		this.usingMove = usingMove;
	}

	public Critter getSelectedCritter() {
		return selectedCritter;
	}

	public void setSelectedCritter(Critter selectedCritter) {
		this.selectedCritter = selectedCritter;
	}

	public double getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(int maxTime) {
		this.maxTime = maxTime;
	}

	public Square getBench() {
		return bench;
	}

	public void setBench(Square bench) {
		this.bench = bench;
	}

	public Square getTopBack() {
		return topBack;
	}

	public void setTopBack(Square topBack) {
		this.topBack = topBack;
	}

	public Square getTopFront() {
		return topFront;
	}

	public void setTopFront(Square topFront) {
		this.topFront = topFront;
	}

	public Square getMiddleBack() {
		return middleBack;
	}

	public void setMiddleBack(Square middleBack) {
		this.middleBack = middleBack;
	}

	public Square getMiddleFront() {
		return middleFront;
	}

	public void setMiddleFront(Square middleFront) {
		this.middleFront = middleFront;
	}

	public Square getBottomBack() {
		return bottomBack;
	}

	public void setBottomBack(Square bottomBack) {
		this.bottomBack = bottomBack;
	}

	public Square[] getSquares() {
		return squares;
	}

	public double getUsedTime() {
		return usedTime;
	}

	public void setUsedTime(double usedTime) {
		this.usedTime = usedTime;
	}

	public Square getBottomFront() {
		return bottomFront;
	}

	public void setBottomFront(Square bottomFront) {
		this.bottomFront = bottomFront;
	}

	public Critter[] getCritters() {
		return critters;
	}

	public void setCritters(Critter[] currentCritters) {
		// Only capture the team the very first time (start of round)
		if (this.totalCritters == null && currentCritters.length == 4) {
			// Create a new array so it doesn't "shrink" later
			this.totalCritters = currentCritters.clone();
		}

		// This is your active list for game logic (can shrink/change)
		this.critters = currentCritters;
	}

	public void setEndpoint(RemoteEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public RemoteEndpoint getEndpoint() {
		return endpoint;
	}

	public boolean isReadied() {
		return readied;
	}

	public void setReadied(boolean readied) {
		this.readied = readied;
	}

	public void setSide(String side) {
		this.side = side;
		if (side == "left") {
			id = 0;
		} else {
			id = 1;
		}
	}

	public int getTurnStartHealthSum() {
		return turnStartHealthSum;
	}

	public void setTurnStartHealthSum(int turnStartHealthSum) {
		this.turnStartHealthSum = turnStartHealthSum;
	}

	public int getTurnEndHealthSum() {
		return turnEndHealthSum;
	}

	public void setTurnEndHealthSum(int turnEndHealthSum) {
		this.turnEndHealthSum = turnEndHealthSum;
	}

	public int getTurnStartEnergySum() {
		return turnStartEnergySum;
	}

	public void setTurnStartEnergySum(int turnStartEnergySum) {
		this.turnStartEnergySum = turnStartEnergySum;
	}

	public int getTurnEndEnergySum() {
		return turnEndEnergySum;
	}

	public void setTurnEndEnergySum(int turnEndEnergySum) {
		this.turnEndEnergySum = turnEndEnergySum;
	}

	public int getId() {
		return id;
	}

	public String getSide() {
		return side;
	}

	public ArrayList<Item> getTotalItems() {
		return totalItems;
	}

	public Inference getInference() {
		return inference;
	}

	public void setInference(Inference inference) {
		this.inference = inference;
	}

	public GameFeaturizer getFeaturizer() {
		return featurizer;
	}

	public void setFeaturizer(GameFeaturizer featurizer) {
		this.featurizer = featurizer;
	}

	public Square identifySquare(String name) {
		for (Square s : squares) {

			if (s.getName().toLowerCase().equals(name.toLowerCase())) {
				return s;
			}
		}
		return null;
	}

	public Critter identifyCritter(String name, String side) {
		for (Critter f : critters) {
			// System.out.println("comapring " + name.toLowerCase() + " to " +
			// f.getName().toLowerCase() + " and sides "
			// + side + " to "
			// + this.side);
			if (name.toLowerCase().equals(f.getName().toLowerCase()) && side.equals(this.side)) {
				return f;
			}
		}

		return null;
	}

	public Critter identifyDeadCritter(String name, String side) {

		for (Critter f : deadCritters) {
			if (name.toLowerCase().equals(f.getName().toLowerCase()) && side.equals(this.side)) {
				return f;
			}
		}

		return null;
	}

	public Item identifyTotalItems(String name) {
		for (int i = 0; i < totalItems.size(); i++) {
			if (totalItems.get(i).getName().toLowerCase().equals(name.toLowerCase())) {
				return totalItems.get(i);
			}
		}
		return null;
	}

	public void setUsername(String username) {
		this.username = username;

	}

	public String getUsername() {
		return this.username;

	}

	public void setMorale(int morale) {
		this.morale = morale;

	}

	public int getMorale() {
		return this.morale;

	}

	public Critter[] getTotalCritters() {
		return totalCritters;
	}

	public void setTotalCritters(Critter[] totalCritters) {
		this.totalCritters = totalCritters;
	}

	public ArrayList<Critter> getDeadCritters() {
		return deadCritters;
	}

	public Item[] getItems() {
		return this.items;
	}

	public String getLastIndicatedStr() {
		return lastIndicatedStr;
	}

	public void setLastIndicatedStr(String lastIndicatedStr) {
		this.lastIndicatedStr = lastIndicatedStr;
	}

	public int getCombinedCompletedActionCount() {
		return combinedCompletedActionCount;
	}

	public void setCombinedCompletedActionCount(int completedActionCount) {
		this.combinedCompletedActionCount = completedActionCount;
	}

	public boolean isBenchActionUsed() {
		return benchActionUsed;
	}

	public void setBenchActionUsed(boolean benchActionUsed) {
		this.benchActionUsed = benchActionUsed;
	}

	public int getPlayerCompletedActionCount() {
		// TODO Auto-generated method stub
		return this.playerCompletedActionCount;
	}

	public void setPlayerCompletedActionCount(int count) {
		// TODO Auto-generated method stub
		this.playerCompletedActionCount = count;
	}

	public void setSession(Session session) {
		this.session = session;

	}

	public Session getSession() {
		return this.session;

	}

	public Action getPerformingAbility() {
		return performingAbility;
	}

	public void setPerformingAbility(Action performingAbility) {
		this.performingAbility = performingAbility;
	}

	public boolean isBot() {
		return isBot;
	}

	public void setBot(boolean isBot) {
		this.isBot = isBot;
	}

	public Critter findCritterAt(String internalPos) {
		// 1. Check living critters first (Priority)
		for (Critter c : critters) {
			if (c != null && c.getPositionKey().equals(internalPos)) {
				return c;
			}
		}

		// 2. If no living critter, check dead ones
		// We sort/filter to find the MOST RECENT death at this spot
		return deadCritters.stream()
				.filter(dc -> dc != null && dc.getPositionKey().equals(internalPos))
				.findFirst() // Or use a timestamp/id to get the latest
				.orElse(null);
	}

	public Critter findActiveCritterAt(String internalPos) {
		// 1. Check living critters first - they always take priority
		for (Critter c : critters) {
			if (c != null && c.isAlive() && c.getSpot().getName().equals(internalPos)) {
				return c;
			}
		}

		// 2. If no living critter, check dead ones (if you keep them in a list)
		// This is useful if the AI needs to see a "corpse" at a location
		if (deadCritters != null) {
			for (Critter dc : deadCritters) {
				if (dc != null && dc.getSpot().getName().equals(internalPos)) {
					return dc;
				}
			}
		}

		return null; // Slot is truly empty
	}

	public PlayerStateDTO toStateDto() {
		List<FighterStateDTO> fixedFighters = new ArrayList<>();

		// Loop through your internal array directly (assuming size is 4)
		for (int i = 0; i < totalCritters.length; i++) {
			Critter c = totalCritters[i];

			if (c != null) {
				FighterStateDTO dto = c.toFighterStateDTO();
				// Ensure the DTO knows exactly which square it is currently on
				dto.setSpot(c.getSpot().getBoardIndex());
				fixedFighters.add(dto);
			} else {
				// Padding if the array slot is null (e.g., player brought fewer than max
				// critters)
				fixedFighters.add(FighterStateDTO.empty(-1));
			}
		}

		// itemsList removed as discussed to reduce feature noise
		return new PlayerStateDTO(side, morale, fixedFighters, new ArrayList<>());
	}

	// private Critter findCritterByBoardIndex(int index) {
	// // Loop through ALL critters owned by this player
	// for (Critter c : critters) {
	// // Match by index - even if HP is 0 or it's 'hidden' on the bench
	// if (c.getSpot().getBoardIndex() == index) {
	// return c;
	// }
	// }
	// return null;
	// }

}
