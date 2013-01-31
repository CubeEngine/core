package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This Matcher provides methods to match Material or Items.
 */
public class MaterialMatcher
{
    //TODO rename item ; is it possible?
    private THashMap<String, ItemStack> items;
    private TIntObjectHashMap<TShortObjectHashMap<String>> itemnames;

    private THashMap<String, ItemStack> bukkitnames;

    private MaterialDataMatcher materialDataMatcher;


    private final Set<Material> repairableMaterials = Collections.synchronizedSet(
        EnumSet.of(
            Material.IRON_SPADE, Material.IRON_PICKAXE, Material.IRON_AXE, Material.IRON_SWORD, Material.IRON_HOE,
            Material.WOOD_SPADE, Material.WOOD_PICKAXE, Material.WOOD_AXE, Material.WOOD_SWORD, Material.WOOD_HOE,
            Material.STONE_SPADE, Material.STONE_PICKAXE, Material.STONE_AXE, Material.STONE_SWORD, Material.STONE_HOE,
            Material.DIAMOND_SPADE, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_HOE,
            Material.GOLD_SPADE, Material.GOLD_PICKAXE, Material.GOLD_AXE, Material.GOLD_SWORD, Material.GOLD_HOE,
            Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS,
            Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS,
            Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS,
            Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS,
            Material.GOLD_HELMET, Material.GOLD_CHESTPLATE, Material.GOLD_LEGGINGS, Material.GOLD_BOOTS,
            Material.FLINT_AND_STEEL,
            Material.BOW,
            Material.FISHING_ROD,
            Material.SHEARS));


    MaterialMatcher(MaterialDataMatcher materialDataMatcher)
    {
        this.materialDataMatcher = materialDataMatcher;
        this.items = new THashMap<String, ItemStack>();
        this.itemnames = new TIntObjectHashMap<TShortObjectHashMap<String>>();
        this.bukkitnames = new THashMap<String, ItemStack>();
        TreeMap<Integer, TreeMap<Short, List<String>>> readItems = this.readItems();
        for (Integer item : readItems.keySet())
        {
            for (short data : readItems.get(item).keySet())
            {
                this.registerItemStack(new ItemStack(item, 1, data), readItems.get(item).get(data));
            }
        }
        // Read Bukkit names
        for (Material mat : Material.values())
        {
            this.bukkitnames.put(mat.name(), new ItemStack(mat));
        }
    }

