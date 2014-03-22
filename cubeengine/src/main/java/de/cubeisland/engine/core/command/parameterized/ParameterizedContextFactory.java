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
package de.cubeisland.engine.core.command.parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContextFactory;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.exception.MissingParameterException;
import de.cubeisland.engine.core.util.formatter.MessageType;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ParameterizedContextFactory implements ContextFactory
{
    private ArgBounds bounds;
    private final Map<String, CommandFlag> flagMap;
    private final Map<String, CommandParameter> paramMap;

    public ParameterizedContextFactory(ArgBounds bounds)
    {
        this.bounds = bounds;
        this.flagMap = new THashMap<>();
        this.paramMap = new THashMap<>();
    }

    public ParameterizedContextFactory(ArgBounds bounds, Collection<CommandFlag> flags, Collection<CommandParameter> params)
    {
        this(bounds);

        if (flags != null)
        {
            this.addFlags(flags);
        }

        if (params != null)
        {
            this.addParameters(params);
        }
    }

    @Override
    public ArgBounds getArgBounds()
    {
        return this.bounds;
    }

    public void setArgBounds(ArgBounds newBounds)
    {
        this.bounds = newBounds;
    }

    public ParameterizedContextFactory addParameters(Collection<CommandParameter> params)
    {
        if (params != null)
        {
            for (CommandParameter param : params)
            {
                this.addParameter(param);
            }
        }
        return this;
    }

    public ParameterizedContextFactory addParameter(CommandParameter param)
    {
        this.paramMap.put(param.getName().toLowerCase(Locale.ENGLISH), param);
        for (String alias : param.getAliases())
        {
            alias = alias.toLowerCase(Locale.ENGLISH);
            if (!this.paramMap.containsKey(alias))
            {
                this.paramMap.put(alias, param);
            }
        }
        return this;
    }

    public ParameterizedContextFactory removeParameter(String name)
    {
        CommandParameter param = this.paramMap.remove(name.toLowerCase(Locale.ENGLISH));
        if (param != null)
        {
            Iterator<Map.Entry<String, CommandParameter>> it = this.paramMap.entrySet().iterator();
            while (it.hasNext())
            {
                if (it.next().getValue() == param)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    public CommandParameter getParameter(String name)
    {
        return this.paramMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public Set<CommandParameter> getParameters()
    {
        return new THashSet<>(this.paramMap.values());
    }

    public void addFlags(Collection<CommandFlag> flags)
    {
        for (CommandFlag flag : flags)
        {
            this.addFlag(flag);
        }
    }

    public ParameterizedContextFactory addFlag(CommandFlag flag)
    {
        this.flagMap.put(flag.getName().toLowerCase(Locale.ENGLISH), flag);
        final String longName = flag.getLongName().toLowerCase(Locale.ENGLISH);
        if (!this.flagMap.containsKey(longName))
        {
            this.flagMap.put(longName, flag);
        }
        return this;
    }

    public ParameterizedContextFactory removeFlag(String name)
    {
        CommandFlag flag = this.flagMap.remove(name.toLowerCase(Locale.ENGLISH));
        if (flag != null)
        {
            Iterator<Map.Entry<String, CommandFlag>> it = this.flagMap.entrySet().iterator();
            while (it.hasNext())
            {
                if (it.next().getValue() == flag)
                {
                    it.remove();
                }
            }
        }
        return this;
    }

    public CommandFlag getFlag(String name)
    {
        return this.flagMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public Set<CommandFlag> getFlags()
    {
        return new THashSet<>(this.flagMap.values());
    }

    @Override
    public ParameterizedContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        final LinkedList<String> args = new LinkedList<>();
        final Set<String> flags = new THashSet<>();
        final Map<String, Object> params = new THashMap<>();

        if (commandLine.length > 0)
        {
            for (int offset = 0; offset < commandLine.length;)
            {
                if (commandLine[offset].isEmpty())
                {
                    offset++;
                    continue; // ignore empty args
                }
                if (commandLine[offset].length() >= 2 && commandLine[offset].charAt(0) == '-') // is flag?
                {
                    String flag = commandLine[offset].substring(1);
                    if (flag.charAt(0) == '-')
                    {
                        flag = flag.substring(1);
                    }
                    if (flag.isEmpty()) // is there still a name?
                    {
                        offset++;
                        args.add(commandLine[offset]);
                        continue;
                    }

                    flag = flag.toLowerCase(Locale.ENGLISH); // lowercase flag

                    CommandFlag cmdFlag = this.flagMap.get(flag);
                    if (cmdFlag != null) // has flag ?
                    {
                        flags.add(cmdFlag.getName()); // added flag
                    }
                    else
                    {
                        args.add(commandLine[offset]); // flag not found, adding it as an indexed param
                    }
                    offset++;
                }
                else //else named param or indexed param
                {
                    String paramName = commandLine[offset].toLowerCase(Locale.ENGLISH);
                    // has alias named Param ?
                    CommandParameter param = paramMap.get(paramName);
                    // is named Param?
                    if (param != null && offset + 1 < commandLine.length)
                    {
                        StringBuilder paramValue = new StringBuilder();
                        try
                        {
                            offset++;
                            offset += readString(paramValue, commandLine, offset);
                            //added named param
                            params.put(param.getName(), ArgumentReader.read(param.getType(), paramValue.toString(), sender));
                        }
                        catch (InvalidArgumentException ex)
                        {
                            throw new IncorrectUsageException(sender.translate(MessageType.NEGATIVE, "Invalid argument for {input}: {}", param
                                .getName(), sender.translate(MessageType.NONE, ex.getMessage(), ex.getMessageArgs())));
                        }
                    }
                    else // else is indexed param
                    {
                        StringBuilder arg = new StringBuilder();
                        offset += readString(arg, commandLine, offset);
                        args.add(arg.toString());// added indexed param
                    }
                }
            }
        }

        if (args.size() < this.getArgBounds().getMin())
        {
            throw new IncorrectUsageException("You've given too few arguments.");
        }
        if (this.getArgBounds().getMax() > ArgBounds.NO_MAX && args.size() > this.getArgBounds().getMax())
        {
            throw new IncorrectUsageException("You've given too many arguments.");
        }

        for (CommandParameter param : this.paramMap.values())
        {
            if (param.isRequired() && !params.containsKey(param.getName()))
            {
                throw new MissingParameterException(param.getName());
            }
        }

        return new ParameterizedContext(command, sender, labels, args, flags, params);
    }

    protected static int readString(StringBuilder sb, String[] args, int offset)
    {
        // string is empty? return an empty string
        if (offset >= args.length || args[offset].isEmpty())
        {
            sb.append("");
            return 1;
        }

        // first char is not a quote char? return the string
        final char quoteChar = args[offset].charAt(0);
        if (quoteChar != '"' && quoteChar != '\'')
        {
            sb.append(args[offset]);
            return 1;
        }

        String string = args[offset].substring(1);
        // string has at least 2 chars and ends with the same quote char? return the string without quotes
        if (string.length() > 0 && string.charAt(string.length() - 1) == quoteChar)
        {
            sb.append(string.substring(0, string.length() - 1));
            return 1;
        }

        sb.append(string);
        offset++;
        int argCounter = 1;

        while (offset < args.length)
        {
            sb.append(' ');
            argCounter++;
            string = args[offset++];
            if (string.length() > 0 && string.charAt(string.length() - 1) == quoteChar)
            {
                sb.append(string.substring(0, string.length() - 1));
                break;
            }
            sb.append(string);
        }

        return argCounter;
    }

    @Override
    public CommandContext parse(CubeCommand command, CommandContext context)
    {
        Set<String> flags;
        Map<String, Object> params;
        if (context instanceof ParameterizedContext)
        {
            flags = ((ParameterizedContext)context).getFlags();
            params = ((ParameterizedContext)context).getParams();
        }
        else
        {
            flags = Collections.emptySet();
            params = Collections.emptyMap();
        }

        return new ParameterizedContext(command, context.getSender(), context.getLabels(), context.getArgs(), flags, params);
    }
}
