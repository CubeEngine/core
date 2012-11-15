package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.CommandExecuteEvent;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * This CommandMap extends the SimpleCommandMap to add some functionality:
 * - an accessor for the known command map
 * - typo correction for the command lookup via edit distance
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core        core;
    private final UserManager um;

    public CubeCommandMap(Core core, Server server, SimpleCommandMap oldMap)
    {
        super(server);
        this.core = core;
        this.um = core.getUserManager();
        for (Command command : oldMap.getCommands())
        {
            command.unregister(oldMap);
            this.register(command);
        }
    }

    /**
     * Returns a map of the known commands
     *
     * @return the known commands
     */
    public Map<String, Command> getKnownCommands()
    {
        return this.knownCommands;
    }

    @Override
    public Command getCommand(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return super.getCommand(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Command getFallback(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return super.getFallback(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException
    {
        String[] args = commandLine.split(" ");

        if (args.length == 0)
        {
            return false;
        }

        String label = args[0].toLowerCase();
        Command command = getCommand(label);

        if (command == null && !"".equals(label))
        {
            User user = this.um.getExactUser(sender);
            if (user != null)
            {
                sender = user;
            }
            List<String> matches = StringUtils.getBestMatches(label, this.knownCommands.keySet(), 1);
            if (matches.size() == 1)
            {
                sender.sendMessage(_(sender, "core", "Couldn't find /%s, but /%s seems to be the one you searched...", label, matches.get(0)));
                label = matches.get(0);
                command = this.getCommand(label);

            }
            else if (matches.size() > 1 && matches.size() <= 5) // TODO maximum configurable
            {
                sender.sendMessage(_(sender, "core", "I could not find the command /%s ...", label));
                sender.sendMessage(_(sender, "core", "Did you mean one of these: %s ?", "/" + StringUtils.implode(", /", matches)));
            }
            else
            {
                sender.sendMessage(_(sender, "core", "I could not find any matching command for /%s ...", label));
            }
        }

        if (command == null || this.core.getEventManager().fireEvent(new CommandExecuteEvent(this.core, command, commandLine)).isCancelled())
        {
            return false;
        }

        try
        {
            // TODO we might catch errors here instead of on CubeCommand
            command.execute(sender, label, Arrays.copyOfRange(args, 1, args.length));
        }
        catch (CommandException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + command, e);
        }

        // return true as command was handled
        return true;
    }

    public boolean register(Command command)
    {
        return this.register(null, command);
    }

    protected synchronized boolean registerAndOverwrite(String label, String fallbackPrefix, Command command, boolean isAlias)
    {
        label = label.trim().toLowerCase();
        Command oldCommand = this.knownCommands.get(label);

        if (isAlias && oldCommand != null && !this.aliases.contains(label) && !(oldCommand instanceof CubeCommand))
        {
            // Request is for an alias and it conflicts with a existing command or previous alias ignore it
            // Note: This will mean it gets removed from the commands list of active aliases
            return false;
        }

        if (oldCommand != null && !aliases.contains(label))
        {
            String fallback = label;
            if (oldCommand instanceof PluginCommand)
            {
                fallback = ((PluginCommand)oldCommand).getPlugin().getName().toLowerCase(Locale.ENGLISH) + ":" + label;
            }
            else if (oldCommand instanceof BukkitCommand)
            {
                fallback = "bukkit:" + label;
            }
            else if (oldCommand instanceof CubeCommand)
            {
                label = fallback = fallbackPrefix.toLowerCase(Locale.ENGLISH) + ":" + label;
                command.setLabel(label);
            }

            if (fallback != label)
            {
                knownCommands.remove(label);
                knownCommands.put(fallback, oldCommand);
            }
        }

        if (isAlias)
        {
            aliases.add(label);
        }
        else
        {
            // Ensure lowerLabel isn't listed as a alias anymore and update the commands registered name
            aliases.remove(label);
        }
        knownCommands.put(label, command);

        return true;
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command)
    {
        registerAndOverwrite(label, fallbackPrefix, command, false);

        for (String alias : command.getAliases())
        {
            registerAndOverwrite(alias, fallbackPrefix, command, true);
        }

        // Register to us so further updates of the commands label and aliases are postponed until its reregistered
        command.register(this);

        return true;
    }
}
