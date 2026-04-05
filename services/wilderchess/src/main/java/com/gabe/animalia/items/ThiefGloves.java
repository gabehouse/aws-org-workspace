package com.gabe.animalia.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.gabe.animalia.enums.ActionEnum;
import com.gabe.animalia.general.Item;
import com.gabe.animalia.general.Player;

public class ThiefGloves extends Item {
	private String imageName = "thiefgloves.png";

	public ThiefGloves(Player player, Player otherPlayer) {
		super(player, otherPlayer, ActionEnum.THIEF_GLOVES);
	}

	@Override
	public boolean customPerform() {
		int pos = -1;
		Random rng = new Random();
		Item toSteal = null;
		ArrayList<Item> itemsBeingUsed = new ArrayList<Item>();
		for (Item i : getOtherPlayer().getItemQueue()) {
			if (i.getSecond() >= second) {
				System.out.println("gloves perform3");
				itemsBeingUsed.add(i);
			}
		}
		if (!itemsBeingUsed.isEmpty()) {
			System.out.println("gloves perform4");
			int rnged = rng.nextInt(itemsBeingUsed.size());
			toSteal = itemsBeingUsed
					.get(rnged);
			toSteal.setPerformable(false);

			toSteal.setPlayer(getPlayer());
			toSteal.setOtherPlayer(getOtherPlayer());
			System.out.println(toSteal.getName());
			Item[] items = getPlayer().getItems();

			for (int i = 0; i < items.length; i++) {
				if (items[i] != null && items[i].getName().equals(this.name)) {
					System.out.println("gloves perform5");
					items[i] = toSteal;
					getPlayer().sendString(
							"inititems,|" + toSteal.getName() + ","
									+ toSteal.getDescription() + "," + i + ","
									+ toSteal.getImageName() + "|");
					// pos = i;
					break;
				}
			}

		}

		return true;
	}

	@Override
	public String getImageName() {

		return imageName;
	}

}
