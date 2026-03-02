package com.gabe.animalia.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AbilityTimer {
    private Action action;
    private Player  player;
    private Player otherPlayer;
    private int iterator;
    private Game game;
    private TypesAndStuff taf = new TypesAndStuff();

	public AbilityTimer(Action action, Player player, Player otherPlayer, int iterator, Game game) {
		this.action = action;
		this.player = player;
		this.otherPlayer = otherPlayer;
		this.iterator = iterator;
		this.game = game;
	}

	public void endTurn() {
		if (!game.getGameOver()) {

			for (Critter f : player.getCritters()) {
				if (f.isBenched()) {
					f.benchedEffect(player, otherPlayer);
				}
			}

			for (Critter f : player.getCritters()) {
				f.triggerEffects();
			}
			for (Critter f : otherPlayer.getCritters()) {
				f.triggerEffects();
			}
			for (Critter f : player.getCritters()) {
				f.effectDuration();
			}
			for (Critter f : otherPlayer.getCritters()) {
				f.effectDuration();
			}
			game.checkDeath();
			game.checkGameOver();
			if (!game.getGameOver()) {
				player.setBenchActionUsed(false);
				otherPlayer.setBenchActionUsed(false);
				player.getItemQueue().clear();
				otherPlayer.getItemQueue().clear();
				player.setSelectedAbility(null);
				player.setSelectedCritter(null);
				otherPlayer.setSelectedAbility(null);
				otherPlayer.setSelectedCritter(null);
				player.getQueue().clear();
				otherPlayer.getQueue().clear();
				player.setReadied(false);
				otherPlayer.setReadied(false);
				player.setUsedTime(0);
				otherPlayer.setUsedTime(0);
				player.setAllottedTime(0);
				otherPlayer.setAllottedTime(0);
				player.setPlayerCompletedActionCount(0);
				otherPlayer.setPlayerCompletedActionCount(0);
				player.setCombinedCompletedActionCount(0);
				otherPlayer.setCombinedCompletedActionCount(0);
				for (Square s : player.getSquares()) {
					if (s.isOccupied()) {
						s.setPlannedMove(true);
						Critter occupier = null;
						for (Critter c : player.getCritters()) {
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
				for (Square s : otherPlayer.getSquares()) {
					if (s.isOccupied()) {
						s.setPlannedMove(true);
						Critter occupier = null;
						for (Critter c : otherPlayer.getCritters()) {
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

					player.sendString("reset");
					otherPlayer.sendString("reset");
				for (Item i : player.getItems()) {
					i.setPerformable(true);
				}
				for (Item i : otherPlayer.getItems()) {
					i.setPerformable(true);
				}
				for (Critter f : player.getCritters()) {
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
					f.sendStats(player, otherPlayer);
				}
				for (Critter f : otherPlayer.getCritters()) {
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
					f.sendStats(player, otherPlayer);
				}
				System.out.println(" player bot?: " + player.isBot());
				if (player.isBot()) {
					System.out.println("ability timer random fill");
					player.randomFillActionQueue(otherPlayer);
				}

					player.sendString("calculating,end");
					otherPlayer.sendString("calculating,end");
			}
		}

	}

	public void performAction(final Timer timer) {

		System.out.println(action.getName() + ", " + action.getSubject() + ", "
				+ action.getOrder() + ", "
				+ player.getCombinedCompletedActionCount());
		if (player.getCombinedCompletedActionCount() == action.getOrder()) {
			if (!(action.isInterrupted() || (action.getTargetType() == "critter" && ((Critter)action.getTarget()).getSpot().getName().contains("Bench"))) && (action.getType() == "item" || action.getEnergyCost() <= action.getSubject().getEnergy())) {
				if (action.getSubject() == null) {
					action.perform();
					action.animate(player, otherPlayer);
				} else if ((!action.getSubject().isBenched() || (action
						.getName().equals("Unbench") && action
						.isStartPerformable()))
						&& !(action.getTargetType().equals("critter") && !((Critter) action
								.getTarget()).isAlive())) {
					action.getSubject().onAction(player, otherPlayer, action);

					boolean performable = action.performable();

					action.perform();
					action.animate(player, otherPlayer);
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
			game.checkDeath();
			game.checkGameOver();

			player.setCombinedCompletedActionCount(player.getCombinedCompletedActionCount() + 1);
			otherPlayer.setCombinedCompletedActionCount(otherPlayer.getCombinedCompletedActionCount() + 1);

		}  else if (!(action.getOrder() < player.getCombinedCompletedActionCount())) {
			timer.schedule(new TimerTask() {
				public void run() {
					performAction(timer);
				}
			}, (long) (100));
		}
	}

	public void animate(final Timer timer) {
		System.out.println("animate: " + player.getCombinedCompletedActionCount() + "," + action.getDisplayOrder());
		if (player.getCombinedCompletedActionCount() == action.getDisplayOrder()) {
			player.setPerformingAbility(action);

			ArrayList<Action> playerQueue = player.getQueue();

			if (iterator < playerQueue.size()) {
				boolean startPerformable = action.isStartPerformable();
				for (Action a : action.getSubject().getEffects()) {
					if (taf.isSuppress(a)) {
						startPerformable = false;
					}
				}
				if (action.getSubject().isBenched() && !action.getName().equals("Unbench")
						|| (action.getTargetType().equals("critter") && ((Critter) action
								.getTarget()).isBenched())) {
					startPerformable = false;
				}
				if (startPerformable
						&& action.getSubject().getEnergy()
								- action.getEnergyCost() >= 0) {
					player.setUsedTime(player.getUsedTime()
							+ action.getTimeCost());
					if (action.getSubject().canUseAction()
							&& action.performable()) {

						action.displayAction();
						if (!action.getName().equals("move")
								&& !action.getName().equals("Bench")
								&& !action.getName().equals("Unbench")) {
							action.getSubject().animate(player, otherPlayer,
									action.getTimeCost(), action);
						}

						for (Action a : action.getSubject().getEffects()) {
							if (taf.isImmobilize(a)) {
								startPerformable = false;
							}
						}
						if (startPerformable) {
							action.displayAction();
						} else {
							action.setStartPerformable(false);
						}

					} else {
						action.setStartPerformable(false);
					}
				} else {
					action.setStartPerformable(false);
				}
				player.setCombinedCompletedActionCount(player
						.getCombinedCompletedActionCount() + 1);
				otherPlayer.setCombinedCompletedActionCount(otherPlayer
						.getCombinedCompletedActionCount() + 1);
			}
		} else {
			timer.schedule(new TimerTask() {
				public void run() {
					animate(timer);
				}
			}, (long) (100));
		}
	}



}
