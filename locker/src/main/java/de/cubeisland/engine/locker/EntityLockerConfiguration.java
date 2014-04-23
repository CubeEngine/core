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
package de.cubeisland.engine.locker;

import org.bukkit.entity.EntityType;

import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.locker.storage.ProtectedType;

public class EntityLockerConfiguration extends LockerSubConfig<EntityLockerConfiguration, EntityType>
{
    public EntityLockerConfiguration(EntityType entityType)
    {
        super(ProtectedType.getProtectedType(entityType));
        this.type = entityType;
    }

    public String getTitle()
    {
        return type.name();
    }

    public static class EntityLockerConfigConverter extends LockerSubConfigConverter<EntityLockerConfiguration>
    {
        protected EntityLockerConfiguration fromString(String s) throws ConversionException
        {
            EntityType entityType;
            try
            {
                entityType = EntityType.valueOf(s);
            }
            catch (IllegalArgumentException ignore)
            {
                try
                {
                    entityType = EntityType.fromId(Integer.valueOf(s));
                }
                catch (NumberFormatException ignoreToo)
                {
                    entityType = Match.entity().any(s);
                }
            }
            if (entityType == null)
            {
                throw ConversionException.of(this, s, "Invalid EntityType!");
            }
            return new EntityLockerConfiguration(entityType);
        }
    }
}
