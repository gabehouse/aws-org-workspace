package com.gabe.animalia.general;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;







import com.gabe.animalia.ability.Bench;
import com.gabe.animalia.ability.Move;
import com.gabe.animalia.ability.Unbench;
import com.gabe.animalia.critter.Bull;
import com.gabe.animalia.critter.Donkey;
import com.gabe.animalia.critter.Dove;
import com.gabe.animalia.critter.Fox;
import com.gabe.animalia.critter.Lion;
import com.gabe.animalia.critter.Newt;
import com.gabe.animalia.critter.Turtle;
import com.gabe.animalia.critter.Wolf;
import com.gabe.animalia.ml.server.GameLogger;
import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.game.PlayerActionInput;
import com.gabe.animalia.ml.game.TurnOrchestrator;
import com.gabe.animalia.ml.game.TwoPlayerGameState;

public class Game {
	private boolean gameOver = false;
	private static final int TRICK_PHASE = 0;
	private static final int ABILITY_PHASE = 1;
	private int gameState = ABILITY_PHASE;
	private int callCount = 0;
	private int playersChosen = 0;
	private String p1Choice = null;
	private RemoteEndpoint p1Endpoint = null;
	private RemoteEndpoint p2Endpoint = null;
	private User user1 = null;
	private User user2 = null;
	private String p1Name = null;
	private String p2Name = null;
	private String str = null;
	private String winnerId = "-1";


	boolean calculatingTurn = false;
	private boolean initialPhase = true;
	private boolean firstInitialPhase = true;
	private boolean vsBot = false;
	private TypesAndStuff tAS = new TypesAndStuff();
	Square leftBottomFront = new Square("leftBottomFront", "left");
	Square leftBottomBack = new Square("leftBottomBack", "left");
	Square leftMiddleFront = new Square("leftMiddleFront", "left");
	Square leftMiddleBack = new Square("leftMiddleBack", "left");
	Square leftTopFront = new Square("leftTopFront", "left");
	Square leftTopBack = new Square("leftTopBack", "left");
	Square leftBench = new Square("leftBench", "left");
	Square rightBottomFront = new Square("rightBottomFront", "right");
	Square rightBottomBack = new Square("rightBottomBack", "right");
	Square rightMiddleFront = new Square("rightMiddleFront", "right");
	Square rightMiddleBack = new Square("rightMiddleBack", "right");
	Square rightTopFront = new Square("rightTopFront", "right");
	Square rightTopBack = new Square("rightTopBack", "right");
	Square rightBench = new Square("rightBench", "right");
	Square[] squares = { leftBottomFront, leftBottomBack, leftMiddleFront,
			leftMiddleBack, leftTopFront, leftTopBack, rightBottomFront,
			rightBottomBack, rightMiddleFront, rightMiddleBack, rightTopFront,
			rightTopBack };

	private Player p1 = new Player(leftBottomFront, leftBottomBack, leftMiddleFront,
			leftMiddleBack, leftTopFront, leftTopBack, leftBench);

	private Player p2 = new Player(rightBottomFront, rightBottomBack, rightMiddleFront,
			rightMiddleBack, rightTopFront, rightTopBack, rightBench);

	//tanks
	Critter critter1 = new Lion("Lion", leftMiddleBack, "left", p1, p2);
	Critter critter2 = new Turtle("Turtle", rightMiddleBack, "right", p2, p1);
	//supports
	Critter critter3 = new Dove("Dove", leftTopBack, "left", p1, p2);
	Critter critter4 = new Donkey("Donkey", leftBench, "left", p1, p2);
	//dps
	Critter critter5 = new Bull("Bull", rightTopBack, "right", p2, p1);
	Critter critter6 = new Newt("Newt", rightBench, "right", p2, p1);
	Critter critter7 = new Fox("Fox", leftBottomBack, "left", p1, p2);
	Critter critter8 = new Wolf("Wolf", rightBottomBack, "right", p2, p1);

	Critter[] critters = {critter1, critter2, critter3, critter4, critter5, critter6, critter7, critter8};

	Item item1;
	Item item2;
	Item item3;
	Item item4;
	Item item5;
	Item item6;
	Item [] items = {item1, item2, item3, item4, item5, item6};
	User [] users = new User[2];

	// for ml
	TurnOrchestrator turnOrchestrator;
	int turnCount;
	int p1InitialHealthSum;
	int p1InitialEnergySum;
	int p2InitialHealthSum;
	int p2InitialEnergySum;
	TwoPlayerGameState initialState;

	boolean botVsBot = false;

	public Game(User user1, User user2, GameLogger gameLogger) {
		if (user2.getID() == -1) vsBot = true;
		if (user2.getID() == -1 && user1.getID() == -1){ botVsBot = true; System.out.println("BOT VS BOT"); }
		this.user1 = user1;
		this.user2 = user2;
		user1.setPlayer(p1);
		user2.setPlayer(p2);
		p1Endpoint = user1.getEndpoint();
		p2Endpoint = user2.getEndpoint();
		p1.setEndpoint(p1Endpoint);
		p2.setEndpoint(p2Endpoint);
		p2.setBot(true);
		users[0] = user1;
		users[1] = user2;
		turnOrchestrator = new TurnOrchestrator(UUID.randomUUID().toString(), gameLogger);
		if (botVsBot) {
			p1.setBot(true);
			runBotVsBot();

		}
	}

	public synchronized void runBotVsBot() {
		while (true) {
			init();
			initialPhase(p1, p2, "left",
				p1.getCritters()[0].getName(), "leftMiddleFront", p1.getCritters()[1].getName(), "leftMiddleBack", p1.getCritters()[2].getName(),
					"leftBottomBack", p1.getCritters()[3].getName(), "leftBench");
			initialPhase(p2, p1, "right",
				p2.getCritters()[0].getName(), "rightMiddleFront", p2.getCritters()[1].getName(), "rightMiddleBack", p2.getCritters()[2].getName(),
					"rightBottomBack", p2.getCritters()[3].getName(), "rightBench");
			while (!gameOver) {
				try {
					int p1HealthSum = getSum(p1, Critter::getHealth);

					int p1EnergySum = getSum(p1, Critter::getEnergy);

					int p2HealthSum = getSum(p2, Critter::getHealth);

					int p2EnergySum = getSum(p2, Critter::getEnergy);

					p1.setTurnStartHealthSum(p1HealthSum);
					p1.setTurnStartEnergySum(p1EnergySum);
					p2.setTurnStartHealthSum(p2HealthSum);
					p2.setTurnStartEnergySum(p2EnergySum);
					initialState = turnOrchestrator.captureInitialState(turnCount, p1, p2);


					calculatingTurn = true;
					p1.randomFillActionQueue(p2);
					p2.randomFillActionQueue(p1);


		turnCount++;

					calculateTurn(p1, p2, 0);
				}	catch (Exception e) {
					turnOrchestrator.recordEmergencySave(turnCount, e.toString());
					// 2. CRITICAL: Stop the loop so it doesn't crash forever
        			gameOver = true;

        // 3. Optional: Print to console so you know why the simulation stopped
        			System.err.println("Simulation aborted due to engine error: " + e.getMessage());
					throw e;
				}
			}
		}

	}

