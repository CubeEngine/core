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
package de.cubeisland.cubeengine.hideme;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;

public class HideCommands implements CommandHolder
{
    private final Hideme module;

    public HideCommands(Hideme module)
    {
        this.module = module;
    }

    @Override
    public Class<? extends CubeCommand> getCommandType()
    {
        return ReflectedCommand.class;
    }

    @Command(desc = "Hides a player.", usage = "{player}")
    public void hide(CommandContext context)
    {

    }

    @Command(desc = "Unhides a player.", usage = "{player}")
    public void unhide(CommandContext context)
    {

    }

    @Command(desc = "Checks whether a player is hidden.", usage = "{player}")
    public void hidden(CommandContext context)
    {

    }

    @Command(desc = "Lists all hidden players.")
    public void listhiddens(CommandContext context)
    {

    }

    @Command(desc = "Toggles the ability to see hidden players.", usage = "{player}")
    public void seehiddens(CommandContext context)
    {

    }

    @Command(desc = "Checks whether a player can see hidden players.", usage = "{player}")
    public void canseehiddens(CommandContext context)
    {

    }

    @Command(desc = "Lists all players who can see hidden players.")
    public void listcanseehiddens(CommandContext context)
    {

    }
}
