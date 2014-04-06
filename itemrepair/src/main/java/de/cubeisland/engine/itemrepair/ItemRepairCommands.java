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
package de.cubeisland.engine.itemrepair;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.itemrepair.repair.RepairBlockManager;

public class ItemRepairCommands extends ContainerCommand implements Listener
{
    private final Set<String> removeRequests;
    private final Set<String> addRequests;
    private final RepairBlockManager rbm;
    private final Itemrepair module;

    public ItemRepairCommands(Itemrepair module)
    {
        super(module, "itemrepair", "ItemRepair commands");
        this.registerAlias(new String[]{"ir"},new String[]{});

        this.module = module;
        module.getCore().getEventManager().registerListener(this.module,this);
        this.rbm = module.getRepairBlockManager();
        this.addRequests = new HashSet<>();
        this.removeRequests = new HashSet<>();
    }

    @Command(desc = "Adds a new RepairBlock")
    public void add(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            if (!this.addRequests.contains(context.getSender().getName()))
            {
                if (!this.removeRequests.contains(context.getSender().getName()))
                {
                    this.addRequests.add(context.getSender().getName());
                    context.sendTranslated(MessageType.NEUTRAL, "Rightclick the block.");
                }
                else
                {
                    context.sendTranslated(MessageType.NEGATIVE, "You are already removing a repair block!");
                }
            }
            else
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are already adding a repair block!");
            }
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "You only need to right-click... {text:NOW!:color=DARK_RED}\nToo slow.");
        }
    }

    @Command(desc = "Removes an existing RepairBlock")
    public void remove(CommandContext context)
    {
        if (context.getSender() instanceof User)
        {
            if (!this.removeRequests.contains(context.getSender().getName()))
            {
                if (!this.addRequests.contains(context.getSender().getName()))
                {
                    this.removeRequests.add(context.getSender().getName());
                    context.sendTranslated(MessageType.NEUTRAL, "Rightclick the block.");
                }
                else
                {
                    context.sendTranslated(MessageType.NEGATIVE, "You are already adding a repair block!");
                }
            }
            else
            {
                context.sendTranslated(MessageType.NEGATIVE, "You are already removing a repair block!");
            }
        }
        else
        {
            context.sendTranslated(MessageType.NEGATIVE, "Only players can remove repair blocks!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdd(PlayerInteractEvent event)
    {
        final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.addRequests.contains(user.getName()))
        {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                final Block block = event.getClickedBlock();
                if (!this.rbm.isRepairBlock(block))
                {
                    if (this.rbm.attachRepairBlock(block))
                    {
                        user.sendTranslated(MessageType.POSITIVE, "Repair block successfully added!");
                    }
                    else
                    {
                        user.sendTranslated(MessageType.NEGATIVE, "This block can't be used as a repair block!");
                    }
                }
                else
                {
                    user.sendTranslated(MessageType.NEGATIVE, "This block is already a repair block!");
                }
            }
            if (event.getAction() != Action.PHYSICAL)
            {
                this.addRequests.remove(user.getName());
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRemove(PlayerInteractEvent event)
    {
        final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getUniqueId());
        if (this.removeRequests.contains(user.getName()))
        {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                if (this.rbm.detachRepairBlock(event.getClickedBlock()))
                {
                    user.sendTranslated(MessageType.POSITIVE, "Repair block successfully removed!");
                }
                else
                {
                    user.sendTranslated(MessageType.NEGATIVE, "This block is not a repair block!");
                }
            }
            if (event.getAction() != Action.PHYSICAL)
            {
                this.removeRequests.remove(user.getName());
                event.setCancelled(true);
            }
        }
    }
}
