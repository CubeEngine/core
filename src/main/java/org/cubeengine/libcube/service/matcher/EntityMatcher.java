/*
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

import com.google.inject.Inject;
import net.kyori.adventure.key.Key;
import org.cubeengine.libcube.service.config.EntityTypeConverter;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.Monster;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.trader.Villager;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This Matcher provides methods to match Entities.
 */
public class EntityMatcher
{
    private final Map<String, EntityType> ids = new HashMap<>();
    private final Map<String, EntityType> translations = new HashMap<>();
    private final Map<Short, EntityType> legacyIds = new HashMap<>(); // TODO fill the map
    private StringMatcher stringMatcher;

    @Inject
    public EntityMatcher(StringMatcher stringMatcher, Reflector reflector)
    {
        this.stringMatcher = stringMatcher;
        RegistryTypes.ENTITY_TYPE.get().streamEntries().forEach(entry -> {
            ids.put(entry.key().asString(), entry.value());
// TODO translactions            translations.put(type.getTranslation().get(Locale.getDefault()).toLowerCase(), type);
        });
        reflector.getDefaultConverterManager().registerConverter(new EntityTypeConverter(), EntityType.class);
    }

                                                                                                       /**
     * Tries to match an EntityType for given string
     *
     * @param name the name to match
     *
     * @return the found EntityType
     */
    public EntityType<?> any(String name, Locale locale)
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

        EntityType<?> entity = Sponge.game().registry(RegistryTypes.ENTITY_TYPE).findValue(ResourceKey.resolve(name)).orElse(null);
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
            Sponge.game().registry(RegistryTypes.ENTITY_TYPE).streamEntries().forEach(entry -> {
                // TODO translation                translations.put(type.getTranslation().get(locale, type), type);
            });
            entity = anyFromMap(name, translations); // Use Language Translations
        }

        return entity;
    }

    private EntityType<?> anyFromMap(String name, Map<String, EntityType> entities)
    {
        EntityType<?> entity = entities.get(name);
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
    public EntityType<?> mob(String s, Locale locale)
    {
        EntityType<?> type = this.any(s, locale);
        final Entity entity = this.getEntity(type);
        if (type != null && entity instanceof Living)
        {
            return type;
        }
        return null;
    }

    public Entity getEntity(EntityType<?> type) {
        final ServerWorld world = Sponge.server().worldManager().defaultWorld();
        return world.createEntity(type, Vector3d.ZERO);
    }

}
