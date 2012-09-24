package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class MaterialMatcher
{
    private THashMap<String, ItemStack> items;
    private TIntObjectHashMap<THashMap<String, Short>> datavalues;
    private static MaterialMatcher instance = null;

    private MaterialMatcher()
    {
        this.items = new THashMap<String, ItemStack>();
        TreeMap<Integer, TreeMap<Short, List<String>>> readItems = this.readItems();
        this.readDataValues();
        for (Integer item : readItems.keySet())
        {
            for (short data : readItems.get(item).keySet())
            {
                this.registerItemStack(new ItemStack(item, 1, data), readItems.get(item).get(data));
            }
        }
    }

    public static MaterialMatcher get()
    {
        if (instance == null)
        {
            instance = new MaterialMatcher();
        }
        return instance;
    }

    public final void registerItemStack(ItemStack item, List<String> names)
    {
        for (String s : names)
        {
            this.items.put(s.toLowerCase(Locale.ENGLISH), item);
        }
    }

    public ItemStack matchItemStack(String name)
    {
        String s = name.toLowerCase(Locale.ENGLISH);
        ItemStack item = this.items.get(s);//direct match
        if (item == null)
        {
            try
            { // id match
                int matId = Integer.parseInt(s);
                return new ItemStack(matId, 1);
            }
            catch (NumberFormatException e)
            {
                try
                { // id and data match
                    item = new ItemStack(Integer.parseInt(s.substring(0, s.indexOf(":"))), 1);
                    this.setData(item, s.substring(s.indexOf(":") + 1)); // Try to set data / returns null if couldn't
                    if (item != null)
                    {
                        return item;
                    }
                }
                catch (Exception ex)
                {
                }
            }
            if (s.contains(":"))
            { // name match with data
                String material = s.substring(0, s.indexOf(":"));
                String data = s.substring(s.indexOf(":") + 1);
                item = this.items.get(material);
                this.setData(item, data); // Try to set data / returns null if couldn't
                if (item == null)
                { //name was probably wrong check ld:
                    item = matchWithLevenshteinDistance(material);
                    this.setData(item, data); // Try to set data / returns null if couldn't
                }
                if (item == null)
                {
                    return null; // Contained ":" but could not find any matching item
                }
            }
            if (item == null)
            { // ld-match
                return matchWithLevenshteinDistance(s);
            }
        }
        return item;
    }

    public DyeColor matchColorData(String data)
    {
        short dataVal = this.datavalues.get(351).get(StringUtils.matchString(data, this.datavalues.get(351).keySet()));
        return DyeColor.getByData((byte)dataVal);
    }

    private ItemStack setData(ItemStack item, String data)
    {
        if (item == null)
        {
            return null;
        }
        try
        { // try dataValue as Number
            item.setDurability(Short.parseShort(data));
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
                default:
                    return item;
            }
        }
        return null; //could not set data -> invalid item
    }

    private ItemStack matchWithLevenshteinDistance(String s)
    {
        String t_key = StringUtils.matchString(s, this.items.keySet(), false);
        if (t_key != null)
        {
            return this.items.get(t_key);
        }
        return null;
    }

    public Material matchMaterial(String name)
    {
        String s = name.toLowerCase(Locale.ENGLISH);
        try
        {
            int matId = Integer.parseInt(s);
            return Material.getMaterial(matId);
        }
        catch (NumberFormatException e)
        {
        }
        ItemStack item = this.matchItemStack(s);
        if (item != null)
        {
            return item.getType();
        }
        return null;
    }

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
                    else if (map.get(id).get(data) == null || map.get(id).get(data).isEmpty()) // Known ID unkown DATA -> Create new DATA
                    {
                        map.get(id).put(data, names);
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
                CubeEngine.getLogger().log(Level.FINER, "Updated items.txt");
                StringBuilder sb = new StringBuilder();
                for (Integer item : readItems.keySet())
                {
                    for (short data : readItems.get(item).keySet())
                    {
                        sb.append(item).append(":").append(data).append("\n");
                        List<String> itemnames = readItems.get(item).get(data);
                        for (String itemname : itemnames)
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
                        this.datavalues.put(Integer.parseInt(key), data);
                    }
                }
                else if (line.contains(":"))
                {
                    for (String key : StringUtils.explode(",", line.substring(0, line.indexOf(":"))))
                    {
                        data.put(key, Short.parseShort(line.substring(line.indexOf(":") + 1).trim()));
                    }
                }
            }
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("datavalues.txt is corrupted!", ex);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading datavalues.txt", ex);
        }
    }
}