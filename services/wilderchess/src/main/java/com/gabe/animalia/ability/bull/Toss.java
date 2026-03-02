package com.gabe.animalia.ability.bull;

import java.io.IOException;
import java.util.ArrayList;

import com.gabe.animalia.general.Action;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Player;
import com.gabe.animalia.general.Square;
import com.gabe.animalia.general.Targetable;
import com.gabe.animalia.general.TypesAndStuff;

public class Toss extends Action{
	private Critter subject;
	private Square target;
	private String targetType = "spot";
	private String name = "Toss";
	private String type = "support";
	private String statusType = "suppress";
	private double timeCost = 2;
	private int energyCost = 60;
	private Player player;
	private Player otherPlayer;
	private String indicatorMessage;
	private boolean performable = true;
	private TypesAndStuff tas = new TypesAndStuff();
	private String effectDescription = "stunned";
	private int duration = 2;
	private Critter critter;



	public Toss(Critter subject, Targetable target, Player player, Player otherPlayer) {
		this.subject = subject;
		this.target = (Square)target;
		this.player = player;
		this.otherPlayer = otherPlayer;
	}

	@Override
	public String getIndicatorMessage() {
		return indicatorMessage;

	}

	@Override
	public void displayOptions() {
		int startX = 0;
		if (otherPlayer.getSide().equals("right")) {
			startX = 3;
		}
		String str = "indicatespots,support," + name + ",";
		if (otherPlayer.getSide().equals("right")) {
			str += otherPlayer.getSide() + "TopFront." + startX + "0-"
					+ (startX + 1) + "0-" + (startX + 1) + "1-" + startX + "1-"
					+ startX + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleFront." + startX + "1-"
					+ (startX + 1) + "1-" + (startX + 1) + "2-" + startX + "2-"
					+ startX + "1." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "BottomFront." + startX + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "3-" + startX + "3-"
					+ startX + "2." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "TopBack." + (startX + 1) + "0-"
					+ (startX + 2) + "0-" + (startX + 2) + "1-" + (startX + 1)
					+ "1-" + (startX + 1) + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleBack." + (startX + 1)
					+ "1-" + (startX + 2) + "1-" + (startX + 2) + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "1."
					+ otherPlayer.getSide() + "," + otherPlayer.getSide()
					+ "BottomBack." + (startX + 1) + "2-" + (startX + 2) + "2-"
					+ (startX + 2) + "3-" + (startX + 1) + "3-" + (startX + 1)
					+ "2." + otherPlayer.getSide() + ",";
		} else {
			str += otherPlayer.getSide() + "TopBack." + startX + "0-"
					+ (startX + 1) + "0-" + (startX + 1) + "1-" + startX + "1-"
					+ startX + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleBack." + startX + "1-"
					+ (startX + 1) + "1-" + (startX + 1) + "2-" + startX + "2-"
					+ startX + "1." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "BottomBack." + startX + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "3-" + startX + "3-"
					+ startX + "2." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "TopFront." + (startX + 1) + "0-"
					+ (startX + 2) + "0-" + (startX + 2) + "1-" + (startX + 1)
					+ "1-" + (startX + 1) + "0." + otherPlayer.getSide() + ","
					+ otherPlayer.getSide() + "MiddleFront." + (startX + 1)
					+ "1-" + (startX + 2) + "1-" + (startX + 2) + "2-"
					+ (startX + 1) + "2-" + (startX + 1) + "1."
					+ otherPlayer.getSide() + "," + otherPlayer.getSide()
					+ "BottomFront." + (startX + 1) + "2-" + (startX + 2) + "2-"
					+ (startX + 2) + "3-" + (startX + 1) + "3-" + (startX + 1)
					+ "2." + otherPlayer.getSide() + ",";
		}

			player.sendString(str);

	}

	@Override
	public void init() {

		if (target.getName().contains(otherPlayer.getSide())) {
			subject.getIndicatedSupports().add(target);
			player.getQueue().add(this);
			indicatorMessage = "indicate|supportspot," + target.getName();
		}

			player.sendString("indicatespots,no moves");

	}

	@Override
	public boolean performable() {
		if (subject.getEnergy() - energyCost >= 0 && performable) {
			return true;
		}
		return false;

	}

	public void actionLog() {
		String str = "actionlog," + subject.getName() + " used " + name
				+ " on " + target.getName() + ". ";
		if (critter != null) {
			if (target.getInfront() != null
					&& !target.getInfront().isOccupied()) {
				str += critter.getName() + " had a rocky landing at "
						+ target.getInfront().getName() + ".";
			} else {
				str += critter.getName() + " had a rocky landing.";
			}

		} else {
			str += "No passengers.";
		}


			player.sendString(str);
			otherPlayer.sendString(str);



	}

