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
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;

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
    public void info(ParameterizedContext context)
    {
        if (isNotUser(context)) return;
        // TODO if keybook in hand show info
        manager.commandListener.setCommandType((User)context.getSender(), CommandType.INFO, null, false);
        context.sendTranslated("&aRightclock to show protection-info");
    }

    @Alias(names = "cpersist")
    @Command(desc = "persists your last container guard command")
    public void persist(ParameterizedContext context)
    {
        if (isNotUser(context)) return;
        if (this.manager.commandListener.persist((User)context.getSender()))
        {
            context.sendTranslated("&aYour commands will now persist!");
        }
        else
        {
            context.sendTranslated("&aYour commands will now no longer persist!");
        }
    }

    @Alias(names = "cremove")
    @Command(desc = "Shows information about a protection")
    public void remove(ParameterizedContext context)
    {
        if (isNotUser(context)) return;
        this.manager.commandListener.setCommandType((User)context.getSender(), CommandType.REMOVE, null);
        context.sendTranslated("&aRightclick a protection to remove it!");
    }

    @Alias(names = "cunlock")
    @Command(desc = "Unlocks a password protected chest", max = 1, min = 1)
    public void unlock(ParameterizedContext context)
    {
        if (isNotUser(context)) return;
        this.manager.commandListener.setCommandType((User)context.getSender(), CommandType.UNLOCK, context.getString(0));
        context.sendTranslated("&aRightclick to unlock a password protected chest!");
    }

    @Alias(names = "cmodify")
    @Command(names = "modify",
             desc = "adds or removes player from the accesslist",
                usage = "<players...>",
    flags = @Flag(longName = "admin", name = "a"), min = 1, max = 1)
    public void modify(ParameterizedContext context) // global flag to allow a user to access ALL your protections
    {
        if (isNotUser(context)) return;
        String[] explode = StringUtils.explode(",", context.getString(0));
        for (String name : explode)
        {
            if (name.startsWith("-"))
            {
                name = name.substring(1);
            }
            User user = this.getModule().getCore().getUserManager().getUser(name, false);
            if (user == null)
            {
                context.sendTranslated("&cUser &2%s&c not found!", name);
                return;
            }
        } // All users do exist!
        this.manager.commandListener.setCommandType((User)context.getSender(), CommandType.MODIFY, context.getString(0));
        context.sendTranslated("&aRightclick a protection to modify it!");
    }

    public void cGive(ParameterizedContext context)
    {

    }

    // TODO master

    @Alias(names = "ckey")
    @Command(names = "key",
             desc = "creates a KeyBook or invalidates previous KeyBooks",
             usage = "[-invalidate]",
             flags = @Flag(longName = "invalidate", name = "i"))
    public void key(ParameterizedContext context)
    {
        if (isNotUser(context)) return;
        if (context.hasFlag("i"))
        {
            this.manager.commandListener.setCommandType((User)context.getSender(), CommandType.INVALIDATE_KEYS, context.getString(0));
            context.sendTranslated("&aRightclick a protection to invalidate old KeyBooks for it!");
        }
        else
        {
            this.manager.commandListener.setCommandType((User)context.getSender(), CommandType.KEYS, context.getString(0), true);
            context.sendTranslated("&aRightclick a protection to with a book to create a new KeyBook!");
        }
    }

    // TODO subcmd for flags
    // TODO subcmd for droptransfer
    // TODO subcmd for admin stuff

    public static boolean isNotUser(ParameterizedContext context)
    {
        if (!(context.getSender() instanceof User))
        {
            context.sendTranslated("&cThis command can only be used ingame");
            return true;
        }
        return false;
    }
}
