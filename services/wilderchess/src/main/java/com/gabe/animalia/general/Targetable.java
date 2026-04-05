package com.gabe.animalia.general;

public class Targetable {
	// 1. Add a field to store the actual ID
	protected int id = -1000;

	public String getName() {
		return null;
	}

	public String getSide() {
		return null;
	}

	public String getType() {
		return null;
	}

	public void unbenchEffect(Player player, Player otherPlayer) {
	}

	public void benchEffect(Player player, Player otherPlayer) {
	}

	// 2. Update this to return the stored field
	public int getId() {
		return this.id;
	}

	// 3. Add a setter so you can assign IDs dynamically if needed
	public void setId(int id) {
		this.id = id;
	}

}
