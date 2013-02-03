package de.cubeisland.cubeengine.core.util.matcher;

import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.util.AliasMapFormat;
import de.cubeisland.cubeengine.core.util.log.LogLevel;
import gnu.trove.map.hash.THashMap;
import net.minecraft.server.v1_4_R1.EntityTypes;
import org.bukkit.entity.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This Matcher provides methods to match Entities.
 */
public class EntityMatcher
{
    private final Map<EntityType, String> reverseNameMap = new EnumMap<EntityType, String>(EntityType.class);
    private final Map<String, EntityType> nameMap = new THashMap<String, EntityType>();

    EntityMatcher()
    {
        TreeMap<String, List<String>> entityList = this.readEntities();
        for (String key : entityList.keySet())
        {
            try
            {
                EntityType entityType = EntityType.valueOf(key);
                boolean first = true;
                for (String name : entityList.get(key))
                {
                    if (first)
                    {
                        this.reverseNameMap.put(entityType,name);
                    }
                    this.nameMap.put(name.toLowerCase(Locale.ENGLISH), entityType);
                    first = false;
                }
            }
            catch (IllegalArgumentException ex)
            {
                CubeEngine.getLogger().warning("Unkown EntityType:"+ key);
                continue;
            }
        }
    }

    /**
     * Loads in the file with the saved entity-names
     *
     * @return the loaded entities with corresponding names
     */
    private TreeMap<String, List<String>> readEntities()
    {
        try
        {
            File file = new File(CubeEngine.getFileManager().getDataFolder(), CoreResource.ENTITIES.getTarget());
            TreeMap<String, List<String>> entityList = new TreeMap<String, List<String>>();
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
        Map<String, EntityType> entities = this.nameMap;
        String s = name.toLowerCase(Locale.ENGLISH);
        EntityType entity = entities.get(s);
        if (entity == null)
        {
            try
            {
                short entityId = Short.parseShort(s);
                return EntityType.fromId(entityId);
            }
            catch (NumberFormatException ignored)
            {}
            String t_key = Match.string().matchString(name, entities.keySet());
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
        if (type != null && this.canBeSpawnedBySpawnEgg(type))
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
        if (type != null && this.isMonster(type))
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
        if (type != null && this.isFriendly(type))
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
        if (type != null && this.isProjectile(type))
        {
            return type;
        }
        return null;
    }

    public EntityType living(String s)
    {
        EntityType type = this.any(s);
        if (type != null && type.isAlive())
        {
            return type;
        }
        return null;
    }

    /**
     * Returns if this EntityType can be spawned via SpawnEgg.
     */
    public boolean canBeSpawnedBySpawnEgg(EntityType entityType)
    {
        return EntityTypes.a.containsKey(entityType.getTypeId());
    }

    /**
     * Returns whether this Entity is a monster
     *
     * @return true if this type is an monster
     */
    public boolean isMonster(EntityType entityType)
    {
        return Monster.class.isAssignableFrom(entityType.getEntityClass()) || entityType == EntityType.ENDER_DRAGON;
    }

    /**
     * Returns whether this Entity is a friendly mob
     *
     * @return true if this type is an friendly entity
     */
    public boolean isFriendly(EntityType entityType)
    {
        return this.isAnimal(entityType) || NPC.class.isAssignableFrom(entityType.getEntityClass());
    }

    /**
     * Returns whether this type is an animal
     *
     * @return true if this type is an animal
     */
    public boolean isAnimal(EntityType entityType)
    {
        return Animals.class.isAssignableFrom(entityType.getEntityClass());
    }

    /**
     * Returns whether this type is a projectile
     *
     * @return true if this type is an projectile
     */
    public boolean isProjectile(EntityType entityType)
    {
        return Projectile.class.isAssignableFrom(entityType.getEntityClass());
    }

    public String getNameFor(EntityType type) {
        return this.reverseNameMap.get(type);
    }
}
