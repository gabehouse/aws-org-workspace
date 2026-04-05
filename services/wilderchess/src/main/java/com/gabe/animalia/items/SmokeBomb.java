package com.gabe.animalia.items;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;

public class SmokeBomb extends Item {
	private String imageName = "smokebomb.png";

	public SmokeBomb(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.SMOKE_BOMB);
	}

	@Override
	public boolean customPerform() {
		for (Critter c : getPlayer().getCritters()) {
			c.addEffect(this);
		}
		for (Critter c : getOtherPlayer().getCritters()) {
			c.addEffect(this);
		}

		return true;
	}

	@Override
	public String getImageName() {

		return imageName;
	}
}
