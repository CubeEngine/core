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
package de.cubeisland.engine.backpack;

import java.util.Arrays;

import org.bukkit.World;
import org.bukkit.event.EventHandler;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;

public class BackpackCommands extends ContainerCommand
{
    private BackpackManager manager;

    public BackpackCommands(Backpack module, BackpackManager manager)
    {
        super(module, "backpack", "The Backpack commands", Arrays.asList("bp"));
        this.manager = manager;
    }

    @Alias(names = "openbp")
    @Command(desc = "opens a backpack", usage = "<name> [-global]",
    flags = @Flag(name = "g", longName = "global"), min = 1, max = 1)
    public void open(ParameterizedContext context)
    {
        if (context.getSender() instanceof User)
        {
            manager.openBackpack((User)context.getSender(), context.getString(0), context.hasFlag("g"));
            return;
        }
                // TODO consolemsg
    }

    @Alias(names = "createbp")
    @Command(desc = "creates a new backpack", usage = "<name> [user] [-global]|[-single] [for <world>]",
             flags = {
                 @Flag(name = "g", longName = "global"),
                 @Flag(name = "s", longName = "single")
                 // TODO named param world
             }, min = 1, max = 2)
    public void create(ParameterizedContext context)
    {
        User forUser = null;
        World forWorld = null;
        if (context.getSender() instanceof User)
        {
            forUser = (User)context.getSender();
            forWorld = ((User)context.getSender()).getWorld();
        }
        else
        {
            // TODO world
        }
        if (context.hasArg(1))
        {
            forUser = context.getUser(1);
            if (forUser == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", context.getString(1));
                return;
            }
        }
        else if (!(context.getSender() instanceof User))
        {
            // TODO  console msg need user
            return;
        }
        manager.createBackpack(context.getSender(), forUser, context.getString(0), forWorld, context.hasFlag("g"), context.hasFlag("s"));
    }
}
