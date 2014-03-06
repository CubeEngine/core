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
package de.cubeisland.engine.log.action.logaction.interact;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.EntityData;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.ENTITY;
import static de.cubeisland.engine.log.action.ActionTypeCategory.PLAYER;

/**
 * spawing entities with spawneggs
 * <p>Events: {@link de.cubeisland.engine.log.action.logaction.block.interaction.RightClickActionType RightClickActionType}</p>
 */
public class MonsterEggUse extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, ENTITY));
    }

    @Override
    public String getName()
    {
        return "monsteregg-use";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        EntityType entityType = EntityType.fromId(logEntry.getItemData().dura); // Dura is entityTypeId
        user.sendTranslated(MessageType.POSITIVE, "{}{user} spawned {name#entity}{}", time, logEntry.getCauserUser().getDisplayName(), new EntityData(entityType, null), loc);
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        if (!super.isSimilar(logEntry, other)) return false;
        return logEntry.getCauser().equals(other.getCauser())
            && logEntry.getWorld() == other.getWorld()
            && logEntry.getItemData().dura == other.getItemData().dura;
   }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).MONSTER_EGG_USE_enable;
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }

    @Override
    public boolean canRollback()
    {
        return false;
    }
}
