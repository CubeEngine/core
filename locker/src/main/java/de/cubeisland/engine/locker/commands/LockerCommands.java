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

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.locker.Locker;
import de.cubeisland.engine.locker.commands.CommandListener.CommandType;
import de.cubeisland.engine.locker.storage.KeyBook;
import de.cubeisland.engine.locker.storage.Lock;
import de.cubeisland.engine.locker.storage.LockManager;
import de.cubeisland.engine.locker.storage.ProtectionFlag;

import static de.cubeisland.engine.core.util.ChatFormat.GOLD;
import static de.cubeisland.engine.core.util.ChatFormat.GREY;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.util.Arrays.asList;

public class LockerCommands extends ContainerCommand
{
    private final Locker module;
    final LockManager manager;

    public LockerCommands(Locker module, LockManager manager)
    {
        super(module, "locker", "Locker commands");
        this.setAliases(new HashSet<>(asList("l")));
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
                context.sendTranslated(POSITIVE, "The strong magic surrounding this KeyBook allows you to access the designated protection");
                if (lock.isBlockLock())
                {
                    Location loc = lock.getFirstLocation();
                    context.sendTranslated(POSITIVE, "The protection corresponding to this book is located at {vector} in {world}", new BlockVector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), loc.getWorld());
                }
                else
                {
                    for (Entity entity : user.getWorld().getEntities())
                    {
                        if (entity.getUniqueId().equals(lock.getEntityUID()))
                        {
                            Location loc = entity.getLocation();
                            context.sendTranslated(POSITIVE, "The entity protection corresponding to this book is located at {vector} in {world}", new BlockVector3(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()), loc.getWorld());
                            return;
                        }
                    }
                    context.sendTranslated(POSITIVE, "Your magic is not strong enough to locate the corresponding entity protection!");
                }
            }
            else
            {
                context.sendTranslated(NEUTRAL, "As you inspect the KeyBook closer you realize that its magic power has disappeared!");
                keyBook.invalidate();
            }
            return;
        }
        manager.commandListener.setCommandType(context.getSender(), CommandType.INFO, null, false);
        context.sendTranslated(POSITIVE, "Right click to show protection-info");
    }

    @Alias(names = "cpersist")
    @Command(desc = "persists your last locker command")
    public void persist(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (this.manager.commandListener.persist((User)context.getSender()))
        {
            context.sendTranslated(POSITIVE, "Your commands will now persist!");
        }
        else
        {
            context.sendTranslated(POSITIVE, "Your commands will now no longer persist!");
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
        context.sendTranslated(POSITIVE, "Right click a protection to remove it!");
    }

    @Alias(names = "cunlock")
    @Command(desc = "Unlocks a password protected chest",
             indexed = @Grouped(@Indexed("password")),
             flags = @Flag(longName = "persist", name = "p"))
    public void unlock(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.UNLOCK, context.getString(0));
        context.sendTranslated(POSITIVE, "Right click to unlock a password protected chest!");
    }

    @Alias(names = "cmodify")
    @Command(names = "modify",
             desc = "adds or removes player from the accesslist",
             indexed = @Grouped(@Indexed("players...")),
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
            User user = this.getModule().getCore().getUserManager().findExactUser(name);
            if (user == null)
            {
                context.sendTranslated(NEGATIVE, "Player {user} not found!", name);
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
            context.sendTranslated(POSITIVE, "Right click a protection to modify it!");
        }
    }

    @Alias(names = "cgive")
    @Command(desc = "gives a protection to someone else",
             indexed = @Grouped(@Indexed("player")),
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
            context.sendTranslated(NEGATIVE, "Player {user} not found!", context.getString(0));
            return;
        }
        this.manager.commandListener.setCommandType(context.getSender(), CommandType.GIVE, context.getString(0));
    }

    @Alias(names = "ckey")
    @Command(names = "key",
             desc = "creates a KeyBook or invalidates previous KeyBooks",
             flags = { @Flag(longName = "invalidate", name = "i"),
                       @Flag(longName = "persist", name = "p")})
    public void key(ParameterizedContext context)
    {
        if (!this.module.getConfig().allowKeyBooks)
        {
            context.sendTranslated(NEGATIVE, "KeyBooks are deactivated!");
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
            context.sendTranslated(POSITIVE, "Right click a protection to invalidate old KeyBooks for it!");
        }
        else
        {
            this.manager.commandListener.setCommandType(context.getSender(), CommandType.KEYS, context.getString(0), true);
            context.sendTranslated(POSITIVE, "Right click a protection to with a book to create a new KeyBook!");
        }
    }

    @Alias(names = "cflag")
    @Command(desc = "Sets or unsets flags",
             indexed = {@Grouped(@Indexed({"!set","!unset"})),
                        @Grouped(@Indexed("flags..."))},
             params = {@Param(names = "set", completer = FlagCompleter.class),
                       @Param(names = "unset", completer = FlagCompleter.class)},
             flags = @Flag(longName = "persist", name = "p"))
    public void flag(ParameterizedContext context)
    {
        if (isNotUser(context.getSender())) return;
        if (context.getParams().isEmpty())
        {
            context.sendTranslated(NEUTRAL, "You need to define which flags to {text:set} or {text:unset}!");
            context.sendTranslated(NEUTRAL, "The following flags are available:");
            String format = "  " + GREY + "-" + GOLD;
            for (String flag : ProtectionFlag.getNames())
            {
                context.sendMessage(format + flag);
            }
            context.sendTranslated(NEUTRAL, "You can also unset {text:all}");
            return;
        }
        if (context.hasFlag("p"))
        {
            this.persist(context);
        }
        if (context.hasParam("set") && context.hasParam("unSet"))
        {
            context.sendTranslated(NEGATIVE, "You have cannot set and unset flags at the same time!");
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
        context.sendTranslated(POSITIVE, "Right click a protection to change its flags!");
    }

    public static class FlagCompleter implements Completer
    {
        @Override
        public List<String> complete(ParameterizedTabContext context, String token)
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
            sender.sendTranslated(NEGATIVE, "This command can only be used ingame");
            return true;
        }
        return false;
    }
}
