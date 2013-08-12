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
package de.cubeisland.engine.cguard.commands;

import java.util.Arrays;

import de.cubeisland.engine.cguard.Cguard;
import de.cubeisland.engine.cguard.commands.CommandListener.CommandType;
import de.cubeisland.engine.cguard.storage.GuardManager;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;

public class GuardCommands extends ContainerCommand
{
    GuardManager manager;

    public GuardCommands(Cguard module, GuardManager manager)
    {
        super(module, "cguard", "ContainerGuard Commands", Arrays.asList("containerguard", "cg"));
        this.manager = manager;
    }

    @Alias(names = "cinfo")
    @Command(desc = "Shows information about a protection")
    public void info(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cThis command can only be used ingame");
            return;
        }
        manager.commandListener.setCommandType((User)context.getSender(), CommandType.INFO);
        context.sendTranslated("&aRightclock to show protection-info");
    }

    @Alias(names = "cpersist")
    @Command(desc = "persists your last container guard command")
    public void persist(CommandContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cThis command can only be used ingame");
            return;
        }
        if (this.manager.commandListener.persist((User)context.getSender()))
        {

        }
        else
        {
            // TODO msg
        }
    }

    public void cRemove(CommandContext context)
    {

    }

    public void cUnlock(CommandContext context)
    {

    }

    public void cModify(CommandContext context) // global flag to allow a user to access ALL your protections
    {

    }

    public void cgive(CommandContext context)
    {

    }

    // TODO subcmd for flags
    // TODO subcmd for droptransfer
    // TODO subcmd for admin stuff
}
