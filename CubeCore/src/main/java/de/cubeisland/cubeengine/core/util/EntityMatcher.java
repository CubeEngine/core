package de.cubeisland.cubeengine.core.util;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.bukkit.entity.EntityType;

/**
 *
 * @author Anselm Brehme
 */
public class EntityMatcher
{
    private THashMap<String, EntityType> entities;
    private static EntityMatcher instance = null;

    public EntityMatcher()
    {
        this.entities = new THashMap<String, EntityType>();
        TIntObjectHashMap<List<String>> entityList = this.readEntities();
        for (int id : entityList.keys())
        {
            this.registerEntity(EntityType.fromId(id), entityList.get(id));
        }
    }

    public static EntityMatcher get()
    {
        if (instance == null)
        {
            instance = new EntityMatcher();
        }
        return instance;
    }

    public EntityType matchEntity(String s)
    {
        EntityType ench = this.entities.get(s.toLowerCase(Locale.ENGLISH));
        try
        {
            int entityId = Integer.parseInt(s);
            return EntityType.fromId(entityId);
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
            String t_key = null;
            for (String key : this.entities.keySet())
            {
                int ld = StringUtils.getLevenshteinDistance(s.toLowerCase(Locale.ENGLISH), key);
                if (ld == 1)
                {
                    return this.entities.get(key);
                }
                if (ld <= 2)
                {
                    t_key = key;
                }
            }
            if (t_key != null)
            {
                return this.entities.get(t_key);
            }
        }
        return ench;
    }

    public EntityType matchMob(String s)
    {
        EntityType type = this.matchEntity(s);
        switch (type)
        {
            case BLAZE:
            case CAVE_SPIDER:
            case CHICKEN:
            case COW:
            case CREEPER:
            case ENDERMAN:
            case GHAST:
            case GIANT:
            case IRON_GOLEM:
            case MAGMA_CUBE:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case PIG_ZOMBIE:
            case SHEEP:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SNOWMAN:
            case SPIDER:
            case SQUID:
            case VILLAGER:
            case WOLF:
            case ZOMBIE:
                return type;
            default:
                return null;
        }
    }

    public EntityType matchMonster(String s)
    {
        EntityType type = this.matchEntity(s);
        switch (type)
        {
            case BLAZE:
            case CAVE_SPIDER:
            case CREEPER:
            case ENDERMAN:
            case GHAST:
            case GIANT:
            case MAGMA_CUBE:
            case PIG_ZOMBIE:
            case SILVERFISH:
            case SKELETON:
            case SLIME:
            case SPIDER:
            case SQUID:
            case ZOMBIE:
                return type;
            default:
                return null;
        }
    }

    public EntityType matchFriendlyMob(String s)
    {
        EntityType type = this.matchEntity(s);
        switch (type)
        {
            case CHICKEN:
            case COW:
            case IRON_GOLEM:
            case MUSHROOM_COW:
            case OCELOT:
            case PIG:
            case SHEEP:
            case SNOWMAN:
            case SQUID:
            case VILLAGER:
            case WOLF:
                return type;
            default:
                return null;
        }
    }

    private void registerEntity(EntityType id, List<String> names)
    {
        for (String s : names)
        {
            this.entities.put(s.toLowerCase(Locale.ENGLISH), id);
        }
    }

    private TIntObjectHashMap<List<String>> readEntities()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENTITIES.getTarget())));
            TIntObjectHashMap<List<String>> entityList = new TIntObjectHashMap<List<String>>();
            String line;
            ArrayList<String> names = new ArrayList<String>();
            while ((line = reader.readLine()) != null)
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
                    entityList.put(id, names);
                }
                else
                {
                    names.add(line);
                }
            }
            return entityList;
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