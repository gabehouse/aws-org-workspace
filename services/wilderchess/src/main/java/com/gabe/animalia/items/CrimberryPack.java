package com.gabe.animalia.items;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;

public class CrimberryPack extends Item {
	private String imageName = "firstaidkit.png";

	public CrimberryPack(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.CRIMBERRY_PACK);
	}

	@Override
	public boolean customPerform() {

		for (Critter f : getPlayer().getCritters()) {
			f.clearEffectsByType(StatusEnum.BURN);
		}

		return true;
	}

	@Override
	public String getImageName() {

		return imageName;
	}
}
