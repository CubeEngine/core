package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileUtil;
import de.cubeisland.cubeengine.core.filesystem.Resource;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    private boolean readEnchantments(TIntObjectHashMap<List<String>> map, List<String> input, boolean update)
    {
        boolean updated = false;
        ArrayList<String> names = new ArrayList<String>();
        for (String line : input)
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            if (line.endsWith(":"))
            {
                int id = Integer.parseInt(line.substring(0, line.length() - 1));
                names = new ArrayList<String>();
                if (!update)
                {
                    map.put(id, names);
                }
                else if (map.get(id) == null || map.get(id).isEmpty())
                {
                    map.put(id, names);
                    updated = true;
                }
            }
            else
            {
                names.add(line);
            }
        }
        return updated;
    }

    private TIntObjectHashMap<List<String>> readEnchantments()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget());
            List<String> input = FileUtil.getFileAsStringList(file);

            TIntObjectHashMap<List<String>> enchs = new TIntObjectHashMap<List<String>>();
            this.readEnchantments(enchs, input, false);
            Resource resource = CubeEngine.getFileManager().getSourceOf(file);
            String source = resource.getSource();
            if (!source.startsWith("/"))
            {
                source = "/" + source;
            }
            List<String> jarinput = FileUtil.getReaderAsStringList(new InputStreamReader(resource.getClass().getResourceAsStream(source)));
            if (this.readEnchantments(enchs, jarinput, true))
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
