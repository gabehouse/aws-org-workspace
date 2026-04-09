package com.gabe.animalia.general;

import java.util.ArrayList;
import java.util.HashMap;

public class Square extends Targetable {
	private boolean plannedMove;

	private Critter critter;
	private String name;
	private Square onLeft;
	private Square onRight;
	private Square behind;
	private Square infront;
	private String side;
	private boolean bench = false;
	private ArrayList<Square> surroundingSquares;
	private HashMap<String, String> topLeftCoord = new HashMap<String, String>();

	public Square(String name, String side) {
		this.name = name;
		topLeftCoord.put("leftTopBack", "00");
		topLeftCoord.put("leftTopFront", "10");
		topLeftCoord.put("leftMiddleBack", "01");
		topLeftCoord.put("leftMiddleFront", "11");
		topLeftCoord.put("leftBottomBack", "02");
		topLeftCoord.put("leftBottomFront", "12");
		topLeftCoord.put("leftBench", "leftBench");
		topLeftCoord.put("rightTopFront", "30");
		topLeftCoord.put("rightTopBack", "40");
		topLeftCoord.put("rightMiddleFront", "31");
		topLeftCoord.put("rightMiddleBack", "41");
		topLeftCoord.put("rightBottomFront", "32");
		topLeftCoord.put("rightBottomBack", "42");
		topLeftCoord.put("rightBench", "rightBench");
		this.side = side;
		if (name.contains("Bench")) {
			bench = true;
		}

		// --- ADD THIS TO FIX THE -1000 ---
		String coord = getTopLeftCoord();
		if (coord != null) {
			if (coord.contains("Bench")) {
				// Assign bench spots a specific range, e.g., 900 for left, 901 for right
				this.id = side.equals("left") ? 900 : 901;
			} else {
				// Convert "12" string to 100 + 12 = 112
				// This ensures squares have a unique, consistent ID range (100-142)
				this.id = 100 + Integer.parseInt(coord);
			}
		}
	}

	public String getTopLeftCoord() {
		return topLeftCoord.get(name);
	}

	public int getBoardIndex() {
		// 1. Give the bench a dedicated index (7)
		if (this.bench)
			return 7;

		String coord = getTopLeftCoord();
		if (coord == null)
			return -1;

		switch (coord) {
			case "00":
			case "40":
				return 0;
			case "10":
			case "30":
				return 1;
			case "01":
			case "41":
				return 2;
			case "11":
			case "31":
				return 3;
			case "02":
			case "42":
				return 4;
			case "12":
			case "32":
				return 5;
			default:
				return 6;
		}
	}

	@Override
	public String getType() {
		return "spot";
	}

	public boolean isBench() {
		return bench;
	}

	public Square getOnLeft() {
		return onLeft;
	}

	public void setSurrounding(Square infront, Square behind, Square onLeft, Square onRight) {
		this.infront = infront;
		this.behind = behind;
		this.onLeft = onLeft;
		this.onRight = onRight;
		surroundingSquares = new ArrayList<>();
		if (infront != null) {
			surroundingSquares.add(infront);
		}
		if (behind != null) {
			surroundingSquares.add(behind);
		}
		if (onLeft != null) {
			surroundingSquares.add(onLeft);
		}
		if (onRight != null) {
			surroundingSquares.add(onRight);
		}

	}

	public Square getSurroundingSquare(String name) {
		if (infront != null) {
			if (name.equals(infront.toString())) {
				return infront;
			}
		}
		if (behind != null) {
			if (name.equals(behind.toString())) {
				return behind;
			}
		}
		if (onLeft != null) {
			if (name.equals(onLeft.toString())) {
				return onLeft;
			}
		}
		if (onRight != null) {
			if (name.equals(onRight.toString())) {
				return onRight;
			}
		}
		return null;

	}

	public boolean compareSurrounding(String name) {
		if (infront != null) {
			if (name.equals(infront.toString())) {
				return true;
			}
		}
		if (behind != null) {
			if (name.equals(behind.toString())) {
				return true;
			}
		}
		if (onLeft != null) {
			if (name.equals(onLeft.toString())) {
				return true;
			}
		}
		if (onRight != null) {
			if (name.equals(onRight.toString())) {
				return true;
			}
		}
		return false;
	}

	public String getSide() {
		return side;
	}

	public boolean isPlannedMove() {
		return plannedMove;
	}

	public void setPlannedMove(boolean plannedMove) {
		this.plannedMove = plannedMove;
	}

	public Square getOnRight() {
		return onRight;
	}

	public Square getBehind() {
		return behind;
	}

	public Square getInfront() {
		return infront;
	}

	public boolean isOccupied() {
		if (critter != null) {
			return true;
		}
		return false;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Critter getCritter() {
		return critter;
	}

	public void setCritter(Critter critter) {
		this.critter = critter;
	}

	public ArrayList<Square> getSurroundingSquares() {
		return surroundingSquares;
	}

	public String toString() {
		if (name == null) {
			return "null";
		}
		return name;
	}

}
