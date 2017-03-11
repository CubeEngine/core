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
package org.cubeengine.libcube.service.matcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.cubeengine.libcube.service.config.EntityTypeConverter;
import org.cubeengine.libcube.service.config.ItemTypeConverter;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.Game;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.Villager;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.item.ItemType;

/**
 * This Matcher provides methods to match Entities.
 */
@ServiceProvider(EntityMatcher.class)
public class EntityMatcher
{
    private final Map<String, EntityType> ids = new HashMap<>();
    private final Map<String, EntityType> translations = new HashMap<>();
    private final Map<Short, EntityType> legacyIds = new HashMap<>(); // TODO fill the map
    private Game game;
    private StringMatcher stringMatcher;

    @Inject
    public EntityMatcher(Game game, StringMatcher stringMatcher, Reflector reflector)
    {
        this.game = game;
        this.stringMatcher = stringMatcher;
        for (EntityType type :  game.getRegistry().getAllOf(EntityType.class))
        {
            ids.put(type.getName().toLowerCase(), type);
            translations.put(type.getTranslation().get(Locale.getDefault()).toLowerCase(), type);
        }
        reflector.getDefaultConverterManager().registerConverter(new EntityTypeConverter(), EntityType.class);
    }

    /**
     * Tries to match an EntityType for given string
     *
     * @param name the name to match
     *
     * @return the found EntityType
     */
    public EntityType any(String name, Locale locale)
    {
        if (name == null)
        {
            return null;
        }

        // 1.11 change // TODO there are more...
        if ("minecraft:entityhorse".equalsIgnoreCase(name))
        {
            name = "minecraft:horse";
        }
        if ("minecraft:minecartchest".equals(name))
        {
            name = "minecraft:chest_minecart";
        }

        if ("minecraft:minecarthopper".equals(name))
        {
            name = "minecraft:hopper_minecart";
        }

        if ("minecraft:itemframe".equals(name))
        {
            name = "minecraft:item_frame";
        }

        EntityType entity = game.getRegistry().getType(EntityType.class, name).orElse(null);
        if (entity != null)
        {
            return entity;
        }

        try
        {
            return legacyIds.get(Short.parseShort(name));
        }
        catch (NumberFormatException ignored)
        {
        }

        name = name.toLowerCase();

        entity = anyFromMap(name, this.ids); // Minecraft IDs
        if (entity == null)
        {
            entity = anyFromMap(name, this.translations); // Use default language translation
        }
        if (entity == null && locale != null)
        {
            Map<String, EntityType> translations = new HashMap<>();
            for (EntityType type : game.getRegistry().getAllOf(EntityType.class))
            {
                translations.put(type.getTranslation().get(locale, type), type);
            }
            entity = anyFromMap(name, translations); // Use Language Translations
        }

        return entity;
    }

    private EntityType anyFromMap(String name, Map<String, EntityType> entities)
    {
        EntityType entity = entities.get(name);
        if (entity == null)
        {
            String t_key = stringMatcher.matchString(name, entities.keySet());
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
     *
     * @return the found Mob
     */
    public EntityType mob(String s, Locale locale)
    {
        EntityType type = this.any(s, locale);
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
     *
     * @return the found Mob
     */
    public EntityType spawnEggMob(String s, Locale locale)
    {
        EntityType type = this.mob(s, locale);
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
     *
     * @return the found Monster
     */
    public EntityType monster(String s, Locale locale)
    {
        EntityType type = this.any(s, locale);
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
     *
     * @return the found friendly Mob
     */
    public EntityType friendlyMob(String s, Locale locale)
    {
        EntityType type = this.any(s, locale);
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
     *
     * @return the found Projectile
     */
    public EntityType projectile(String s, Locale locale)
    {
        EntityType type = this.any(s, locale);
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
        // TODO return EntityTypes.eggInfo.containsKey(entityType.getTypeId());
        return true;
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
