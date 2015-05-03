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
package de.cubeisland.engine.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
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
import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.filesystem.FileUtil;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackBuilder;

/**
 * This Matcher provides methods to match Material or Items.
 */
public class MaterialMatcher
{
    private final HashMap<String, ItemType> items;
    private final HashMap<String, ItemType> bukkitnames;

    private final HashMap<ItemType, Map<Short, String>> itemnames;

    private final MaterialDataMatcher materialDataMatcher;
    private final ItemStackBuilder builder;

    private final Set<ItemType> repairableMaterials = Collections.synchronizedSet(
        new HashSet<>(Arrays.asList(
            ItemTypes.IRON_SHOVEL, ItemTypes.IRON_PICKAXE, ItemTypes.IRON_AXE, ItemTypes.IRON_SWORD, ItemTypes.IRON_HOE,
            ItemTypes.WOODEN_SHOVEL, ItemTypes.WOODEN_PICKAXE, ItemTypes.WOODEN_AXE, ItemTypes.WOODEN_SWORD, ItemTypes.WOODEN_HOE,
            ItemTypes.STONE_SHOVEL, ItemTypes.STONE_PICKAXE, ItemTypes.STONE_AXE, ItemTypes.STONE_SWORD, ItemTypes.STONE_HOE,
            ItemTypes.DIAMOND_SHOVEL, ItemTypes.DIAMOND_PICKAXE, ItemTypes.DIAMOND_AXE, ItemTypes.DIAMOND_SWORD, ItemTypes.DIAMOND_HOE,
            ItemTypes.GOLDEN_SHOVEL, ItemTypes.GOLDEN_PICKAXE, ItemTypes.GOLDEN_AXE, ItemTypes.GOLDEN_SWORD, ItemTypes.GOLDEN_HOE,
            ItemTypes.LEATHER_HELMET, ItemTypes.LEATHER_CHESTPLATE, ItemTypes.LEATHER_LEGGINGS, ItemTypes.LEATHER_BOOTS,
            ItemTypes.CHAINMAIL_HELMET, ItemTypes.CHAINMAIL_CHESTPLATE, ItemTypes.CHAINMAIL_LEGGINGS, ItemTypes.CHAINMAIL_BOOTS,
            ItemTypes.IRON_HELMET, ItemTypes.IRON_CHESTPLATE, ItemTypes.IRON_LEGGINGS, ItemTypes.IRON_BOOTS,
            ItemTypes.DIAMOND_HELMET, ItemTypes.DIAMOND_CHESTPLATE, ItemTypes.DIAMOND_LEGGINGS, ItemTypes.DIAMOND_BOOTS,
            ItemTypes.GOLDEN_HELMET, ItemTypes.GOLDEN_CHESTPLATE, ItemTypes.GOLDEN_LEGGINGS, ItemTypes.GOLDEN_BOOTS,
            ItemTypes.FLINT_AND_STEEL,
            ItemTypes.BOW,
            ItemTypes.FISHING_ROD,
            ItemTypes.SHEARS)));

    MaterialMatcher(ItemStackBuilder builder, MaterialDataMatcher materialDataMatcher)
    {
        this.builder = builder;

        this.materialDataMatcher = materialDataMatcher;
        this.items = new HashMap<>();
        this.itemnames = new HashMap<>();
        this.bukkitnames = new HashMap<>();
        // Read Bukkit names
        for (ItemType mat : Material.values())
        {
            this.bukkitnames.put(mat.getName(), mat);
        }
        TreeMap<String, TreeMap<Short, List<String>>> readItems = this.readItems();
        for (String item : readItems.keySet())
        {
            for (short data : readItems.get(item).keySet())
            {
                this.registerItem(item, data, readItems.get(item).get(data));
            }
        }

    }

    /**
     * Registers an ItemStack for the matcher with a list of names
     *
     * @param materialName the Item
     * @param names the corresponding names
     */
    private void registerItem(String materialName, short data, List<String> names)
    {
        if (names.isEmpty())
        {
            return;
        }
        try
        {
            ItemType material = Material.valueOf(materialName);
            Map<Short, String> dataMap = this.itemnames.get(material);
            if (dataMap == null)
            {
                dataMap = new HashMap<>();
                this.itemnames.put(material, dataMap);
            }
            dataMap.put(data, names.get(0));
            ItemStack item;
            if (data == 0)
            {
                item = this.bukkitnames.get(material.getName());
            }
            else
            {
                item = builder.itemType(material).build();//new ImmutableItemStack(material, data);
            }
            for (String name : names)
            {
                this.items.put(name.toLowerCase(Locale.ENGLISH), item);
            }
        }
        catch (IllegalArgumentException ex)
        {
            CubeEngine.getLog().warn("Unknown Material: {}", materialName);
        }
    }

