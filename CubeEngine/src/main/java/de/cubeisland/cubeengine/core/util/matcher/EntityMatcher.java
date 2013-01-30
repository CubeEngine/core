package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.AliasMapFormat;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * This Matcher provides methods to match Entities.
 */
public class EntityMatcher
{
    EntityMatcher()
    {
        TreeMap<Integer, List<String>> entityList = this.readEntities();
        for (int id : entityList.keySet())
        {
            try
            {
                EntityType.fromId((short)id).registerNames(entityList.get(id));
            }
            catch (NullPointerException e)
            {
                CubeEngine.getLogger().log(LogLevel.WARNING, "Unknown Entity ID: " + id + " " + entityList.get(id).get(0));
            }
        }
    }

    /**
     * Loads in the file with the saved entity-names
     *
     * @return the loaded entities with corresponding names
     */
    private TreeMap<Integer, List<String>> readEntities()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENTITIES.getTarget());
            TreeMap<Integer, List<String>> entityList = new TreeMap<Integer, List<String>>();
            AliasMapFormat.parseStringList(file, entityList, false);
            if (AliasMapFormat.parseStringList(CubeEngine.getFileManager().getSourceOf(file), entityList, true))
            {
                CubeEngine.getLogger().log(LogLevel.NOTICE, "Updated entities.txt");
                AliasMapFormat.parseAndSaveStringListMap(entityList, file);
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

    /**
     * Tries to match an EntityType for given string
     *
     * @param name the name to match
     * @return the found EntityType
     */
    public EntityType any(String name)
    {
        if (name == null)
        {
            return null;
        }
        Map<String, EntityType> entities = EntityType.getNameSets();
        String s = name.toLowerCase(Locale.ENGLISH);
        EntityType entity = entities.get(s);
        try
        {
            short entityId = Short.parseShort(s);
            return EntityType.fromId(entityId);
        }
        catch (NumberFormatException ignored)
        {}
        if (entity == null)
        {
            if (s.length() < 4)
            {
                return null;
            }
            String t_key = StringUtils.matchString(name, entities.keySet());
            if (t_key != null)
            {
                return entities.get(t_key);
            }
        }
        return entity;
    }

    /**
     * Tries to match an EntityType that is a Mob for given string
     *
     * @param s the string to match
     * @return the found Mob
     */
    public EntityType mob(String s)
    {
        EntityType type = this.any(s);
        if (type != null && type.isAlive())
        {
            return type;
        }
        return null;
    }

    /**
     * Tries to match an EntityType that is a Mob that can be spawned by spawneggs for given string
     *
     * @param s the string to match
     * @return the found Mob
     */
    public EntityType spawnEggMob(String s)
    {
        EntityType type = this.mob(s);
        if (type != null && type.canBeSpawnedBySpawnEgg())
        {
            return type;
        }
        return null;
    }

    /**
     * Tries to match an EntityType that is a Monster for given string
     *
     * @param s the string to match
     * @return the found Monster
     */
    public EntityType monster(String s)
    {
        EntityType type = this.any(s);
        if (type != null && type.isMonster())
        {
            return type;
        }
        return null;
    }

    /**
     * Tries to match an EntityType that is a friendly Mob for given string
     *
     * @param s the string to match
     * @return the found friendly Mob
     */
    public EntityType friendlyMob(String s)
    {
        EntityType type = this.any(s);
        if (type != null && type.isFriendly())
        {
            return type;
        }
        return null;
    }

    /**
     * Tries to match an EntityType that is a Projectile for given string
     *
     * @param s the string to match
     * @return the found Projectile
     */
    public EntityType projectile(String s)
    {
        EntityType type = this.any(s);
        if (type != null && type.isProjectile())
        {
            return type;
        }
        return null;
    }


}