	@Override
	public boolean perform() {

		if (subject.getEnergy() - energyCost >= 0 && subject.canUseAction()
				&& performable()) {

				critter = target.getCritter();
				actionLog();
				subject.setEnergy(subject.getEnergy() - energyCost);
				if (critter != null) {

					if (target.getInfront() != null
							&& !target.getInfront().isOccupied()) {
						critter.getSpot().setCritter(null);
						critter.getSpot().setOccupied(false);
						critter.setSpot(target.getInfront());
						target.getInfront().setOccupied(true);
						target.getInfront().setCritter(critter);
						subject.removeEffect("Running Start");
					}
					if (otherPlayer.getPerformingAbility() != null) {
						if ((otherPlayer.getPerformingAbility().getType()
								.equals("move")
								&& (otherPlayer.getPerformingAbility()
										.getTarget().equals(critter.getSpot())) || (otherPlayer
								.getPerformingAbility().getSubject()
									.equals(critter)))
								|| (otherPlayer.getPerformingAbility()
										.getName().contains("bench")
										&& otherPlayer.getDeadCritters().size() == 0
										&& (!((Square) otherPlayer
												.getPerformingAbility()
												.getTarget()).isOccupied()) || otherPlayer
										.getPerformingAbility().getSubject()
										.equals(target))) {
							Critter a = null;
							if (otherPlayer.getPerformingAbility().getName()
									.equals("Bench")) {

								a = otherPlayer.getBench().getCritter();
							} else {
								a = otherPlayer.getPerformingAbility()
										.getSubject();
							}
							player.sendString(
									"animate,cancel," + a.getName() + ","
											+ otherPlayer.getSide());
							otherPlayer.sendString(
									"animate,cancel," + a.getName() + ","
											+ otherPlayer.getSide());
							player.sendString(
									"move," + a.getName() + ","
											+ otherPlayer.getSide() + ","
											+ a.getSpot().getName() + ","
											+ 1000);
							otherPlayer.sendString(
									"move," + a.getName() + ","
											+ otherPlayer.getSide() + ","
											+ a.getSpot().getName() + ","
											+ 1000);
						}
					}

					critter.getEffects().add(this);
					showEffect();
					for (Action a : critter.getEffects()) {
						if (tas.isChanneling(a)) {
							critter.removeEffect(a.getName());
						}

					}

					player.sendString(
							"animate,cancel," + critter.getName() + ","
									+ critter.getSide());
					otherPlayer.sendString(
							"animate,cancel," + critter.getName() + ","
									+ critter.getSide());
					player.sendString(
							"move," + critter.getName() + ","
									+ critter.getSide() + ","
									+ critter.getSpot().getName() + ","
									+ 1000);
					otherPlayer.sendString(
							"move," + critter.getName() + ","
									+ critter.getSide() + ","
									+ critter.getSpot().getName() + ","
									+ 1000);

				}

				player.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");
				otherPlayer.sendString(
						"energy," + subject.getName() + "," + subject.getSide()
								+ "," + subject.getEnergy() + "|");


			return true;
		}
		return false;
	}

	public void showEffect() {


			if (critter != null) {
				player.sendString(
						"effecttt,create,stun.png," + name + ","
								+ critter.getName() + "," + critter.getSide()
								+ "," + duration + "," + effectDescription
								+ "," + critter.getEffects().indexOf(this));
				otherPlayer.sendString(
						"effecttt,create,stun.png," + name + ","
								+ critter.getName() + "," + critter.getSide()
								+ "," + duration + "," + effectDescription
								+ "," + critter.getEffects().indexOf(this));
			}


	}

	@Override
	public void endEffect(Critter critter) {
		;

			player.sendString(
					"effecttt,remove," + critter.getName() + ","
							+ critter.getSide() + ","
							+ critter.getEffects().indexOf(this));
			otherPlayer.sendString(
					"effecttt,remove," + critter.getName() + ","
							+ critter.getSide() + ","
							+ critter.getEffects().indexOf(this));


	}

	@Override
	public void delete() {
		subject.getIndicatedSupports().remove(target);
	}

	@Override
	public int getDuration() {
		return duration;
	}

	@Override
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Override
	public String getStatusType() {
		return statusType;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public void setPlayer(Player player) {
		this.player = player;
	}

	@Override
	public Player getOtherPlayer() {
		return otherPlayer;
	}
	@Override
	public void setOtherPlayer(Player otherPlayer) {
		this.otherPlayer = otherPlayer;
	}
	@Override
	public Critter getSubject() {
		return subject;
	}
	@Override
	public void setSubject(Critter subject) {
		this.subject = subject;
	}
	@Override
	public Square getTarget() {
		return target;
	}
	@Override
	public void setTarget(Targetable target) {
		this.target = (Square)target;
	}

	@Override
	public String getTargetType() {
		return targetType;
	}

	@Override
	public String getDescription() {
		return name + "\nType: " + this.type + "\nEnergy: " + energyCost
				+ "\nTime: " + timeCost
				+ "\nDescription: Stuns fighter on \ntargeted spot for " + duration + " turns and \nmoves the fighter forward if \npossible.";
	}

//	@Override
//	public String getSelectDescription() {
//		return name + "\nHeal: "
//				// + heal + "\nType: " + this.type + "\nEnergy: " + energyCost
//				+ "\nTime: " + timeCost + "\nDescription: Heals critters on \ntargeted column.";
//	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTargetName() {
		return target.getName();
	}

	@Override
	public Action getNew() {
		return new Toss(null, null, null, null);

	}
	@Override
	public String getEffects() {
		return "stun.png";
	}

	@Override
	public String getUsingName() {
		return subject.getName();
	}

	@Override
	public double getTimeCost() {
		return timeCost;
	}

	@Override
	public int getEnergyCost() {
		return energyCost;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isPerformable() {
		return performable;
	}

	@Override
	public void setPerformable(boolean performable) {
		this.performable = performable;
	}

	@Override
	public void setTimeCost(double timeCost) {
		this.timeCost = timeCost;
	}

	@Override
	public void setEnergyCost(int energyCost) {
		this.energyCost = energyCost;
	}


}