    /**
     * Registers an ItemStack for the matcher with a list of names
     *
     * @param item the Item
     * @param names the corresponding names
     */
    private void registerItemStack(ItemStack item, List<String> names)
    {
        if (names.isEmpty())
        {
            return;
        }
        TShortObjectHashMap<String> dataMap = this.itemnames.get(item.getTypeId());
        if (dataMap == null)
        {
            dataMap = new TShortObjectHashMap<String>();
            this.itemnames.put(item.getTypeId(),dataMap);
        }
        dataMap.put(item.getDurability(), names.get(0));
        for (String s : names)
        {
            this.items.put(s.toLowerCase(Locale.ENGLISH), item);
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
    private boolean readItems(TreeMap<Integer, TreeMap<Short, List<String>>> map, List<String> input, boolean update)
    {
        boolean updated = false;
        TreeMap<Short, List<String>> readData = new TreeMap<Short, List<String>>();
        ArrayList<String> names = new ArrayList<String>();
        int currentId = 0;
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
                int id = Integer.parseInt(line.substring(0, line.indexOf(":")));
                short data = Short.parseShort(line.substring(line.indexOf(":") + 1));
                if (!update)
                {
                    if (currentData != data)
                    {
                        names = new ArrayList<String>(); // New DATA Create new nameList
                        readData.put(data, names);
                        currentData = data;
                    }
                    if (currentId != id)
                    {
                        readData = new TreeMap<Short, List<String>>(); // New ID Create new ID & DATA
                        names = new ArrayList<String>(); // New DATA Create new nameList
                        readData.put(data, names);
                        map.put(id, readData);
                        currentData = data;
                        currentId = id;
                    }
                }
                else
                {
                    if (currentData != data)
                    {
                        names = new ArrayList<String>(); // New DATA Create new nameList
                        currentData = data;
                    }
                    if (currentId != id)
                    {
                        readData = new TreeMap<Short, List<String>>(); // New ID Create new DataValContainer
                        names = new ArrayList<String>(); // New DATA Create new nameList
                        currentData = data;
                        currentId = id;
                    }
                    if (map.get(id) == null || map.get(id).isEmpty()) // Unkown ID -> Create new ID & DATA
                    {

                        readData.put(data, names);
                        map.put(id, readData);

                        updated = true;
                    }
                    else
                    {
                        if (map.get(id).get(data) == null || map.get(id).get(data).isEmpty()) // Known ID unkown DATA -> Create new DATA
                        {
                            map.get(id).put(data, names);
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
    private TreeMap<Integer, TreeMap<Short, List<String>>> readItems()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ITEMS.getTarget());
            List<String> input = FileUtil.readStringList(file);

            TreeMap<Integer, TreeMap<Short, List<String>>> readItems = new TreeMap<Integer, TreeMap<Short, List<String>>>();
            this.readItems(readItems, input, false);

            List<String> jarinput = FileUtil.readStringList(CubeEngine.getFileManager().getSourceOf(file));
            if (jarinput != null && this.readItems(readItems, jarinput, true))
            {
                CubeEngine.getLogger().log(LogLevel.NOTICE, "Updated items.txt");
                StringBuilder sb = new StringBuilder();
                for (Integer item : readItems.keySet())
                {
                    for (short data : readItems.get(item).keySet())
                    {
                        sb.append(item).append(":").append(data).append("\n");
                        List<String> list = readItems.get(item).get(data);
                        for (String itemname : list)
                        {
                            sb.append("  ").append(itemname).append("\n");
                        }
                    }
                }
                FileUtil.saveFile(sb.toString(), file);
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
        ItemStack item = this.items.get(s);//direct match
        if (item == null)
        {
            try
            { // id match
                Material mat = Material.getMaterial(Integer.parseInt(s));
                if (mat != null)
                {
                    return new ItemStack(mat, 1);
                }
            }
            catch (NumberFormatException e)
            {
                try
                {
                    // id and data match
                    item = new ItemStack(Integer.parseInt(s.substring(0, s.indexOf(":"))), 1);
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
                item = materialDataMatcher.setData(this.items.get(material), data); // Try to set data / returns null if couldn't
                if (item == null)
                {
                    //name was probably wrong check ld:
                    item = matchWithLevenshteinDistance(material, items);
                    item = materialDataMatcher.setData(item, data); // Try to set data / returns null if couldn't
                }
                if (item == null) // Contained ":" but could not find any matching item
                {
                    // Try to match bukkit name
                    item = this.matchWithLevenshteinDistance(s, bukkitnames);
                    item = materialDataMatcher.setData(item, data);
                    return item;
                }
            }
            if (item == null)
            {
                // ld-match
                item = matchWithLevenshteinDistance(s, items);
                if (item == null)
                {
                    // Try to match bukkit name
                    return this.matchWithLevenshteinDistance(s, bukkitnames);
                }
            }
        }
        return item.clone();
    }

    private ItemStack matchWithLevenshteinDistance(String s, Map<String, ItemStack> map)
    {
        String t_key = Match.string().matchString(s, map.keySet(), false);
        if (t_key != null)
        {
            return map.get(t_key);
        }
        return null;
    }

    /**
     * Tries to match a Material for given name
     *
     * @param name the name
     * @return the material or null if not found
     */
    public Material material(String name)
    {
        String s = name.toLowerCase(Locale.ENGLISH);
        try
        {
            int matId = Integer.parseInt(s);
            return Material.getMaterial(matId);
        }
        catch (NumberFormatException ignored)
        {}
        ItemStack item = this.itemStack(s);
        if (item != null)
        {
            return item.getType();
        }
        return null;
    }

    /**
     * Returns whether the given ItemStack is repairable
     */
    public boolean repairable(ItemStack item)
    {
        return this.repairableMaterials.contains(item.getType());
    }

    /**
     * Returns the name for given ItemStack
     *
     * @param item the item
     * @return the name or null if none was found
     */
    public String getNameFor(ItemStack item)
    {
        if (item == null)
        {
            return null;
        }
        TShortObjectHashMap<String > dataMap = this.itemnames.get(item.getTypeId());
        if (dataMap == null)
        {
            CubeEngine.getLogger().warning("Unknown Item! ("+item.toString()+")");
            return null;
        }
        String itemName = dataMap.get(item.getDurability());
        if (itemName == null)
        {
            return dataMap.get((short) 0);
        }
        return itemName;
    }

    public String getNameFor(int id, short data) {
        TShortObjectHashMap<String > dataMap = this.itemnames.get(id);
        if (dataMap == null)
        {
            CubeEngine.getLogger().warning("Unknown Item! ID: "+id+ " DATA: "+ data);
            return null;
        }
        String itemName = dataMap.get(data);
        if (itemName == null)
        {
            return dataMap.get((short) 0);
        }
        return itemName;
    }
}