	public synchronized void selectCritter(Player player, Player otherPlayer,
			String name, String side) {




			player.sendString("newcritter");
			player.sendString("option,remove,");
		Critter clickedCritter = null;
		if (side.equals(player.getSide())) {
			clickedCritter = player.identifyCritter(name, side);
			if (clickedCritter == null) {
				clickedCritter = player.identifyDeadCritter(name, side);

			}
		} else {
			clickedCritter = otherPlayer.identifyCritter(name, side);
			if (clickedCritter == null) {
				clickedCritter = otherPlayer.identifyDeadCritter(name, side);
			}
		}
		System.out.println("clicked critter = " + clickedCritter);
		player.setSelectedCritter(clickedCritter);
		player.getSelectedCritter().displayAbilities(player, otherPlayer);
		if (player.getSide().equals(side) && clickedCritter.isAlive()) {
			if (!player.isReadied()) {
				if (player.getSelectedCritter().getTempSpot().getName()
						.contains("Bench")) {
					player.getSelectedCritter().displayUnbenchOptions(player);
				} else {
					player.getSelectedCritter().displayPossibleMoves(player,
							"move");
				}
			}

		}
	}

	public synchronized void unbench(Player player, Player otherPlayer,
			String name, Critter unbenching) {

		if (unbenching.getSide().equals(player.getSide())) {
// System.out.println("target = " + unbenching
// 		.getTempSpot().getSurroundingSquare(name));
			Unbench newUnbench = new Unbench(unbenching, unbenching
					.getTempSpot().getSurroundingSquare(name), player,
					otherPlayer);
			if (player.getAllottedTime() + newUnbench.getTimeCost() <= 10) {
				newUnbench.init();
				player.setLastIndicatedStr(newUnbench.getIndicatorMessage());

					player.sendString(
							"moveQueue,_"
									+ player.iconString(player.getQueue()));
					player.sendString(
							"time," + player.getAllottedTime() + ","
									+ player.getMaxTime());

				sendIndicators(player, player.getSelectedCritter());
				player.getSelectedCritter().displayPossibleMoves(player, "move");

			}
		}
	}

	public synchronized void bench(Player player, Player otherPlayer,
			String name, Critter benching) {
		System.out.println("bench function");

		if (benching.getSide().equals(player.getSide())
				&& player.getCritters().length > 1) {

			Bench newBench = new Bench(benching, benching.getTempSpot()
					.getSurroundingSquare(name), player, otherPlayer);
			if (player.getAllottedTime() + newBench.getTimeCost() <= 10) {
				newBench.init();
				player.setLastIndicatedStr(newBench.getIndicatorMessage());

					player.sendString(
							"moveQueue,_"
									+ player.iconString(player.getQueue()));
					player.sendString(
							"time," + player.getAllottedTime() + ","
									+ player.getMaxTime());

				sendIndicators(player, player.getSelectedCritter());
				if (player.getSelectedCritter().getTempSpot().getName()
						.contains("Bench")) {
					player.getSelectedCritter().displayUnbenchOptions(player);

				} else {
					player.getSelectedCritter().displayPossibleMoves(player,
							"move");
				}

			}
		}
	}

	public synchronized void move(Player player, Player otherPlayer, String name) {
		if (player.getSelectedCritter().getSide().equals(player.getSide())) {
			Action newAction = new Action();
			if (player.getSelectedCritter().getTempSpot().getName().contains("Bench")) {
				if (player.getCritters().length != 4) {
					newAction = new Unbench(player.getSelectedCritter(), player
							.getSelectedCritter().getTempSpot()
							.getSurroundingSquare(name), player, otherPlayer);
				}
			}
			if (player.getAllottedTime() + newAction.getTimeCost() <= 10) {
				newAction = new Move(player.getSelectedCritter(), player
						.getSelectedCritter().getTempSpot()
						.getSurroundingSquare(name), player, otherPlayer);
				newAction.init();
				player.setLastIndicatedStr(newAction.getIndicatorMessage());

					player.sendString(
							"moveQueue,_"
									+ player.iconString(player.getQueue()));
					player.sendString(
							"time," + player.getAllottedTime() + ","
									+ player.getMaxTime());




	//			player.getSelectedCritter().setMoved(true);
				sendIndicators(player, player.getSelectedCritter());
				player.getSelectedCritter().displayPossibleMoves(player, "move");
			}
		}
	}

	public synchronized void initialPhase(Player player, Player otherPlayer,
			String side, String f1Name, String s1Name, String f2Name,
			String s2Name, String f3Name, String s3Name, String f4Name, String s4Name) {
		System.out.println("INITIAL PHASE");

		if (firstInitialPhase) {
			for (Square s : squares) {
				s.setOccupied(false);
				s.setPlannedMove(false);
				s.setCritter(null);
				System.out.println("initial phase clear square " + s.getName());
			}
			firstInitialPhase = false;
		}

		player.setReadied(true);
		Critter f1 = player.identifyCritter(f1Name, side);
		Critter f2 = player.identifyCritter(f2Name, side);
		Critter f3 = player.identifyCritter(f3Name, side);
		Critter f4 = player.identifyCritter(f4Name, side);
		Square s1 = player.identifySquare(s1Name);
		Square s2 = player.identifySquare(s2Name);
		Square s3 = player.identifySquare(s3Name);
		Square s4 = player.identifySquare(s4Name);
		Critter [] fs  = new Critter [] {f1, f2, f3, f4};


		f1.getSpot().setOccupied(false);
		f2.getSpot().setOccupied(false);
		f3.getSpot().setOccupied(false);
		f4.getSpot().setOccupied(false);
		f1.getSpot().setPlannedMove(false);
		f2.getSpot().setPlannedMove(false);
		f3.getSpot().setPlannedMove(false);
		f4.getSpot().setPlannedMove(false);
		f1.setSpot(s1);
		f2.setSpot(s2);
		f3.setSpot(s3);
		f4.setSpot(s4);
		s1.setPlannedMove(true);
		s2.setPlannedMove(true);
		s3.setPlannedMove(true);
		s4.setPlannedMove(true);
		s1.setCritter(f1);
		s2.setCritter(f2);
		s3.setCritter(f3);
		s4.setCritter(f4);
		f1.setTempSpot(s1);
		f2.setTempSpot(s2);
		f3.setTempSpot(s3);
		f4.setTempSpot(s4);
		s1.setOccupied(true);
		s2.setOccupied(true);
		s3.setOccupied(true);
		s4.setOccupied(true);
		for (Critter f : fs) {
			f.getSpot().setPlannedMove(true);
			if (f.getSpot().getName().contains("Bench")) {
				f.setBenched(true);
			}
		}
		if (otherPlayer.isReadied() && !botVsBot) {
			player.setReadied(false);
			otherPlayer.setReadied(false);
			String str = "";
			String playerItemStr = "";
			String otherPlayerItemStr = "";
			//name, description, pos, imageStr
			int pos = 0;
			for (Item i : player.getItems()) {
				playerItemStr += "" + i.getName() + "," + i.getDescription() + "," + pos + "," + i.getImageName() + "|";
				pos++;
			}
			pos = 0;
			for (Item i : otherPlayer.getItems()) {
				otherPlayerItemStr += "" + i.getName() + "," + i.getDescription() + "," + pos + "," + i.getImageName()+ "|";
				pos++;
			}

			for (Critter f : critters) {
				f.getSpot().setOccupied(true);
				str += "," + f.getName() + "," + f.getSide() + "," + f.getSpot();
			}
			player.getBench().getCritter().benchEffect(player, otherPlayer);
			otherPlayer.getBench().getCritter().benchEffect(otherPlayer, player);

			System.out.println("player vs bot fill action queue");
			if (player.isBot())
				player.randomFillActionQueue(otherPlayer);

			if (otherPlayer.isBot())
				otherPlayer.randomFillActionQueue(player);

			for (Critter f : critters) {
				f.sendStats(player, otherPlayer);
			}

				player.sendString("inititems,|" + playerItemStr);
				otherPlayer.sendString("inititems,|" + otherPlayerItemStr);
				player.sendString("initialphaseend" + str);
				otherPlayer.sendString("initialphaseend" + str);
				System.out.println("initialphaseend" + str);
		}
	}

