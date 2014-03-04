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
package de.cubeisland.engine.core.command.commands;

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
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
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
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.core.util.Profiler;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.engine.core.permission.PermDefault.FALSE;
import static de.cubeisland.engine.core.util.ChatFormat.*;
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
        usage = "[message]",
        max = NO_MAX
    )
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

    @Command(desc = "Reloads the server.", usage = "[-m]", max = NO_MAX, flags = @Flag(name = "m", longName = "modules"))
    public void reload(ParameterizedContext context)
    {
        final String message = context.getStrings(0);
        if (message != null)
        {
            context.getCore().getUserManager().broadcastMessageWithPerm(message, core.perms().COMMAND_RELOAD_NOTIFY);
        }

        if (context.hasFlag("m"))
        {
            context.sendTranslated("&eReloading the modules...");
            this.core.getModuleManager().reloadModules();
            context.sendTranslated("&aSuccessfully reloaded %d modules!");
        }
        else
        {
            context.sendTranslated("&eReloading the whole server... (this may take some time)");
            // pre-translate to avoid a NPE
            final String preTranslated = context.getSender().translate("&aThe reload is completed after %d seconds");

            long time = System.currentTimeMillis();
            this.core.getServer().reload();
            context.sendMessage(String.format(preTranslated, MILLISECONDS.toSeconds(System.currentTimeMillis() - time)));
        }
    }

    @Command(
        desc = "Changes the difficulty level of the server",
        usage = "[difficulty] {world <world>}",
        max = 1,
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
                context.sendTranslated("&cYou have to specify a world!");
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
                    sender.sendTranslated("&cThe given difficulty level is unknown!");
                    return;
                }
            }
            if (difficulty == null)
            {
                difficulty = Difficulty.valueOf(context.getString(0).toUpperCase(Locale.US));
                if (difficulty == null)
                {
                    sender.sendTranslated("&c'%s' is not a known difficulty!", context.getString(0));
                    return;
                }
            }
            world.setDifficulty(difficulty);
            context.sendMessage("&aThe difficulty has been successfully set!");
        }
        else
        {
            context.sendTranslated("The current difficulty level: %s", sender.translate(world.getDifficulty().toString().toLowerCase(Locale.US)));
            if (this.core.getServer().isHardcore())
            {
                context.sendTranslated("Your server has the hardcore mode enabled.");
            }
        }
    }

    @Command(
        desc = "Makes a player an operator",
        min = 0, max = 1,
        usage = "[player] [-f]",
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
                context.sendTranslated("&eThere are currently no operators!");
            }
            else
            {
                context.sendTranslated("The following users are operators:");
                context.sendMessage(" ");
                final CommandSender sender = context.getSender();
                final DateFormat dateFormat = SimpleDateFormat.getDateInstance(SHORT, sender.getLocale());
                for (OfflinePlayer player : ops)
                {
                    context.sendMessage(" - " + BRIGHT_GREEN + player.getName() + WHITE + " (" + sender.translate("Last seen: %s", dateFormat.format(new Date(player.getLastPlayed()))) + ")");
                }
            }
            return;
        }
        User user = this.core.getUserManager().getUser(context.getString(0), false);
        if (user == null && !context.hasFlag("f"))
        {
            context.sendTranslated("&cThe given player has never played on this server!");
            context.sendTranslated("&cIf you still want to op him, use the -force flag.");
            return;
        }

        OfflinePlayer offlinePlayer = user;
        if (offlinePlayer == null)
        {
            offlinePlayer = this.core.getServer().getOfflinePlayer(context.getString(0));
        }
        if (offlinePlayer.isOp())
        {
            context.sendTranslated("&eThe given player is already an operator.");
            return;
        }
        offlinePlayer.setOp(true);
        user = this.core.getUserManager().getUser(offlinePlayer.getName(), false);
        if (user != null)
        {
            user.sendTranslated("&aYou were opped by &2%s&a.", context.getSender().getName());
        }
        context.sendTranslated("&2%s &ais now an operator!", offlinePlayer.getName());

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser == user || onlineUser == context.getSender() || !core.perms().COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated("&eUser &2%s &ehas been opped by &2%s&e!", offlinePlayer.getName(), context.getSender().getName());
        }

        this.core.getLog().info("Player {} has been opped by {}", offlinePlayer.getName(), context.getSender().getName());
    }

    @Command(
        desc = "Revokes the operator status of a player",
        usage = "{player}",
        min = 0, max = 1,
        permDefault = FALSE
    )
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
            context.sendTranslated("&cYou have to specify an operator!");
            return;
        }

        if (!sender.getName().equals(offlinePlayer.getName()) && !core.perms().COMMAND_DEOP_OTHER.isAuthorized(sender))
        {
            sender.sendTranslated("&cYou are not allowed to deop others!");
            return;
        }

        if (!offlinePlayer.isOp())
        {
            sender.sendTranslated("&cThe player you tried to deop is not an operator.");
            return;
        }
        offlinePlayer.setOp(false);
        User user = this.core.getUserManager().getUser(offlinePlayer.getName(), false);
        if (user != null)
        {
            user.sendTranslated("&aYou were deopped by &2%s&a.", context.getSender().getName());
        }
        context.sendTranslated("&2%s&a is no operator anymore!", offlinePlayer.getName());

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser == user || onlineUser == context.getSender() || !core.perms().COMMAND_DEOP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated("&aUser &2%s&a has been opped by &2%s&a!", offlinePlayer.getName(), context.getSender().getName());
        }

        this.core.getLog().info("Player {} has been deopped by {}", offlinePlayer.getName(), context.getSender().getName());
    }

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext context)
    {
        Plugin[] plugins = this.core.getServer().getPluginManager().getPlugins();
        Collection<Module> modules = this.core.getModuleManager().getModules();

        context.sendTranslated("There are %d plugins and %d CubeEngine modules loaded:", plugins.length, modules.size());
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
    @Command(names = {"save-all", "saveall"}, max = 1, desc = "Saves all or a specific world to disk.", usage = "[world]")
    public void saveall(CommandContext context)
    {
        if (context.hasArg(0))
        {
            World world = context.getArg(0, World.class);
            if (world == null)
            {
                context.sendTranslated("&cThe given world was not found!");
                return;
            }
            
            context.sendTranslated("&eSaving...");
            world.save();
            for(Player player : world.getPlayers())
            {
                player.saveData();
            }
            context.sendTranslated("&aWorld '%s' has been saved to disk!", world.getName());
        }
        else
        {
            context.sendTranslated("&eSaving...");
            Profiler.startProfiling("save-worlds");
            for (World world : this.core.getServer().getWorlds())
            {
                world.save();
            }
            this.core.getServer().savePlayers();
            context.sendTranslated("&aAll worlds have been saved to disk!");
            context.sendTranslated("&aThe saving took %d milliseconds.", Profiler.endProfiling("save-worlds", MILLISECONDS));
        }
    }

    @Command(desc = "Displays the version of the server or a given plugin", usage = "[plugin]", flags = @Flag(name = "s", longName = "source"), max = 1)
    public void version(ParameterizedContext context)
    {
        Server server = this.core.getServer();
        if (context.hasArgs())
        {
            if (!core.perms().COMMAND_VERSION_PLUGINS.isAuthorized(context.getSender()))
            {
                throw new PermissionDeniedException();
            }
            Plugin plugin = server.getPluginManager().getPlugin(context.getString(0));
            if (plugin == null)
            {
                context.sendTranslated("&cThe given plugin doesn't seem to be loaded, have you typed it correctly (casing does matter)?");
            }
            else
            {
                context.sendMessage(" ");
                context.sendTranslated("&e%s&f is currently running in version &9%s&f.", plugin.getName(), plugin.getDescription().getVersion());
                context.sendMessage(" ");
                context.sendTranslated("&nPlugin information:");
                context.sendMessage(" ");
                if (plugin instanceof Core)
                {
                    showSourceVersion(context, core.getSourceVersion());
                }
                context.sendTranslated("Description: &6%s", plugin.getDescription().getDescription());
                context.sendTranslated("Website: &6%s", plugin.getDescription().getWebsite());
                context.sendTranslated("Authors:");
                for (String author : plugin.getDescription().getAuthors())
                {
                    context.sendMessage("   - " + ChatFormat.AQUA + author);
                }
            }
        }
        else
        {
            context.sendTranslated("This server is running &e%s&r in version &9%s", server.getName(), server.getVersion());
            context.sendTranslated("&eBukkit API&r version: &9%s", server.getBukkitVersion());
            context.sendMessage(" ");
            context.sendTranslated("Expanded and improved by &aCubeEngine&r version &9%s", context.getCore().getVersion());
            showSourceVersion(context, core.getSourceVersion());
        }
    }

    private static final String SOURCE_LINK = "https://github.com/CubeEngineDev/CubeEngine/tree/";
    protected static void showSourceVersion(ParameterizedContext context, String sourceVersion)
    {
        if (context.hasFlag("s") && sourceVersion != null)
        {
            final String commit = sourceVersion.substring(sourceVersion.lastIndexOf('-') + 1, sourceVersion.length() - 32);
            context.sendTranslated("&aSource Version: &6%s", sourceVersion);
            context.sendTranslated("&aSource link: &6%s", SOURCE_LINK + commit);
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

        @Command(desc = "Adds a player to the whitelist.", usage = "<player>", max = 1)
        public void add(CommandContext context)
        {
            if (!context.hasArgs())
            {
                context.sendTranslated("&cYou have to specify the player to add to the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (player.isWhitelisted())
            {
                context.sendTranslated("&eThe given player is already whitelisted.");
                return;
            }

            player.setWhitelisted(true);
            context.sendTranslated("&2%s&a is now whitelisted.", player.getName());
        }

        @Command(names = {
        "remove", "rm"
        }, desc = "Removes a player from the whitelist.", usage = "<player>", max = 1)
        public void remove(CommandContext context)
        {
            if (!context.hasArgs())
            {
                context.sendTranslated("&cYou have to specify the player to remove from the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (!player.isWhitelisted())
            {
                context.sendTranslated("&eThe given player is not whitelisted.");
                return;
            }

            player.setWhitelisted(false);
            context.sendTranslated("&2%s&a is not whitelisted anymore.", player.getName());
        }

        @Command(desc = "Lists all the whitelisted players")
        public void list(CommandContext context)
        {
            Set<OfflinePlayer> whitelist = this.core.getServer ().getWhitelistedPlayers();
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendTranslated("&eThe whitelist is currently disabled.");
            }
            else
            {
                context.sendTranslated("&2The whitelist is enabled!.");
            }
            context.sendMessage(" ");
            if (whitelist.isEmpty())
            {
                context.sendTranslated("&eThere are currently no whitelisted players!");
            }
            else
            {
                context.sendTranslated("The following players are whitelisted:");
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
                context.sendTranslated("&cThe whitelist is already enabled!");
                return;
            }
            this.core.getServer().setWhitelist(true);
            BukkitUtils.saveServerProperties();
            context.sendTranslated("&aThe whitelist is now enabled.");
        }

        @Command(desc = "Disables the whitelisting")
        public void off(CommandContext context)
        {
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendTranslated("&cThe whitelist is already disabled!");
                return;
            }
            this.core.getServer().setWhitelist(false);
            BukkitUtils.saveServerProperties();
            context.sendTranslated("&aThe whitelist is now disabled.");
        }

        @Command(desc = "Wipes the whitelist completely")
        public void wipe(CommandContext context)
        {
            if (context.isSender(User.class))
            {
                context.sendTranslated("&cThis command is too dangerous for users!");
                return;
            }

            BukkitUtils.wipeWhiteliste();
            context.sendTranslated("&aThe whitelist was successfully wiped!");
        }
    }
}