    /**
     * Loads in the file with the saved item-names.
     *
     * @param map the map to read into
     * @param input the input to read
     * @param update whether to update
     * @return whether it was updated
     */
    private boolean readItems(TreeMap<String, TreeMap<Short, List<String>>> map, List<String> input, boolean update)
    {
        boolean updated = false;
        TreeMap<Short, List<String>> readData = new TreeMap<>();
        ArrayList<String> names = new ArrayList<>();
        String currentItemName = "";
        short currentData = 0;
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.contains(":"))
            {
                String name = line.substring(0, line.indexOf(":"));
                short data = Short.parseShort(line.substring(line.indexOf(":") + 1));
                if (!update)
                {
                    if (currentData != data)
                    {
                        names = new ArrayList<>(); // New DATA Create new nameList
                        readData.put(data, names);
                        currentData = data;
                    }
                    if (!currentItemName.equals(name))
                    {
                        readData = new TreeMap<>(); // New ID Create new ID & DATA
                        names = new ArrayList<>(); // New DATA Create new nameList
                        readData.put(data, names);
                        map.put(name, readData);
                        currentData = data;
                        currentItemName = name;
                    }
                }
                else
                {
                    if (currentData != data)
                    {
                        names = new ArrayList<>(); // New DATA Create new nameList
                        currentData = data;
                    }
                    if (!currentItemName.equals(name))
                    {
                        readData = new TreeMap<>(); // New ID Create new DataValContainer
                        names = new ArrayList<>(); // New DATA Create new nameList
                        currentData = data;
                        currentItemName = name;
                    }
                    if (map.get(name) == null || map.get(name).isEmpty()) // Unknown ID -> Create new ID & DATA
                    {
                        readData.put(data, names);
                        map.put(name, readData);
                        updated = true;
                    }
                    else
                    {
                        if (map.get(name).get(data) == null || map.get(name).get(data).isEmpty()) // Known ID unknown DATA -> Create new DATA
                        {
                            map.get(name).put(data, names);
                        }
                    }
                }
            }
            else
            {
                names.add(line);
            }
        }
        return updated;
    }

    /**
     * Loads in the file with the saved item-names.
     */
    private TreeMap<String, TreeMap<Short, List<String>>> readItems()
    {
        try
        {
            Path file = CubeEngine.getFileManager().getDataPath().resolve(CoreResource.ITEMS.getTarget());
            List<String> input = FileUtil.readStringList(file);

            TreeMap<String, TreeMap<Short, List<String>>> readItems = new TreeMap<>();
            this.readItems(readItems, input, false);

            try (InputStream is = CubeEngine.getFileManager().getSourceOf(file))
            {
                List<String> jarinput = FileUtil.readStringList(is);
                if (jarinput != null && this.readItems(readItems, jarinput, true))
                {
                    CubeEngine.getLog().info("Updated items.txt");
                    StringBuilder sb = new StringBuilder();
                    for (String itemName : readItems.keySet())
                    {
                        for (short data : readItems.get(itemName).keySet())
                        {
                            sb.append(itemName).append(":").append(data).append("\n");
                            List<String> list = readItems.get(itemName).get(data);
                            for (String itemname : list)
                            {
                                sb.append("  ").append(itemname).append("\n");
                            }
                        }
                    }
                    FileUtil.saveFile(sb.toString(), file);
                }
            }
            return readItems;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("items.txt is corrupted!", ex);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading items.txt", ex);
        }
    }

    private ItemType matchWithLevenshteinDistance(String s, Map<String, ItemType> map)
    {
        String t_key = Match.string().matchString(s, map.keySet());
        if (t_key != null)
        {
            return map.get(t_key);
        }
        return null;
    }

    private HashMap<ItemStack, Double> allMatchesWithLevenshteinDistance(String s, Map<String, ItemType> map, int maxDistance, int minPercentage)
    {
        HashMap<ItemStack, Double> itemMap = new HashMap<>();
        TreeMap<String, Integer> itemNameList = Match.string().getMatches(s, map.keySet(), maxDistance, true);

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
        String s = name.toLowerCase(Locale.ENGLISH);
        ItemType type = this.items.get(s); //direct match
        if (type == null)
        {
            try
            { // id match
                ItemType mat = Material.getMaterial(Integer.parseInt(s));
                if (mat != null)
                {
                    return builder.itemType(mat).quantity(1).build();
                }
            }
            catch (NumberFormatException e)
            {
                try
                {
                    // id and data match
                    //Integer.parseInt(s.substring(0, s.indexOf(":"))
                    ItemStack item = builder.itemType(type).quantity(1).build();
                    item = materialDataMatcher.setData(item, name.substring(name.indexOf(":") + 1)); // Try to set data / returns null if couldn't
                    return item;
                }
                catch (Exception ignored)
                {}
            }
            if (s.contains(":"))
            {
                // name match with data
                String material = s.substring(0, s.indexOf(":"));
                String data = name.substring(name.indexOf(":") + 1);
                ItemStack item = materialDataMatcher.setData(builder.itemType(items.get(material)).build(), data); // Try to set data / returns null if couldn't
                if (item == null)
                {
                    //name was probably wrong check ld:
                    type = this.matchWithLevenshteinDistance(material, items);
                    item = materialDataMatcher.setData(builder.itemType(type).build(), data); // Try to set data / returns null if couldn't
                }
                if (item == null) // Contained ":" but could not find any matching item
                {
                    // Try to match sponge name
                    type = this.matchWithLevenshteinDistance(material, bukkitnames);
                    item = materialDataMatcher.setData(builder.itemType(type).build(), data);
                    return item;
                }
            }
            if (type == null)
            {
                // ld-match
                type = this.matchWithLevenshteinDistance(s, items);
                if (type == null)
                {
                    // Try to match sponge name
                    type = this.matchWithLevenshteinDistance(s, bukkitnames);
                    if (type == null)
                    {
                        return null;
                    }
                }
            }
        }
        return builder.itemType(type).quantity(1).build();;
    }

    /**
     * Tries to match a ItemStack-list for given name
     *
     * @param name the name
     * @return the found ItemStack-list
     */
    public TreeSet<Entry<ItemStack, Double>> itemStackList(String name)
    {
        if (name == null)
        {
            return null;
        }

        String s = name.toLowerCase(Locale.ENGLISH);
        HashMap<ItemStack, Double> itemMap = new HashMap<>();
        TreeSet<Entry<ItemStack, Double>> itemSet = new TreeSet<>(new ItemStackComparator());

        try
        { // id match
            ItemType mat = Material.getMaterial(Integer.parseInt(s));
            if (mat != null)
            {
                itemMap.put(builder.itemType(mat).quantity(1).build(), 0d);
                itemSet.addAll(itemMap.entrySet());
                return itemSet;
            }
        }
        catch (NumberFormatException e)
        {
            try
            { // id and data match
                //int typeId = Integer.parseInt(s.substring(0, s.indexOf(":")));
                ItemStack item = builder.itemType(type).quantity(1).build();
                item = materialDataMatcher.setData(item, name.substring(name.indexOf(":") + 1)); // Try to set data / returns null if couldn't
                itemMap.put(item, 0d);
                itemSet.addAll(itemMap.entrySet());
                return itemSet;
            }
            catch (Exception ignored)
            {}
        }

        String material = s;
        if (s.contains(":"))
        {
            material = s.substring(0, s.indexOf(":"));
        }

        // ld-match
        itemMap = this.allMatchesWithLevenshteinDistance(material, items, 5, 50);
        // Try to match sponge name
        itemMap.putAll(this.allMatchesWithLevenshteinDistance(material, bukkitnames, 5, 50));

        if (s.contains(":"))
        {
            // name match with data
            String data = name.substring(name.indexOf(":") + 1);

            for (Entry<ItemStack, Double> item : itemMap.entrySet())
            {
                if (materialDataMatcher.setData(item.getKey(), data) == null) // returns null if the item data could not be found
                {
                    itemMap.remove(item.getKey());
                }
            }
        }

        itemSet.addAll(itemMap.entrySet());

        return itemSet;
    }

    /**
     * Tries to match a Material for given name
     *
     * @param name the name
     * @return the material or null if not found
     */
    public ItemType material(String name)
    {
        String s = name.toLowerCase(Locale.ENGLISH);
        try
        {
            int matId = Integer.parseInt(s);
            return Material.getMaterial(matId);
        }
        catch (NumberFormatException ignored)
        {}
        ItemType material = Material.getMaterial(name);
        if (material != null) return material;
        ItemStack item = this.itemStack(s);
        if (item != null)
        {
            return item.getItem();
        }
        return null;
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
        return this.getNameForItem(item.getItem(), item.getDurability());
    }

    public String getNameForItem(ItemType mat, short data)
    {
        Map<Short, String> dataMap = this.itemnames.get(mat);
        if (dataMap == null)
        {
            CubeEngine.getLog().warn("Unknown Block-Data: {} DATA: {}", mat, data);
            return mat.getName();
        }
        String itemName = dataMap.get(data);
        if (itemName == null)
        {
            itemName = dataMap.get((short)0);
            if (itemName == null)
            {
                CubeEngine.getLog().warn("Unknown Block-Data: {} DATA: {}", mat, data);
                return mat.getName() + ":" + data;
            }
            itemName += ":" + data;
        }
        return itemName;
    }

    public String getNameForBlock(ItemType mat, Byte blockData)
    {
        Map<Short, String> dataMap = this.itemnames.get(mat);
        if (dataMap == null)
        {
            CubeEngine.getLog().warn("Unknown Block-Data: {} DATA: {}", mat, blockData);
            return null;
        }
        String itemName = dataMap.get((short)0);
        String data = Match.materialData().getDataNameFor(mat, blockData);
        if (data == null)
        {
            itemName += ":" + blockData;
        }
        else
        {
            itemName += data;
        }
        return itemName;
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
