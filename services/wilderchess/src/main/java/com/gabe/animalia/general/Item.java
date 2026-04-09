package com.gabe.animalia.general;

import com.gabe.animalia.enums.ActionEnum;

public class Item extends Action {
	protected int second = -1;
	protected boolean used = false;

	public Item(Player player, Player otherPlayer, ActionEnum actionData) {
		super(null, null, player, otherPlayer, actionData);
	}

	public boolean isUsed() {
		return this.used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public String getImageName() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getSecond() {
		// TODO Auto-generated method stub
		return this.second;
	}

	public void setSecond(int second) {
		this.second = second;

	}

}
