package com.alex.scripts;

import com.alex.loaders.items.ItemDefinitions;
import com.alex.store.Store;
import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;

import java.io.IOException;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/12/2016
 */
public class ItemDefinitionModifier {

	public static void main(String[] args) throws IOException {
		Cache.init();
		Store store = new Store(GameConstants.CACHE_PATH);
		ItemDefinitions def = ItemDefinitions.getItemDefinition(store, 7312);
		def.setName("Casket (gold)");
		def.write(store, true);
	}
}
