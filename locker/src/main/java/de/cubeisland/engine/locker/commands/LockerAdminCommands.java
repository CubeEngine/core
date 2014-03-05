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
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;

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
            context.sendTranslated(MessageType.NEGATIVE, "{input} is not a valid id!", context.getString(0));
            return null;
        }
        Lock lockById = this.manager.getLockById(id);
        if (lockById == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "There is no protection with the id {integer}", id);
        }
        return lockById;
    }

    @Command(desc = "Opens a protected chest by protection id",
    usage = "<id>", min = 1, max = 1)
    public void view(ParameterizedContext context)
    {

        Lock lock = this.getLockById(context, context.getArg(0, Integer.class, null));
        if (lock == null) return;
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
                    context.sendTranslated(MessageType.NEGATIVE, "The protection with the id {integer} is an entity and cannot be accessed from far away!", lock.getId());
                }
                return;
            default:
                context.sendTranslated(MessageType.NEGATIVE, "The protection with the id {integer} is not a container!", lock.getId());
        }
    }

    @Command(desc = "Deletes a protection by its id", usage = "<id>", min = 1, max = 1)
    public void remove(ParameterizedContext context)
    {
        Lock lock = this.getLockById(context, context.getArg(0, Integer.class, null));
        if (lock == null) return;
        lock.delete((User)context.getSender());
    }

    @Command(desc = "Teleport to a protection", usage = "<id>", min = 1, max = 1)
    public void tp(ParameterizedContext context)
    {
        Lock lock = this.getLockById(context, context.getArg(0, Integer.class, null));
        if (lock == null) return;
        if (lock.isBlockLock())
        {
            ((User)context.getSender()).safeTeleport(lock.getFirstLocation(), TeleportCause.PLUGIN, false);
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "You cannot teleport to an entity protection!");
        }
    }

    @Command(desc = "Deletes all locks of given player", usage = "<player>", min = 1, max = 1)
    public void purge(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(MessageType.NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        this.manager.purgeLocksFrom(user);
        context.sendTranslated(MessageType.POSITIVE, "All locks from {user} are now deleted!", user);
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
