package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.AliasMapFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import gnu.trove.map.hash.THashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.enchantments.Enchantment;

public class EnchantMatcher
{
    private THashMap<String, Enchantment> enchantments;
    private THashMap<Enchantment, String> enchantmentName;
    private static EnchantMatcher instance = null;

    private EnchantMatcher()
    {
        this.enchantments = new THashMap<String, Enchantment>();
        this.enchantmentName = new THashMap<Enchantment, String>();

        TreeMap<Integer, List<String>> enchs = this.readEnchantments();
        for (int id : enchs.keySet())
        {
            this.registerEnchantment(Enchantment.getById(id), enchs.get(id));
            this.enchantmentName.put(Enchantment.getById(id), enchs.get(id).get(0));
        }
    }

    public static EnchantMatcher get()
    {
        if (instance == null)
        {
            instance = new EnchantMatcher();
        }
        return instance;
    }
    
    public String getNameFor(Enchantment ench)
    {
        return this.enchantmentName.get(ench);
    }

    public final void registerEnchantment(Enchantment ench, List<String> names)
    {
        for (String s : names)
        {
            this.enchantments.put(s.toLowerCase(Locale.ENGLISH), ench);
        }
    }

    public Enchantment matchEnchantment(String s)
    {
        Enchantment ench = this.enchantments.get(s.toLowerCase(Locale.ENGLISH));
        try
        {
            int enchId = Integer.parseInt(s);
            return Enchantment.getById(enchId);
        }
        catch (NumberFormatException e)
        {
        }
        if (ench == null)
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
        }
        return ench;
    }

    private TreeMap<Integer, List<String>> readEnchantments()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget());
            TreeMap<Integer, List<String>> enchs = new TreeMap<Integer, List<String>>();
            AliasMapFormat.parseStringList(file, enchs, false);
            if (AliasMapFormat.parseStringList(CubeEngine.getFileManager().getSourceOf(file), enchs, true))
            {
                CubeEngine.getLogger().log(Level.FINER, "Updated enchantments.txt");
                AliasMapFormat.parseAndSaveStringListMap(enchs, file);
            }
            return enchs;
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
}
