package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.BukkitCore;
import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommand;
import de.cubeisland.cubeengine.core.module.Module;
import org.bukkit.plugin.Plugin;

import java.util.Collection;

import static de.cubeisland.cubeengine.core.util.ChatFormat.*;

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

    @Command(names = {
    "stop", "shutdown", "killserver", "quit"
    }, desc = "Shuts down the server", usage = "[message]")
    public void stop(CommandContext context)
    {}

    @Command(desc = "Reloads the server.", params = @Param(names = "test"))
    public void reload(CommandContext context)
    {}

    @Command(desc = "Changes the diffivulty level of the server")
    public void difficulty(CommandContext context)
    {}

    @Command(desc = "Makes a player an operator", usage = "<player>")
    public void op(CommandContext context)
    {}

    @Command(desc = "Revokes the operator status of a player", usage = "{player}")
    public void deop(CommandContext context)
    {}

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext context)
    {
        Plugin[] plugins = this.core.getServer().getPluginManager().getPlugins();
        Collection<Module> modules = this.core.getModuleManager().getModules();

        context.sendMessage("core", "There are %d plugins and %d modules loaded:", plugins.length, modules.size());
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
    @Command(desc = "Saves all or a specific world to disk.", usage = "{world}")
    public void save(CommandContext context)
    {}

    @Command(desc = "Displays the version of the server or a given plugin", usage = "[plugin]")
    public void version(CommandContext context)
    {}

    public class WhitelistCommand extends ContainerCommand
    {
        private WhitelistCommand(Module module)
        {
            super(module, "whitelist", "Allows you to manage your whitelist");
        }

        @Command(desc = "Adds a player to the whitelist.", usage = "<player>")
        public void add(CommandContext context)
        {}

        @Command(names = {
        "remove", "rm"
        }, desc = "Removes a player from the whitelist.", usage = "<player>")
        public void remove(CommandContext context)
        {}

        @Command(desc = "Lists all the whitelisted players")
        public void list(CommandContext context)
        {}

        @Command(desc = "Enables the whiltelisting")
        public void on(CommandContext context)
        {}

        @Command(desc = "Disables the whiltelisting")
        public void off(CommandContext context)
        {}
    }
}
