package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This Matcher provides methods to match Material or Items.
 */
public class MaterialMatcher
{
    //TODO rename item ; is it possible?
    private THashMap<String, ItemStack> items;
    private THashMap<ItemStack, String> itemnames;
    private TIntObjectHashMap<THashMap<String, Short>> datavalues;
    private static MaterialMatcher instance = null;
    private THashMap<String, ItemStack> bukkitnames;

    private MaterialMatcher()
    {
        this.items = new THashMap<String, ItemStack>();
        this.itemnames = new THashMap<ItemStack, String>();
        this.bukkitnames = new THashMap<String, ItemStack>();
        TreeMap<Integer, TreeMap<Short, List<String>>> readItems = this.readItems();
        this.readDataValues();
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
     * Returns an instance of the matcher
     *
     * @return the singleton instance of the material matcher
     */
    public static MaterialMatcher get()
    {
        if (instance == null)
        {
            instance = new MaterialMatcher();
        }
        return instance;
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
        this.itemnames.put(new ItemStack(item.getType(), 1, item.getDurability()), names.get(0));
        for (String s : names)
        {
            this.items.put(s.toLowerCase(Locale.ENGLISH), item);
        }
    }

    /**
     * Tries to match a ItemStack for given name
     *
     * @param name the name
     * @return the found ItemStack
     */
    public ItemStack matchItemStack(String name)
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
                    item = this.setData(item, name.substring(name.indexOf(":") + 1)); // Try to set data / returns null if couldn't
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
                item = this.setData(this.items.get(material), data); // Try to set data / returns null if couldn't
                if (item == null)
                {
                    //name was probably wrong check ld:
                    item = matchWithLevenshteinDistance(material, items);
                    item = this.setData(item, data); // Try to set data / returns null if couldn't
                }
                if (item == null) // Contained ":" but could not find any matching item
                {
                    // Try to match bukkit name
                    item = this.matchWithLevenshteinDistance(s, bukkitnames);
                    item = this.setData(item, data);
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

    /**
     * Matches a DyeColor
     *
     * @param data the data
     * @return the dye color
     */
    public DyeColor matchColorData(String data)
    {
        Short dataVal = this.datavalues.get(35).get(StringUtils.matchString(data, this.datavalues.get(35).keySet()));
        if (dataVal == null)
        {
            return null;
        }
        return DyeColor.getByData(dataVal.byteValue());
    }

    /**
     * Sets the data for an ItemStack
     *
     * @param item the item
     * @param rawData the data
     * @return the modified clone of the item
     */
    private ItemStack setData(ItemStack item, String rawData)
    {
        String data = rawData.toLowerCase(Locale.ENGLISH);
        if (item == null)
        {
            return null;
        }
        item = item.clone();
        try
        { // try dataValue as Number
            item.setDurability(Short.parseShort(data));
            return item;
        }
        catch (NumberFormatException e)
        { // check for special cases
            switch (item.getType())
            {
                case WOOD:
                case LOG:
                case LEAVES:
                case WOOD_STEP:
                case WOOD_DOUBLE_STEP:
                case WOOL:
                case INK_SACK:
                case DOUBLE_STEP:
                case STEP:
                case SANDSTONE:
                case BRICK:
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                case POTION:
                    String foundData = StringUtils.matchString(data, this.datavalues.get(item.getTypeId()).keySet());
                    if (foundData != null)
                    {
                        item.setDurability(this.datavalues.get(item.getType().getId()).get(foundData));
                    }
                    return item;
                case MONSTER_EGG:
                    EntityType foundEggData = EntityMatcher.get().matchSpawnEggMobs(data);
                    if (foundEggData != null)
                    {
                        item.setDurability(foundEggData.getBukkitType().getTypeId());
                    }
                    return item;
                case SKULL_ITEM:
                    item.setDurability((short)3);
                    SkullMeta meta = ((SkullMeta)item.getItemMeta());
                    meta.setOwner(rawData);
                    item.setItemMeta(meta);
                    return item;
                default:
                    return null;
            }
        }
    }

    private ItemStack matchWithLevenshteinDistance(String s, Map<String, ItemStack> map)
    {
        String t_key = StringUtils.matchString(s, map.keySet(), false);
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
    public Material matchMaterial(String name)
    {
        String s = name.toLowerCase(Locale.ENGLISH);
        try
        {
            int matId = Integer.parseInt(s);
            return Material.getMaterial(matId);
        }
        catch (NumberFormatException ignored)
        {}
        ItemStack item = this.matchItemStack(s);
        if (item != null)
        {
            return item.getType();
        }
        return null;
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
     * Loads in the file with the saved item-datavalues
     */
    private void readDataValues()
    {
        this.datavalues = new TIntObjectHashMap<THashMap<String, Short>>();
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.DATAVALUES.getTarget());
            List<String> input = FileUtil.readStringList(file);
            THashMap<String, Short> data = new THashMap<String, Short>();
            for (String line : input)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.endsWith(":"))
                {
                    for (String key : StringUtils.explode(",", line.substring(0, line.length() - 1)))
                    {
                        data = new THashMap<String, Short>();
                        this.datavalues.put(Integer.parseInt(key), data);
                    }
                }
                else
                {
                    if (line.contains(":"))
                    {
                        for (String key : StringUtils.explode(",", line.substring(0, line.indexOf(":"))))
                        {
                            data.put(key, Short.parseShort(line.substring(line.indexOf(":") + 1).trim()));
                        }
                    }
                }
            }
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("datavalues.txt is corrupted!", ex);
        }
    }

