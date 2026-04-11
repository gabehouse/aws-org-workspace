package com.gabe.animalia.general;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;

public class User {
	private Critter[] team;
	private Item[] items;
	private RemoteEndpoint endpoint;
	private int id;
	private Player player;
	private String difficulty; // New field for bot difficulty

	public User(RemoteEndpoint endpoint, int id) {
		this.endpoint = endpoint;
		this.id = id;
	}

	// New Getter/Setter for Difficulty
	public String getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(String difficulty) {
		this.difficulty = difficulty;
	}

	public Critter[] getTeam() {
		return team;
	}

	public void setTeam(Critter[] team) {
		this.team = team;
	}

	public Item[] getItems() {
		return items;
	}

	public void setItems(Item[] items) {
		this.items = items;
	}

	public RemoteEndpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(RemoteEndpoint endpoint) {
		this.endpoint = endpoint;
	}

	public int getID() {
		return this.id;
	}

	public Player getPlayer() {
		return this.player;

	}

	public void setPlayer(Player player) {
		this.player = player;
		if (difficulty != null) {
			this.player.setDifficulty(this.difficulty);
		}
	}
}
