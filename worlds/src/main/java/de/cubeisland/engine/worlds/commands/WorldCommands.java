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
package de.cubeisland.engine.worlds.commands;

import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.module.Module;

public class WorldCommands extends ContainerCommand
{
    public WorldCommands(Module module)
    {
        super(module, "worlds", "Worlds commands");
    }

    @Command(desc = "Creates and loads a new world")
    public void create(ParameterizedContext context)
    {

    }
    // create name environement seed generator worldtype structures?

    @Command(desc = "Loads a world from configuration")
    public void load(CommandContext context)
    {

    }
    // load (config file must exist)


    @Command(desc = "Unload a loaded world")
    public void unload(CommandContext context) // -f to tp players out of that world
    {

    }
    // unload (-f teleports all players out of this world)

    @Command(desc = "Remove a world")
    public void remove(CommandContext context) // -f unload and tp players out
    {

    }
    // remove/delete

    @Command(desc = "Lists all worls")
    public void list(CommandContext context)
    {

    }
    // list / list worlds that you can enter

    @Command(desc = "Show info about a world")
    public void info(CommandContext context)
    {

    }
    // info

    @Command(desc = "Lists the players in a world")
    public void listplayers(CommandContext context)
    {

    }
    // listplayers in world/universe

    @Command(desc = "Reloads the module")
    public void reload(CommandContext context)
    {

    }
    // reload

    @Command(desc = "Sets the main world")
    public void setMainWorld(CommandContext context)
    {

    }
    // set main world (of universe) (of universes)
    // set main universe

    @Command(desc = "Moves a world into another universe")
    public void move(CommandContext context)
    {

    }
    // move to other universe

    @Command(desc = "Teleports to the spawn of a world")
    public void spawn(CommandContext context)
    {
        
    }
    // spawn to universe spawn
    // spawn to world spawn
}
