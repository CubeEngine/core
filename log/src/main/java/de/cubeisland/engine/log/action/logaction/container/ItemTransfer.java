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
package de.cubeisland.engine.log.action.logaction.container;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.action.ActionTypeCategory;
import de.cubeisland.engine.log.action.logaction.SimpleLogActionType;
import de.cubeisland.engine.log.storage.LogEntry;

import static de.cubeisland.engine.log.action.ActionTypeCategory.*;

/**
 * Items transferred by hoppers or droppers
 * <p>Events: {@link ContainerActionType#onItemMove ContainerAction}
 */
public class ItemTransfer extends SimpleLogActionType
{
    @Override
    protected Set<ActionTypeCategory> getCategories()
    {
        return new HashSet<>(Arrays.asList(PLAYER, INVENTORY, ITEM));
    }

    @Override
    public String getName()
    {
        return "item-transfer";
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        user.sendTranslated("%s&6%s&a got moved out of &6%s%s",
                            time,logEntry.getItemData(),
                            logEntry.getContainerTypeFromBlock(),loc);
    }
    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        return ContainerActionType.isSubActionSimilar(logEntry,other);
    }

    @Override
    public boolean isActive(World world)
    {
        return this.lm.getConfig(world).container.ITEM_TRANSFER_enable;
    }

    @Override
    public boolean canRollback()
    {
        return false; // TODO implement rollback
    }

    @Override
    public boolean canRedo()
    {
        return false;
    }

    @Override
    public boolean isBlockBound()
    {
        return true;
    }
}
