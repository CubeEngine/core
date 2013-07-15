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
package de.cubeisland.cubeengine.itemrepair;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.itemrepair.repair.RepairBlockManager;

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
        this.addRequests = new HashSet<String>();
        this.removeRequests = new HashSet<String>();
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
                    context.sendTranslated("&eRightclick the block.");
                }
                else
                {
                    context.sendTranslated("&cYou are already removing a repair block!");
                }
            }
            else
            {
                context.sendTranslated("&cYou are already adding a repair block!");
            }
        }
        else
        {
            context.sendTranslated("&eOk now you only need to right-click &4NOW&e!\n... too slow");
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
                    context.sendTranslated("&eRightclick the block.");
                }
                else
                {
                    context.sendTranslated("&cYou are already adding a repair block!");
                }
            }
            else
            {
                context.sendTranslated("&cYou are already removing a repair block!");
            }
        }
        else
        {
            context.sendTranslated("&cOnly players can remove repair blocks!");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAdd(PlayerInteractEvent event)
    {
        final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (this.addRequests.contains(user.getName()))
        {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                final Block block = event.getClickedBlock();
                if (!this.rbm.isRepairBlock(block))
                {
                    if (this.rbm.attachRepairBlock(block))
                    {
                        user.sendTranslated("&aRepair block successfully added!");
                    }
                    else
                    {
                        user.sendTranslated("&cThis block can't be used as a repair block!");
                    }
                }
                else
                {
                    user.sendTranslated("&cThis block is already a repair block!");
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
        final User user = this.module.getCore().getUserManager().getExactUser(event.getPlayer().getName());
        if (this.removeRequests.contains(user.getName()))
        {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
            {
                if (this.rbm.detachRepairBlock(event.getClickedBlock()))
                {
                    user.sendTranslated("&aRepair block successfully removed!");
                }
                else
                {
                    user.sendTranslated("&cThis block is not a repair block!");
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
