package com.gabe.animalia.items;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.enums.StatusEnum;
import com.gabe.animalia.general.Critter;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;

public class OilBomb extends Item {
	private String imageName = "oilbomb.png";

	public OilBomb(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.OIL_BOMB);
	}

	@Override
	public boolean customPerform() {
		for (Critter f : getOtherPlayer().getCritters()) {
			while (f.removeEffectByType(StatusEnum.BURN)) {
				f.setHealth(f.getHealth() - 25);
			}
		}

		return true;
	}

	@Override
	public String getImageName() {

		return imageName;
	}

}
