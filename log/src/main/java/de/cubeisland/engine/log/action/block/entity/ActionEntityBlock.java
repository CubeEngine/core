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
package de.cubeisland.engine.log.action.block.entity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.log.action.ActionCategory;
import de.cubeisland.engine.log.action.BaseAction;
import de.cubeisland.engine.log.action.block.ActionBlock;
import de.cubeisland.engine.reflect.Section;

/**
 * Represents an Entity changing a block
 * <p>SubActions:
 * {@link de.cubeisland.engine.log.action.block.entity.explosion.ExplosionAction}
 * {@link SheepEat}
 * {@link EndermanPickup}
 * {@link EndermanPlace}
 * {@link EntityChange}
 * {@link EntityBreak}
 * {@link EntityForm}
 */
public abstract class ActionEntityBlock extends ActionBlock
{
    public EntitySection entity;

    protected ActionEntityBlock(String name, ActionCategory... categories)
    {
        super(name, categories);
    }

    public void setEntity(Entity entity)
    {
        this.entity = new EntitySection(entity);
    }

    protected final int countUniqueEntities()
    {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(this.entity.uuid);
        int count = 1;
        for (BaseAction action : this.getAttached())
        {
            if (!uuids.contains(((ActionEntityBlock)action).entity.uuid))
            {
                uuids.add(((ActionEntityBlock)action).entity.uuid);
                count++;
            }
        }
        return count;
    }

    public static class EntitySection implements Section
    {
        // TODO oldColor sheep/wolf
        // TODO tamer
        // TODO villager profession
        // TODO isSitting
        // TODO istAgeable

        public UUID uuid;
        public EntityType type;

        public EntitySection()
        {
        }

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
