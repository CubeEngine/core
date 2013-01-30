package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.AliasMapFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

/**
 * This Matcher provides methods to match Enchantments.
 */
public class EnchantMatcher
{
    private THashMap<String, Enchantment> enchantments;
    private THashMap<String, Enchantment> bukkitnames;
    private THashMap<Enchantment, String> enchantmentName;

    EnchantMatcher()
    {
        this.enchantments = new THashMap<String, Enchantment>();
        this.enchantmentName = new THashMap<Enchantment, String>();
        this.bukkitnames = new THashMap<String, Enchantment>();

        TreeMap<Integer, List<String>> enchs = this.readEnchantments();
        for (int id : enchs.keySet())
        {
            this.registerEnchantment(Enchantment.getById(id), enchs.get(id));
            this.enchantmentName.put(Enchantment.getById(id), enchs.get(id).get(0));
        }
        for (Enchantment enchantment : Enchantment.values())
        {
            this.bukkitnames.put(enchantment.getName(), enchantment);
        }
    }

    /**
     * Registers an enchantment for the matcher with a list of names
     *
     * @param ench  the enchantment
     * @param names the corresponding names
     */
    private void registerEnchantment(Enchantment ench, List<String> names)
    {
        for (String s : names)
        {
            this.enchantments.put(s.toLowerCase(Locale.ENGLISH), ench);
        }
    }

    /**
     * Loads in the file with the saved enchantment-names
     *
     * @return the loaded enchantments with corresponding names
     */
    private TreeMap<Integer, List<String>> readEnchantments()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget());
            TreeMap<Integer, List<String>> enchantments = new TreeMap<Integer, List<String>>();
            AliasMapFormat.parseStringList(file, enchantments, false);
            if (AliasMapFormat.parseStringList(CubeEngine.getFileManager().getSourceOf(file), enchantments, true))
            {
                CubeEngine.getLogger().log(LogLevel.NOTICE, "Updated enchantments.txt");
                AliasMapFormat.parseAndSaveStringListMap(enchantments, file);
            }
            return enchantments;
        }
        catch (NumberFormatException ex)
        {
            throw new IllegalStateException("enchantments.txt is corrupted!", ex);
        }
        catch (IOException ex)
        {
            throw new IllegalStateException("Error while reading enchantments.txt", ex);
        }
    }

    /**
     * Gets the name for the given Enchantment
     *
     * @param enchant the enchantment to get the name for
     * @return the name corresponding to the enchantment
     */
    public String nameFor(Enchantment enchant)
    {
        return this.enchantmentName.get(enchant);
    }

    /**
     * Tries to match an Enchantment for given string
     *
     * @param s the string to match
     * @return the found Enchantment
     */
    public Enchantment enchantment(String s)
    {
        Enchantment enchantment = this.enchantments.get(s.toLowerCase(Locale.ENGLISH));
        try
        {
            int enchId = Integer.parseInt(s);
            return Enchantment.getById(enchId);
        }
        catch (NumberFormatException ignored)
        {}
        if (enchantment == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            String t_key = StringUtils.matchString(s, this.enchantments.keySet());
            if (t_key != null)
            {
                return this.enchantments.get(t_key);
            }
            else
            {
                t_key = StringUtils.matchString(s, this.bukkitnames.keySet());
                if (t_key != null)
                {
                    return this.bukkitnames.get(t_key);
                }
            }
        }
        return enchantment;
    }
}
