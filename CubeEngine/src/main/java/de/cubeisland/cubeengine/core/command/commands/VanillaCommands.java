package de.cubeisland.cubeengine.core.command.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.ContainerCommand;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.module.Module;

public class VanillaCommands
{
    @Command(names = {"stop", "shutdown", "killserver", "quit"}, desc = "Shuts down the server", usage =  "[message]")
    public void stop(CommandContext ctx)
    {}

    @Command(desc = "Reloads the server.")
    public void reload(CommandContext ctx)
    {}

    @Command(desc = "Changes the diffivulty level of the server")
    public void difficulty(CommandContext cty)
    {}

    @Command(desc = "Makes a player an operator", usage = "<player>")
    public void op(CommandContext ctx)
    {}

    @Command(desc = "Revokes the operator status of a player", usage = "{player}")
    public void deop()
    {
    }

    @Command(desc = "Lists all loaded plugins")
    public void plugins(CommandContext ctx)
    {}

    // integrate /saveoff and /saveon and provide aliases
    @Command(desc = "Saves all or a specific world to disk.", usage = "{world}")
    public void save(CommandContext ctx)
    {}

    @Command(desc = "Displays the version of the server or a given plugin", usage = "[plugin]")
    public void version(CommandContext ctx)
    {}


    private class WhitelistCommand extends ContainerCommand
    {
        private WhitelistCommand(Module module)
        {
            super(module, "whitelist", "Allows you to manage your whitelist");
        }

        @Command(desc = "Adds a player to the whitelist.", usage = "<player>")
        public void add(CommandContext context)
        {}

        @Command(names = {"remove", "rm"}, desc = "Removes a player from the whitelist.", usage = "<player>")
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
