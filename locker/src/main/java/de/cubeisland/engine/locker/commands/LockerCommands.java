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

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.commands.CommandListener.CommandType;
import de.cubeisland.engine.locker.storage.KeyBook;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.ProtectionFlag;

public class LockerCommands extends ContainerCommand
{
    private final Locker module;
    final LockManager manager;

    public LockerCommands(Locker module, LockManager manager)
    {
        super(module, "locker", "Locker commands", Arrays.asList("l"));
        this.module = module;
        this.manager = manager;
    }

    @Alias(names = "cinfo")
    @Command(desc = "Shows information about a protection",
    flags = @Flag(longName = "persist", name = "p"))
    public void info(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        User user = (User)context.getSender();
        KeyBook keyBook = KeyBook.getKeyBook(((User)context.getSender()).getItemInHand(), (User)context.getSender(), this.module);
        if (keyBook != null)
        {
            Lock lock = this.manager.getLockById(keyBook.lockID);
            if (lock != null && keyBook.isValidFor(lock))
            {
                context.sendTranslated("&aThe strong magic surrounding this KeyBook allows you to access the designated protection");
                if (lock.isBlockLock())
                {
                    Location loc = lock.getFirstLocation();
                    context.sendTranslated("&aThe protection corresponding to this book is located at &6%d&a:&6%d&a:&6%d&a in &6%s",
                                           loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
                }
                else
                {
                    for (Entity entity : user.getWorld().getEntities())
                    {
                        if (entity.getUniqueId().equals(lock.getEntityUID()))
                        {
                            Location loc = entity.getLocation();
                            context.sendTranslated("&aThe entity-protection corresponding to this book is located at &6%d&a:&6%d&a:&6%d&a in &6%s",
                                                   loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName());
                            return;
                        }
                    }
                    context.sendTranslated("&aYour magic is not strong enough to locate the corresponding entity-protection!");
                }
            }
            else
            {
                context.sendTranslated("&eAs you inspect the KeyBook closer you realize that its magic power has disappeared!");
                keyBook.invalidate();
            }
            return;
        }
        manager.commandListener.setCommandType(context.getSender(), CommandType.INFO, null, false);
        context.sendTranslated("&aRightclick to show protection-info");
    }

    @Alias(names = "cpersist")
    @Command(desc = "persists your last locker command")
    public void persist(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
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
    @Command(desc = "Shows information about a protection",
             flags = @Flag(longName = "persist", name = "p"))
    public void remove(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.REMOVE, null);
        context.sendTranslated("&aRightclick a protection to remove it!");
    }

    @Alias(names = "cunlock")
    @Command(desc = "Unlocks a password protected chest", max = 1, min = 1,
             flags = @Flag(longName = "persist", name = "p"))
    public void unlock(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.UNLOCK, context.getString(0));
        context.sendTranslated("&aRightclick to unlock a password protected chest!");
    }

    @Alias(names = "cmodify")
    @Command(names = "modify",
             desc = "adds or removes player from the accesslist",
                usage = "<players...>", min = 1, max = 1,
    flags = {@Flag(name = "g", longName = "global"),
             @Flag(longName = "persist", name = "p")})
    public void modify(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
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
        if (context.hasFlag("g"))
        {
            this.manager.setGlobalAccess((User)context.getSender(), context.getString(0));
        }
        else
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.MODIFY, context.getString(0));
            context.sendTranslated("&aRightclick a protection to modify it!");
        }
    }

    @Alias(names = "cgive")
    @Command(desc = "gives a protection to someone else",
    usage = "<player>", min = 1, max = 1,
    flags = @Flag(longName = "persist", name = "p"))
    public void give(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser &2%s&c not found!", context.getString(0));
            return;
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.GIVE, context.getString(0));
    }

    @Alias(names = "ckey")
    @Command(names = "key",
             desc = "creates a KeyBook or invalidates previous KeyBooks",
             usage = "[-invalidate]",
             flags = { @Flag(longName = "invalidate", name = "i"),
                       @Flag(longName = "persist", name = "p")})
    public void key(ParameterizedContext context)
    {
        if (!this.module.getConfig().allowKeyBooks)
        {
            context.sendTranslated("&cKeyBooks are deactivated!");
            return;
        }
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
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

    @Alias(names = "cflag")
    @Command(desc = "Sets or unsets flags",
             usage = "set|unset <flags...>",
             params = {
                 @Param(names = "set", completer = FlagCompleter.class),
                 @Param(names = "unset", completer = FlagCompleter.class),
             },
             flags = @Flag(longName = "persist", name = "p"))
    public void flag(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.getParams().isEmpty())
        {
            context.sendTranslated("&eYou need to define which flags to &6set&e or &6unSet&a!");
            context.sendTranslated("&eThe following flags are available:");
            String format = ChatFormat.parseFormats(" &7- &6%s");
            for (String flag : ProtectionFlag.getNames())
            {
                context.sendMessage(String.format(format, flag));
            }
            context.sendTranslated("&eYou can also unset \"&6all&e\"");
            return;
        }
        if (context.hasFlag("p"))
        {
            this.persist(context);
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

    public static boolean isNotUser(CommandSender sender)
    {
        if (!(sender instanceof User))
        {
            sender.sendTranslated("&cThis command can only be used ingame");
            return true;
        }
        return false;
    }
}
