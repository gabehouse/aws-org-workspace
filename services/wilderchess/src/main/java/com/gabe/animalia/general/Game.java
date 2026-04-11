package com.gabe.animalia.general;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.stream.Stream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.io.File;

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
import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.TargetTypeEnum;
import com.gabe.animalia.ml.server.GameLogger;
import com.gabe.animalia.ml.server.Inference;

import ai.onnxruntime.OrtException;

import com.gabe.animalia.ml.dtos.LoggableActionDTO;
import com.gabe.animalia.ml.game.GameFeaturizer;
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
	private int winnerId = -2;
	StringBuilder manifest;

	// ai
	private static Inference aiBrain;
	private static GameFeaturizer featurizer;

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
			leftMiddleBack, leftTopFront, leftTopBack, leftBench, this, "left");

	private Player p2 = new Player(rightBottomFront, rightBottomBack, rightMiddleFront,
			rightMiddleBack, rightTopFront, rightTopBack, rightBench, this, "right");

	// tanks
	Critter critter1 = new Lion("Lion", leftMiddleBack, "left", p1, p2);
	Critter critter2 = new Turtle("Turtle", rightMiddleBack, "right", p2, p1);
	// supports
	Critter critter3 = new Dove("Dove", leftTopBack, "left", p1, p2);
	Critter critter4 = new Donkey("Donkey", leftBench, "left", p1, p2);
	// dps
	Critter critter5 = new Bull("Bull", rightTopBack, "right", p2, p1);
	Critter critter6 = new Newt("Newt", rightBench, "right", p2, p1);
	Critter critter7 = new Fox("Fox", leftBottomBack, "left", p1, p2);
	Critter critter8 = new Wolf("Wolf", rightBottomBack, "right", p2, p1);

	Critter[] critters = { critter1, critter2, critter3, critter4, critter5, critter6, critter7, critter8 };

	Item item1;
	Item item2;
	Item item3;
	Item item4;
	Item item5;
	Item item6;
	Item[] items = { item1, item2, item3, item4, item5, item6 };
	User[] users = new User[2];

	// for ml
	TurnOrchestrator turnOrchestrator;
	int turnNumber;

	int p1InitialHealthSum;
	int p1InitialEnergySum;
	int p2InitialHealthSum;
	int p2InitialEnergySum;
	TwoPlayerGameState initialState;

	// dont need to change this, change the one in mainapp
	boolean botVsBot = false;
	boolean botIsAi = true;

	public Game(User user1, User user2, GameLogger gameLogger) {
		if (user2.getID() == -1)
			vsBot = true;
		p2.setBot(true);
		if (user2.getID() == -1 && user1.getID() == -1) {
			botVsBot = true;
			System.out.println("BOT VS BOT");
			p1.setBot(true);
			p2.setBot(true);
		}
		this.user1 = user1;
		this.user2 = user2;
		user1.setPlayer(p1);
		user2.setPlayer(p2);
		p1Endpoint = user1.getEndpoint();
		p2Endpoint = user2.getEndpoint();
		p1.setEndpoint(p1Endpoint);
		p2.setEndpoint(p2Endpoint);
		p2.setBot(vsBot);
		users[0] = user1;
		users[1] = user2;
		turnOrchestrator = new TurnOrchestrator(UUID.randomUUID().toString(), gameLogger);
		if (botIsAi && aiBrain == null) {
			System.out.println("Initializing AI Brain and Weights...");
			try {

				// 1. Setup a dedicated temp directory for the model files
				Path tempDir = Files.createTempDirectory("wilderchess-model");

				// 2. Define the paths in the temp folder
				Path modelPath = tempDir.resolve("wilderchess.onnx");
				Path dataPath = tempDir.resolve("wilderchess.onnx.data");

				// 3. Extract the .onnx file
				try (InputStream is = Game.class.getClassLoader().getResourceAsStream("wilderchess.onnx")) {
					if (is == null)
						throw new RuntimeException("wilderchess.onnx not found!");
					Files.copy(is, modelPath, StandardCopyOption.REPLACE_EXISTING);
				}

				// 4. Extract the .onnx.data file (CRITICAL STEP)
				try (InputStream isData = Game.class.getClassLoader().getResourceAsStream("wilderchess.onnx.data")) {
					if (isData == null)
						throw new RuntimeException("wilderchess.onnx.data not found!");
					Files.copy(isData, dataPath, StandardCopyOption.REPLACE_EXISTING);
				}

				// 5. Load the model from the new temp location
				aiBrain = new Inference(modelPath.toAbsolutePath().toString());
				featurizer = new GameFeaturizer();

				System.out.println("AI Brain and Weights loaded successfully at: " + tempDir);

			} catch (Exception e) {
				System.err.println("AI Initialization Failed!");
				e.printStackTrace();
				botIsAi = false;
			}
		}
		Stream.of(p1, p2)
				.filter(Player::isBot)
				.forEach(p -> {
					p.setFeaturizer(featurizer);
					p.setInference(aiBrain);
				});
		if (botVsBot) {

			runBotVsBot();

		}

	}

	public synchronized void runBotVsBot() {
		System.out.println("runBotvsBot");
		while (true) {
			System.out.println("runBotvsBot top of while(true)");
			init();

			initialPhase(p1, p2, "left",
					p1.getCritters()[0].getName(), "leftMiddleFront", p1.getCritters()[1].getName(), "leftMiddleBack",
					p1.getCritters()[2].getName(),
					"leftBottomBack", p1.getCritters()[3].getName(), "leftBench");
			initialPhase(p2, p1, "right",
					p2.getCritters()[0].getName(), "rightMiddleFront", p2.getCritters()[1].getName(), "rightMiddleBack",
					p2.getCritters()[2].getName(),
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
					initialState = turnOrchestrator.captureInitialState(turnNumber, p1, p2);

					calculatingTurn = true;
					p1.fillActionQueue(p2);
					p2.fillActionQueue(p1);

					calculateTurn(p1, p2, 0);
				} catch (Exception e) {
					turnOrchestrator.recordEmergencySave(turnNumber, e.toString());
					// 2. CRITICAL: Stop the loop so it doesn't crash forever
					gameOver = true;

					// 3. Optional: Print to console so you know why the simulation stopped
					System.err.println("Simulation aborted due to engine error: " + e.getMessage());
					throw e;
				}

			}
			System.out.println("RESTART GAME BOT VS BOT");
			turnOrchestrator.recordGameOutcome(winnerId, turnNumber);
			turnOrchestrator.newGame();
			p1.getQueue().clear();
			p2.getQueue().clear();
			p1.getDeadCritters().clear();
			p2.getDeadCritters().clear();
			turnNumber = 1;
			firstInitialPhase = true;
		}

	}

	public synchronized void selectCritter(Player player, Player otherPlayer,
			String name, String side) {
		System.out.println("selectCritter");

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
			// .getTempSpot().getSurroundingSquare(name));
			Unbench newUnbench = new Unbench(unbenching, unbenching
					.getTempSpot().getSurroundingSquare(name), player,
					otherPlayer);
			if (player.getAllottedTime() + newUnbench.getTimeCost() <= 10) {
				newUnbench.init();
				player.setLastIndicatedStr(newUnbench.getIndicatorMessage());

				newUnbench.setPlayer(player);
				newUnbench.setOtherPlayer(otherPlayer);
				player.sendString(
						"moveQueue,$"
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
				newBench.setPlayer(player);
				newBench.setOtherPlayer(otherPlayer);

				player.setLastIndicatedStr(newBench.getIndicatorMessage());

				player.sendString(
						"moveQueue,$"
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
			Action newAction = new Action(null, null, player, otherPlayer, ActionEnum.NONE);
			if (player.getSelectedCritter().getTempSpot().getName().contains("Bench")) {
				if (player.getCritters().length != 4) {
					newAction = new Unbench(player.getSelectedCritter(), player
							.getSelectedCritter().getTempSpot()
							.getSurroundingSquare(name), player, otherPlayer);
				}
			} else if (player.getAllottedTime() + (new Move(null, null, null, null)).getTimeCost() <= 10) {
				newAction = new Move(player.getSelectedCritter(), player
						.getSelectedCritter().getTempSpot()
						.getSurroundingSquare(name), player, otherPlayer);
			}
			newAction.setPlayer(player);
			newAction.setOtherPlayer(otherPlayer);
			newAction.init();
			player.setLastIndicatedStr(newAction.getIndicatorMessage());

			player.sendString(
					"moveQueue,$"
							+ player.iconString(player.getQueue()));
			player.sendString(
					"time," + player.getAllottedTime() + ","
							+ player.getMaxTime());

			// player.getSelectedCritter().setMoved(true);
			sendIndicators(player, player.getSelectedCritter());
			player.getSelectedCritter().displayPossibleMoves(player, "move");
		}

	}

	public synchronized void initialPhase(Player player, Player otherPlayer,
			String side, String f1Name, String s1Name, String f2Name,
			String s2Name, String f3Name, String s3Name, String f4Name, String s4Name) {
		System.out.println("INITIAL PHASE");

		boolean hasBenchOccupant = java.util.Arrays.asList(s1Name, s2Name, s3Name, s4Name)
				.stream()
				.anyMatch(s -> s != null && s.contains("Bench"));

		// 2. Use the check
		if (!hasBenchOccupant) {
			System.out.println("ERR: At least one fighter must start on the bench.");
			return;
		}

		if (firstInitialPhase) {
			for (Square s : squares) {
				s.setPlannedMove(false);
				s.setCritter(null);
				System.out.println("initial phase clear square " + s.getName());
			}
			firstInitialPhase = false;
		}

		player.setReadied(true);
		Critter f1 = player.identifyCritter(f1Name, player.getSide());
		Critter f2 = player.identifyCritter(f2Name, player.getSide());
		Critter f3 = player.identifyCritter(f3Name, player.getSide());
		Critter f4 = player.identifyCritter(f4Name, player.getSide());
		Square s1 = player.identifySquare(s1Name);
		Square s2 = player.identifySquare(s2Name);
		Square s3 = player.identifySquare(s3Name);
		Square s4 = player.identifySquare(s4Name);
		Critter[] fs = new Critter[] { f1, f2, f3, f4 };

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
			// name, description, pos, imageStr
			int pos = 0;
			for (Item i : player.getItems()) {
				playerItemStr += "" + i.getName() + "," + i.getInfo() + "," + pos + "," + i.getImageName() + "|";
				pos++;
			}
			pos = 0;
			for (Item i : otherPlayer.getItems()) {
				otherPlayerItemStr += "" + i.getName() + "," + i.getInfo() + "," + pos + "," + i.getImageName()
						+ "|";
				pos++;
			}

			for (Critter f : critters) {
				str += "$" + f.getName() + "$" + f.getSide() + "$" + f.getSpot();
			}
			player.getBench().getCritter().benchEffect(player, otherPlayer);
			otherPlayer.getBench().getCritter().benchEffect(otherPlayer, player);
			String initialManifest = "$";
			// Stream.of(player.getCritters(), otherPlayer.getCritters()).map(null)
			for (Critter[] carr : new Critter[][] { player.getCritters(), otherPlayer.getCritters() }) {
				for (Critter c : carr) {
					initialManifest += "|" + c.generateSystemSnapshot(0);
				}

			}

			str += initialManifest;

			System.out.println("player vs bot fill action queue");

			player.sendString("inititems,|" + playerItemStr);
			otherPlayer.sendString("inititems,|" + otherPlayerItemStr);
			player.sendString("initialphaseend," + str);
			otherPlayer.sendString("initialphaseend," + str);
			System.out.println("initialphaseend," + str);
			// player.sendString(initialManifest);
			// otherPlayer.sendString(initialManifest);

			if (botVsBot) {
				p1.fillActionQueue(p2);
				p2.fillActionQueue(p1);
			} else {
				if (player.isBot())
					player.fillActionQueue(otherPlayer);

				if (otherPlayer.isBot())
					otherPlayer.fillActionQueue(player);
			}

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
			initialState = turnOrchestrator.captureInitialState(turnNumber, p1, p2);

			calculatingTurn = true;
			calculateTurn(player, otherPlayer, System.currentTimeMillis());

			// player.sendString("transition,begin");
			// otherPlayer.sendString("transition,begin");
			// Timer timer = new Timer();
			// timer.schedule(new TimerTask() {
			// public void run() {
			// System.out.println(getGameState());
			// System.out.println(stringifyActionQueues());
			// calculateTurn(player, otherPlayer, System.currentTimeMillis());
			// System.out.println(getGameState());
			// }
			// }, 750);

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
			if (player.getSelectedCritter().getTempSpot().getName()
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

	public synchronized TargetTypeEnum incomingType(String type) {
		TargetTypeEnum incomingType;
		switch (type.toLowerCase()) {
			case "targetless":
				incomingType = TargetTypeEnum.NONE;
				break;
			case "critter":
				incomingType = TargetTypeEnum.FIGHTER;
				break;
			case "spot":
				incomingType = TargetTypeEnum.SQUARE;
				break;
			default:
				incomingType = TargetTypeEnum.NONE;
		}
		return incomingType;
	}

	public synchronized void useAbility(Player player, Player otherPlayer,
			String type, String targetName, String side) {
		Targetable target = null;
		System.out.println("USEABILITY");
		Action ability = player.getSelectedAbility().getNew();
		if (player.getAllottedTime() + ability.getTimeCost() <= 10) {
			ability.setOtherPlayer(otherPlayer);
			ability.setPlayer(player);
			ability.setSubject(player.getSelectedCritter());
			TargetTypeEnum incomingType = incomingType(type);
			if (incomingType.equals(player.getSelectedAbility().getTargetType())) {
				if (incomingType.equals(TargetTypeEnum.SQUARE)) {

					target = player.identifySquare(targetName);
					if (target == null) {
						target = otherPlayer.identifySquare(targetName);
					}
					if (target != null) {
						ability.setTarget(target);
						if (ability.initable()) {
							// init abil on spot
							System.out.println("abil is initable");
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

							if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER)) {

								player.sendString(
										"option,remove,"
												+ player.getSelectedAbility().getName());

							} else if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.SQUARE)) {
								player.getSelectedCritter().displayPossibleMoves(player,
										"move");
							}
							player.setSelectedAbility(null);
						}

					} else {
						player.setSelectedAbility(null);
					}
				} else {
					System.out.println("use abil on critter");
					if (side.equals(player.getSide())) {
						target = player.identifyCritter(targetName, side);
					} else {
						target = otherPlayer.identifyCritter(targetName, side);
					}
					if (ability.initable()) {
						System.out.println("abil is initable");
						ability.setTarget(target);
						ability.init();
						player.setAllottedTime(player.getAllottedTime()
								+ ability.getTimeCost());
						player.setLastIndicatedStr(ability
								.getIndicatorMessage());
						sendIndicators(player, player.getSelectedCritter());
					}
				}
				System.out.println("sending moveque");
				player.sendString(
						"moveQueue,$"
								+ player.iconString(player.getQueue()));
				player.sendString(
						"time," + player.getAllottedTime() + ","
								+ player.getMaxTime());

			}
		} else {

			if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER)) {

				player.sendString(
						"option,remove,"
								+ player.getSelectedAbility().getName());

			} else if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.SQUARE)
					&& !player.getSelectedCritter().hasMoved()) {
				player.getSelectedCritter().displayPossibleMoves(player,
						"move");
			}
		}

		player.setSelectedAbility(null);

	}

	public synchronized void selectAbility(Player player, Player otherPlayer,
			String name) {
		System.out.println("select ability 1");
		player.sendString("option,remove,");
		if (player.getSelectedCritter().identifyAbility(name) != null) {
			System.out.println("select ability 2 " + name);
			player.setSelectedAbility(player.getSelectedCritter()
					.identifyAbility(name));
			System.out.println("select ability 2 " + player.getSelectedCritter()
					.identifyAbility(name));
		}
		Action ability = player.getSelectedAbility().getNew();
		ability.setPlayer(player);
		ability.setSubject(player.getSelectedCritter());
		ability.setOtherPlayer(otherPlayer);

		ability.displayOptions();
		if ((player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.NONE)
				|| player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.SELF))
				&& player.getAllottedTime() + ability.getTimeCost() <= 10) {
			// self or no target ability init
			if (!ability.initable()) {
				player.setSelectedAbility(null);
				return;
			}
			// use targetless ability
			ability.init();
			player.setLastIndicatedStr(ability.getIndicatorMessage());
			sendIndicators(player, player.getSelectedCritter());

			player.sendString(
					"moveQueue,$" + player.iconString(player.getQueue()));
			player.setAllottedTime(player.getAllottedTime()
					+ ability.getTimeCost());
			player.sendString(
					"time," + player.getAllottedTime() + ","
							+ player.getMaxTime());

			player.setSelectedAbility(null);

			player.sendString("option,remove," + name);

		}

	}

	public synchronized void useItem(String action, String name, String pos, String second, Player player) {
		System.out.println(String.format("[USE_ITEM] Action: %s | Name: %s | Pos: %s | Val: %s",
				action, name, pos, second));
		System.out.println("~~~~use item~~~~");
		if (action.equals("add")) {

			Item toAdd = player.identifyItem(name, Integer.parseInt(pos));
			System.out.println("toadd = " + toAdd + ", " + name + ", " + Integer.parseInt(pos));
			toAdd.setSecond(Integer.parseInt(second));
			player.getItemQueue().add(toAdd);
		} else if (action.equals("remove")) {
			Item toRemove = player.identifyItem(name, Integer.parseInt(pos));
			System.out.println("toremove = " + toRemove + ", " + name + ", " + Integer.parseInt(pos));
			player.getItemQueue().remove(toRemove);
		}
		System.out.println("player.getItemQueue().size() = " + player.getItemQueue().size());
	}

	public synchronized String stringifyActionQueues() {
		String str = "";
		for (Action a : p1.getQueue()) {
			str += a.getName() + "-" + a.getSubject().getName() + "-" + a.getTarget().getName() + '|';
		}
		str += "$";
		for (Action a : p2.getQueue()) {
			str += a.getName() + "-" + a.getSubject().getName() + "-" + a.getTarget().getName() + '|';
		}
		return str;
	}

	public synchronized String getGameState() {

		String gameState = "";

		Map<Critter, Integer> fighterEnum = new HashMap<>();

		for (int i = 0; i < critters.length; i++) {
			fighterEnum.put(critters[i], i);
		}
		Map<Square, Integer> squareEnum = new HashMap<>();
		for (int i = 0; i < squares.length; i++) {
			squareEnum.put(squares[i], i);
		}
		Map<String, Integer> effectsEnum = new HashMap<>();
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
			gameState += "-$";
			for (Action a : c.getEffects()) {
				gameState += a.getName();
				gameState += ".";
				gameState += a.getDuration();
				gameState += "|";
			}
			gameState += "$-";
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
			gameState += "-$";
			for (Action a : c.getEffects()) {
				gameState += a.getName();
				gameState += ".";
				gameState += a.getDuration();
				gameState += "|";
			}
			gameState += "$-";
		}
		for (Item i : p2.getItems()) {
			gameState += i.getName() + ".";
		}
		return gameState;
	}

	public synchronized void update(RemoteEndpoint endpoint, String message) {
		try {

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

				System.out.println("click " + player.getSide() + ", " + otherPlayer.getSide() + ", "
						+ player.identifyCritter(tokens[2], player.getSide()) + ", "
						+ otherPlayer.identifyCritter(tokens[2], otherPlayer.getSide()) + ", "
						+ player.getSelectedAbility());

				if ((player.identifyCritter(tokens[2], player.getSide()) != null || otherPlayer
						.identifyCritter(tokens[2], otherPlayer.getSide()) != null)
						&& player.getSelectedAbility() == null) {

					System.out.println("1");
					selectCritter(player, otherPlayer, tokens[2], tokens[3]);

				} else if (player.getSelectedCritter() != null
						&& (player.getSelectedCritter().getTempSpot()
								.compareSurrounding(tokens[2]))
						&& !player.isReadied()
						&& player.getSelectedAbility() == null) {

					System.out.println("2");

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
						// Critter unbenching = null;
						// Square targetSpot = player.identifySquare(tokens[2]);
						// for (Critter c : player.getCritters()) {
						// if (c.getTempSpot().equals(targetSpot)) {
						// unbenching = c;
						// }
						// }
						// if (benching != null) {
						unbench(player, otherPlayer, tokens[2], player.getSelectedCritter());
						// }

					}

				} else if (tokens[1].equals("ready")) {
					if (gameOver) {
						// restartGame();
					} else if (!player.isReadied()) {
						ready(player, otherPlayer);
					}

				} else if (tokens[1].equals("item") && !player.isReadied()) {
					useItem(tokens[2], tokens[3], tokens[4], tokens[5], player);
				} else if (tokens[1].equals("actionqueue") && !player.isReadied()) {
					updateActionQueue(player, otherPlayer, tokens[2], tokens[3],
							tokens[4]);
				} else if (player.getSelectedCritter() != null
						&& player.getSelectedAbility() != null
						&& !player.isReadied()) {
					System.out.println("use ability click: " + player + ", " + otherPlayer + ", " + tokens[1] + ", "
							+ tokens[2] + ", " + tokens[3]);
					if (incomingType(tokens[1]).equals(TargetTypeEnum.SQUARE)
							&& player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.SQUARE)) {
						System.out.println(
								"use ability on square: " + player + ", " + otherPlayer + ", " + tokens[1] + ", "
										+ tokens[2] + ", " + tokens[3]);
						useAbility(player, otherPlayer, tokens[1], tokens[2], tokens[3]);
					} else if (incomingType(tokens[1]).equals(TargetTypeEnum.FIGHTER)
							&& player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER)) {
						if (!tokens[2].contains("Bench")) {
							Critter f = player
									.identifyCritter(tokens[2], tokens[3]);
							System.out.println("identify critter result = " + f);
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
									System.out.println("use ability on critter pre");
									useAbility(player, otherPlayer, tokens[1],
											tokens[2], tokens[3]);
								} else {

									if (player.getSelectedCritter().hasMoved()) {
										player.sendString("possiblemoves, ");
										player.sendString(
												"indicatespots,no moves");
									}
									if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER)) {
										player.sendString(
												"option,remove,"
														+ player.getSelectedAbility()
																.getName());
									} else if (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.SQUARE)
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
						System.out.println("3");

						// player.sendString("possiblemoves, ");
						// player.sendString(
						// "indicatespots,no moves");
						player.getSelectedCritter().displayPossibleMoves(
								player, "move");

						// if
						// (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER))
						// {
						// System.out.println("4");
						player.sendString(
								"option,remove,"
										+ player.getSelectedAbility()
												.getName());
						// } else if
						// (player.getSelectedAbility().getTargetType().equals(TargetTypeEnum.FIGHTER)
						// && !player.getSelectedCritter().hasMoved()) {
						// player.getSelectedCritter().displayPossibleMoves(
						// player, "move");
						// }
						player.setSelectedAbility(null);
						// player.getSelectedCritter().displayPossibleMoves(player);
					}
				} else if (player.getSelectedCritter() != null
						&& tokens[1].equals("ability") && !player.isReadied()) {
					// comes here when any ability is pressed
					System.out.println("4");
					selectAbility(player, otherPlayer, tokens[3]);
				} else if (tokens[1].equals("initialposition")) {
					if (!player.isReadied()) {
						if (vsBot) {
							initialPhase(otherPlayer, player, "right",
									otherPlayer.getCritters()[0].getName(), "rightMiddleFront",
									otherPlayer.getCritters()[1].getName(), "rightMiddleBack",
									otherPlayer.getCritters()[2].getName(),
									"rightBottomBack", otherPlayer.getCritters()[3].getName(), "rightBench");
						}
						initialPhase(player, otherPlayer, tokens[2], tokens[3],
								tokens[4], tokens[5], tokens[6], tokens[7], tokens[8],
								tokens[9], tokens[10]);
						// initialPhase(Player player, Player otherPlayer,
						// String side, String f1Name, String s1Name, String f2Name,
						// String s2Name, String f3Name, String s3Name, String f4Name, String s4Name)
					} else {
						player.setReadied(false);
						player.sendString("formationsetcancel");

					}

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

				// if (tokens[1].equals("critter")) {
				// if (tokens[4].equals(player.getSide())) {
				// Critter critter = player.identifyCritter(tokens[3],
				// tokens[4]);
				// String str = "critteractions,|"
				// + critter.getIndicationStr();
				//
				// player.sendString(str);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				// }
				// }
			} else if (tokens[0].equals("calculate")) {
				calculateTurn(player, otherPlayer, System.currentTimeMillis());
				// 1. Transform Player 1's Queue
				// ml record actions and end state

			}
		} catch (

		Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void restartGame() {
		winnerId = -2;
		System.out.println("RESTART GAME");
		turnOrchestrator.newGame();
		p1.getQueue().clear();
		p2.getQueue().clear();
		p1.getDeadCritters().clear();
		p2.getDeadCritters().clear();
		turnNumber = 1;
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
		player.setTotalCritters(selectedCritters);
		player.setSelectedCritter(critter1);

		String s = "player critters";
		for (Critter f : player.getCritters()) {
			s += f.getName() + "," + f.getMaxHealth() + ","
					+ f.getMaxEnergy() + "," + f.getSide() + ","
					+ f.getPassiveName() + "," + f.getPassiveCritterDescription() + "|";
		}
		System.out.println(s);

		if (otherPlayer.getCritters() != null) {
			// start game
			critters[0] = otherPlayer.getCritters()[0];
			critters[1] = otherPlayer.getCritters()[1];
			critters[2] = otherPlayer.getCritters()[2];
			critters[3] = otherPlayer.getCritters()[3];
			critters[4] = player.getCritters()[0];
			critters[5] = player.getCritters()[1];
			critters[6] = player.getCritters()[2];
			critters[7] = player.getCritters()[3];

			String str = "critterselect,|";
			for (Critter f : critters) {
				str += f.getName() + "," + f.getMaxHealth() + ","
						+ f.getMaxEnergy() + "," + f.getSide() + ","
						+ f.getPassiveName() + "," + f.getPassiveCritterDescription() + "|";
			}

			System.out.println("Selected Critters: " + str);

			player.sendString(str);
			otherPlayer.sendString(str);
			player.sendString("showgrid," + player.getSide());
			otherPlayer.sendString("showgrid," + otherPlayer.getSide());

			for (int i = 0; i < p1.getCritters().length; i++) {
				p1.getCritters()[i].setId(i + 1);
			}
			for (int i = 0; i < p2.getCritters().length; i++) {
				p2.getCritters()[i].setId(i + p1.getCritters().length + 1);
			}
		}

	}

	public synchronized void selectItems(Player player, Player otherPlayer,
			String item1Name, String item2Name, String item3Name) {
		System.out.println("~~~~select items~~~~~");

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
		for (Item i : player.getItemQueue()) {
			System.out.println("player item queue: " + i.getName() + ", " + i.getSecond());
		}
		calculatingTurn = true;
		// player.sendString("calculating,begin");
		// otherPlayer.sendString("calculating,begin");

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
		for (; h < player.getQueue().size() * 2
				+ otherPlayer.getQueue().size() * 2 + player.getItemQueue().size()
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
				// else if (otherPlayer.getQueue().get(opa).getTimeCost() + opt ==
				// prioritizedTime
				// && prioritizedAction.getSubject() != null
				// && new Random().nextInt(10) < otherPlayer.getMorale()) {
				// prioritizedAction = otherPlayer.getQueue().get(opa);
				// prioritizedPlayer = otherPlayer;
				// prioritizedTime = opt
				// + otherPlayer.getQueue().get(opa).getTimeCost();
				//
				// }
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
			} else if (otherPlayer.getQueue().size() > opa
					&& prioritizedAction.equals(otherPlayer.getQueue().get(opa))) {
				if (otherPlayer.getQueue().size() > opa + 1) {
					h++;
					otherPlayer.getQueue().get(opa + 1).setDisplayOrder(h);
				}
				opa++;
			} else if (otherPlayer.getItemQueue().size() > opi
					&& prioritizedAction.equals(otherPlayer.getItemQueue().get(opi))) {
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
		// for (int i = 0; i < player.getQueue().size(); i++) {
		// player.getQueue().get(i).setPlayerOrder
		// }
		for (int i = 0; i < playersActions.size(); i++) {
			System.out.println(playersActions.get(i).getName() + ", subject = " + playersActions.get(i).getSubject()
					+ ", target = " + playersActions.get(i).getTarget() + ", " + playersActions.get(i).getOrder());
		}
		// if (new Random().nextInt(10) < player.getMorale()) {
		try {
			// if (botVsBot || botIsAi) {
			System.out.println("BOT VS BOT PERFORMING");
			manifest = new StringBuilder("ANIMMANIFEST,|");
			double timeOffsetP1 = 0;
			double timeOffsetP2 = 0;

			for (Action action : playersActions) {
				System.out.println("Players actions: " + action.getName());
				boolean actionPerformed = action.perform();

				if (action.getType() != ActionCategoryEnum.ITEM) {
					String onActionManifest = action.getSubject().onAction(player, otherPlayer, action);

					// Check for null AND empty, and ensure we don't double-pipe
					if (onActionManifest != null && !onActionManifest.trim().isEmpty()) {
						manifest.append(onActionManifest);
						if (!onActionManifest.endsWith("|")) {
							manifest.append("|");
						}
					}
				}

				if (action.getPlayer().equals(p1)) {
					timeOffsetP1 += action.getTimeCost();
					p1.setUsedTime(timeOffsetP1);
				} else {
					timeOffsetP2 += action.getTimeCost();
					p2.setUsedTime(timeOffsetP2);
				}
				double currentOffset = action.getPlayer().equals(p1) ? timeOffsetP1 : timeOffsetP2;
				double offsetToUse = (action.getType() == ActionCategoryEnum.ITEM)
						? ((Item) action).getSecond()
						: currentOffset;

				// Capture the main action data
				String data = action.getManifestData(offsetToUse);
				if (data != null && !data.isEmpty()) {
					manifest.append(data).append("|");
				}

				if (actionPerformed) {

					// 2. Check for Dirty Critters
					for (Critter c : critters) {
						if (c.isDirty()) {
							String snapshot = c.generateSystemSnapshot(offsetToUse);
							if (!snapshot.isEmpty()) {
								manifest.append(snapshot).append("|");
							}
							c.clearDirty();
						}
					}
				}
				checkDeath();
				checkGameOver();

				if (gameOver) {
					String winnerMsg = (winnerId == -1) ? "Draw!"
							: (winnerId == p1.getId() ? "P1 Wins!" : "P2 Wins!");

					manifest.append(String.format(
							"action,GameOver,Victory,system,none,none,0,0,0,0,none,none,none,-1,-1,%.2f,%s,none,none|",
							timeOffsetP1 >= timeOffsetP2 ? timeOffsetP1 + 0.5 : timeOffsetP2 + 0.5,
							winnerMsg));
					break;
				}

			}

			for (int i = 0; i < player.getItemQueue().size(); i++) {

				otherPlayer.sendString(
						"itemdisplay" + ","
								+ player.getItemQueue().get(i).getName() + ","
								+ player.getItemQueue().get(i).getInfo()
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
										.getInfo()
								+ ",2,"
								+ otherPlayer.getItemQueue().get(i)
										.getImageName()
								+ ","
								+ otherPlayer.getItemQueue().get(i)
										.getSecond());

			}

			endTurn(player, otherPlayer);

			// }
		} catch (Exception e) {
			// This prints the error type, the message, AND the line numbers
			e.printStackTrace();

			// Optional: Log which specific ability caused the crash
		}
	}

	public synchronized void endTurn(Player player, Player otherPlayer) {
		System.out.println("END TURN");

		double currentTime = 10;

		Player[] players = { p1, p2 };

		// PASS 1: Damage
		for (Player p : players) {
			for (Critter f : p.getCritters()) {
				f.triggerEffects();
			}
		}

		// PASS 2: Expiry & Spreading (The "State Change" Phase)
		for (Player p : players) {
			for (Critter f : p.getCritters()) {
				String durationEvents = f.effectDuration(currentTime);
				if (!durationEvents.isEmpty()) {
					manifest.append(durationEvents).append("|");
					currentTime += 0.2;
				}
			}
		}

		for (Player p : players) {
			for (Critter f : p.getCritters()) {
				if (f.isBenched()) {
					f.benchedEffect(p, f.getOpponent());
				}
			}
		}

		// PASS 3: Energy (The "What Happened" Phase)
		for (Player p : players) {
			for (Critter f : p.getCritters()) {
				f.restoreEnergy(f.isBenched() ? 40 : 20);
			}
		}

		// PASS 4: Snapshots (The "UI Sync" Phase)
		for (Player p : players) {
			for (Critter f : p.getCritters()) {
				String snap = f.generateSystemSnapshot(currentTime);
				if (snap != null && !snap.equals("none")) {
					manifest.append(snap).append("|");
					// No need to increment time much here, just a tiny buffer
					currentTime += 0.01;
				}
			}
		}
		// removes from getCritters() so need at the end
		checkDeath();

		checkGameOver();
		endTurnLog();

		if (gameOver && !manifest.toString().contains("GameOver")) {
			String winnerMsg = (winnerId == -1) ? "Draw!" : (winnerId == p1.getId() ? "P1 Wins!" : "P2 Wins!");
			manifest.append(String.format(
					"action,GameOver,Victory,system,none,none,0,0,0,0,none,none,none,-1,-1,%.2f,%s,none,none|",
					10.5, winnerMsg));
		}

		String finalManifest = manifest.toString();
		p1.sendString(finalManifest);
		p2.sendString(finalManifest);

		player.sendString(
				"moveQueue,$" + player.iconString(player.getQueue())
						+ "$380");
		player.sendString(
				"moveQueue,$"
						+ otherPlayer.iconString(otherPlayer.getQueue())
						+ "$430");
		otherPlayer.sendString(
				"moveQueue,$" + player.iconString(player.getQueue())
						+ "$430");
		otherPlayer.sendString(
				"moveQueue,$"
						+ otherPlayer.iconString(otherPlayer.getQueue())
						+ "$380");

		if (gameOver) {
			System.out.println("GAME OVER: Stopping turn finalization.");
			return; // <--- DO NOT clear queues, do not reset energy, do not send "reset"
		}

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
				f.setResting(true);
				f.setPlasterved(false);
				f.sendStats(p1, p2);
			}
			if (!botVsBot) {
				System.out.println(" player bot?: " + p1.isBot());
				if (p1.isBot()) {
					p1.fillActionQueue(p2);
				}
				if (p2.isBot()) {
					p2.fillActionQueue(p1);
				}
			}

			// p1.sendString("calculating,end");
			// p2.sendString("calculating,end");
		}

	}

	public synchronized void endTurnLog() {
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

		// PlayerActionInput p2Input = new PlayerActionInput("2", p2Actions);
		turnOrchestrator.recordTurnTransition(
				turnNumber,
				initialState,
				p1,
				p2,
				p1Actions,
				p2Actions);

		turnNumber++;
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
					pf.setEnergy(

							0);
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
				}
			}
		}
		for (Critter of : p2.getCritters()) {
			if (of.getHealth() <= 0) {
				if (of.isPlasterved()) {
					of.setHealth(1);
				} else {
					of.setAlive(false);
					of.setEnergy(

							0);
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

				}
			}
		}
	}

	public void checkGameOver() {
		// Count how many units are actually capable of fighting for each player
		long p1ActiveCount = Arrays.stream(p1.getCritters())
				.filter(c -> c.isAlive() && !c.isBenched())
				.count();

		long p2ActiveCount = Arrays.stream(p2.getCritters())
				.filter(c -> c.isAlive() && !c.isBenched())
				.count();

		if (p1ActiveCount == 0 && p2ActiveCount == 0) {
			System.out.println("draw!");
			setGameOver(true);
			winnerId = -1;
		} else if (p1ActiveCount == 0) {
			System.out.println("Player 2 wins!");
			setGameOver(true);
			winnerId = p2.getId();
		} else if (p2ActiveCount == 0) {
			System.out.println("Player 1 wins!");
			setGameOver(true);
			winnerId = p1.getId();
		}

		// if (gameOver) {
		// turnOrchestrator.recordGameOutcome(winnerId, turnNumber);
		// }
	}

	public synchronized boolean isCalculatingTurn() {
		return calculatingTurn;
	}

	public synchronized void randomSelect(Player player, Player bot) {
		System.out.println("randomselect");
		Random random = new Random();

		// Critter critter1 = new Fox("Fox", leftBottomBack, "left", p1, p2);
		// Critter critter2 = new Lion("Lion", leftMiddleBack, "left", p1, p2);
		// Critter critter3 = new Dove("Dove", leftTopBack, "left", p1, p2);
		// Critter critter4 = new Donkey("Donkey", leftBench, "left", p1, p2);
		// Critter critter5 = new Wolf("Wolf", rightBottomBack, "right", p2, p1);
		// Critter critter6 = new Turtle("Turtle", rightMiddleBack, "right", p2, p1);
		// Critter critter7 = new Bull("Bull", rightTopBack, "right", p2, p1);
		// Critter critter8 = new Newt("Newt", rightBench, "right", p2, p1);
		// Critter[] critters = {critter1, critter2, critter3, critter4, critter5,
		// critter6, critter7, critter8};
		// pick 4 different fighters
		// pick random tank
		int f1n = random.nextInt(2);
		Critter f1 = critters[f1n];
		// random support
		int f2n = random.nextInt(2) + 2;
		Critter f2 = critters[f2n];
		// random dps
		int f3n = random.nextInt(4) + 4;
		Critter f3 = critters[f3n];
		// 2nd random dps
		int f4n = random.nextInt(4) + 4;
		while (f4n == f3n)
			f4n = random.nextInt(4) + 4;
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
		bot.setCritters(selectedCritters);
		bot.setTotalCritters(selectedCritters);
		System.out.println("setcritters random select");
		// pick 3 different items
		bot.setItem1(player.identifyTotalItems("SMOKE_BOMB"));
		bot.getItem1().setPlayer(bot);
		bot.getItem1().setOtherPlayer(player);
		bot.setItem2(player.identifyTotalItems("WAR_HORN"));
		bot.getItem2().setPlayer(bot);
		bot.getItem2().setOtherPlayer(player);
		bot.setItem3(player.identifyTotalItems("COXCOMB"));
		bot.getItem3().setPlayer(bot);
		bot.getItem3().setOtherPlayer(player);
	}

	public synchronized void init() {
		System.out.println("INIT");
		p1.setUsername("player1");
		p2.setUsername("player2");
		gameOver = false;
		p1.setMorale(5);
		p2.setMorale(5);

		if (vsBot) {
			randomSelect(p1, p2);
		}
		if (botVsBot) {
			randomSelect(p1, p2);
			randomSelect(p2, p1);
			// set ids for bot vs bot
			for (int i = 0; i < p1.getCritters().length; i++) {
				p1.getCritters()[i].setId(i + 1);
			}
			for (int i = 0; i < p2.getCritters().length; i++) {
				p2.getCritters()[i].setId(i + p1.getCritters().length + 1);
			}
		}

		String itemStr = "select,items,";
		for (int i = 0; i < p1.getTotalItems().size(); i++) {
			Item item = p1.getTotalItems().get(i);
			itemStr += "|" + item.getName() + "," + item.getInfo()
					+ "," + i + "," + item.getImageName();
		}

		p1.sendString(itemStr);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		itemStr = "select,items,";
		for (int i = 0; i < p2.getTotalItems().size(); i++) {
			Item item = p2.getTotalItems().get(i);
			itemStr += "|" + item.getName() + "," + item.getInfo() + "," + i + "," + item.getImageName();
		}

		p2.sendString(itemStr);
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

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
		RemoteEndpoint[] eps = { p1Endpoint, p2Endpoint };
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

	private InetAddress[] reconnectingAddresses = { null, null };

	public InetAddress[] getReconnectingAddresses() {
		return reconnectingAddresses;
	}

	public User[] getUsers() {
		return users;
	}

	public int getWinnerId() {
		return winnerId;
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public void setTurnNumber(int turnNumber) {
		this.turnNumber = turnNumber;
	}

	public static Inference getAiBrain() {
		return aiBrain;
	}

	public static GameFeaturizer getFeaturizer() {
		return featurizer;
	}

}
