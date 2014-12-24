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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.Difficulty;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.alias.Alias;
import de.cubeisland.engine.command.filter.Restricted;
import de.cubeisland.engine.command.methodic.Command;
import de.cubeisland.engine.command.methodic.Flag;
import de.cubeisland.engine.command.methodic.Flags;
import de.cubeisland.engine.command.methodic.Param;
import de.cubeisland.engine.command.methodic.Params;
import de.cubeisland.engine.command.methodic.parametric.Label;
import de.cubeisland.engine.command.methodic.parametric.Named;
import de.cubeisland.engine.command.methodic.parametric.Optional;
import de.cubeisland.engine.command.parameter.TooFewArgumentsException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandContainer;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.annotation.CommandPermission;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.command.parameter.Parameter.INFINITE;
import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.util.ChatFormat.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class VanillaCommands
{
    private static final String SOURCE_LINK = "https://github.com/CubeEngineDev/CubeEngine/tree/";
    private final BukkitCore core;
    private final UserManager um;

    public VanillaCommands(BukkitCore core)
    {
        this.core = core;
        this.um = core.getUserManager();
    }

    public static void showSourceVersion(CommandContext context, String sourceVersion)
    {
        if (sourceVersion != null)
        {
            if (sourceVersion.contains("-") && sourceVersion.length() > 40)
            {
                final String commit = sourceVersion.substring(sourceVersion.lastIndexOf('-') + 1,
                                                              sourceVersion.length() - 32);
                context.sendTranslated(POSITIVE, "Source Version: {input}", sourceVersion);
                context.sendTranslated(POSITIVE, "Source link: {input}", SOURCE_LINK + commit);
            }
            else
            {
                context.sendTranslated(POSITIVE, "Source Version: unknown");
            }
        }
    }

    @Command(alias = {"shutdown", "killserver", "quit"}, desc = "Shuts down the server")
    @Params(positional = @Param(label = "message", req = false, greed = INFINITE))
    public void stop(CommandContext context)
    {
        String message = context.getStrings(0);
        if (message == null || message.isEmpty())
        {
            message = this.core.getServer().getShutdownMessage();
        }
        message = ChatFormat.parseFormats(message);

        um.kickAll(message);
        this.core.getServer().shutdown();
    }

    @Command(desc = "Reloads the server.")
    @Params(positional = @Param(label = "message", req = false, greed = INFINITE))
    @Flags(@Flag(name = "m", longName = "modules"))
    public void reload(CommandContext context)
    {
        final String message = context.getStrings(0);
        if (message != null)
        {
            um.broadcastMessageWithPerm(NONE, message, core.perms().COMMAND_RELOAD_NOTIFY);
        }

        if (context.hasFlag("m"))
        {
            context.sendTranslated(NEUTRAL, "Reloading the modules...");
            this.core.getModuleManager().reloadModules();
            context.sendTranslated(POSITIVE, "Successfully reloaded {amount} modules!",
                                   this.core.getModuleManager().getModules().size());
        }
        else
        {
            context.sendTranslated(NEUTRAL, "Reloading the whole server... this may take some time.");
            // pre-translate to avoid a NPE
            Locale locale = context.getSource().getLocale();
            long time = System.currentTimeMillis();
            I18n i18n = this.core.getI18n();
            this.core.getServer().reload();
            context.sendMessage(i18n.translate(locale, POSITIVE, "The reload is completed after {amount} seconds",
                                               MILLISECONDS.toSeconds(System.currentTimeMillis() - time)));
        }
    }

    @Command(desc = "Changes the difficulty level of the server")
    public void difficulty(CommandContext context, @Optional @Label("difficulty") Difficulty difficulty, @Named({"world", "w", "in"}) World world)
    {
        if (world == null)
        {
            if (context.isSource(User.class))
            {
                world = ((User)context.getSource()).getWorld();
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You have to specify a world");
                throw new TooFewArgumentsException();
            }
        }
        if (difficulty != null)
        {
            world.setDifficulty(difficulty);
            context.sendTranslated(POSITIVE, "The difficulty has been successfully set!");
            return;
        }
        context.sendTranslated(POSITIVE, "Current difficulty level: {input}", world.getDifficulty().name());
        if (this.core.getServer().isHardcore())
        {
            context.sendTranslated(POSITIVE, "Your server has the hardcore mode enabled.");
        }
    }

    @Command(desc = "Makes a player an operator")
    @Params(positional = @Param(label = "player", type = OfflinePlayer.class, req = false))
    @Flags(@Flag(name = "f", longName = "force"))
    @CommandPermission(permDefault = FALSE)
    public void op(CommandContext context)
    {
        if (context.hasPositional())
        {
            OfflinePlayer player = context.get(0);
            if (!(player.hasPlayedBefore() || player.isOnline()) && !context.hasFlag("f"))
            {
                context.sendTranslated(NEGATIVE, "{user} has never played on this server!", context.get(0));
                context.sendTranslated(NEGATIVE, "If you still want to op him, use the -force flag.");
                return;
            }
            if (player.isOp())
            {
                context.sendTranslated(NEUTRAL, "{user} is already an operator.", player);
                return;
            }
            player.setOp(true);
            if (player.isOnline())
            {
                um.getExactUser(player.getUniqueId()).sendTranslated(POSITIVE, "You were opped by {sender}",
                                                                     context.getSource());
            }
            context.sendTranslated(POSITIVE, "{user} is now an operator!", player);

            for (User onlineUser : um.getOnlineUsers())
            {
                if (onlineUser.getUniqueId().equals(player.getUniqueId()) ||
                    onlineUser.getUniqueId().equals(context.getSource().getUniqueId()) ||
                    !core.perms().COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
                {
                    continue;
                }
                onlineUser.sendTranslated(NEUTRAL, "User {user} has been opped by {sender}!", player,
                                          context.getSource());
            }

            this.core.getLog().info("Player {} has been opped by {}", player.getName(), context.getSource().getName());
            return;
        }
        // else list operators
        Set<OfflinePlayer> ops = this.core.getServer().getOperators();
        if (ops.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "There are currently no operators!");
            return;
        }
        context.sendTranslated(NEUTRAL, "The following users are operators:");
        context.sendMessage(" ");
        for (OfflinePlayer player : ops)
        {
            context.sendTranslated(POSITIVE, " - {user} (Last seen: {date:notime})", player, new Date(player.getLastPlayed()));
            // TODO locale dependent formatter for date
        }
    }

    @Command(desc = "Revokes the operator status of a player")
    @Params(positional = @Param(label = "player", type = OfflinePlayer.class))
    @CommandPermission(permDefault = FALSE)
    public void deop(CommandContext context)
    {
        OfflinePlayer offlinePlayer = context.get(0);
        if (!context.getSource().getUniqueId().equals(offlinePlayer.getUniqueId()))
        {
            context.ensurePermission(core.perms().COMMAND_DEOP_OTHER);
        }
        if (!offlinePlayer.isOp())
        {
            context.sendTranslated(NEGATIVE, "The player you tried to deop is not an operator.");
            return;
        }
        offlinePlayer.setOp(false);
        if (offlinePlayer.isOnline())
        {
            um.getExactUser(offlinePlayer.getUniqueId()).sendTranslated(POSITIVE, "You were deopped by {user}.",
                                                                        context.getSource());
        }
        context.sendTranslated(POSITIVE, "{user} is no longer an operator!", offlinePlayer);

        for (User onlineUser : um.getOnlineUsers())
        {
            if (onlineUser.getUniqueId().equals(offlinePlayer.getUniqueId()) ||
                onlineUser.getUniqueId().equals(context.getSource().getUniqueId()) ||
                !core.perms().COMMAND_DEOP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated(POSITIVE, "User {user} has been deopped by {sender}!", offlinePlayer,
                                      context.getSource());
        }

        this.core.getLog().info("Player {} has been deopped by {}", offlinePlayer.getName(),
                                context.getSource().getName());
    }

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext context)
    {
        Plugin[] plugins = this.core.getServer().getPluginManager().getPlugins();
        Collection<Module> modules = this.core.getModuleManager().getModules();

        context.sendTranslated(NEUTRAL, "There are {amount} plugins and {amount} CubeEngine modules loaded:",
                               plugins.length, modules.size());
        context.sendMessage(" ");
        context.sendMessage(
            " - " + BRIGHT_GREEN + core.getName() + RESET + " (" + context.getCore().getVersion() + ")");

        for (Module m : modules)
        {
            context.sendMessage(
                "   - " + (m.isEnabled() ? BRIGHT_GREEN : RED) + m.getName() + RESET + " (" + m.getVersion() + ")");
        }

        for (Plugin p : plugins)
        {
            if (p != this.core)
            {
                context.sendMessage(" - " + (p.isEnabled() ? BRIGHT_GREEN : RED) + p.getName() + RESET + " ("
                                        + p.getDescription().getVersion() + ")");
            }
        }
    }

    // integrate /saveoff and /saveon and provide aliases
    @Alias(value = "save-all")
    @Command(desc = "Saves all or a specific world to disk.")
    @Params(positional = @Param(label = "world", type = World.class, req = false))
    public void saveall(CommandContext context)
    {
        context.sendTranslated(NEUTRAL, "Saving...");
        if (context.hasPositional(0))
        {
            World world = context.get(0);
            world.save();
            for (Player player : world.getPlayers())
            {
                player.saveData();
            }
            context.sendTranslated(POSITIVE, "World {world} has been saved to disk!", world);
            return;
        }
        Profiler.startProfiling("save-worlds");
        for (World world : this.core.getServer().getWorlds())
        {
            world.save();
        }
        this.core.getServer().savePlayers();
        context.sendTranslated(POSITIVE, "All worlds have been saved to disk!");
        context.sendTranslated(POSITIVE, "The saving took {integer#time} milliseconds.", Profiler.endProfiling(
            "save-worlds", MILLISECONDS));
    }

    @Command(desc = "Displays the version of the server or a given plugin")
    @Params(positional = @Param(label = "plugin", req = false))
    @Flags(@Flag(name = "s", longName = "source"))
    public void version(CommandContext context)
    {
        Server server = this.core.getServer();
        if (context.hasPositional(0))
        {
            context.ensurePermission(core.perms().COMMAND_VERSION_PLUGINS);
            Plugin plugin = server.getPluginManager().getPlugin(context.getString(0));
            if (plugin == null)
            {
                List<Plugin> plugins = new ArrayList<>();
                for (Plugin p : server.getPluginManager().getPlugins())
                {
                    if (p.getName().toLowerCase().startsWith(context.getString(0).toLowerCase()))
                    {
                        plugins.add(p);
                    }
                }
                context.sendTranslated(NEGATIVE, "The given plugin doesn't seem to be loaded, have you typed it correctly (casing does matter)?");
                if (!plugins.isEmpty())
                {
                    context.sendTranslated(NEGATIVE, "You might want to try one of these:");
                    for (Plugin p : plugins)
                    {
                        context.sendMessage(" - " + p.getName());
                    }
                }
                return;
            }
            context.sendTranslated(NEUTRAL,
                                   "{name#plugin} is currently running in version {input#version:color=INDIGO}.",
                                   plugin.getName(), plugin.getDescription().getVersion());
            context.sendMessage(" ");
            context.sendTranslated(NEUTRAL.and(UNDERLINE), "Plugin information:");
            context.sendMessage(" ");
            if (plugin instanceof Core && context.hasFlag("f"))
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
            return;
        }
        context.sendTranslated(NEUTRAL, "This server is running {name#server} in version {input#version:color=INDIGO}",
                               server.getName(), server.getVersion());
        context.sendTranslated(NEUTRAL, "Bukkit API {text:version\\::color=WHITE} {input#version:color=INDIGO}",
                               server.getBukkitVersion());
        context.sendMessage(" ");
        context.sendTranslated(NEUTRAL, "Expanded and improved by {text:CubeEngine:color=BRIGHT_GREEN} version {input#version:color=INDIGO}",
                               context.getCore().getVersion().toString());
        if (context.hasFlag("f"))
        {
            showSourceVersion(context, core.getSourceVersion());
        }
    }

    @Command(name = "whitelist", desc = "Allows you to manage your whitelist")
    public static class WhitelistCommand extends CommandContainer
    {
        private final BukkitCore core;

        public WhitelistCommand(BukkitCore core)
        {
            super(core.getModuleManager().getCoreModule());
            this.core = core;
        }

        @Override
        protected boolean selfExecute(CommandInvocation invocation)
        {
            if (invocation.isConsumed())
            {
                return this.getCommand("list").execute(invocation);
            }
            else if (invocation.tokens().size() - invocation.consumed() == 1)
            {
                return this.getCommand("add").execute(invocation);
            }
            return super.execute(invocation);
        }

        @Command(desc = "Adds a player to the whitelist.")
        @Params(positional = @Param(label = "player", type = OfflinePlayer.class))
        public void add(CommandContext context)
        {
            if (!context.hasPositional())
            {
                context.sendTranslated(NEGATIVE, "You have to specify the player to add to the whitelist!");
                return;
            }
            final OfflinePlayer player = context.get(0);
            if (player.isWhitelisted())
            {
                context.sendTranslated(NEUTRAL, "{user} is already whitelisted.", player);
                return;
            }
            player.setWhitelisted(true);
            context.sendTranslated(POSITIVE, "{user} is now whitelisted.", player);
        }

        @Command(alias = "rm", desc = "Removes a player from the whitelist.")
        @Params(positional = @Param(label = "player", type = OfflinePlayer.class))
        public void remove(CommandContext context)
        {
            if (!context.hasPositional())
            {
                context.sendTranslated(NEGATIVE, "You have to specify the player to remove from the whitelist!");
                return;
            }
            final OfflinePlayer player = context.get(0);
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
            Set<OfflinePlayer> whitelist = this.core.getServer().getWhitelistedPlayers();
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
                for (OfflinePlayer player : whitelist)
                {
                    context.sendMessage(" - " + player.getName());
                }
            }
            Set<OfflinePlayer> operators = this.core.getServer().getOperators();
            if (!operators.isEmpty())
            {
                context.sendTranslated(NEUTRAL, "The following players are OP and can bypass the whitelist");
                for (OfflinePlayer operator : operators)
                {
                    context.sendMessage(" - " + operator.getName());
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
        @Restricted(value = ConsoleCommandSender.class, msg = "This command is too dangerous for users!")
        public void wipe(CommandContext context)
        {
            BukkitUtils.wipeWhitelist();
            context.sendTranslated(POSITIVE, "The whitelist was successfully wiped!");
        }
    }
}