	public synchronized void ready(final Player player, final Player otherPlayer) {
		player.setReadied(true);
		str = "indicatespots,no moves";

			player.sendString(str);
			str = "";

		if (otherPlayer.isReadied() || vsBot) {

			// record ml start state
			int playerHealthSum = getSum(player, Critter::getHealth);

			int playerEnergySum = getSum(player, Critter::getEnergy);

			int otherPlayerHealthSum = getSum(otherPlayer, Critter::getHealth);

			int otherPlayerEnergySum = getSum(otherPlayer, Critter::getEnergy);

			player.setTurnStartHealthSum(playerHealthSum);
			player.setTurnStartEnergySum(playerEnergySum);
			otherPlayer.setTurnStartHealthSum(otherPlayerHealthSum);
			otherPlayer.setTurnStartEnergySum(otherPlayerEnergySum);
			initialState = turnOrchestrator.captureInitialState(turnCount, p1, p2);


			calculatingTurn = true;


				player.sendString("transition,begin");
				otherPlayer.sendString("transition,begin");
				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					public void run() {
						System.out.println(getGameState());
						System.out.println(stringifyActionQueues());
						calculateTurn(player, otherPlayer, System.currentTimeMillis());
						System.out.println(getGameState());
					}
				}, 750);


		}
	}

	public synchronized void updateActionQueue(Player player,
			Player otherPlayer, String operation, String pos1, String pos2) {
		ArrayList<Action> actionQueue = null;
		actionQueue = player.getQueue();
		if (operation.equals("remove")) {
			Action actionToRemove = actionQueue.get(Integer.parseInt(pos1));
			player.setAllottedTime(player.getAllottedTime()
					- actionToRemove.getTimeCost());


				player.sendString(
						"time," + player.getAllottedTime() + ","
								+ player.getMaxTime());

			actionToRemove.delete();
			actionQueue.remove(Integer.parseInt(pos1));
			sendIndicators(player, player.getSelectedCritter());
			if (player.
					getSelectedCritter().
					getTempSpot().
					getName()
					.contains("Bench")) {
				player.getSelectedCritter().displayUnbenchOptions(player);

			} else {
				player.getSelectedCritter()
						.displayPossibleMoves(player, "move");
			}
		} else if (operation.equals("switch")) {
			actionQueue.add(Integer.parseInt(pos2),
					actionQueue.remove(Integer.parseInt(pos1)));
		}

	}

	public synchronized void useAbility(Player player, Player otherPlayer,
			String type, String targetName, String side) {
		Targetable target = null;

		Action ability = player.getSelectedAbility().getNew();
		if (player.getAllottedTime() + ability.getTimeCost() <= 10) {
			ability.setOtherPlayer(otherPlayer);
			ability.setPlayer(player);
			ability.setSubject(player.getSelectedCritter());
			if (type.equals(player.getSelectedAbility().getTargetType())) {
				if (type.equals("spot")) {

						target = player.identifySquare(targetName);
					if (target == null) {
						target = otherPlayer.identifySquare(targetName);
					}
					if (target != null) {
						ability.setTarget(target);
						if (ability.initable()) {
							ability.init();
							player.setAllottedTime(player.getAllottedTime()
									+ ability.getTimeCost());
							player.setLastIndicatedStr(ability.getIndicatorMessage());
							sendIndicators(player, player.getSelectedCritter());

								player.sendString(
										"option,remove,"
												+ ability.getName());

							player.getSelectedCritter().displayPossibleMoves(player,
									"move");
						} else {

							if (player.getSelectedAbility().getTargetType()
									.equals("critter")) {


									player.sendString(
											"option,remove,"
													+ player.getSelectedAbility().getName());


							} else if (player.getSelectedAbility().getTargetType()
									.equals("spot")) {
								player.getSelectedCritter().displayPossibleMoves(player,
 "move");
							}
							player.setSelectedAbility(null);
						}

					} else {
						player.setSelectedAbility(null);
					}
				} else {
					if (side.equals(player.getSide())) {
						target = player.identifyCritter(targetName, side);
					} else {
						target = otherPlayer.identifyCritter(targetName, side);
					}
					if (ability.initable()) {
						ability.setTarget(target);
						ability.init();
						player.setAllottedTime(player.getAllottedTime()
								+ ability.getTimeCost());
						player.setLastIndicatedStr(ability
								.getIndicatorMessage());
						sendIndicators(player, player.getSelectedCritter());
					}
				}

					player.sendString(
							"moveQueue,_"
									+ player.iconString(player.getQueue()));
					player.sendString(
							"time," + player.getAllottedTime() + ","
									+ player.getMaxTime());

			}
		} else {

				if (player.getSelectedAbility().getTargetType()
						.equals("critter")) {

					player.sendString(
							"option,remove,"
									+ player.getSelectedAbility().getName());

				} else if (player.getSelectedAbility().getTargetType()
						.equals("spot")
						&& !player.getSelectedCritter().hasMoved()) {
					player.getSelectedCritter().displayPossibleMoves(player,
							"move");
				}
		}

		player.setSelectedAbility(null);

	}

	public synchronized void selectAbility(Player player, Player otherPlayer,
			String name) {


			player.sendString("option,remove,");

		if (player.getSelectedCritter().identifyAbility(name) != null) {
			player.setSelectedAbility(player.getSelectedCritter()
					.identifyAbility(name));
		}

		Action ability = player.getSelectedAbility().getNew();
		ability.setPlayer(player);
		ability.setSubject(player.getSelectedCritter());
		ability.setOtherPlayer(otherPlayer);

		ability.displayOptions();

		if (player.getSelectedAbility().getTargetType().equals("targetless")
				&& player.getAllottedTime() + ability.getTimeCost() <= 10) {

			ability.init();
			player.setLastIndicatedStr(ability.getIndicatorMessage());
			sendIndicators(player, player.getSelectedCritter());


				player.sendString(
						"moveQueue,_" + player.iconString(player.getQueue()));
				player.setAllottedTime(player.getAllottedTime()
						+ ability.getTimeCost());
				player.sendString(
						"time," + player.getAllottedTime() + ","
								+ player.getMaxTime());


			player.setSelectedAbility(null);


				player.sendString("option,remove," + name);

		}

	}

	public synchronized void useItem(String action, String name, String second, Player player) {
		if (action.equals("add")) {

			Item toAdd = player.identifyItem(name);
			toAdd.setSecond(Integer.parseInt(second));
			player.getItemQueue().add(toAdd);
		} else if (action.equals("remove")) {
			Item toRemove = player.identifyItem(name);
			player.getItemQueue().remove(toRemove);
		}
	}

	public synchronized String stringifyActionQueues() {
		String str = "";
		for (Action a : p1.getQueue()) {
			str += a.getName() + "-" + a.getSubject().getName() + "-" + a.getTarget().getName() + '|';
		}
		str += "_";
		for (Action a : p2.getQueue()) {
			str += a.getName() + "-" + a.getSubject().getName() + "-" + a.getTarget().getName() + '|';
		}
		return str;
	}

	public synchronized String getGameState() {

		String gameState = "";

		Map<Critter,Integer> fighterEnum = new HashMap<>();

		for (int i = 0; i < critters.length; i++) {
			fighterEnum.put(critters[i], i);
		}
		Map<Square,Integer> squareEnum = new HashMap<>();
		for (int i = 0; i < squares.length; i++) {
			squareEnum.put(squares[i], i);
		}
		Map<String,Integer> effectsEnum = new HashMap<>();
		for (int i = 0; i < tAS.getAllEffects().length; i++) {
			effectsEnum.put(tAS.getAllEffects()[i], i);
		}

		for (Critter c : p1.getCritters()) {
			gameState += c.getName();
			gameState += '-';
			gameState += c.getSpot();
			gameState += '-';
			gameState += c.getHealth();
			gameState += '-';
			gameState += c.getEnergy();
			gameState += "-_";
			for (Action a : c.getEffects()) {
				gameState += a.getName();
				gameState += ".";
				gameState += a.getDuration();
				gameState += "|";
			}
			gameState += "_-";
		}
		for (Item i : p1.getItems()) {
			gameState += i.getName();
		}
		gameState += '/';
		for (Critter c : p2.getCritters()) {
			gameState += c.getName();
			gameState += '-';
			gameState += c.getSpot();
			gameState += '-';
			gameState += c.getHealth();
			gameState += '-';
			gameState += c.getEnergy();
			gameState += "-_";
			for (Action a : c.getEffects()) {
				gameState += a.getName();
				gameState += ".";
				gameState += a.getDuration();
				gameState += "|";
			}
			gameState += "_-";
		}
		for (Item i : p2.getItems()) {
			gameState += i.getName() + ".";
		}
		return gameState;
	}
	public synchronized void update(RemoteEndpoint endpoint, String message) {
		System.out.println(message);
		String tokens[] = message.split(",");
		Player player;
		Player otherPlayer;

		if (endpoint.equals(p1.getEndpoint())) {
			player = p1;
			otherPlayer = p2;
		} else {
			player = p2;
			otherPlayer = p1;
		}
		if (tokens[0].equals("click")) {
			if ((player.identifyCritter(tokens[2], player.getSide()) != null || otherPlayer
					.identifyCritter(tokens[2], otherPlayer.getSide()) != null)
					&& player.getSelectedAbility() == null) {
				selectCritter(player, otherPlayer, tokens[2], tokens[3]);
			} else if (player.getSelectedCritter() != null
					&& (player.getSelectedCritter().getTempSpot()
							.compareSurrounding(tokens[2]))
					&& !player.isReadied()
					&& player.getSelectedAbility() == null) {
				System.out.println(tokens[2]);
				if (tokens[2].contains("Bench")) {
					System.out.println("calling bench");

					bench(player, otherPlayer, tokens[2], player.getSelectedCritter());
					// player.setSelectedCritter(player.identifyCritter(,
					// side));
				} else if (!player.getSelectedCritter().getTempSpot()
						.getSurroundingSquare(tokens[2]).isPlannedMove()) {
					move(player, otherPlayer, tokens[2]);
				} else if (player.getSelectedCritter().getTempSpot().getName()
						.contains("Bench")) {
//					Critter unbenching = null;
//					Square targetSpot = player.identifySquare(tokens[2]);
//					for (Critter c : player.getCritters()) {
//						if (c.getTempSpot().equals(targetSpot)) {
//							unbenching = c;
//						}
//					}
//					if (benching != null) {
						unbench(player, otherPlayer, tokens[2], player.getSelectedCritter());
	//				}

				}

			} else if (tokens[1].equals("ready")) {
				if (gameOver) {
					restartGame();
				} else if (!player.isReadied()) {
					ready(player, otherPlayer);
				}

			} else if (tokens[1].equals("item") && !player.isReadied()) {
				useItem(tokens[2], tokens[3], tokens[4], player);
			} else if (tokens[1].equals("actionqueue") && !player.isReadied()) {
				updateActionQueue(player, otherPlayer, tokens[2], tokens[3],
						tokens[4]);
			} else if (player.getSelectedCritter() != null
					&& player.getSelectedAbility() != null
					&& !player.isReadied()) {
				if (tokens[1].equals(player.getSelectedAbility()
						.getTargetType())) {
					if (!tokens[2].contains("Bench")) {
						Critter f = player
								.identifyCritter(tokens[2], tokens[3]);
						if (f == null) {
							f = otherPlayer.identifyCritter(tokens[2],
									tokens[3]);
						}
						if (f == null) {
							f = player.identifyDeadCritter(tokens[2],
									tokens[3]);
						}
						if (f == null) {
							f = otherPlayer.identifyDeadCritter(tokens[2],
									tokens[3]);
						}


						if (f != null) {
							if (f.isAlive()) {
								useAbility(player, otherPlayer, tokens[1],
										tokens[2], tokens[3]);
							} else {

									if (player.getSelectedCritter().hasMoved()) {
										player.sendString("possiblemoves, ");
										player.sendString(
												"indicatespots,no moves");
									}
									if (player.getSelectedAbility().getTargetType()
											.equals("critter")) {
										player.sendString(
												"option,remove,"
														+ player.getSelectedAbility()
																.getName());
									} else if (player.getSelectedAbility().getTargetType()
											.equals("spot")
											&& !player.getSelectedCritter().hasMoved()) {
										player.getSelectedCritter().displayPossibleMoves(
												player, "move");
									}
								player.setSelectedAbility(null);
							}
						} else {
							useAbility(player, otherPlayer, tokens[1],
									tokens[2], tokens[3]);
						}
					}
				} else {


						if (player.getSelectedCritter().hasMoved()) {
							player.sendString("possiblemoves, ");
							player.sendString(
									"indicatespots,no moves");
						}
						if (player.getSelectedAbility().getTargetType()
								.equals("critter")) {
							player.sendString(
									"option,remove,"
											+ player.getSelectedAbility()
													.getName());
						} else if (player.getSelectedAbility().getTargetType()
								.equals("spot")
								&& !player.getSelectedCritter().hasMoved()) {
							player.getSelectedCritter().displayPossibleMoves(
									player, "move");
						}
					player.setSelectedAbility(null);
					// player.getSelectedCritter().displayPossibleMoves(player);
				}
			} else if (player.getSelectedCritter() != null
					&& tokens[1].equals("ability") && !player.isReadied()) {
				selectAbility(player, otherPlayer, tokens[3]);
			} else if (tokens[1].equals("initialposition")
					&& !player.isReadied()) {
				if (vsBot) {
					initialPhase(otherPlayer, player, "right",
					otherPlayer.getCritters()[0].getName(), "rightMiddleFront", otherPlayer.getCritters()[1].getName(), "rightMiddleBack", otherPlayer.getCritters()[2].getName(),
						"rightBottomBack", otherPlayer.getCritters()[3].getName(), "rightBench");
				}
				initialPhase(player, otherPlayer, tokens[2], tokens[3],
						tokens[4], tokens[5], tokens[6], tokens[7], tokens[8],
						tokens[9], tokens[10]);
			// 			initialPhase(Player player, Player otherPlayer,
			// String side, String f1Name, String s1Name, String f2Name,
			// String s2Name, String f3Name, String s3Name, String f4Name, String s4Name)

			}
		} else if (tokens[0].equals("chat")) {

				player.sendString("chat,you," + tokens[1]);
				otherPlayer.sendString(
						"chat," + player.getUsername() + "," + tokens[1]);

		} else if (tokens[0].equals("select")) {
			System.out.println("select");
			selectCritters(player, otherPlayer, tokens[1], tokens[2], tokens[3], tokens[4]);
			selectItems(player, otherPlayer, tokens[5], tokens[6], tokens[7]);
		} else if (tokens[0].equals("mouseover")) {
			// vv if i ever wanna do specific indicators to hovered over critter vv

//			if (tokens[1].equals("critter")) {
//				if (tokens[4].equals(player.getSide())) {
//					Critter critter = player.identifyCritter(tokens[3],
//							tokens[4]);
//					String str = "critteractions,|"
//							+ critter.getIndicationStr();
//
//						player.sendString(str);
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
		} else if (tokens[0].equals("calculate")) {
			calculateTurn(player, otherPlayer, System.currentTimeMillis());
// 1. Transform Player 1's Queue
// ml record actions and end state

		}
	}

	public synchronized void restartGame() {
		System.out.println("RESTART GAME");
		p1.getQueue().clear();
		p2.getQueue().clear();
		firstInitialPhase = true;
	}

	// Critter critter1 = new Fox("Fox", leftBottomBack, "left", p1, p2);
	public synchronized void selectCritters(Player player, Player otherPlayer,
			String critter1Name, String critter2Name, String critter3Name, String critter4Name) {
				System.out.println("start seletcritters()");
		String[] critterNames = { critter1Name, critter2Name, critter3Name, critter4Name };
		Critter[] selectedCritters = new Critter[4];
		for (int i = 0; i < 4; i++) {
			if (critterNames[i].equals("fox")) {
				selectedCritters[i] = new Fox("Fox", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("dove")) {
				selectedCritters[i] = new Dove("Dove", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("lion")) {
				selectedCritters[i] = new Lion("Lion", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("turtle")) {
				selectedCritters[i] = new Turtle("Turtle", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("donkey")) {
				selectedCritters[i] = new Donkey("Donkey", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("wolf")) {
				selectedCritters[i] = new Wolf("Wolf", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("bull")) {
				selectedCritters[i] = new Bull("Bull", leftBottomBack,
						player.getSide(), player, otherPlayer);
			} else if (critterNames[i].equals("newt")) {
				selectedCritters[i] = new Newt("Newt", leftBottomBack,
						player.getSide(), player, otherPlayer);
			}
		}
		player.setCritters(selectedCritters);

		if (otherPlayer.getCritters() != null) {
			critters[0] = otherPlayer.getCritters()[0];
			critters[1] = otherPlayer.getCritters()[1];
			critters[2] = otherPlayer.getCritters()[2];
			critters[3] = otherPlayer.getCritters()[3];
			critters[4] = player.getCritters()[0];
			critters[5] = player.getCritters()[1];
			critters[6] = player.getCritters()[2];
			critters[7] = player.getCritters()[3];
			for (int i = 0; i < critters.length; i++) {
				critters[i].setId(Integer.toString(i));
			}
			String str = "critterselect,|";
			for (Critter f : critters) {
				str += f.getName() + "," + f.getMaxHealth() + ","
						+ f.getMaxEnergy() + "," + f.getSide() + ","
						+ f.getPassiveName() + "," + f.getPassiveCritterDescription() + "|";
			}

				player.sendString(str);
				otherPlayer.sendString(str);
				player.sendString("showgrid,left");
				otherPlayer.sendString("showgrid,right");
				System.out.println("end of selecrtcritters() " + str);


		}
	}

	public synchronized void selectItems(Player player, Player otherPlayer,
			String item1Name, String item2Name, String item3Name) {

		player.setItem1(player.identifyTotalItems(item1Name));
		player.getItem1().setPlayer(player);
		player.getItem1().setOtherPlayer(otherPlayer);
		player.setItem2(player.identifyTotalItems(item2Name));
		player.getItem2().setPlayer(player);
		player.getItem2().setOtherPlayer(otherPlayer);
		player.setItem3(player.identifyTotalItems(item3Name));
		player.getItem3().setPlayer(player);
		player.getItem3().setOtherPlayer(otherPlayer);
	}

	public synchronized void calculateTurn(Player player, Player otherPlayer,
			double startTime) {
			System.out.println("CALCULATE TURN");
			calculatingTurn = true;
			player.sendString("calculating,begin");
			otherPlayer.sendString("calculating,begin");

		for (int i = 0; i < player.getItemQueue().size(); i++) {

				otherPlayer.sendString(
						"itemdisplay" + ","
								+ player.getItemQueue().get(i).getName() + ","
								+ player.getItemQueue().get(i).getDescription()
								+ ",2,"
								+ player.getItemQueue().get(i).getImageName()
								+ ","
								+ player.getItemQueue().get(i).getSecond());

		}

		for (int i = 0; i < otherPlayer.getItemQueue().size(); i++) {

				player.sendString(
								"itemdisplay"
										+ ","
										+ otherPlayer.getItemQueue().get(i)
												.getName()
										+ ","
										+ otherPlayer.getItemQueue().get(i)
												.getDescription()
										+ ",2,"
										+ otherPlayer.getItemQueue().get(i)
												.getImageName()
										+ ","
										+ otherPlayer.getItemQueue().get(i)
												.getSecond());


		}


			player.sendString(
					"moveQueue,_" + player.iconString(player.getQueue())
							+ "_380");
			player.sendString(
					"moveQueue,_"
							+ otherPlayer.iconString(otherPlayer.getQueue())
							+ "_430");
			otherPlayer.sendString(
					"moveQueue,_" + player.iconString(player.getQueue())
							+ "_430");
			otherPlayer.sendString(
					"moveQueue,_"
							+ otherPlayer.iconString(otherPlayer.getQueue())
							+ "_380");


		int time = 0;
		ArrayList<Action> playerQueue = new ArrayList<Action>();
		ArrayList<Action> otherPlayerQueue = new ArrayList<Action>();
		ArrayList<Action> playersActions = new ArrayList<Action>();
		playerQueue.addAll(player.getQueue());
		otherPlayerQueue.addAll(otherPlayer.getQueue());

		int opa = 0;
		int pa = 0;
		int opi = 0;
		int pi = 0;
		double pt = 0;
		double opt = 0;
		int h = 0;
		if (!player.getQueue().isEmpty() && !otherPlayer.getQueue().isEmpty()) {
			player.getQueue().get(0).setDisplayOrder(0);
			otherPlayer.getQueue().get(0).setDisplayOrder(1);
			h += 2;
		} else if (!player.getQueue().isEmpty()) {
			player.getQueue().get(0).setDisplayOrder(0);
			h++;
		} else if (!otherPlayer.getQueue().isEmpty()) {
			otherPlayer.getQueue().get(0).setDisplayOrder(0);
			h++;
		}
		for (; h < player.getQueue().size()*2
				+ otherPlayer.getQueue().size()*2 + player.getItemQueue().size()
				+ otherPlayer.getItemQueue().size(); h++) {

			Player prioritizedPlayer = player;
			double prioritizedTime = 0;
			Action prioritizedAction = null;
			if (player.getItemQueue().size() > pi) {
				prioritizedAction = player.getItemQueue().get(pi);
				prioritizedTime = player.getItemQueue().get(pi).getSecond();
			}
			if (otherPlayer.getItemQueue().size() > opi) {
				if (prioritizedAction == null || otherPlayer.getItemQueue().get(opi).getSecond() < prioritizedTime) {
					prioritizedAction = otherPlayer.getItemQueue().get(opi);
					prioritizedPlayer = otherPlayer;
					prioritizedTime = otherPlayer.getItemQueue().get(opi).getSecond();
				} else if (otherPlayer.getItemQueue().get(opi).getSecond() == prioritizedTime
						&& new Random().nextInt(10) < otherPlayer.getMorale()) {
					prioritizedAction = otherPlayer.getItemQueue().get(opi);
					prioritizedPlayer = otherPlayer;
					prioritizedTime = otherPlayer.getItemQueue().get(opi)
							.getSecond();

				}
			}
			if (otherPlayer.getQueue().size() > opa) {
				if (prioritizedAction == null
						|| otherPlayer.getQueue().get(opa).getTimeCost() + opt < prioritizedTime) {
					prioritizedAction = otherPlayer.getQueue().get(opa);
					prioritizedPlayer = otherPlayer;
					prioritizedTime = opt
							+ otherPlayer.getQueue().get(opa).getTimeCost();
				}
//				else if (otherPlayer.getQueue().get(opa).getTimeCost() + opt == prioritizedTime
//						&& prioritizedAction.getSubject() != null
//						&& new Random().nextInt(10) < otherPlayer.getMorale()) {
//					prioritizedAction = otherPlayer.getQueue().get(opa);
//					prioritizedPlayer = otherPlayer;
//					prioritizedTime = opt
//							+ otherPlayer.getQueue().get(opa).getTimeCost();
//
//				}
			}
			if (player.getQueue().size() > pa) {
				if (prioritizedAction == null
						|| player.getQueue().get(pa).getTimeCost() + pt < prioritizedTime) {
					prioritizedAction = player.getQueue().get(pa);
					prioritizedPlayer = player;
					prioritizedTime = pt
							+ player.getQueue().get(pa).getTimeCost();
				} else if (player.getQueue().get(pa).getTimeCost() + pt == prioritizedTime
						&& prioritizedAction.getSubject() != null
						&& new Random().nextInt(10) < player.getMorale()) {
					prioritizedAction = player.getQueue().get(pa);
					prioritizedPlayer = player;
					prioritizedTime = pt
							+ player.getQueue().get(pa).getTimeCost();

				}
			}
			prioritizedAction.setOrder(h);

			if (player.getQueue().size() > pa
					&& prioritizedAction.equals(player.getQueue().get(pa))) {
				if (player.getQueue().size() > pa + 1) {
					h++;
					player.getQueue().get(pa + 1).setDisplayOrder(h);
				}
				pa++;
			} else if (otherPlayer.getQueue().size() > opa && prioritizedAction.equals(otherPlayer.getQueue().get(opa))) {
				if (otherPlayer.getQueue().size() > opa + 1) {
					h++;
					otherPlayer.getQueue().get(opa + 1).setDisplayOrder(h);
				}
				opa++;
			} else if (otherPlayer.getItemQueue().size() > opi && prioritizedAction.equals(otherPlayer.getItemQueue().get(opi))) {
				opi++;
			} else if (player.getItemQueue().size() > pi && prioritizedAction.equals(player.getItemQueue().get(pi))) {
				pi++;
			}

			playersActions.add(prioritizedAction);
			if (prioritizedPlayer.equals(player)) {
				pt += prioritizedAction.getTimeCost();
			} else {
				opt += prioritizedAction.getTimeCost();
			}
		}
//		for (int i = 0; i < player.getQueue().size(); i++) {
//			player.getQueue().get(i).setPlayerOrder
//		}
		for (int i = 0; i < playersActions.size(); i++) {
			System.out.println(playersActions.get(i).getName() + ", subject = " + playersActions.get(i).getSubject() + ", target = " + playersActions.get(i).getTarget() + ", "  + playersActions.get(i).getOrder());
		}
	//	if (new Random().nextInt(10) < player.getMorale()) {
		if (botVsBot) {
			System.out.println("BOT VS BOT PERFORMING");
			for (Action action : playersActions) {

					if (!(action.isInterrupted() || (action.getTargetType() == "critter" && ((Critter)action.getTarget()).getSpot().getName().contains("Bench"))) && (action.getType() == "item" || action.getEnergyCost() <= action.getSubject().getEnergy())) {
						if (action.getSubject() == null) {
							System.out.println("item perform: " + action.getName());
							action.perform();
						} else if ((!action.getSubject().isBenched() || (action
								.getName().equals("Unbench") && action
								.isStartPerformable()))
								&& !(action.getTargetType().equals("critter") && !((Critter) action
										.getTarget()).isAlive())) {
							action.getSubject().onAction(player, otherPlayer, action);

							boolean performable = action.performable();
							System.out.println("perform 2" + action.getName() + ", " + action.getSubject() + ", " + action.getTarget().getName() + ", " + action.getOrder() );
							for (Square s : otherPlayer.getSquares()) {
								System.out.println(s.isOccupied());
							}
							action.perform();
							player.setPerformingAbility(null);
							if (action.getStatusType() == null
									&& !action.getName().equals("Unbench")
									&& performable) {
								action.getSubject().unstealth(otherPlayer);
							} else if (action.getStatusType() != null) {
								for (String s : action.getStatusType().split(",")) {
									if (!(s.equals("invisible") && action.getSubject()
											.equals(action.getTarget()))
											&& !action.getName().equals("Unbench")
											&& performable) {
										action.getSubject().unstealth(otherPlayer);
									}
								}
							}
						}
					}
				checkDeath();
				checkGameOver();
				if (gameOver) return;
			}
			endTurn();
		} else {

			int i = 0;
			int j = 0;

			// test

			final Timer timer = new Timer();


			for (int r = 0; r < player.getItemQueue().size(); r++) {

				final AbilityTimer itemTimer = new AbilityTimer(player
						.getItemQueue().get(r), player, otherPlayer, r, this);
				timer.schedule(new TimerTask() {
					public void run() {
						itemTimer.performAction(timer);
					}
				}, (long) (player.getItemQueue().get(r).getSecond() * 1000));

			}

			for (int r = 0; r < otherPlayer.getItemQueue().size(); r++) {

				final AbilityTimer itemTimer = new AbilityTimer(otherPlayer
						.getItemQueue().get(r), otherPlayer, player, r, this);
				timer.schedule(new TimerTask() {
					public void run() {
						itemTimer.performAction(timer);
					}
				}, (long) (otherPlayer.getItemQueue().get(r).getSecond() * 1000));

			}

			for (int z = 0; z < player.getQueue().size(); z++) {
				double tempDelay = 0;
				for (int k = 0; k <= z; k++) {
					tempDelay += player.getQueue().get(k).getTimeCost();
				}
				final double performDelay = tempDelay;
				final double animateDelay = tempDelay
						- player.getQueue().get(z).getTimeCost();
				final AbilityTimer abilityTimer = new AbilityTimer(player
						.getQueue().get(z), player, otherPlayer, z, this);



				timer.schedule(new TimerTask() {
					public void run() {
						abilityTimer.performAction(timer);
					}
				}, (long) (performDelay * 1000));

			timer.schedule(new TimerTask() {
					public void run() {
						abilityTimer.animate(timer);
					}
				}, (long) (animateDelay * 1000));
			}

			for (int z = 0; z < otherPlayer.getQueue().size(); z++) {
				double tempDelay = 0;
				int tieBreaker = 0;
				for (int k = 0; k <= z; k++) {
					tempDelay += otherPlayer.getQueue().get(k).getTimeCost();
				}

				if (new Random().nextInt(10) < player.getMorale()) {
					tieBreaker = 10;
				} else {
					tieBreaker = -10;
				}

				final double performDelay = tempDelay;
				final double animateDelay = tempDelay
						- otherPlayer.getQueue().get(z).getTimeCost();
				final AbilityTimer abilityTimer = new AbilityTimer(otherPlayer
						.getQueue().get(z), otherPlayer, player, z, this);

				timer.schedule(new TimerTask() {
					public void run() {
						abilityTimer.animate(timer);
					}
				}, (long) (animateDelay * 1000));
				timer.schedule(new TimerTask() {
					public void run() {
						abilityTimer.performAction(timer);
					}
				}, (long) (performDelay * 1000 + tieBreaker));

			}
			final AbilityTimer abilityTimer = new AbilityTimer(null, otherPlayer,
					player, 0, this);
			timer.schedule(new TimerTask() {
				public void run() {
					endTurn();
				}
			}, 10 * 1100);
		}

	}

public void endTurn() {
	System.out.println("END TURN");
	if (gameOver) return;

			for (Critter f : p1.getCritters()) {
				if (f.isBenched()) {
					f.benchedEffect(p1, p2);
				}
			}

			for (Critter f : p1.getCritters()) {
				f.triggerEffects();
			}
			for (Critter f : p2.getCritters()) {
				f.triggerEffects();
			}
			for (Critter f : p1.getCritters()) {
				f.effectDuration();
			}
			for (Critter f : p2.getCritters()) {
				f.effectDuration();
			}

			checkDeath();
			endTurnLog();
			checkGameOver();

			if (!getGameOver()) {
				System.out.println("CLEARING QUEUES");
				p1.setBenchActionUsed(false);
				p2.setBenchActionUsed(false);
				p1.getItemQueue().clear();
				p2.getItemQueue().clear();
				p1.setSelectedAbility(null);
				p1.setSelectedCritter(null);
				p2.setSelectedAbility(null);
				p2.setSelectedCritter(null);
				p1.getQueue().clear();
				p2.getQueue().clear();
				p1.setReadied(false);
				p2.setReadied(false);
				p1.setUsedTime(0);
				p2.setUsedTime(0);
				p1.setAllottedTime(0);
				p2.setAllottedTime(0);
				p1.setPlayerCompletedActionCount(0);
				p2.setPlayerCompletedActionCount(0);
				p1.setCombinedCompletedActionCount(0);
				p2.setCombinedCompletedActionCount(0);
				for (Square s : p1.getSquares()) {
					if (s.isOccupied()) {
						s.setPlannedMove(true);
						Critter occupier = null;
						for (Critter c : p1.getCritters()) {
							if (c.getSpot().equals(s)) {
								occupier = c;
								c.setTempSpot(c.getSpot());
							}
						}
						s.setCritter(occupier);
					} else {
						s.setCritter(null);
						s.setPlannedMove(false);
					}
				}
				for (Square s : p2.getSquares()) {
					if (s.isOccupied()) {
						s.setPlannedMove(true);
						Critter occupier = null;
						for (Critter c : p2.getCritters()) {
							if (c.getSpot().equals(s)) {
								occupier = c;
								c.setTempSpot(c.getSpot());
							}
						}
						s.setCritter(occupier);
					} else {
						s.setCritter(null);
						s.setPlannedMove(false);
					}
				}

					p1.sendString("reset");
					p2.sendString("reset");
				for (Item i : p1.getItems()) {
					i.setPerformable(true);
				}
				for (Item i : p2.getItems()) {
					i.setPerformable(true);
				}
				for (Critter f : p1.getCritters()) {
					f.setMoved(false);
					f.getIndicatedMoves().clear();
					f.getIndicatedAttacks().clear();
					f.getIndicatedBlocks().clear();
					f.getIndicatedSupports().clear();
					f.getMoves().clear();
					if (f.isBenched()) {
						f.setEnergy(f.getEnergy() + 30);
					} else {
						f.setEnergy(f.getEnergy() + 10);
					}
					f.setResting(true);
					f.setPlasterved(false);
					f.sendStats(p1, p2);
				}
				for (Critter f : p2.getCritters()) {
					f.setMoved(false);
					f.getIndicatedMoves().clear();
					f.getIndicatedAttacks().clear();
					f.getIndicatedSupports().clear();
					f.getIndicatedBlocks().clear();
					f.getMoves().clear();
					if (f.isBenched()) {
						f.setEnergy(f.getEnergy() + 30);
					} else {
						f.setEnergy(f.getEnergy() + 10);
					}
					f.setResting(true);
					f.setPlasterved(false);
					f.sendStats(p1, p2);
				}
				if (!botVsBot) {
					System.out.println(" player bot?: " + p1.isBot());
					if (p1.isBot()) {
						System.out.println("ability timer random fill");
						p1.randomFillActionQueue(p2);
					}
					if (p2.isBot()) {
						System.out.println("ability timer random fill");
						p2.randomFillActionQueue(p1);
					}
				}

					p1.sendString("calculating,end");
					p2.sendString("calculating,end");
			}


	}


	public synchronized void endTurnLog () {
		System.out.println("ENDTURNLOG");

			List<LoggableActionDTO> p1Actions = p1.getQueue().stream()
    			.map(Action::toLoggableDTO)
    			.collect(Collectors.toList());


// 3. Transform Player 2's Queue
			List<LoggableActionDTO> p2Actions = p2.getQueue().stream()
    			.map(Action::toLoggableDTO)
    			.collect(Collectors.toList());

			System.out.println("p1 actions size = " + p1Actions.size());



			int p1HealthSum = getSum(p1, Critter::getHealth);

			int p2HealthSum = getSum(p2, Critter::getHealth);

			int p1EnergySum = getSum(p1, Critter::getEnergy);

			int p2EnergySum = getSum(p2, Critter::getEnergy);

			int p1HealthDelta = p1HealthSum - p1.getTurnStartHealthSum();
			int p1EnergyDelta = p1EnergySum - p1.getTurnStartEnergySum();
			int p2HealthDelta = p2HealthSum - p2.getTurnStartHealthSum();
			int p2EnergyDelta = p2EnergySum - p2.getTurnStartEnergySum();

//PlayerActionInput p2Input = new PlayerActionInput("2", p2Actions);
			turnOrchestrator.recordTurnTransition(
			turnCount,
			initialState,
			p1,
			p2,
			p1Actions,
			p2Actions,
			p1HealthDelta,
			p1EnergyDelta,
			p2HealthDelta,
			p2EnergyDelta
		);

		turnCount++;
	}

	public synchronized void sendIndicators(Player player, Critter selected) {
		String str = "indicate,|";
		for (Critter f : player.getCritters()) {
			str += f.getIndicationStr();
		}
		str += "~" + player.getLastIndicatedStr();

			player.sendString(str);

	}

public void checkDeath() {
	System.out.println("CHECK DEATH");
		for (Critter pf : p1.getCritters()) {

			if (pf.getHealth() <= 0) {
				if (pf.isPlasterved()) {
					pf.setHealth(1);
				} else {
					System.out.println("Death Found");
					pf.setAlive(false);
					pf.setEnergy(0);
					pf.getSpot().setOccupied(false);
					pf.getSpot().setCritter(null);
					pf.getSpot().setPlannedMove(false);
					pf.getEffects().clear();
					p1.getDeadCritters().add(pf);
					Critter[] newPlayerCritters = new Critter[0];

					if (p1.getCritters().length > 0) {
						newPlayerCritters = new Critter[p1.getCritters().length - 1];
					}

					int q = 0;
					for (Critter f : p1.getCritters()) {
						if (!f.equals(pf)) {
							newPlayerCritters[q] = f;
							q++;
						}
					}

					p1.setCritters(newPlayerCritters);

						p2.sendString(
								"dead," + pf.getName() + "," + pf.getSide());
						p1.sendString(
								"dead," + pf.getName() + "," + pf.getSide());
				}
			}
		}
		for (Critter of : p2.getCritters()) {
			if (of.getHealth() <= 0) {
				if (of.isPlasterved()) {
					of.setHealth(1);
				} else {
					of.setAlive(false);
					of.setEnergy(0);
					of.getSpot().setOccupied(false);
					of.getSpot().setCritter(null);
					of.getSpot().setPlannedMove(false);
					of.getEffects().clear();
					p2.getDeadCritters().add(of);
					Critter[] newOtherPlayerCritters = new Critter[0];
					if (p2.getCritters().length > 0) {
						newOtherPlayerCritters = new Critter[p2
								.getCritters().length - 1];
					}
					// Critter newOtherPlayerCritters[] = new
					// Critter[otherPlayer.getCritters().length - 1];
					int q = 0;
					for (Critter f : p2.getCritters()) {
						if (!f.equals(of)) {
							newOtherPlayerCritters[q] = f;
							q++;
						}
					}

					p2.setCritters(newOtherPlayerCritters);


						p2.sendString(
								"dead," + of.getName() + "," + of.getSide());
						p1.sendString(
								"dead," + of.getName() + "," + of.getSide());

				}
			}
		}
	}

	public void checkGameOver() {

			if ((p1.getCritters().length == 0 || (p1.getCritters().length == 1 && p1
					.getCritters()[0].isBenched()))
					&& (p2.getCritters().length == 0 || (p2
							.getCritters().length == 1 && p2
							.getCritters()[0].isBenched()))) {
				p1.sendString("gameover,draw");
				p2.sendString("gameover,draw");
				System.out.println("draw!");
				setGameOver(true);
				setWinnerId("-1");
			} else if ((p1.getCritters().length == 0 || (p1
					.getCritters().length == 1 && p1.getCritters()[0]
					.isBenched()))) {
				p1.sendString("gameover,loss");
				p2.sendString("gameover,win");
				setGameOver(true);
				System.out.println("win!");
				setWinnerId(p2.getId());
			} else if ((p2.getCritters().length == 0 || (p2
					.getCritters().length == 1 && p2.getCritters()[0]
					.isBenched()))) {
				p1.sendString("gameover,win");
				System.out.println("loss!");
				p2.sendString("gameover,loss");
				setGameOver(true);
				setWinnerId(p1.getId());
			}
			if (gameOver) {
				turnOrchestrator.recordGameOutcome(winnerId, turnCount);

				restartGame();
			}

	}

	public synchronized boolean isCalculatingTurn() {
		return calculatingTurn;
	}

	public synchronized void randomSelect(Player player, Player bot) {
		Random random = new Random();

	// 		Critter critter1 = new Fox("Fox", leftBottomBack, "left", p1, p2);
	// Critter critter2 = new Lion("Lion", leftMiddleBack, "left", p1, p2);
	// Critter critter3 = new Dove("Dove", leftTopBack, "left", p1, p2);
	// Critter critter4 = new Donkey("Donkey", leftBench, "left", p1, p2);
	// Critter critter5 = new Wolf("Wolf", rightBottomBack, "right", p2, p1);
	// Critter critter6 = new Turtle("Turtle", rightMiddleBack, "right", p2, p1);
	// Critter critter7 = new Bull("Bull", rightTopBack, "right", p2, p1);
	// Critter critter8 = new Newt("Newt", rightBench, "right", p2, p1);
	// Critter[] critters = {critter1, critter2, critter3, critter4, critter5, critter6, critter7, critter8};
		// pick 4 different fighters
		// pick random tank
        int f1n = random.nextInt(2);
		Critter f1 = critters[f1n];
		//random support
		int f2n = random.nextInt(2) + 2;
		Critter f2 = critters[f2n];
		//random dps
		int f3n = random.nextInt(4) + 4;
		Critter f3 = critters[f3n];
		//2nd random dps
		int f4n = random.nextInt(4) + 4;
		while (f4n == f3n) f4n = random.nextInt(4) + 4;
		Critter f4 = critters[f4n];

		String[] critterNames = { f1.getName(), f2.getName(), f3.getName(), f4.getName() };
		Critter[] selectedCritters = new Critter[4];
		for (int i = 0; i < 4; i++) {
			if (critterNames[i].equals("Fox")) {
				selectedCritters[i] = new Fox("Fox", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Dove")) {
				selectedCritters[i] = new Dove("Dove", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Lion")) {
				selectedCritters[i] = new Lion("Lion", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Turtle")) {
				selectedCritters[i] = new Turtle("Turtle", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Donkey")) {
				selectedCritters[i] = new Donkey("Donkey", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Wolf")) {
				selectedCritters[i] = new Wolf("Wolf", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Bull")) {
				selectedCritters[i] = new Bull("Bull", leftBottomBack,
						bot.getSide(), bot, player);
			} else if (critterNames[i].equals("Newt")) {
				selectedCritters[i] = new Newt("Newt", leftBottomBack,
						bot.getSide(), bot, player);
			}
		}
		for (int i = 0; i < selectedCritters.length; i++) {
			selectedCritters[i].setId(Integer.toString(i + 3));
		}
		bot.setCritters(selectedCritters);

		//pick 3 different items
		bot.setItem1(player.identifyTotalItems("Smoke Bomb"));
		bot.getItem1().setPlayer(bot);
		bot.getItem1().setOtherPlayer(player);
		bot.setItem2(player.identifyTotalItems("War Horn"));
		bot.getItem2().setPlayer(bot);
		bot.getItem2().setOtherPlayer(player);
		bot.setItem3(player.identifyTotalItems("Ratland Jester's Hat"));
		bot.getItem3().setPlayer(bot);
		bot.getItem3().setOtherPlayer(player);
	}

	public synchronized void init() {
		System.out.println("INIT");
		p1.setUsername("player1");
		p2.setUsername("player2");
		gameOver = false;


		p1.setSide("left");
		p2.setSide("right");
		if (vsBot) {
			randomSelect(p1, p2);
		}
		if (botVsBot) {
			randomSelect(p1, p2);
			randomSelect(p2, p1);
		}


		String itemStr = "select,items,";
		for (int i = 0; i < p1.getTotalItems().size(); i++) {
			Item item = p1.getTotalItems().get(i);
			itemStr += "|" + item.getName() + "," + item.getDescription()
					+ "," + i + "," + item.getImageName();
		}


			p1.sendString(itemStr);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		itemStr = "select,items,";
		for (int i = 0; i < p2.getTotalItems().size(); i++) {
			Item item = p2.getTotalItems().get(i);
			itemStr += "|" + item.getName() + "," + item.getDescription() + "," + i + "," + item.getImageName();
		}

			p2.sendString(itemStr);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		leftBottomFront.setSurrounding(null, leftBottomBack, leftMiddleFront,
				null);

		leftBottomBack.setSurrounding(leftBottomFront, leftBench, leftMiddleBack,
				null);
		leftMiddleFront.setSurrounding(null, leftMiddleBack, leftTopFront,
				leftBottomFront);
		leftMiddleBack.setSurrounding(leftMiddleFront, leftBench, leftTopBack,
				leftBottomBack);
		leftTopFront.setSurrounding(null, leftTopBack, null, leftMiddleFront);
		leftTopBack.setSurrounding(leftTopFront, leftBench, null, leftMiddleBack);
		leftBench.setSurrounding(leftMiddleBack, null, leftTopBack, leftBottomBack);

		rightBottomFront.setSurrounding(null, rightBottomBack,
				rightMiddleFront, null);
		rightBottomBack.setSurrounding(rightBottomFront, rightBench, rightMiddleBack,
				null);
		rightMiddleFront.setSurrounding(null, rightMiddleBack, rightTopFront,
				rightBottomFront);
		rightMiddleBack.setSurrounding(rightMiddleFront, rightBench, rightTopBack,
				rightBottomBack);
		rightTopFront
				.setSurrounding(null, rightTopBack, null, rightMiddleFront);
		rightTopBack.setSurrounding(rightTopFront, rightBench, null, rightMiddleBack);
		rightBench.setSurrounding(rightMiddleBack, null, rightTopBack, rightBottomBack);

	}
	public RemoteEndpoint[] getEndpoints() {
		RemoteEndpoint [] eps = {p1Endpoint, p2Endpoint};
		return eps;
	}

	private int getSum(Player player, ToIntFunction<Critter> mapper) {
		return Arrays.stream(player.getCritters())
					.mapToInt(mapper)
					.sum();
	}

	public boolean getGameOver() {
		return gameOver;
	}
	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}
	private InetAddress[] reconnectingAddresses = {null, null};

	public InetAddress[] getReconnectingAddresses() {
		return reconnectingAddresses;
	}

	public User [] getUsers() {
		// TODO Auto-generated method stub
		return users;
	}
	public String getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(String winnerId) {
		this.winnerId = winnerId;
	}

}
