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
package de.cubeisland.engine.module.vanillaplus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import de.cubeisland.engine.butler.alias.Alias;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parametric.Command;
import de.cubeisland.engine.butler.parametric.Flag;
import de.cubeisland.engine.butler.parametric.Greed;
import de.cubeisland.engine.butler.parametric.Named;
import de.cubeisland.engine.butler.parametric.Optional;
import de.cubeisland.engine.modularity.core.Module;

import de.cubeisland.engine.module.service.command.CommandContext;
import de.cubeisland.engine.module.service.command.CommandSender;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.service.user.User;
import de.cubeisland.engine.module.service.user.UserManager;
import de.cubeisland.engine.module.core.util.ChatFormat;
import de.cubeisland.engine.module.core.util.Profiler;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text.Literal;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;

import static de.cubeisland.engine.butler.parameter.Parameter.INFINITE;
import static de.cubeisland.engine.module.core.util.formatter.MessageType.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class VanillaCommands
{
    private static final String SOURCE_LINK = "https://github.com/CubeEngineDev/CubeEngine/tree/";
    private final CoreModule core;
    private final UserManager um;

    public VanillaCommands(CoreModule core)
    {
        this.core = core;
        this.um = core.getModularity().start(UserManager.class);
    }

    public static void showSourceVersion(CommandSender context, String sourceVersion)
    {
        if (sourceVersion == null)
        {
            return;
        }
        if (sourceVersion.contains("-") && sourceVersion.length() > 40)
        {
            final String commit = sourceVersion.substring(sourceVersion.lastIndexOf('-') + 1,
                                                          sourceVersion.length() - 32);
            context.sendTranslated(POSITIVE, "Source Version: {input}", sourceVersion);
            context.sendTranslated(POSITIVE, "Source link: {input}", SOURCE_LINK + commit);
            return;
        }
        context.sendTranslated(POSITIVE, "Source Version: unknown");
    }

    @Command(alias = {"shutdown", "killserver", "quit"}, desc = "Shuts down the server")
    public void stop(CommandSender context, @Optional @Greed(INFINITE) String message)
    {
        if (message == null || message.isEmpty())
        {
            message = "";
            // TODO message = this.core.getGame().getServer().getShutdownMessage();
        }
        message = ChatFormat.parseFormats(message);

        @SuppressWarnings("deprecation")
        Literal literal = Texts.fromLegacy(message, '&');
        this.core.getGame().getServer().shutdown(literal);
    }

    /*
    @Command(desc = "Reloads the server.")
    public void reload(CommandSender context, @Optional @Greed(INFINITE) String message, @Flag boolean modules)
    {
        if (message != null)
        {
            um.broadcastMessageWithPerm(NONE, message, core.perms().COMMAND_RELOAD_NOTIFY);
        }

        if (modules)
        {
            context.sendTranslated(NEUTRAL, "Reloading the modules...");
            this.core.getModuleManager().reloadModules();
            context.sendTranslated(POSITIVE, "Successfully reloaded {amount} modules!",
                                   this.core.getModuleManager().getModules().size());
            return;
        }
        context.sendTranslated(NEUTRAL, "Reloading the whole server... this may take some time.");
        // pre-translate to avoid a NPE
        Locale locale = context.getLocale();
        long time = System.currentTimeMillis();
        I18n i18n = this.core.getI18n();
        this.core.getGame().getServer().reload();
        context.sendMessage(i18n.translate(locale, POSITIVE, "The reload is completed after {amount} seconds",
                                           MILLISECONDS.toSeconds(System.currentTimeMillis() - time)));
    }
    */

    @Command(desc = "Changes the difficulty level of the server")
    public void difficulty(CommandSender context, @Optional Difficulty difficulty, @Named({"world", "w", "in"}) World world)
    {
        if (world == null)
        {
            if (context instanceof User)
            {
                world = ((User)context).getWorld();
            }
            else
            {
                context.sendTranslated(NEGATIVE, "You have to specify a world");
                throw new TooFewArgumentsException();
            }
        }
        if (difficulty != null)
        {
            world.getProperties().setDifficulty(difficulty); // TODO is this saved?
            context.sendTranslated(POSITIVE, "The difficulty has been successfully set!");
            return;
        }
        context.sendTranslated(POSITIVE, "Current difficulty level: {input}", world.getDifficulty().getName());
        if (world.getProperties().isHardcore())
        {
            context.sendTranslated(POSITIVE, "The world {world} has the hardcore mode enabled.", world);
        }
    }

    /* TODO override op commands in separate module
    @Command(desc = "Makes a player an operator")
    @CommandPermission(permDefault = PermDefault.FALSE)
    public void op(CommandSender context, @Optional org.spongepowered.api.entity.player.User player, @Flag boolean force) // TODO gameprofile instead?
    {
        if (player == null)
        {
            // else list operators
            Set<org.spongepowered.api.entity.player.User> ops = this.core.getServer().getOperators();
            if (ops.isEmpty())
            {
                context.sendTranslated(NEUTRAL, "There are currently no operators!");
                return;
            }
            context.sendTranslated(NEUTRAL, "The following users are operators:");
            context.sendMessage(" ");
            for (org.spongepowered.api.entity.player.User opPlayer : ops)
            {
                context.sendTranslated(POSITIVE, " - {user} (Last seen: {date:notime})", opPlayer, opPlayer.getData(JoinData.class).get().getLastPlayed());
            }
            return;
        }
        if (!(player.getData(JoinData.class).isPresent() || player.isOnline()) && !force)
        {
            context.sendTranslated(NEGATIVE, "{user} has never played on this server!", player);
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
            um.getExactUser(player.getUniqueId()).sendTranslated(POSITIVE, "You were opped by {sender}", context);
        }
        context.sendTranslated(POSITIVE, "{user} is now an operator!", player);

        for (User onlineUser : um.getOnlineUsers())
        {
            if (onlineUser.getUniqueId().equals(player.getUniqueId()) ||
                onlineUser.getUniqueId().equals(context.getUniqueId()) ||
                !core.perms().COMMAND_OP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated(NEUTRAL, "User {user} has been opped by {sender}!", player, context);
        }
        this.core.getLog().info("Player {} has been opped by {}", player.getName(), context.getName());
    }

    @Command(desc = "Revokes the operator status of a player")
    @CommandPermission(permDefault = PermDefault.FALSE)
    public void deop(CommandContext context, org.spongepowered.api.entity.player.User player)
    {
        if (!context.getSource().getUniqueId().equals(player.getUniqueId()))
        {
            context.ensurePermission(core.perms().COMMAND_DEOP_OTHER);
        }
        if (!player.isOp())
        {
            context.sendTranslated(NEGATIVE, "The player you tried to deop is not an operator.");
            return;
        }
        player.setOp(false);
        if (player.isOnline())
        {
            um.getExactUser(player.getUniqueId()).sendTranslated(POSITIVE, "You were deopped by {user}.",
                                                                 context.getSource());
        }
        context.sendTranslated(POSITIVE, "{user} is no longer an operator!", player);

        for (User onlineUser : um.getOnlineUsers())
        {
            if (onlineUser.getUniqueId().equals(player.getUniqueId()) ||
                onlineUser.getUniqueId().equals(context.getSource().getUniqueId()) ||
                !core.perms().COMMAND_DEOP_NOTIFY.isAuthorized(onlineUser))
            {
                continue;
            }
            onlineUser.sendTranslated(POSITIVE, "User {user} has been deopped by {sender}!", player,
                                      context.getSource());
        }

        this.core.getLog().info("Player {} has been deopped by {}", player.getName(),
                                context.getSource().getName());
    }
    */

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandSender context)
    {
        Collection<PluginContainer> plugins = this.core.getGame().getPluginManager().getPlugins();
        Set<Module> modules = this.core.getModularity().getModules();

        context.sendTranslated(NEUTRAL, "There are {amount} plugins and {amount} CubeEngine modules loaded:",
                               plugins.size(), modules.size());
        context.sendMessage(" ");
        context.sendMessage(" - " + ChatFormat.BRIGHT_GREEN + "CubeEngine" + ChatFormat.RESET + " (" + core.getVersion() + ")");

        for (Module m : modules)
        {
            context.sendMessage("   - " + m.getInformation().getName() + ChatFormat.RESET + " (" + m.getInformation().getVersion() + ")");
        }

        plugins.stream().filter(p -> p.getInstance() != this.core)
                        .forEach(p -> context.sendMessage(
                            " - " + ChatFormat.BRIGHT_GREEN + p.getName() + ChatFormat.RESET + " (" + p.getVersion()
                                + ")"));
    }

    // integrate /saveoff and /saveon and provide aliases
    @Alias(value = "save-all")
    @Command(desc = "Saves all or a specific world to disk.")
    public void saveall(CommandSender context, @Optional World world)
    {
        context.sendTranslated(NEUTRAL, "Saving...");
        if (world != null)
        {
            core.getGame().getServer().saveWorldProperties(world.getProperties()); // TODO is this saving the world?
            // TODO world.getEntities().stream().filter(entity -> entity instanceof Player).forEach(player -> player.saveData());
            context.sendTranslated(POSITIVE, "World {world} has been saved to disk!", world);
            return;
        }
        Profiler.startProfiling("save-worlds");
        for (World aWorld : this.core.getGame().getServer().getWorlds())
        {
            core.getGame().getServer().saveWorldProperties(aWorld.getProperties()); // TODO is this saving the world?
        }
        // TODO this.core.getServer().savePlayers();
        context.sendTranslated(POSITIVE, "All worlds have been saved to disk!");
        context.sendTranslated(POSITIVE, "The saving took {integer#time} milliseconds.", Profiler.endProfiling("save-worlds", MILLISECONDS));
    }

    @Command(desc = "Displays the version of the server or a given plugin")
    public void version(CommandContext context, @Optional String plugin, @Flag boolean source)
    {
        Game game = this.core.getGame();
        if (plugin == null)
        {
            Platform platform = game.getPlatform();
            context.sendTranslated(NEUTRAL, "This server is running {name#server} in version {input#version:color=INDIGO}", platform.getMinecraftVersion().getName(), platform.getVersion());
            context.sendTranslated(NEUTRAL, "Sponge API {text:version\\::color=WHITE} {input#version:color=INDIGO}", platform.getApiVersion());
            context.sendMessage(" ");
            context.sendTranslated(NEUTRAL, "Expanded and improved by {text:CubeEngine:color=BRIGHT_GREEN} version {input#version:color=INDIGO}", core.getVersion());
            if (source)
            {
                showSourceVersion(context.getSource(), core.getSourceVersion());
            }
            return;
        }
        context.ensurePermission(core.perms().COMMAND_VERSION_PLUGINS);
        com.google.common.base.Optional<PluginContainer> instance = game.getPluginManager().getPlugin(plugin);
        if (!instance.isPresent())
        {
            List<PluginContainer> plugins = new ArrayList<>();
            for (PluginContainer container : game.getPluginManager().getPlugins())
            {
                if (container.getName().toLowerCase().startsWith(plugin.toLowerCase()))
                {
                    plugins.add(container);
                }
            }
            context.sendTranslated(NEGATIVE,
                                   "The given plugin doesn't seem to be loaded, have you typed it correctly (casing does matter)?");
            if (!plugins.isEmpty())
            {
                context.sendTranslated(NEGATIVE, "You might want to try one of these:");
                for (PluginContainer p : plugins)
                {
                    context.sendMessage(" - " + p.getName());
                }
            }
            return;
        }
        context.sendTranslated(NEUTRAL, "{name#plugin} is currently running in version {input#version:color=INDIGO}.",
                               instance.get().getName(), instance.get().getVersion());
        context.sendMessage(" ");
        context.sendTranslated(NEUTRAL, "Plugin information:");
        context.sendMessage(" ");
        if (instance.get().getInstance() instanceof CoreModule && source)
        {
            showSourceVersion(context.getSource(), core.getSourceVersion());
        }
        /* TODO if possible later get detailed descriptions
        context.sendTranslated(NEUTRAL, "Description: {input}", instance.getDescription().getDescription() == null ? "NONE" : instance.getDescription().getDescription());
        context.sendTranslated(NEUTRAL, "Website: {input}", instance.getDescription().getWebsite() == null ? "NONE" : instance.getDescription().getWebsite());
        context.sendTranslated(NEUTRAL, "Authors:");
        for (String author : instance.getDescription().getAuthors())
        {
            context.sendMessage("   - " + ChatFormat.AQUA + author);
        }
        */
    }
}
