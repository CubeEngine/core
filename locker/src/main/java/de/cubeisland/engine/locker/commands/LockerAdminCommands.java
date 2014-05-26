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
package de.cubeisland.engine.locker.commands;

import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.InventoryHolder;

import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.POSITIVE;

public class LockerAdminCommands extends ContainerCommand
{
    private final LockManager manager;

    public LockerAdminCommands(Locker module, LockManager manager)
    {
        super(module, "admin", "Administrate the protections");
        this.manager = manager;
    }

    private Lock getLockById(ParameterizedContext context, Integer id)
    {
        if (LockerCommands.isNotUser(context.getSender())) return null;
        if (id == null)
        {
            context.sendTranslated(NEGATIVE, "{input} is not a valid id!", context.getArg(0));
            return null;
        }
        Lock lockById = this.manager.getLockById(id);
        if (lockById == null)
        {
            context.sendTranslated(NEGATIVE, "There is no protection with the id {integer}", id);
        }
        return lockById;
    }

    @Command(desc = "Opens a protected chest by protection id")
    @IParams(@Grouped(@Indexed(label = "id", type = Integer.class)))
    public void view(ParameterizedContext context)
    {
        Lock lock = this.getLockById(context, context.<Integer>getArg(0));
        switch (lock.getProtectedType())
        {
            case CONTAINER:
            case ENTITY_CONTAINER:
            case ENTITY_CONTAINER_LIVING:
                if (lock.isBlockLock())
                {
                    ((User)context.getSender()).openInventory(((InventoryHolder)lock.getFirstLocation().getBlock()
                                                                                        .getState()).getInventory());
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "The protection with the id {integer} is an entity and cannot be accessed from far away!", lock.getId());
                }
                return;
            default:
                context.sendTranslated(NEGATIVE, "The protection with the id {integer} is not a container!", lock.getId());
        }
    }

    @Command(desc = "Deletes a protection by its id")
    @IParams(@Grouped(@Indexed(label = "id", type = Integer.class)))
    public void remove(ParameterizedContext context)
    {
        Lock lock = this.getLockById(context, context.<Integer>getArg(0, null));
        if (lock == null) return;
        lock.delete((User)context.getSender());
    }

    @Command(desc = "Teleport to a protection")
    @IParams(@Grouped(@Indexed(label = "id", type = Integer.class)))
    public void tp(ParameterizedContext context)
    {
        Lock lock = this.getLockById(context, context.<Integer>getArg(0, null));
        if (lock == null) return;
        if (lock.isBlockLock())
        {
            ((User)context.getSender()).safeTeleport(lock.getFirstLocation(), TeleportCause.PLUGIN, false);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You cannot teleport to an entity protection!");
        }
    }

    @Command(desc = "Deletes all locks of given player")
    @IParams(@Grouped(@Indexed(label = "player", type = User.class)))
    public void purge(ParameterizedContext context)
    {
        User user = context.getArg(0);
        this.manager.purgeLocksFrom(user);
        context.sendTranslated(POSITIVE, "All locks for {user} are now deleted!", user);
    }

    // TODO admin cmds

    public void cleanup(ParameterizedContext context)
    {
        // cleanup remove not accessed protections / time in config
    }

    public void list(ParameterizedContext context)
    {
        // find & show all protections of a user/selection
    }
}
