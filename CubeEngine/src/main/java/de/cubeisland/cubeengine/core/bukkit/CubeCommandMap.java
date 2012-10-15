package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.CommandExecuteEvent;
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
import org.bukkit.command.SimpleCommandMap;

import static de.cubeisland.cubeengine.core.i18n.I18n._;

/**
 * Extends the Bukkit-CommandMap.
 * Allows matching commands with LD-Distance = 1.
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core core;
    private final UserManager um;
    
    public CubeCommandMap(Core core, Server server, SimpleCommandMap oldMap)
    {
        super(server);
        this.core = core;
        this.um = core.getUserManager();
        for (Command command : oldMap.getCommands())
        {
            this.knownCommands.put(command.getName().toLowerCase(Locale.ENGLISH), command);
        }
    }
    
    public Map<String, Command> getKnownCommands()
    {
        return this.knownCommands;
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command)
    {
        return super.register(label, fallbackPrefix, command);
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

        if (command == null)
        {
            User user = this.um.getUser(sender);
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
                sender.sendMessage(_(sender, "core", "Did you mean once of these: %s ?", "/" + StringUtils.implode(", /", matches)));
            }
            else
            {
                sender.sendMessage(_(sender, "core", "I could not find any matching command for /%s ...", label));
            }
        }
        
        if (command == null || this.core.getEventManager().fireEvent(new CommandExecuteEvent(this.core, command)).isCancelled())
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
        catch (Throwable t)
        {
            throw new CommandException("Unhandled exception executing '" + commandLine + "' in " + command, t);
        }

        // return true as command was handled
        return true;
    }
}
