package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
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

        TIntObjectHashMap<String[]> enchs = this.readEnchantments();
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

    public final void registerEnchantment(Enchantment ench, String... names)
    {
        for (String s : names)
        {
            this.enchantments.put(s.toLowerCase(Locale.ENGLISH), ench);
        }
    }

    public Enchantment matchEnchantment(String s)
    {
        Enchantment ench = this.enchantments.get(s.toLowerCase(Locale.ENGLISH));
        if (ench == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            for (String key : this.enchantments.keySet())
            {
                if (StringUtils.getLevenshteinDistance(s.toLowerCase(Locale.ENGLISH), key) <= 2)
                {
                    ench = this.enchantments.get(key);
                }
            }
        }
        return ench;
    }

    private TIntObjectHashMap<String[]> readEnchantments()
    {
        try
        {
            Scanner scanner = new Scanner(new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENCHANTMENTS.getTarget()));
            TIntObjectHashMap<String[]> enchs = new TIntObjectHashMap<String[]>();
            int id = -1;
            ArrayList<String> names = new ArrayList<String>();
            while (scanner.hasNext())
            {


                if (scanner.hasNext("\\d+:"))
                {
                    if (id != -1)
                    {
                        enchs.put(id, (String[])names.toArray());
                        names.clear();
                    }
                    id = Integer.parseInt(scanner.next("(\\d+):"));
                }
                else if (scanner.hasNext("\t+\\w+"))
                {
                    names.add(scanner.next("\t+(\\w+)"));
                }
            }
            return enchs;
        }
        catch (Exception ex)
        {
            throw new IllegalStateException("Error while reading enchantments.txt");
        }
    }
}
