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

    public ItemStack matchItemStack(String s)
    {
        ItemStack item = this.items.get(s.toLowerCase(Locale.ENGLISH));
        if (item == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            for (String key : this.items.keySet())
            {
                if (StringUtils.getLevenshteinDistance(s.toLowerCase(Locale.ENGLISH), key) <= 2)
                {
                    item = this.items.get(key);
                }
            }
        }
        return item;
    }

    public Material matchMaterial(String s)
    {
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
                    data = Short.parseShort(line.substring(line.indexOf(":")+1));
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
