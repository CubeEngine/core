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

import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.commands.CommandListener.CommandType;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.ProtectionFlag;

public class LockerCommands extends ContainerCommand
{
    private Locker module;
    LockManager manager;

    public LockerCommands(Locker module, LockManager manager)
    {
        super(module, "locker", "Locker commands", Arrays.asList("l"));
        this.module = module;
        this.manager = manager;
    }

    @Alias(names = "cinfo")
    @Command(desc = "Shows information about a protection")
    public void info(ParameterizedContext context)
    {
        // TODO if keybook in hand show info
        manager.commandListener.setCommandType(context.getSender(), CommandType.INFO, null, false);
        context.sendTranslated("&aRightclock to show protection-info");
    }

    @Alias(names = "cpersist")
    @Command(desc = "persists your last locker command")
    public void persist(ParameterizedContext context)
    {
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
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.REMOVE, null);
        context.sendTranslated("&aRightclick a protection to remove it!");
    }

    @Alias(names = "cunlock")
    @Command(desc = "Unlocks a password protected chest", max = 1, min = 1)
    public void unlock(ParameterizedContext context)
    {
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.UNLOCK, context.getString(0));
        context.sendTranslated("&aRightclick to unlock a password protected chest!");
    }

    // TODO modify global access

    @Alias(names = "cmodify")
    @Command(names = "modify",
             desc = "adds or removes player from the accesslist",
                usage = "<players...>", min = 1, max = 1)
    public void modify(ParameterizedContext context)
    {
        String[] explode = StringUtils.explode(",", context.getString(0));
        for (String name : explode)
        {
            if (name.startsWith("@"))
            {
                name = name.substring(1);
            }
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
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.MODIFY, context.getString(0));
        context.sendTranslated("&aRightclick a protection to modify it!");
    }

    @Command(desc = "gives a protection to someone else",
    usage = "<player>", max = 1)
    public void give(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
            return;
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.GIVE, context.getString(0));
    }

    // TODO masterKeys / multiKeys

    @Alias(names = "ckey")
    @Command(names = "key",
             desc = "creates a KeyBook or invalidates previous KeyBooks",
             usage = "[-invalidate]",
             flags = @Flag(longName = "invalidate", name = "i"))
    public void key(ParameterizedContext context)
    {
        if (!this.module.getConfig().allowKeyBooks)
        {
            context.sendTranslated("&cKeyBooks are deactivated!");
            return;
        }
        if (context.hasFlag("i"))
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.INVALIDATE_KEYS, context.getString(0));
            context.sendTranslated("&aRightclick a protection to invalidate old KeyBooks for it!");
        }
        else
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.KEYS, context.getString(0), true);
            context.sendTranslated("&aRightclick a protection to with a book to create a new KeyBook!");
        }
    }

    @Command(desc = "Sets or unsets flags",
             usage = "flag <flags...>",
             params = {
                 @Param(names = "set", completer = FlagCompleter.class),
                 @Param(names = "unset", completer = FlagCompleter.class),
             })
    public void flag(ParameterizedContext context)
    {
        if (context.getParams().isEmpty())
        {
            context.sendTranslated("&eYou need to define which flags to &6set&a or &6unSet&a!");
            return;
        }
        if (context.hasParam("set") && context.hasParam("unSet"))
        {
            context.sendTranslated("&cYou have cannot set and unset flags at the same time!");
            return;
        }
        if (context.hasParam("set"))
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.FLAGS_SET, context.getString("set"));
        }
        else
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.FLAGS_UNSET, context.getString("unset"));
        }
        context.sendTranslated("&aRightclick a protection to change its flags!");
    }

    public static class FlagCompleter implements Completer
    {
        @Override
        public List<String> complete(CommandSender sender, String token)
        {
            String subToken = token;
            if (subToken.contains(","))
            {
                subToken = subToken.substring(subToken.lastIndexOf(",") + 1);
            }
            return ProtectionFlag.getTabCompleteList(token, subToken);
        }
    }

    // TODO subcmd for droptransfer

    public static boolean isNotUser(CommandSender sender)
    {
        if (!(sender instanceof User))
        {
            sender.sendTranslated("&cThis command can only be used ingame");
            return true;
        }
        return false;
    }



    // TODO add buttons to door-protection to open door for x-sec = autoclose time BUT deny redstone so ONLY that button can open the door/doubledoor
}
