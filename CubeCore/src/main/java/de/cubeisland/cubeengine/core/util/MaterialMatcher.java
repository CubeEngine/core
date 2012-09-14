package de.cubeisland.cubeengine.core.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Anselm Brehme
 */
public class MaterialMatcher
{
    public static ItemStack matchItemStack(String s)
    {
        if (s.contains(":"))
        {
            return matchItemStack(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":")+1));
        }
        return null; // TODO
    }
    
    public static ItemStack matchItemStack(String s, String dataValue)
    {
        return null; // TODO
    }
    
    public static Material matchMaterial(String s)
    {
        return null; // TODO
    }
}
