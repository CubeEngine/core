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
package de.cubeisland.engine.core.bukkit;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandHolder;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContainerCommand;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.Grouped;
import de.cubeisland.engine.core.command.reflected.Indexed;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.text.DateFormat.SHORT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class VanillaCommands implements CommandHolder
{
    private final BukkitCore core;

    public VanillaCommands(BukkitCore core)
    {
        this.core = core;
    }

    public Class<? extends CubeCommand> getCommandType()
    {
        return ReflectedCommand.class;
    }

    @Command(
        names = {"stop", "shutdown", "killserver", "quit"},
        desc = "Shuts down the server",
        indexed = @Grouped(req = false, value = @Indexed("message"), greedy = true))
    public void stop(CommandContext context)
    {
        String message = context.getStrings(0);
        if (message == null || message.isEmpty())
        {
            message = this.core.getServer().getShutdownMessage();
        }
        message = ChatFormat.parseFormats(message);

        this.core.getUserManager().kickAll(message);
        this.core.getServer().shutdown();
    }

    @Command(desc = "Reloads the server.", flags = @Flag(name = "m", longName = "modules"))
    public void reload(ParameterizedContext context)
    {
        final String message = context.getStrings(0);
        if (message != null)
        {
            context.getCore().getUserManager().broadcastMessageWithPerm(NONE, message, core.perms().COMMAND_RELOAD_NOTIFY);
        }

        if (context.hasFlag("m"))
        {
            context.sendTranslated(NEUTRAL, "Reloading the modules...");
            this.core.getModuleManager().reloadModules();
            context.sendTranslated(POSITIVE, "Successfully reloaded {amount} modules!", this.core.getModuleManager().getModules().size());
        }
        else
        {
            context.sendTranslated(NEUTRAL, "Reloading the whole server... this may take some time.");
            // pre-translate to avoid a NPE
            Locale locale = context.getSender().getLocale();
            long time = System.currentTimeMillis();
            this.core.getServer().reload();
            // TODO NPE here fix me!!!
            context.sendMessage(this.core.getI18n().translate(locale, POSITIVE, "The reload is completed after {amount} seconds", MILLISECONDS.toSeconds(System.currentTimeMillis() - time)));
        }
    }

    @Command(
        desc = "Changes the difficulty level of the server",
        indexed = @Grouped(req = false, value = @Indexed("difficulty")),
        params = @Param(names = {"world", "w", "in"}, type = World.class, completer = WorldCompleter.class)
    )
    public void difficulty(ParameterizedContext context)
    {
        CommandSender sender = context.getSender();
        World world = context.getParam("world", null);
        if (world == null)
        {
            if (sender instanceof User)
            {
                world = ((User)sender).getWorld();
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You have to specify a world!");
                return;
            }
        }
        if (context.hasArg(0))
        {
            Difficulty difficulty = null;
            Integer difficultyLevel = context.getArg(0, Integer.class);
            if (difficultyLevel != null)
            {
                difficulty = Difficulty.getByValue(difficultyLevel);
                if (difficulty == null)
                {
                    sender.sendTranslated(NEGATIVE, "The given difficulty level is unknown!");
                    return;
                }
            }
            if (difficulty == null)
            {
                try
                {
                    difficulty = Difficulty.valueOf(context.getString(0).toUpperCase(Locale.US));
                }
                catch (IllegalArgumentException e)
                {
                    sender.sendTranslated(NEGATIVE, "{input} is not a known difficulty!", context.getString(0));
                    return;
                }
            }
            world.setDifficulty(difficulty);
            context.sendTranslated(POSITIVE, "The difficulty has been successfully set!");
        }
        else
        {
            context.sendTranslated(NONE, "Current difficulty level: {input}", world.getDifficulty().name());
            if (this.core.getServer().isHardcore())
            {
                context.sendTranslated(NONE, "Your server has the hardcore mode enabled.");
            }
        }
    }

    @Command(
        desc = "Makes a player an operator",
        indexed = @Grouped(req = false, value = @Indexed("player")),
        flags = @Flag(name = "f", longName = "force"),
        permDefault = FALSE
    )
    public void op(ParameterizedContext context)
    {
        if (!context.hasArgs())
        {
            Set<OfflinePlayer> ops = this.core.getServer().getOperators();
            if (ops.isEmpty())
            {
                context.sendTranslated(NEUTRAL, "There are currently no operators!");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "The following users are operators:");
                context.sendMessage(" ");
                final CommandSender sender = context.getSender();
                final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SHORT, sender.getLocale());
                for (OfflinePlayer player : ops)
                {
                    context.sendMessage(" - " + BRIGHT_GREEN + player.getName() + WHITE + " (" + sender.getTranslation(
                        NONE, "Last seen: {input#date}", dateFormat
                        .format(new Date(player.getLastPlayed()))) + ")");
                }
            }
            return;
        }
        User user = this.core.getUserManager().findExactUser(context.getString(0));
        if (user == null && !context.hasFlag("f"))
        {
            context.sendTranslated(NEGATIVE, "{user} has never played on this server!", context.getString(0));
            context.sendTranslated(NEGATIVE, "If you still want to op him, use the -force flag.");
            return;
        }

        OfflinePlayer offlinePlayer = user;
        if (offlinePlayer == null)
        {
            offlinePlayer = this.core.getServer().getOfflinePlayer(context.getString(0));
        }
        if (offlinePlayer.isOp())
        {
            context.sendTranslated(NEUTRAL, "{user} is already an operator.", offlinePlayer);
            return;
        }
        offlinePlayer.setOp(true);
        if (offlinePlayer.isOnline())
        {
            user = this.core.getUserManager().getExactUser(offlinePlayer.getUniqueId());
            if (user != null)
            {
                user.sendTranslated(POSITIVE, "You were opped by {sender}", context.getSender());
            }
        }
        context.sendTranslated(POSITIVE, "{user} is now an operator!", offlinePlayer);

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser.getUniqueId().equals(offlinePlayer.getUniqueId()) || onlineUser == context.getSender() || !core.perms().COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated(NEUTRAL, "User {user} has been opped by {sender}!", offlinePlayer, context.getSender());
        }

        this.core.getLog().info("Player {} has been opped by {}", offlinePlayer.getName(), context.getSender().getName());
    }

    @Command(desc = "Revokes the operator status of a player", permDefault = FALSE,
             indexed = @Grouped(req = false, value = @Indexed("player")))
    public void deop(CommandContext context)
    {
        CommandSender sender = context.getSender();
        OfflinePlayer offlinePlayer;
        if (context.hasArg(0))
        {
            offlinePlayer = context.getArg(0, OfflinePlayer.class);
        }
        else
        {
            context.sendTranslated(NEGATIVE, "You have to specify an operator!");
            return;
        }

        if (!sender.getName().equals(offlinePlayer.getName()) && !core.perms().COMMAND_DEOP_OTHER.isAuthorized(sender))
        {
            sender.sendTranslated(NEGATIVE, "You are not allowed to deop others!");
            return;
        }

        if (!offlinePlayer.isOp())
        {
            sender.sendTranslated(NEGATIVE, "The player you tried to deop is not an operator.");
            return;
        }
        offlinePlayer.setOp(false);
        if (offlinePlayer.isOnline())
        {
            User user = this.core.getUserManager().getExactUser(offlinePlayer.getUniqueId());
            if (user != null)
            {
                user.sendTranslated(POSITIVE, "You were deopped by {user}.", context.getSender());
            }
        }
        context.sendTranslated(POSITIVE, "{user} is no longer an operator!", offlinePlayer);

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser.getUniqueId().equals(offlinePlayer.getUniqueId()) || onlineUser == context.getSender() || !core.perms().COMMAND_DEOP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated(POSITIVE, "User {user} has been opped by {sender}!", offlinePlayer, context.getSender());
        }

        this.core.getLog().info("Player {} has been deopped by {}", offlinePlayer.getName(), context.getSender().getName());
    }

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext context)
    {
        Plugin[] plugins = this.core.getServer().getPluginManager().getPlugins();
        Collection<Module> modules = this.core.getModuleManager().getModules();

        context.sendTranslated(NEUTRAL, "There are {amount} plugins and {amount} CubeEngine modules loaded:", plugins.length, modules.size());
        context.sendMessage(" ");
        context.sendMessage(" - " + BRIGHT_GREEN + core.getName() + RESET + " (" + context.getCore().getVersion() + ")");

        for (Module m : modules)
        {
            context.sendMessage("   - " + (m.isEnabled() ? BRIGHT_GREEN : RED) + m.getName() + RESET + " (" + m.getVersion() + ")");
        }

        for (Plugin p : plugins)
        {
            if (p != this.core)
            {
                context.sendMessage(" - " + (p.isEnabled() ? BRIGHT_GREEN : RED) + p.getName() + RESET + " (" + p.getDescription().getVersion() + ")");
            }
        }
    }

    // integrate /saveoff and /saveon and provide aliases
    @Command(names = {"save-all", "saveall"},
             indexed = @Grouped(req = false, value = @Indexed("world")),
             desc = "Saves all or a specific world to disk.")
    public void saveall(CommandContext context)
    {
        if (context.hasArg(0))
        {
            World world = context.getArg(0, World.class);
            if (world == null)
            {
                context.sendTranslated(NEGATIVE, "The given world was not found!");
                return;
            }
            
            context.sendTranslated(NEUTRAL, "Saving...");
            world.save();
            for(Player player : world.getPlayers())
            {
                player.saveData();
            }
            context.sendTranslated(POSITIVE, "World {world} has been saved to disk!", world);
        }
        else
        {
            context.sendTranslated(NEUTRAL, "Saving...");
            Profiler.startProfiling("save-worlds");
            for (World world : this.core.getServer().getWorlds())
            {
                world.save();
            }
            this.core.getServer().savePlayers();
            context.sendTranslated(POSITIVE, "All worlds have been saved to disk!");
            context.sendTranslated(POSITIVE, "The saving took {integer#time} milliseconds.", Profiler.endProfiling("save-worlds", MILLISECONDS));
        }
    }

    @Command(desc = "Displays the version of the server or a given plugin",
             indexed = @Grouped(req = false, value = @Indexed("plugin")),
             flags = @Flag(name = "s", longName = "source"))
    public void version(ParameterizedContext context)
    {
        Server server = this.core.getServer();
        if (context.hasArgs())
        {
            if (!core.perms().COMMAND_VERSION_PLUGINS.isAuthorized(context.getSender()))
            {
                throw new PermissionDeniedException(core.perms().COMMAND_VERSION_PLUGINS);
            }
            Plugin plugin = server.getPluginManager().getPlugin(context.getString(0));
            if (plugin == null)
            {
                context.sendTranslated(NEGATIVE, "The given plugin doesn't seem to be loaded, have you typed it correctly (casing does matter)?");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "{name#plugin} is currently running in version {input#version:color=INDIGO}.", plugin.getName(), plugin.getDescription().getVersion());
                context.sendMessage(" ");
                context.sendTranslated(NEUTRAL, ChatFormat.UNDERLINE + "Plugin information:");
                context.sendMessage(" ");
                if (plugin instanceof Core)
                {
                    showSourceVersion(context, core.getSourceVersion());
                }
                context.sendTranslated(NEUTRAL, "Description: {input}", plugin.getDescription().getDescription() == null ? "NONE" : plugin.getDescription().getDescription());
                context.sendTranslated(NEUTRAL, "Website: {input}", plugin.getDescription().getWebsite() == null ? "NONE" : plugin.getDescription().getWebsite());
                context.sendTranslated(NEUTRAL, "Authors:");
                for (String author : plugin.getDescription().getAuthors())
                {
                    context.sendMessage("   - " + ChatFormat.AQUA + author);
                }
            }
        }
        else
        {
            context.sendTranslated(NEUTRAL, "This server is running {name#server} in version {input#version:color=INDIGO}", server.getName(), server.getVersion());
            context.sendTranslated(NEUTRAL, "Bukkit API {text:version\\::color=WHITE} {input#version:color=INDIGO}", server.getBukkitVersion());
            context.sendMessage(" ");
            context.sendTranslated(NEUTRAL, "Expanded and improved by {text:CubeEngine:color=BRIGHT_GREEN} version {input#version:color=INDIGO}", context.getCore().getVersion().toString());
            showSourceVersion(context, core.getSourceVersion());
        }
    }

    private static final String SOURCE_LINK = "https://github.com/CubeEngineDev/CubeEngine/tree/";
    public static void showSourceVersion(ParameterizedContext context, String sourceVersion)
    {
        if (context.hasFlag("s") && sourceVersion != null)
        {
            final String commit = sourceVersion.substring(sourceVersion.lastIndexOf('-') + 1, sourceVersion.length() - 32);
            context.sendTranslated(POSITIVE, "Source Version: {input}", sourceVersion);
            context.sendTranslated(POSITIVE, "Source link: {input}", SOURCE_LINK + commit);
        }
    }

    public static class WhitelistCommand extends ContainerCommand
    {
        private final BukkitCore core;

        public WhitelistCommand(BukkitCore core)
        {
            super(core.getModuleManager().getCoreModule(), "whitelist", "Allows you to manage your whitelist");
            this.core = core;
            this.delegateChild("list");
        }

        @Command(desc = "Adds a player to the whitelist.",
                 indexed = @Grouped(@Indexed("player")))
        public void add(CommandContext context)
        {
            if (!context.hasArgs())
            {
                context.sendTranslated(NEGATIVE, "You have to specify the player to add to the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (player.isWhitelisted())
            {
                context.sendTranslated(NEUTRAL, "{user} is already whitelisted.", player);
                return;
            }

            player.setWhitelisted(true);
            context.sendTranslated(POSITIVE, "{user} is now whitelisted.", player);
        }

        @Command(names = {
        "remove", "rm"
        }, desc = "Removes a player from the whitelist.",
                 indexed = @Grouped(@Indexed("player")))
        public void remove(CommandContext context)
        {
            if (!context.hasArgs())
            {
                context.sendTranslated(NEGATIVE, "You have to specify the player to remove from the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (!player.isWhitelisted())
            {
                context.sendTranslated(NEUTRAL, "{user} is not whitelisted.", player);
                return;
            }

            player.setWhitelisted(false);
            context.sendTranslated(POSITIVE, "{user} is not whitelisted anymore.", player.getName());
        }

        @Command(desc = "Lists all the whitelisted players")
        public void list(CommandContext context)
        {
            Set<OfflinePlayer> whitelist = this.core.getServer ().getWhitelistedPlayers();
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendTranslated(NEUTRAL, "The whitelist is currently disabled.");
            }
            else
            {
                context.sendTranslated(POSITIVE, "The whitelist is enabled!.");
            }
            context.sendMessage(" ");
            if (whitelist.isEmpty())
            {
                context.sendTranslated(NEUTRAL, "There are currently no whitelisted players!");
            }
            else
            {
                context.sendTranslated(NEUTRAL, "The following players are whitelisted:");
                // TODO show op
                context.sendMessage(" ");
                for (OfflinePlayer player : whitelist)
                {
                    context.sendMessage(" - " + player.getName());
                }
            }
        }

        @Command(desc = "Enables the whitelisting")
        public void on(CommandContext context)
        {
            if (this.core.getServer().hasWhitelist())
            {
                context.sendTranslated(NEGATIVE, "The whitelist is already enabled!");
                return;
            }
            this.core.getServer().setWhitelist(true);
            BukkitUtils.saveServerProperties();
            context.sendTranslated(POSITIVE, "The whitelist is now enabled.");
        }

        @Command(desc = "Disables the whitelisting")
        public void off(CommandContext context)
        {
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendTranslated(NEGATIVE, "The whitelist is already disabled!");
                return;
            }
            this.core.getServer().setWhitelist(false);
            BukkitUtils.saveServerProperties();
            context.sendTranslated(POSITIVE, "The whitelist is now disabled.");
        }

        @Command(desc = "Wipes the whitelist completely")
        public void wipe(CommandContext context)
        {
            if (context.isSender(User.class))
            {
                context.sendTranslated(NEGATIVE, "This command is too dangerous for users!");
                return;
            }

            BukkitUtils.wipeWhiteliste();
            context.sendTranslated(POSITIVE, "The whitelist was successfully wiped!");
        }
    }
}
