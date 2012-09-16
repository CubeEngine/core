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
                    else if (data.equals("pine") || data.equals("spruce"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("birch"))
                    {
                        item.setDurability((short)2);
                    }
                    else if (data.equals("jungle"))
                    {
                        item.setDurability((short)3);
                    }
                    return item;
                case WOOL:
                    if (data.equals("white"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("orange"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("magenta"))
                    {
                        item.setDurability((short)2);
                    }
                    else if (data.equals("lightblue") || data.equals("lblue"))
                    {
                        item.setDurability((short)3);
                    }
                    else if (data.equals("yellow"))
                    {
                        item.setDurability((short)4);
                    }
                    else if (data.equals("lime") || data.equals("lgreen"))
                    {
                        item.setDurability((short)5);
                    }
                    else if (data.equals("pink"))
                    {
                        item.setDurability((short)6);
                    }
                    else if (data.equals("gray"))
                    {
                        item.setDurability((short)7);
                    }
                    else if (data.equals("lightgray") || data.equals("lgray"))
                    {
                        item.setDurability((short)8);
                    }
                    else if (data.equals("cyan"))
                    {
                        item.setDurability((short)9);
                    }
                    else if (data.equals("purple"))
                    {
                        item.setDurability((short)10);
                    }
                    else if (data.equals("blue"))
                    {
                        item.setDurability((short)11);
                    }
                    else if (data.equals("brown"))
                    {
                        item.setDurability((short)12);
                    }
                    else if (data.equals("green"))
                    {
                        item.setDurability((short)13);
                    }
                    else if (data.equals("red"))
                    {
                        item.setDurability((short)14);
                    }
                    else if (data.equals("black"))
                    {
                        item.setDurability((short)15);
                    }
                    return item;
                case INK_SACK:
                    if (data.equals("white"))
                    {
                        item.setDurability((short)15);
                    }
                    else if (data.equals("orange"))
                    {
                        item.setDurability((short)14);
                    }
                    else if (data.equals("magenta"))
                    {
                        item.setDurability((short)13);
                    }
                    else if (data.equals("lightblue") || data.equals("lblue"))
                    {
                        item.setDurability((short)12);
                    }
                    else if (data.equals("yellow"))
                    {
                        item.setDurability((short)11);
                    }
                    else if (data.equals("lime") || data.equals("lgreen"))
                    {
                        item.setDurability((short)10);
                    }
                    else if (data.equals("pink"))
                    {
                        item.setDurability((short)9);
                    }
                    else if (data.equals("gray"))
                    {
                        item.setDurability((short)8);
                    }
                    else if (data.equals("lightgray") || data.equals("lgray"))
                    {
                        item.setDurability((short)7);
                    }
                    else if (data.equals("cyan"))
                    {
                        item.setDurability((short)6);
                    }
                    else if (data.equals("purple"))
                    {
                        item.setDurability((short)5);
                    }
                    else if (data.equals("blue"))
                    {
                        item.setDurability((short)4);
                    }
                    else if (data.equals("brown"))
                    {
                        item.setDurability((short)3);
                    }
                    else if (data.equals("green"))
                    {
                        item.setDurability((short)2);
                    }
                    else if (data.equals("red"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("black"))
                    {
                        item.setDurability((short)0);
                    }
                    return item;
                case DOUBLE_STEP:
                case STEP:
                    if (data.equals("clean") || data.equals("stone"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("sandstone"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("wood"))
                    {
                        item.setDurability((short)2);
                    }
                    else if (data.equals("cobble"))
                    {
                        item.setDurability((short)3);
                    }
                    else if (data.equals("brick"))
                    {
                        item.setDurability((short)4);
                    }
                    else if (data.equals("stonebrick"))
                    {
                        item.setDurability((short)5);
                    }
                    return item;
                case SANDSTONE:
                    if (data.equals("normal"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("chiseled") || data.equals("creeper"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("smooth") || data.equals("clean"))
                    {
                        item.setDurability((short)2);
                    }
                    return item;
                case BRICK:
                    if (data.equals("normal"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("mossy"))
                    {
                        item.setDurability((short)1);
                    }
                    else if (data.equals("cracked") || data.equals("old"))
                    {
                        item.setDurability((short)2);
                    }
                    else if (data.equals("chiseled") || data.equals("round"))
                    {
                        item.setDurability((short)3);
                    }
                    return item;
                case HUGE_MUSHROOM_1:
                case HUGE_MUSHROOM_2:
                    if (data.equals("pores"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("cap"))
                    {
                        item.setDurability((short)5);
                    }
                    if (data.equals("stem"))
                    {
                        item.setDurability((short)10);
                    }
                    else if (data.equals("allstem"))
                    {
                        item.setDurability((short)14);
                    }
                    else if (data.equals("allcap"))
                    {
                        item.setDurability((short)15);
                    }
                    return item;
                case POTION:
                    if (data.equals("water"))
                    {
                        item.setDurability((short)0);
                    }
                    else if (data.equals("awkward"))
                    {
                        item.setDurability((short)16);
                    }
                    else if (data.equals("thick"))
                    {
                        item.setDurability((short)32);
                    }
                    else if (data.equals("mundane"))
                    {
                        item.setDurability((short)64);
                    }
                    else
                    {
                        short dv = 0;
                        // Effect Type:
                        if (data.contains("regen"))
                        {
                            dv += 1;
                        }
                        else if (data.contains("speed"))
                        {
                            dv += 2;
                        }
                        else if (data.contains("fireresistance"))
                        {
                            dv += 3;
                        }
                        else if (data.contains("poison"))
                        {
                            dv += 4;
                        }
                        else if (data.contains("heal"))
                        {
                            dv += 5;
                        }
                        else if (data.contains("nightvision"))
                        {
                            dv += 6;
                        }
                        else if (data.contains("weak"))
                        {
                            dv += 8;
                        }
                        else if (data.contains("strength"))
                        {
                            dv += 9;
                        }
                        else if (data.contains("slow"))
                        {
                            dv += 10;
                        }
                        else if (data.contains("damage"))
                        {
                            dv += 12;
                        }
                        else if (data.contains("invisible") || data.contains("invisibility"))
                        {
                            dv += 14;
                        }
                        // Tier
                        if (data.contains("2"))
                        {
                            dv += 32;
                        }
                        // Duration
                        if (data.contains("long"))
                        {
                            dv += 32;
                        }
                        // splash
                        if (data.contains("splash"))
                        {
                            dv += 16384;
                        }
                        item.setDurability(dv);
                    }
                    return item;
                case MONSTER_EGG: // TODO Entity Matcher ??
                    if (data.equals("creeper"))
                    {
                        item.setDurability((short)50);
                    }
                    else if (data.equals("skeleton"))
                    {
                        item.setDurability((short)51);
                    }
                    else if (data.equals("spider"))
                    {
                        item.setDurability((short)52);
                    }
                    else if (data.equals("zombie"))
                    {
                        item.setDurability((short)54);
                    }
                    else if (data.equals("slime"))
                    {
                        item.setDurability((short)55);
                    }
                    else if (data.equals("ghast"))
                    {
                        item.setDurability((short)56);
                    }
                    else if (data.equals("zombiepigman"))
                    {
                        item.setDurability((short)57);
                    }
                    else if (data.equals("enderman"))
                    {
                        item.setDurability((short)58);
                    }
                    else if (data.equals("cavespider"))
                    {
                        item.setDurability((short)59);
                    }
                    else if (data.equals("silverfish"))
                    {
                        item.setDurability((short)60);
                    }
                    else if (data.equals("blaze"))
                    {
                        item.setDurability((short)61);
                    }
                    else if (data.equals("magmacube"))
                    {
                        item.setDurability((short)62);
                    }
                    else if (data.equals("pig"))
                    {
                        item.setDurability((short)90);
                    }
                    else if (data.equals("sheep"))
                    {
                        item.setDurability((short)91);
                    }
                    else if (data.equals("cow"))
                    {
                        item.setDurability((short)92);
                    }
                    else if (data.equals("chicken"))
                    {
                        item.setDurability((short)93);
                    }
                    else if (data.equals("squid"))
                    {
                        item.setDurability((short)94);
                    }
                    else if (data.equals("wolf"))
                    {
                        item.setDurability((short)95);
                    }
                    else if (data.equals("mooshroom"))
                    {
                        item.setDurability((short)96);
                    }
                    else if (data.equals("ocelot"))
                    {
                        item.setDurability((short)98);
                    }
                    else if (data.equals("villager"))
                    {
                        item.setDurability((short)120);
                    }
                    return item;
                case JUKEBOX: // TODO check if even possible
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
