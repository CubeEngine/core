package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.AliasCommand;
import de.cubeisland.cubeengine.core.command.CommandExecuteEvent;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static de.cubeisland.cubeengine.core.i18n.I18n._;
import static de.cubeisland.cubeengine.core.logger.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;
import static de.cubeisland.cubeengine.core.util.Misc.arr;

/**
 * This CommandMap extends the SimpleCommandMap to add some functionality:
 * - an accessor for the known command map
 * - typo correction for the command lookup via edit distance
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core core;
    private final Logger commandLogger;

    public CubeCommandMap(Core core, Server server, SimpleCommandMap oldMap)
    {
        super(server);
        this.core = core;
        this.commandLogger = new CubeLogger("commands");
        try
        {
            FileHandler handler = new CubeFileHandler(Level.ALL,  new File(core.getFileManager().getLogDir(), "commands").getPath());
            handler.setFormatter(new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record)
                {
                    StringBuilder sb = new StringBuilder(this.dateFormat.format(new Date(record.getMillis())));
                    sb.append(' ').append(record.getMessage());
                    return sb.toString();
                }
            });
            this.commandLogger.addHandler(handler);
        }
        catch (IOException e)
        {
            core.getCoreLogger().log(WARNING, "Failed to create the command log!", e);
        }
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
    public Command getCommand(String name)
    {
        name = name.trim();
        if (name == null || name.isEmpty())
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
    public boolean dispatch(CommandSender sender, final String commandLine) throws CommandException
    {
        if (commandLine.isEmpty())
        {
            return true;
        }

        String[] parts = commandLine.split(" ");

        if (parts.length == 0)
        {
            return false;
        }

        String label = parts[0].toLowerCase();
        Command command = getCommand(label);

        if (command == null)
        {
            final String language = BukkitUtils.getLanguage(sender);
            Set<String> matches = Match.string().getBestMatches(label, this.knownCommands.keySet(), 1);
            if (matches.size() > 0 && matches.size() <= this.core.getConfiguration().commandOffers)
            {
                if (matches.size() == 1)
                {
                    sender.sendMessage(_(language, "core", "&cCouldn't find &e/%s&c. Did you mean &a/%s&c?", arr(label, matches.iterator().next())));
                }
                else
                {
                    sender.sendMessage(_(language, "core", "&eDid you mean one of these: &a%s &e?", arr("/" + StringUtils.implode(", /", matches))));
                }
            }
            else
            {
                sender.sendMessage(_(language, "core", "&cI couldn't find any command for &e/%s &c...", arr(label)));
            }
            return true;
        }

        String[] args = null;
        // our commands expect spaces to be preserved
        if (command instanceof CubeCommand)
        {
            final int spaceIndex = commandLine.indexOf(' ');
            if (spaceIndex > -1 && spaceIndex + 1 < commandLine.length())
            {
                args = StringUtils.explode(" ", commandLine.substring(spaceIndex + 1));
            }
        }
        if (args == null)
        {
            args = Arrays.copyOfRange(parts, 1, parts.length);
        }

        if (command instanceof AliasCommand)
        {
            AliasCommand alias = ((AliasCommand)command);
            String[] prefix = alias.getPrefix();
            String[] suffix = alias.getSuffix();

            String[] newArgs = new String[prefix.length + args.length + suffix.length];
            System.arraycopy(prefix, 0, newArgs, 0, prefix.length);
            System.arraycopy(args, 0, newArgs, prefix.length, args.length);
            System.arraycopy(suffix, 0, newArgs, prefix.length + args.length, suffix.length);

            args = newArgs;
            command = alias.getTarget(); // TODO does this actually work? (test usage strings!)
        }

        if (this.core.getEventManager().fireEvent(new CommandExecuteEvent(this.core, command, commandLine)).isCancelled())
        {
            return false;
        }

        try
        {
            // TODO we might catch errors here instead of on CubeCommand
            command.execute(sender, label, args);
            if (!(command instanceof CubeCommand) || ((CubeCommand)command).isLoggable())
            {
                this.commandLogger.log(INFO, sender.getName() + " " + commandLine);
            }
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
