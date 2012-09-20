package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import org.bukkit.enchantments.Enchantment;

/**
 *
 * @author Anselm Brehme
 */
public class EnchantMatcher
{
    private THashMap<String, Enchantment> enchantments;
    private static EnchantMatcher instance = null;

    private EnchantMatcher()
    {
        this.enchantments = new THashMap<String, Enchantment>();

        TIntObjectHashMap<List<String>> enchs = this.readEnchantments();
        for (int id : enchs.keys())
        {
            this.registerEnchantment(Enchantment.getById(id), enchs.get(id));
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

    private TIntObjectHashMap<List<String>> readEnchantments()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget());
            TIntObjectHashMap<List<String>> enchs = new TIntObjectHashMap<List<String>>();
            FileUtil.parseStringList(file, enchs, false);
            if (FileUtil.parseStringList(CubeEngine.getFileManager().getSourceOf(file), enchs, true))
            {
                CubeEngine.getLogger().log(Level.FINER, "Updated enchantments.txt");
                StringBuilder sb = new StringBuilder();
                for (int key : enchs.keys())
                {
                    sb.append(key).append(":").append("\n");
                    List<String> entitynames = enchs.get(key);
                    for (String entityname : entitynames)
                    {
                        sb.append("    ").append(entityname).append("\n");
                    }
                }
                FileUtil.saveFile(sb.toString(), file);
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
