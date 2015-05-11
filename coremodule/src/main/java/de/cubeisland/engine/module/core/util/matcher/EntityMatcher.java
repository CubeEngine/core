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
package de.cubeisland.engine.module.core.util.matcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.projectile.Projectile;

/**
 * This Matcher provides methods to match Entities.
 */
public class EntityMatcher
{
    private final Map<String, EntityType> nameMap = new HashMap<>();
    private final Map<Short, EntityType> legacyIds = new HashMap<>(); // TODO fill the map
    private CoreModule core;

    EntityMatcher(CoreModule core, Game game)
    {
        this.core = core;
        for (EntityType type : game.getRegistry().getAllOf(EntityType.class))
        {
            nameMap.put(type.getName(), type);
        }
        // TODO read entity names
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
                return legacyIds.get(Short.parseShort(s));
            }
            catch (NumberFormatException ignored)
            {}
            String t_key = core.getModularity().start(StringMatcher.class).matchString(name, entities.keySet());
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
        if (type != null && Living.class.isAssignableFrom(type.getEntityClass()))
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
        return Monster.class.isAssignableFrom(entityType.getEntityClass());
    }

    /**
     * Returns whether this Entity is a friendly mob
     *
     * @return true if this type is an friendly entity
     */
    public boolean isFriendly(EntityType entityType)
    {
        return this.isAnimal(entityType) || Villager.class.isAssignableFrom(entityType.getEntityClass());
    }

    /**
     * Returns whether this type is an animal
     *
     * @return true if this type is an animal
     */
    public boolean isAnimal(EntityType entityType)
    {
        return Animal.class.isAssignableFrom(entityType.getEntityClass());
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
}
