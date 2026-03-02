package com.gabe.animalia.general;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gabe.animalia.items.CrimberryPack;
import com.gabe.animalia.items.WarHorn;
import com.gabe.animalia.ml.dtos.FighterStateDTO;
import com.gabe.animalia.ml.dtos.PlayerStateDTO;
import com.gabe.animalia.items.OilBomb;
import com.gabe.animalia.items.RatlandJestersHat;
import com.gabe.animalia.items.ThiefGloves;
import com.gabe.animalia.items.SmokeBomb;
import com.gabe.animalia.ability.Bench;
import com.gabe.animalia.ability.Move;
import com.gabe.animalia.ability.Unbench;


public class Player {
	private double maxTime = 10;
	private double usedTime;
	private int morale = 5;
	private double allottedTime;
	private RemoteEndpoint endpoint;
	private Square topBack, topFront, middleBack, middleFront, bottomBack, bottomFront, bench;
	private Square [] squares = new Square [7];
	private String side;
	private boolean readied;
	private boolean usingMove;
	private boolean benchActionUsed = false;
	private String lastIndicatedStr;
	private Action selectedAbility;
	private Critter selectedCritter;
	private Critter[] critters;
	private ArrayList <Item> queuedItems = new ArrayList<Item>();
	private ArrayList<Action> queue = new ArrayList<Action>();
	private ArrayList<Item> totalItems = new ArrayList<Item>();
	private ArrayList<Critter> deadCritters = new ArrayList<Critter>();
	private Session session;
	private Action performingAbility = null;
	private Item item1, item2, item3;


	private Item [] items = new Item[3];
	private String username;
	private int playerCompletedActionCount = 0;
	private int combinedCompletedActionCount = 0;
	private boolean isBot = false;
	private String id;

	private int turnStartHealthSum;
	private int turnEndHealthSum;
	private int turnStartEnergySum;
	private int turnEndEnergySum;

	public Player(Square topBack, Square topFront, Square middleBack,
			Square middleFront, Square bottomBack, Square bottomFront, Square bench) {
		//player's squares
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
		//items the player can choose from
		totalItems.add(new CrimberryPack(null,null));
		totalItems.add(new WarHorn(null,null));
		totalItems.add(new OilBomb(null,null));
		totalItems.add(new RatlandJestersHat(null,null));
		totalItems.add(new ThiefGloves(null,null));
		totalItems.add(new SmokeBomb(null,null));

	}
	public Square pickRandomSpot() {
		Random random = new Random();
		return squares[random.nextInt(6)];
	}
	public Critter pickRandomCritter() {
		//check at least one is alive
		Random random = new Random();
		ArrayList<Critter> livingCritters = new ArrayList<Critter>();
		for (int i = 0; i < critters.length; i++) {
			if (critters[i].isAlive() && !critters[i].isBenched()) {
				livingCritters.add(critters[i]);
			}
		}
		if (livingCritters.isEmpty()) return null;
		Critter c = livingCritters.get(random.nextInt(livingCritters.size()));
		return c;
	}
	public Action pickRandomAction(Critter c) {
		Random random = new Random();

		Action a = c.getAbilities()[random.nextInt(4)];
		return a;
	}
	public void randomFillActionQueueEasy(Player otherPlayer) {
		Random random = new Random();
				double totalTime = 0;
		while (totalTime < 7) {
			Critter c = pickRandomCritter();
			if (c.isBenched()) continue;
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
			if (totalTime + a.getTimeCost() > 10 ) break;
			if (a.getEnergyCost() > c.getEnergy()) break;
			// bench if below 30 mana and have enough time and mana left and in back row
			if (c.getEnergy() < 30 && c.getEnergy() >= 10 && totalTime + a.getTimeCost() <= 9 && c.canBench() && this.getCritters().length > 1) {
				Bench bench = new Bench(c, this.bench, this, otherPlayer);
				bench.init();
				totalTime += bench.getTimeCost();
				continue;
			}
			Action ability = a.getNew();
			ability.setPlayer(this);
			ability.setSubject(c);
			ability.setOtherPlayer(otherPlayer);
			if (ability.getTargetType().equals("critter")) {
				if (ability.getType().equals("attack")) {
					ability.setTarget(otherPlayer.pickRandomCritter());
				} else {
					ability.setTarget(pickRandomCritter());
				}
			} else if (ability.getTargetType().equals("spot")) {
				if (ability.getType().equals("attack")) {
					ability.setTarget(otherPlayer.pickRandomCritter().getSpot());
				} else if (ability.getType().equals("support")) {
					ability.setTarget(pickRandomCritter().getSpot());
				} else if (ability.getType().equals("block")) {
					ability.setTarget(pickRandomSpot());
				}
			}
			if (ability.initable()) {
				System.out.println("RANDOMFILL EASY INIT ABILITY " + ability + " subject = " + ability.getSubject() + " target = " + ability.getTarget());
				ability.init();
				totalTime += ability.getTimeCost();
			}
		}

	}