    /**
     * This enum contains all repairable items
     */
    public enum RepairableMaterials
    {
        IRON_SPADE,
        IRON_PICKAXE,
        IRON_AXE,
        IRON_SWORD,
        WOOD_SPADE,
        WOOD_PICKAXE,
        WOOD_AXE,
        WOOD_SWORD,
        STONE_SPADE,
        STONE_PICKAXE,
        STONE_AXE,
        STONE_SWORD,
        DIAMOND_SPADE,
        DIAMOND_PICKAXE,
        DIAMOND_AXE,
        DIAMOND_SWORD,
        GOLD_SPADE,
        GOLD_PICKAXE,
        GOLD_AXE,
        GOLD_SWORD,
        WOOD_HOE,
        STONE_HOE,
        IRON_HOE,
        DIAMOND_HOE,
        GOLD_HOE,
        LEATHER_HELMET,
        LEATHER_CHESTPLATE,
        LEATHER_LEGGINGS,
        LEATHER_BOOTS,
        CHAINMAIL_HELMET,
        CHAINMAIL_CHESTPLATE,
        CHAINMAIL_LEGGINGS,
        CHAINMAIL_BOOTS,
        IRON_HELMET,
        IRON_CHESTPLATE,
        IRON_LEGGINGS,
        IRON_BOOTS,
        DIAMOND_HELMET,
        DIAMOND_CHESTPLATE,
        DIAMOND_LEGGINGS,
        DIAMOND_BOOTS,
        GOLD_HELMET,
        GOLD_CHESTPLATE,
        GOLD_LEGGINGS,
        GOLD_BOOTS,
        FLINT_AND_STEEL,
        BOW,
        FISHING_ROD,
        SHEARS;
        private static final Set<Material> mats = Collections.synchronizedSet(EnumSet.noneOf(Material.class));

        static
        {
            for (RepairableMaterials rMats : values())
            {
                mats.add(Material.matchMaterial(rMats.name()));
            }

        }

        /**
         * Returns whether the given ItemStack is repairable
         */
        public static boolean isRepairable(ItemStack item)
        {
            if (item == null)
            {
                return false;
            }
            return mats.contains(item.getType());
        }
    }

    /**
     * Returns whether the given ItemStack is repairable
     */
    public boolean isRepairable(ItemStack item)
    {
        return RepairableMaterials.isRepairable(item);
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
        // TODO find a better solution
        return this.itemnames.get(new ItemStack(item.getType(), 1, item.getDurability()));
    }
}
