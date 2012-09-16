package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import gnu.trove.map.hash.THashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class MaterialMatcher
{
    private THashMap<String, ItemStack> items;
    private static MaterialMatcher instance = null;

    private MaterialMatcher()
    {
        this.items = new THashMap<String, ItemStack>();
        THashMap<ItemStack, List<String>> readItems = this.readItems();
        for (ItemStack item : readItems.keySet())
        {
            this.registerItemStack(item, readItems.get(item));
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
                }
                catch (NumberFormatException ex)
                {
                }
            }
            if (s.contains(":"))
            { // name match with data
                String material = s.substring(0, s.indexOf(":"));
                String data = s.substring(s.indexOf("."));
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

    private ItemStack setData(ItemStack item, String data)
    {
        try
        { // try dataValue as Number
            item.setDurability(Short.parseShort(data));
        }
        catch (NullPointerException e)
        {
            return null;
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
                    if (data.equals("oak"))
                    {
                        item.setDurability((short)0);
                    }
                    if (data.equals("pine")||data.equals("spruce"))
                    {
                        item.setDurability((short)1);
                    }
                    if (data.equals("birch"))
                    {
                        item.setDurability((short)2);
                    }
                    if (data.equals("jungle"))
                    {
                        item.setDurability((short)3);
                    }
                    return item;
                case WOOL:
                    if (data.equals("white"))
                    {
                        item.setDurability((short)0);
                    }//TODO more ...
                    
                    return item;
                case INK_SACK:
                    // TODO colors
                    return item;
                case DOUBLE_STEP:
                case STEP:
                    //TODO stoneslabs
                    return item;
                case SANDSTONE:
                    //TODO sandstone
                    return item;
                case JUKEBOX: // TODO check if even possible
                    return item;
                case BRICK:
                    return item;
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                    return item;
                case POTION:
                    return item;
                case MOB_SPAWNER://TODO can do this probably not
                    return item;
                default:
                    return null;
            }

        }
        return null; //could not set data -> invalid item
    }

    private ItemStack matchWithLevenshteinDistance(String s)
    {
        if (s.length() < 4)
        {
            return null;
        }
        String t_key = null;
        for (String key : this.items.keySet())
        {
            int ld = StringUtils.getLevenshteinDistance(s, key);
            if (ld == 1)
            {
                return this.items.get(key);
            }
            if (ld <= 2)
            {
                t_key = key;
            }
        }
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

    private THashMap<ItemStack, List<String>> readItems()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ITEMS.getTarget())));
            THashMap<ItemStack, List<String>> readItems = new THashMap<ItemStack, List<String>>();
            String line;
            int id;
            short data;
            ArrayList<String> names = new ArrayList<String>();
            while ((line = reader.readLine()) != null)
            {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#"))
                {
                    continue;
                }
                if (line.contains(":"))
                {
                    id = Integer.parseInt(line.substring(0, line.indexOf(":")));
                    data = Short.parseShort(line.substring(line.indexOf(":") + 1));
                    names = new ArrayList<String>();
                    readItems.put(new ItemStack(id, 1, data), names);
                }
                else
                {
                    names.add(line);
                }
            }
            return readItems;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading items.txt", ex);
        }
    }
}