	public void randomFillActionQueueMedium(Player otherPlayer) {
		Random random = new Random();
		double totalTime = 0;
		Critter tank = critters[0];
		if (getDeadCritters().size() >= 1 && getBench().getCritter() != null) {
			Critter benched = getBench().getCritter();
			ArrayList<Square> possibleMoves = benched.getPossibleMoves();
			if (possibleMoves.size() > 0)  {
					// if (!s.isPlannedMove()) {
					// 	backRow.remove(s);
					// }

				Square toMove = possibleMoves.get(random.nextInt(possibleMoves.size()));
				System.out.println("unbench fill queue medium " + toMove.getName());

				Unbench u = new Unbench(getBench().getCritter(), toMove, this, otherPlayer);
				u.init();
				totalTime += u.getTimeCost();
			}
		}
		if (random.nextInt(2) == 1 && !tank.isBenched()) {
			System.out.println("attempt to move tank " + tank);
				// 1/2 chance for tank to move infront of lowest hp fighter

			//get lowest hp critter
			int leastHP = 1000;
			Critter leastHPCritter = null;
			for (Critter oc : getCritters()) {
				if (oc.equals(tank)) continue;
				if (oc.isBenched()) continue;
				if (oc.getHealth() < leastHP) {
					leastHP = oc.getHealth();
					leastHPCritter = oc;
				}
			}

			// if theres an empty spot infront of the lowest hp critter, move c to that spot
			if (leastHPCritter != null && leastHPCritter.getInfront() == null && leastHPCritter.getTempSpot().getInfront() != null) {
				System.out.println("movetank2");
				Square spotToMove = leastHPCritter.getTempSpot().getInfront();
				totalTime += tank.move(spotToMove, 0);
			}
		}
		while (totalTime < 7) {
			Critter c = pickRandomCritter();
			if (c.isBenched()) continue;


			int possibleMoveCount = c.getPossibleMoves().size();
			if (possibleMoveCount > 0 && random.nextInt(13) == 1) {
				//random move
				ArrayList<Square> possibleMoves = c.getPossibleMoves();
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
			Action a = pickRandomAction(c);

			int tries = 0;
			while ((totalTime + a.getTimeCost() > 10 || a.getEnergyCost() > c.getEnergy()) && tries < 6) {
				c = pickRandomCritter();
				a = pickRandomAction(c);
				tries += 1;
			}
			if (totalTime + a.getTimeCost() > 10 ) break;
			if (a.getEnergyCost() > c.getEnergy()) break;
			// bench if below 30 mana and have enough time and mana left and in back row
			if (c.getEnergy() < 30 && c.getEnergy() >= 10 && totalTime + a.getTimeCost() <= 9 && c.canBench() && this.getCritters().length > 1) {
				Bench bench = new Bench(c, this.bench, this, otherPlayer);
				bench.init();
				totalTime += bench.getTimeCost();
				continue;
			}
			Action ability = a.getNew();
			ability.setPlayer(this);
			ability.setSubject(c);
			ability.setOtherPlayer(otherPlayer);
			if (ability.getTargetType().equals("critter")) {
				if (ability.getType().equals("attack")) {

					//1/3 chance to target their non-tanks that aren't protected
					if (random.nextInt(3) == 1) {
						Critter toAttack = null;
						for (int i = 1; i < otherPlayer.getCritters().length; i++) {
							Critter oc = otherPlayer.getCritters()[i];

							if (oc.isBenched()) continue;
							if (oc.getInfront() != null) continue;
							if (!oc.isAlive()) continue;

							toAttack = oc;
							break;
						}

						if (toAttack == null) {
							toAttack = otherPlayer.pickRandomCritter();
						}
						ability.setTarget(toAttack);
					}


					//1/3 chance to target their lowest hp enemy
					else if (random.nextInt(3) == 1) {
						int leastHP = 1000;
						Critter leastHPCritter = null;
						for (Critter oc : otherPlayer.getCritters()) {
							if (oc.isBenched()) continue;
							if (!oc.isAlive()) continue;
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
			} else if (ability.getTargetType().equals("spot")) {
				if (ability.getType().equals("attack")) {
					ability.setTarget(otherPlayer.pickRandomCritter().getSpot());
				} else if (ability.getType().equals("support")) {
					ability.setTarget(pickRandomCritter().getSpot());
				} else if (ability.getType().equals("block")) {
					ability.setTarget(pickRandomSpot());
				}
			}
			if (ability.initable())
				ability.init();
			totalTime += ability.getTimeCost();
		}

	}
	public void randomFillActionQueue(Player otherPlayer) {
		System.out.println("RANDOMFILLACTIONQUEUE");
		randomFillActionQueueEasy(otherPlayer);
	}

	public void sendString(String str) {
		if (endpoint != null) {
			try {
	//			System.out.println("sendstring " + str);
				endpoint.sendString(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @param queue action queue to be turned into a string
	 * @return string to be sent to client in order to show icons
	 * Takes the action queue and creates a string with the details
	 * needed to create the visual representations in the client
	 */
	public String iconString(ArrayList<Action> queue) {
		String str = "";
		for (int i = 0; i < queue.size(); i++) {
			str += queue.get(i).getTimeCost() + "/"
					+ queue.get(i).getEnergyCost() + "/"
					+ queue.get(i).getUsingName() + "/"
					+ queue.get(i).getTargetName() + "/"
					+ queue.get(i).getName() + "/";
					if (queue.get(i).getType().equals("attack") || queue.get(i).getType().equals("support") || queue.get(i).getType().equals("block")) {
						str += queue.get(i).getType() + "/"
						+ queue.get(i).getTarget().getSide()  + "/"
						+ queue.get(i).getIndicatorMessage()  + "/"
						+ queue.get(i).getDescription()
						+ "_";
					} else if (queue.get(i).getType().equals("move")) {
						str += queue.get(i).getType()  +"/"
						+ "unimportant" + "/"
						+ queue.get(i).getIndicatorMessage() + "/"
						+ queue.get(i).getDescription()
						+ "_";
					}
		}
		return str;
	}

	public String queueToString(ArrayList<Action> queue) {
		String str = "";
		for (int i = 0; i < queue.size(); i++) {
			str += queue.get(i).getType() + "," + queue.get(i).getUsingName() + "," + queue.get(i).getTargetName() + ", ";
		}
		return str;
	}

	public Item identifyItem(String name) {
		items[0] = item1;
		items[1] = item2;
		items[2] = item3;
		for (Item item : items) {

			if (name.toLowerCase().equals(item.getName().toLowerCase())) {
				return item;
			}
		}
		return null;
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

	public Square [] getSquares() {
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

	public void setCritters(Critter[] critters) {
		this.critters = critters;
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
			id = "0";
		} else {
			id = "1";
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
	public String getId() {
		return id;
	}
	public String getSide() {
		return side;
	}

	public ArrayList<Item> getTotalItems() {
		return totalItems;
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

	public Item identifyTotalItems (String name) {
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
	public ArrayList<Critter> getDeadCritters() {
		return deadCritters;
	}

	public Item [] getItems() {
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

	public boolean isBot () {
		return isBot;
	}

	public void setBot(boolean isBot) {
		this.isBot = isBot;
	}
	public PlayerStateDTO toStateDto() {
		Critter [] totalCritters = Stream.concat(Arrays.stream(critters), deadCritters.stream())
                        .toArray(Critter[]::new);
		List<String> itemNames = Arrays.stream(items).map(Item::getName).collect(Collectors.toList());
		List<FighterStateDTO> fighterDtos = Arrays.stream(totalCritters).map(Critter::toFighterStateDTO).collect(Collectors.toList());
		PlayerStateDTO pdto = new PlayerStateDTO(side == "left" ? "0" : "1", morale, fighterDtos, itemNames);
		return pdto;
	}

}
