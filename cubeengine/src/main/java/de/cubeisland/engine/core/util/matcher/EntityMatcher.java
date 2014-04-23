/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.server.v1_7_R3.EntityTypes;
import net.minecraft.server.v1_7_R3.NPC;

import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Projectile;

import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.util.AliasMapFormat;
import gnu.trove.map.hash.THashMap;

/**
 * This Matcher provides methods to match Entities.
 */
public class EntityMatcher
{
    private final Map<EntityType, String> reverseNameMap = new EnumMap<>(EntityType.class);
    private final Map<String, EntityType> nameMap = new THashMap<>();

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
                        this.reverseNameMap.put(entityType, name);
                    }
                    this.nameMap.put(name.toLowerCase(Locale.ENGLISH), entityType);
                    first = false;
                }
            }
            catch (IllegalArgumentException ex)
            {
                CubeEngine.getLog().warn("Unknown EntityType: {}", key);
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
            Path file = CubeEngine.getFileManager().getDataPath().resolve(CoreResource.ENTITIES.getTarget());
            TreeMap<String, List<String>> entityList = new TreeMap<>();
            AliasMapFormat.parseStringList(file, entityList, false);
            try (InputStream is = CubeEngine.getFileManager().getSourceOf(file))
            {
                if (AliasMapFormat.parseStringList(is, entityList, true))
                {
                    CubeEngine.getLog().info("Updated entities.txt");
                    AliasMapFormat.parseAndSaveStringListMap(entityList, file);
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
        return EntityTypes.eggInfo.containsKey(entityType.getTypeId());
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

    public String getNameFor(EntityType type)
    {
        return this.reverseNameMap.get(type);
    }

    public boolean isTameable(EntityType type)
    {
        switch (type)
        {
            case WOLF:
            case OCELOT:
                return true;
            default:
                return false;
        }
    }
}
