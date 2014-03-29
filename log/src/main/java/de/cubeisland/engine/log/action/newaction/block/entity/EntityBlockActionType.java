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
package de.cubeisland.engine.log.action.newaction.block.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.newaction.ActionTypeBase;
import de.cubeisland.engine.log.action.newaction.block.BlockActionType;

public abstract class EntityBlockActionType<ListenerType> extends BlockActionType<ListenerType>
{
    public EntitySection entity;

    public void setEntity(Entity entity)
    {
        this.entity = new EntitySection(entity);
    }

    protected final int countUniqueEntities()
    {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(this.entity.uuid);
        int count = 1;
        for (ActionTypeBase action : this.getAttached())
        {
            if (!uuids.contains(((EntityBlockActionType)action).entity.uuid))
            {
                uuids.add(((EntityBlockActionType)action).entity.uuid);
                count++;
            }
        }
        return count;
    }

    public static class EntitySection
    {
        public UUID uuid;
        public EntityType type;

        public EntitySection(Entity entity)
        {
            this.type = entity.getType();
            this.uuid = entity.getUniqueId();
        }

        public boolean equals(EntitySection section)
        {
            return this.uuid.equals(section.uuid);
        }

        public boolean isSameType(EntitySection section)
        {
            return this.type == section.type;
        }

        public String name()
        {
            return this.type.name();
        }
    }

    // TODO additional
}
