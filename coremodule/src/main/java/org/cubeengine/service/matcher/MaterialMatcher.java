/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cubeengine.service.matcher;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.spongepowered.api.Game;
import org.spongepowered.api.GameDictionary;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;

import static org.spongepowered.api.item.ItemTypes.*;

/**
 * This Matcher provides methods to match Material or Items.
 */
@ServiceProvider(MaterialMatcher.class)
public class MaterialMatcher
{
    private final Map<String, ItemType> names = new HashMap<>();
    private final Map<Integer, ItemType> legacyIds = new HashMap<>(); // TODO fill legacy map
    private final Map<String, ItemType> ids = new HashMap<>();

    private final ItemStack.Builder builder;

    private final Set<ItemType> repairableMaterials = Collections.synchronizedSet(new HashSet<>(Arrays.asList(
                                                                                      IRON_SHOVEL, IRON_PICKAXE,
                                                                                      IRON_AXE, IRON_SWORD, IRON_HOE,
                                                                                      WOODEN_SHOVEL, WOODEN_PICKAXE,
                                                                                      WOODEN_AXE, WOODEN_SWORD,
                                                                                      WOODEN_HOE, STONE_SHOVEL,
                                                                                      STONE_PICKAXE, STONE_AXE,
                                                                                      STONE_SWORD, STONE_HOE,
                                                                                      DIAMOND_SHOVEL, DIAMOND_PICKAXE,
                                                                                      DIAMOND_AXE, DIAMOND_SWORD,
                                                                                      DIAMOND_HOE, GOLDEN_SHOVEL,
                                                                                      GOLDEN_PICKAXE, GOLDEN_AXE,
                                                                                      GOLDEN_SWORD, GOLDEN_HOE,
                                                                                      LEATHER_HELMET,
                                                                                      LEATHER_CHESTPLATE,
                                                                                      LEATHER_LEGGINGS, LEATHER_BOOTS,
                                                                                      CHAINMAIL_HELMET,
                                                                                      CHAINMAIL_CHESTPLATE,
                                                                                      CHAINMAIL_LEGGINGS,
                                                                                      CHAINMAIL_BOOTS, IRON_HELMET,
                                                                                      IRON_CHESTPLATE, IRON_LEGGINGS,
                                                                                      IRON_BOOTS, DIAMOND_HELMET,
                                                                                      DIAMOND_CHESTPLATE,
                                                                                      DIAMOND_LEGGINGS, DIAMOND_BOOTS,
                                                                                      GOLDEN_HELMET, GOLDEN_CHESTPLATE,
                                                                                      GOLDEN_LEGGINGS, GOLDEN_BOOTS,
                                                                                      FLINT_AND_STEEL, BOW, FISHING_ROD,
                                                                                      SHEARS)));
    @Inject private StringMatcher stringMatcher;
    private Game game;

    @Inject
    public MaterialMatcher( Game game)
    {
        this.game = game;
        this.builder = game.getRegistry().createBuilder(ItemStack.Builder.class);

        // Read names from GameDirectory
        for (Entry<String, GameDictionary.Entry> entry : game.getGameDictionary().getAll().entries())
        {
            names.put(entry.getKey(), entry.getValue().getType());
        }

        for (ItemType itemType : game.getRegistry().getAllOf(ItemType.class))
        {
            ids.put(itemType.getName(), itemType);
        }

        // TODO legacy ID -> ItemType Map
    }

    private ItemType matchWithLevenshteinDistance(String s, Map<String, ItemType> map)
    {

        String t_key = stringMatcher.matchString(s, map.keySet());
        if (t_key != null)
        {
            return map.get(t_key);
        }
        return null;
    }

    private HashMap<ItemStack, Double> allMatchesWithLevenshteinDistance(String s, Map<String, ItemType> map,
                                                                         int maxDistance, int minPercentage)
    {
        HashMap<ItemStack, Double> itemMap = new HashMap<>();
        TreeMap<String, Integer> itemNameList = stringMatcher.getMatches(s, map.keySet(), maxDistance, true);

        for (Entry<String, Integer> entry : itemNameList.entrySet())
        {
            double curPercentage = (entry.getKey().length() - entry.getValue()) * 100 / entry.getKey().length();
            if (curPercentage >= minPercentage)
            {
                itemMap.put(builder.itemType(map.get(entry.getKey())).build(), curPercentage);
            }
        }

        return itemMap;
    }

    /**
     * Tries to match a ItemStack for given name
     *
     * @param name the name
     * @return the found ItemStack
     */
    public ItemStack itemStack(String name)
    {
        if (name == null)
        {
            return null;
        }
        String[] parts = name.toLowerCase(Locale.ENGLISH).split(":");
        ItemType type = material(parts[0]);

        ItemStack.Builder builder = this.builder.itemType(type).quantity(1);
        if (parts.length > 1)
        {
            for (int i = 1; i < parts.length; i++)
            {
                // TODO match data and add to itemstack
            }
        }
        return builder.build();
    }

    /**
     * Tries to match a ItemStack-list for given name
     *
     * @param name the name
     * @return the found ItemStack-list
     */
    public List<ItemStack> itemStackList(String name)
    {
        if (name == null)
        {
            return null;
        }

        name = name.toLowerCase(Locale.ENGLISH);

        ItemStack best = itemStack(name);

        HashMap<ItemStack, Double> itemMap = this.allMatchesWithLevenshteinDistance(name, names, 5, 50);
        itemMap.put(best, 0d);
        TreeSet<Entry<ItemStack, Double>> itemSet = new TreeSet<>(new ItemStackComparator());
        itemSet.addAll(itemMap.entrySet());

        return itemSet.stream().map(Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Tries to match a Material for given name
     *
     * @param name the name
     * @return the material or null if not found
     */
    public ItemType material(String name)
    {
        ItemType type = this.names.get(name); //direct match
        if (type == null)
        {
            try
            {
                type = legacyIds.get(Integer.valueOf(name));
            }
            catch (NumberFormatException e)
            {
                String match = stringMatcher.matchString(name, names.keySet());
                type = names.get(match);
            }
        }
        return type;
    }


    public BlockType block(String name)
    {
        return game.getRegistry().getType(BlockType.class, name).orElse(null);
    }

    /**
     * Returns whether the given ItemStack is repairable
     */
    public boolean repairable(ItemStack item)
    {
        return item != null && this.repairableMaterials.contains(item.getItem());
    }

    /**
     * Returns the name for given ItemStack
     *
     * @param item the item
     * @return the name or null if none was found
     */
    public String getNameFor(ItemStack item)
    {
        return item.getItem().getTranslation().get(); // TODO get Humanreadablename
    }

    private static class ItemStackComparator implements Comparator<Entry<ItemStack, Double>>
    {
        @Override
        public int compare(Entry<ItemStack, Double> item1, Entry<ItemStack, Double> item2)
        {
            if (item1.getValue() > item2.getValue())
            {
                return -1;
            }
            else if (item1.getValue() < item2.getValue())
            {
                return 1;
            }
            return 0;
        }
    }
}
