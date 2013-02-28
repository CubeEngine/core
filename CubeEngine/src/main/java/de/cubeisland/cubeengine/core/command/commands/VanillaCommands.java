package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CorePerms;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.exception.PermissionDeniedException;
import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.parameterized.completer.WorldCompleter;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.core.util.Profiler;
import org.bukkit.Difficulty;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static de.cubeisland.cubeengine.core.command.ArgBounds.NO_MAX;
import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.logger.LogLevel.NOTICE;
import static de.cubeisland.cubeengine.core.permission.PermDefault.FALSE;
import static de.cubeisland.cubeengine.core.util.ChatFormat.*;
import static de.cubeisland.cubeengine.core.util.Misc.arr;
import static java.text.DateFormat.SHORT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

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
        usage = "[message]"
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
            context.getCore().getUserManager().broadcastStatus("core", message);
        }

        if (context.hasFlag("m"))
        {
            context.sendMessage("core", "&eReloading the modules...");
            this.core.getModuleManager().reloadModules();
            context.sendMessage("core", "&aSuccessfully reloaded %d modules!");
        }
        else
        {
            context.sendMessage("core", "&eReloading the whole server... (this may take some time)");
            // pre-translate to avoid a NPE
            final String preTranslated = this.core.getI18n().translate(context.getSender().getLanguage(), "core", "&aThe reload is completed after %d seconds");

            Profiler.startProfiling("reload_server");
            this.core.getServer().reload();
            context.sendMessage(String.format(preTranslated, Profiler.endProfiling("reload_server", SECONDS)));
        }
    }

    @Command(
        desc = "Changes the difficulty level of the server",
        usage = "[difficulty] {world <world>}",
        params = @Param(names = {"world", "w"}, type = World.class, completer = WorldCompleter.class)
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
                context.sendMessage("core", "&cYou have to specify a world!");
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
                    sender.sendMessage("core", "&cThe given difficulty level is unknown!");
                    return;
                }
            }
            if (difficulty == null)
            {
                difficulty = Difficulty.valueOf(context.getString(0).toUpperCase(Locale.ENGLISH));
                if (difficulty == null)
                {
                    sender.sendMessage("core", "&c'%s' is not a known difficulty!", context.getString(0));
                    return;
                }
            }
            world.setDifficulty(difficulty);
            context.sendMessage("&aThe difficulty has been successfully set!");
        }
        else
        {
            context.sendMessage("core", "The current difficulty level: %s", _("core", world.getDifficulty().toString().toLowerCase(Locale.ENGLISH)));
            if (this.core.getServer().isHardcore())
            {
                context.sendMessage("core", "Your server has the hardcore mode enabled.");
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
                context.sendMessage("core", "&eThere are currently no operators!");
            }
            else
            {
                context.sendMessage("core", "The following users are operators:");
                context.sendMessage(" ");
                DateFormat dateFormat = SimpleDateFormat.getDateInstance(SHORT, Locale.ENGLISH); // TODO replace with sender's locale
                for (OfflinePlayer player : ops)
                {
                    context.sendMessage(" - " + BRIGHT_GREEN + player.getName() + WHITE + " (" + _(context.getSender(), "core", "Last seen: %s", arr(dateFormat.format(new Date(player.getLastPlayed())))) + ")");
                }
            }
            return;
        }
        User user = this.core.getUserManager().getUser(context.getString(0), false);
        if (user == null && !context.hasFlag("f"))
        {
            context.sendMessage("core", "&cThe given player has never played on this server!");
            context.sendMessage("core", "&cIf you still want to op him, use the -force flag.");
            return;
        }

        OfflinePlayer offlinePlayer = user;
        if (offlinePlayer == null)
        {
            offlinePlayer = this.core.getServer().getOfflinePlayer(context.getString(0));
        }
        if (offlinePlayer.isOp())
        {
            context.sendMessage("core", "&eThe given player is already an operator.");
            return;
        }
        offlinePlayer.setOp(true);
        user = this.core.getUserManager().getUser(offlinePlayer.getName(), false);
        if (user != null)
        {
            user.sendMessage("core", "&aYou were opped by &2%s&a.", context.getSender().getName());
        }
        context.sendMessage("core", "&2%s &ais now an operator!", offlinePlayer.getPlayer().getName());

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser == user || onlineUser == context.getSender() || !CorePerms.COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendMessage("core", "&eUser &2%s &ehas been opped by &2%s&e!", offlinePlayer.getName(), context.getSender().getName());
        }

        this.core.getCoreLogger().log(NOTICE, "Player {0} has been opped by {1}", arr(offlinePlayer.getName(), context.getSender().getName()));
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
            context.sendMessage("core", "&cYou have to specify an operator!");
            return;
        }

        if (!sender.equals(offlinePlayer) && !CorePerms.COMMAND_DEOP_OTHER.isAuthorized(sender))
        {
            sender.sendMessage("core", "&cYou are not allowed to deop others!");
            return;
        }

        if (!offlinePlayer.isOp())
        {
            sender.sendMessage("core", "&cThe player you tried to deop is not an operator.");
            return;
        }
        offlinePlayer.setOp(false);
        User user = this.core.getUserManager().getUser(offlinePlayer.getName(), false);
        if (user != null)
        {
            user.sendMessage("core", "&aYou were deopped by &2%s&a.", context.getSender().getName());
        }
        context.sendMessage("core", "&2%s &ais no operator anymore!", offlinePlayer.getPlayer().getName());

        for (User onlineUser : this.core.getUserManager().getOnlineUsers())
        {
            if (onlineUser == user || onlineUser == context.getSender() || !CorePerms.COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendMessage("core", "&eUser &2%s&a has been opped by &2%s&a!", offlinePlayer.getName(), context.getSender().getName());
        }

        this.core.getCoreLogger().log(NOTICE, "Player {0} has been deopped by {1}", arr(offlinePlayer.getName(), context.getSender().getName()));
    }

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext context)
    {
        Plugin[] plugins = this.core.getServer().getPluginManager().getPlugins();
        Collection<Module> modules = this.core.getModuleManager().getModules();

        context.sendMessage("core", "There are %d plugins and %d CubeEngine modules loaded:", plugins.length, modules.size());
        context.sendMessage(" ");
        context.sendMessage(" - " + BRIGHT_GREEN + core.getName() + RESET + " (r" + Core.REVISION + ")");

        for (Module m : modules)
        {
            context.sendMessage("   - " + (m.isEnabled() ? BRIGHT_GREEN : RED) + m.getName() + RESET + " (r" + m.getRevision() + ")");
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
                context.sendMessage("core", "&cThe given world was not found!");
                return;
            }
            context.sendMessage("core", "&eSaving...");
            world.save();
            context.sendMessage("core", "&aWorld '%s' has been saved to disk!", world.getName());
        }
        else
        {
            context.sendMessage("core", "&eSaving...");
            Profiler.startProfiling("save-worlds");
            for (World world : this.core.getServer().getWorlds())
            {
                world.save();
            }
            context.sendMessage("core", "&aAll worlds have been saved to disk!");
            context.sendMessage("core", "&aThe saving took %d milliseconds.", Profiler.endProfiling("save-worlds", MILLISECONDS));
        }
    }

    @Command(desc = "Displays the version of the server or a given plugin", usage = "[plugin]", max = 1)
    public void version(CommandContext context)
    {
        Server server = this.core.getServer();
        if (context.hasArgs())
        {
            if (!CorePerms.COMMAND_VERSION_PLUGINS.isAuthorized(context.getSender()))
            {
                throw new PermissionDeniedException();
            }
            Plugin plugin = server.getPluginManager().getPlugin(context.getString(0));
            if (plugin == null)
            {
                context.sendMessage("core", "&cThe given plugin doesn't seem to be loaded, have you type it correctly (casing does matter)?");
                return;
            }
            else
            {
                context.sendMessage(" ");
                context.sendMessage("core", "&e%s&f is currently running in version &9%s&f.", plugin.getName(), plugin.getDescription().getVersion());
                context.sendMessage(" ");
                context.sendMessage("core", "&nPlugin information:");
                context.sendMessage(" ");
                context.sendMessage("core", "Description: &6%s", plugin.getDescription().getDescription());
                context.sendMessage("core", "Website: &6%s", plugin.getDescription().getWebsite());
                context.sendMessage("core", "Authors:");
                for (String author : plugin.getDescription().getAuthors())
                {
                    context.sendMessage("   - " + ChatFormat.AQUA + author);
                }
            }
        }
        else
        {
            if (server.getName().equalsIgnoreCase("craftbukkit"))
            {
                context.sendMessage("core", "This server is unfortunately running &c%s&r in version &9%s", server.getName(), server.getVersion());
                context.sendMessage("core", "&kTrololol EvilSeph&r!");
            }
            else
            {
                context.sendMessage("core", "This server is running &e%s&r in version &9%s", server.getName(), server.getVersion());
            }
            context.sendMessage("core", "&eBukkit API&r Version: &9%s", server.getBukkitVersion());
            context.sendMessage(" ");
            context.sendMessage("core", "Expanded and improved by &aCubeEngine&r revision &9%s", Core.REVISION);
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
                context.sendMessage("core", "&cYou have to specify the player to add to the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (player.isWhitelisted())
            {
                context.sendMessage("core", "&eThe given player is already whitelisted.");
                return;
            }

            player.setWhitelisted(true);
            context.sendMessage("core", "&2%s&a is now whitelisted.",player.getName());
        }

        @Command(names = {
        "remove", "rm"
        }, desc = "Removes a player from the whitelist.", usage = "<player>", max = 1)
        public void remove(CommandContext context)
        {
            if (!context.hasArgs())
            {
                context.sendMessage("core", "&cYou have to specify the player to remove from the whitelist!");
                return;
            }
            final OfflinePlayer player = context.getArg(0, OfflinePlayer.class);
            if (!player.isWhitelisted())
            {
                context.sendMessage("core", "&eThe given player is not whitelisted.");
                return;
            }

            player.setWhitelisted(false);
            context.sendMessage("core", "&2%s&a is not whitelisted anymore.",player.getName());
        }

        @Command(desc = "Lists all the whitelisted players")
        public void list(CommandContext context)
        {
            Set<OfflinePlayer> whitelist = this.core.getServer ().getWhitelistedPlayers();
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendMessage("core", "&eThe whitelist is currently disabled.");
                context.sendMessage(" ");
            }
            if (whitelist.isEmpty())
            {
                context.sendMessage("core", "&eThere are currently no whitelisted players!");
            }
            else
            {
                context.sendMessage("core", "The following players are whitelisted:");
                context.sendMessage(" ");
                for (OfflinePlayer player : whitelist)
                {
                    context.sendMessage(" - " + player.getName());
                }
            }
        }

        @Command(desc = "Enables the whiltelisting")
        public void on(CommandContext context)
        {
            if (this.core.getServer().hasWhitelist())
            {
                context.sendMessage("core", "&cThe whitelist is already enabled!");
                return;
            }
            this.core.getServer().setWhitelist(true);
            context.sendMessage("core", "&aThe whitelist is now enabled.");
        }

        @Command(desc = "Disables the whiltelisting")
        public void off(CommandContext context)
        {
            if (!this.core.getServer().hasWhitelist())
            {
                context.sendMessage("core", "&cThe whitelist is already disabled!");
                return;
            }
            this.core.getServer().setWhitelist(false);
            context.sendMessage("core", "&aThe whitelist is now disabled.");
        }
    }
}
