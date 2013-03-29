package de.cubeisland.cubeengine.core.bukkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.util.StringUtil;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.command.AliasCommand;
import de.cubeisland.cubeengine.core.command.CommandExecuteEvent;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.util.StringUtils;
import de.cubeisland.cubeengine.core.util.matcher.Match;

import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.Validate;

import static de.cubeisland.cubeengine.core.logger.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

/**
 * This CommandMap extends the SimpleCommandMap to add some functionality:
 * - an accessor for the known command map
 * - typo correction for the command lookup via edit distance
 */
public class CubeCommandMap extends SimpleCommandMap
{
    private final Core core;
    private final Logger commandLogger;
    private final Map<String, List<String>> lastCommandOffers;

    public CubeCommandMap(Core core, Server server, SimpleCommandMap oldMap)
    {
        super(server);
        this.core = core;
        this.commandLogger = new CubeLogger("commands");
        this.lastCommandOffers = new THashMap<String, List<String>>();
        try
        {
            FileHandler handler = new CubeFileHandler(Level.ALL, new File(core.getFileManager().getLogDir(), this.commandLogger.getName()).getPath());
            handler.setFormatter(new Formatter() {
                private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                @Override
                public String format(LogRecord record)
                {
                    StringBuilder sb = new StringBuilder(this.dateFormat.format(new Date(record.getMillis())));
                    sb.append(' ').append(record.getMessage()).append('\n');
                    return sb.toString();
                }
            });
            this.commandLogger.addHandler(handler);
        }
        catch (IOException e)
        {
            core.getLog().log(WARNING, "Failed to create the command log!", e);
        }
        for (Command command : oldMap.getCommands())
        {
            command.unregister(oldMap);
            this.register(command);
        }
    }

    public List<String> getLastOfferFor(String sender)
    {
        return this.lastCommandOffers.remove(sender);
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
    public boolean dispatch(CommandSender sender, String commandLine) throws CommandException
    {
        if (!CubeEngine.isMainThread())
        {
            throw new IllegalStateException("Commands my only be called synchronously!");
        }
        commandLine = StringUtils.trimLeft(commandLine);
        if (commandLine.isEmpty())
        {
            return false;
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
            final Locale language = BukkitUtils.getLanguage(this.core.getI18n(), sender);
            List<String> matches = new LinkedList<String>(Match.string().getBestMatches(label, this.knownCommands.keySet(), 1));
            if (matches.size() > 0 && matches.size() <= this.core.getConfiguration().commandOffers)
            {
                Collections.sort(matches, String.CASE_INSENSITIVE_ORDER);
                if (matches.size() == 1)
                {
                    sender.sendMessage(this.core.getI18n().translate(language, "core", "&cCouldn't find &e/%s&c. Did you mean &a/%s&c?", label, matches.iterator().next()));
                }
                else
                {
                    sender.sendMessage(this.core.getI18n().translate(language, "core", "&eDid you mean one of these: &a%s &e?", "/" + StringUtils.implode(", /", matches)));
                }
                if (matches.size() > this.core.getConfiguration().commandTabCompleteOffers)
                {
                    matches = matches.subList(0, this.core.getConfiguration().commandTabCompleteOffers);
                }
                if (sender instanceof ConsoleCommandSender)
                {
                    this.lastCommandOffers.put(":console", matches);
                }
                else
                {
                    this.lastCommandOffers.put(sender.getName(), matches);
                }
            }
            else
            {
                sender.sendMessage(this.core.getI18n().translate(language, "core", "&cI couldn't find any command for &e/%s &c...", label));
            }
            return false;
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
                this.commandLogger.log(INFO, "execute " + sender.getName() + ' ' + commandLine);
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

        // return true as the command was handled
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

        if (oldCommand != null && !this.aliases.contains(label))
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

            if (fallback.equals(label))
            {
                this.knownCommands.remove(label);
                this.knownCommands.put(fallback, oldCommand);
            }
        }

        if (isAlias)
        {
            this.aliases.add(label);
        }
        else
        {
            // Ensure lowerLabel isn't listed as a alias anymore and update the commands registered name
            this.aliases.remove(label);
        }
        this.knownCommands.put(label, command);

        return true;
    }

    private final Pattern PATTERN_ON_SPACE = Pattern.compile(" ", Pattern.LITERAL);

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine)
    {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(cmdLine, "Command line cannot null");

        int spaceIndex = cmdLine.indexOf(' ');

        if (spaceIndex == -1)
        {

            List<String> lastOffer = this.lastCommandOffers.remove(sender.getName());
            if (lastOffer != null && cmdLine.isEmpty())
            {
                List<String> commands = new ArrayList<String>(lastOffer.size());
                for (String cmd : lastOffer)
                {
                    commands.add('/' + cmd);
                }
                return commands;
            }
            List<String> completions = new ArrayList<String>();

            for (VanillaCommand command : fallbackCommands)
            {
                if (completions.size() == this.core.getConfiguration().commandTabCompleteOffers)
                {
                    break;
                }
                String name = command.getName();

                if (!command.testPermissionSilent(sender))
                {
                    continue;
                }
                if (this.knownCommands.containsKey(name))
                {
                    // Don't let a vanilla command override a command added below
                    // This has to do with the way aliases work
                    continue;
                }
                if (!StringUtil.startsWithIgnoreCase(name, cmdLine))
                {
                    continue;
                }

                completions.add('/' + name);
            }

            for (Map.Entry<String, Command> commandEntry : this.knownCommands.entrySet())
            {
                if (completions.size() == this.core.getConfiguration().commandTabCompleteOffers)
                {
                    break;
                }
                Command command = commandEntry.getValue();

                if (!command.testPermissionSilent(sender))
                {
                    continue;
                }

                String name = commandEntry.getKey(); // Use the alias, not command name

                if (StringUtil.startsWithIgnoreCase(name, cmdLine))
                {
                    completions.add('/' + name);
                }
            }

            Collections.sort(completions, String.CASE_INSENSITIVE_ORDER);
            return completions;
        }

        final String commandName = cmdLine.substring(0, spaceIndex);
        final Command target = getCommand(commandName);

        if (target == null)
        {
            return null;
        }

        if (!target.testPermissionSilent(sender))
        {
            return null;
        }

        final String[] args = PATTERN_ON_SPACE.split(cmdLine.substring(spaceIndex + 1, cmdLine.length()), -1); // TODO what exactly is done here?

        try
        {
            if (!(target instanceof CubeCommand) || ((CubeCommand)target).isLoggable())
            {
                this.commandLogger.log(INFO, "complete " + sender.getName() + ' ' + commandName + ' ' + args);
            }
            return target.tabComplete(sender, commandName, args);
        }
        catch (CommandException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CommandException("Unhandled exception executing tab-completer for '" + cmdLine + "' in " + target, e);
        }
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
