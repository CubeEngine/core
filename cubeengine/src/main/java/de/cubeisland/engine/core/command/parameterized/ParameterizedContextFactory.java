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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.BasicContextFactory;
import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.exception.IncorrectUsageException;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext.LastType;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext.Type;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext.Type.*;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;
import static java.util.Locale.ENGLISH;

public class ParameterizedContextFactory extends BasicContextFactory
{
    private final Map<String, CommandFlag> flagMap = new THashMap<>();
    private final LinkedHashMap<String, CommandParameter> paramMap = new LinkedHashMap<>();

    public ParameterizedContextFactory(List<CommandParameterIndexed> indexed, Collection<CommandFlag> flags, Collection<CommandParameter> params)
    {
        super(indexed);
        this.addFlags(flags);
        this.addParameters(params);
    }

    public ParameterizedContextFactory(List<CommandParameterIndexed> indexed)
    {
        super(indexed);
    }

    public ParameterizedContextFactory(CommandParameterIndexed action)
    {
        this(Arrays.asList(action));
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
        this.paramMap.put(param.getName().toLowerCase(ENGLISH), param);
        for (String alias : param.getAliases())
        {
            alias = alias.toLowerCase(ENGLISH);
            if (!this.paramMap.containsKey(alias))
            {
                this.paramMap.put(alias, param);
            }
        }
        return this;
    }

    public ParameterizedContextFactory removeParameter(String name)
    {
        CommandParameter param = this.paramMap.remove(name.toLowerCase(ENGLISH));
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
        return this.paramMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandParameter> getParameters()
    {
        return new LinkedHashSet<>(this.paramMap.values());
    }

    public void addFlags(Collection<CommandFlag> flags)
    {
        if (flags != null)
        {
            for (CommandFlag flag : flags)
            {
                this.addFlag(flag);
            }
        }
    }

    public ParameterizedContextFactory addFlag(CommandFlag flag)
    {
        this.flagMap.put(flag.getName().toLowerCase(ENGLISH), flag);
        final String longName = flag.getLongName().toLowerCase(ENGLISH);
        if (!this.flagMap.containsKey(longName))
        {
            this.flagMap.put(longName, flag);
        }
        return this;
    }

    public ParameterizedContextFactory removeFlag(String name)
    {
        CommandFlag flag = this.flagMap.remove(name.toLowerCase(ENGLISH));
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
        return this.flagMap.get(name.toLowerCase(ENGLISH));
    }

    public Set<CommandFlag> getFlags()
    {
        return new THashSet<>(this.flagMap.values());
    }

    @Override
    public ParameterizedContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        return parse(command, sender, labels, rawArgs, ParameterizedContext.class);
    }

    @Override
    public ParameterizedTabContext tabCompleteParse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        return parse(command, sender, labels, rawArgs, ParameterizedTabContext.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractParameterizedContext<?>> T parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs,Class<T> clazz)
    {
        final List<String> args = new LinkedList<>();
        final Set<String> flags = new THashSet<>();
        final Map<String, String> rawParams = new LinkedHashMap<>();

        Type last = readCommand(rawArgs, clazz == ParameterizedTabContext.class, flags, args, rawParams);

        if (clazz == ParameterizedTabContext.class)
        {
            return (T)new ParameterizedTabContext(command, sender, labels, args, flags, rawParams, last);
        }
        return (T)new ParameterizedContext(command, sender, labels, args, flags, readParams(sender, rawParams));
    }

    private Type readCommand(String[] rawArgs, boolean tabComplete, Set<String> flags, List<String> args,
                             Map<String, String> rawParams)
    {
        if (rawArgs.length < 1)
        {
            return Type.NOTHING;
        }
        LastType type = new LastType();
        for (int offset = 0; offset < rawArgs.length;)
        {
            String rawArg = rawArgs[offset];
            if (rawArg.isEmpty())
            {
                // ignore empty args except last when tabcomplete
                if (tabComplete && offset == rawArgs.length -1)
                {
                    args.add(rawArg);
                }
                offset++;
                type.last = ANY;
            }
            else if (rawArg.length() >= 1 && rawArg.charAt(0) == '-')
            {
                // reads a flag or indexed param
                offset = readFlag(rawArg, args, flags, offset, type);
            }
            else
            {
                // reads a named param or indexed param
                offset = readRawParam(rawArgs, args, rawParams, offset, type);
            }
        }

        return type.last;
    }

    private int readFlag(String rawArg, List<String> args, Set<String> flags, int offset, LastType type)
    {
        String flag = rawArg;
        if (flag.charAt(0) == '-')
        {
            flag = flag.substring(1);
        }
        if (flag.isEmpty()) // is there still a name?
        {
            offset++;
            args.add(rawArg);
            type.last = FLAG_OR_INDEXED;
            return offset;
        }

        flag = flag.toLowerCase(ENGLISH); // lowercase flag

        CommandFlag cmdFlag = this.flagMap.get(flag);
        if (cmdFlag != null) // has flag ?
        {
            flags.add(cmdFlag.getName()); // added flag
            type.last = NOTHING;
        }
        else
        {
            type.last = FLAG_OR_INDEXED;
            args.add(rawArg); // flag not found, adding it as an indexed param
        }
        offset++;
        return offset;
    }

    private int readRawParam(String[] rawArgs, List<String> args, Map<String, String> rawParams, int offset, LastType type)
    {
        String paramName = rawArgs[offset].toLowerCase(ENGLISH);
        // has alias named Param ?
        CommandParameter param = paramMap.get(paramName);
        // is named Param?
        if (param != null && offset + 1 < rawArgs.length)
        {
            StringBuilder paramValue = new StringBuilder();
            offset++;
            offset += readString(paramValue, rawArgs, offset);
            //added named param
            rawParams.put(param.getName(), paramValue.toString());
            type.last = PARAM_VALUE;
        }
        else // else is indexed param
        {
            StringBuilder arg = new StringBuilder();
            offset += readString(arg, rawArgs, offset);
            args.add(arg.toString());// added indexed param
            type.last = INDEXED_OR_PARAM;
        }
        return offset;
    }

    private Map<String, Object> readParams(CommandSender sender, Map<String, String> rawParams)
    {
        Map<String, Object> readParams = new LinkedHashMap<>();

        for (Entry<String, String> entry : rawParams.entrySet())
        {
            CommandParameter param = paramMap.get(entry.getKey());
            try
            {
                readParams.put(entry.getKey(), ArgumentReader.read(param.getType(), entry.getValue(), sender));
            }
            catch (InvalidArgumentException ex)
            {
                throw new IncorrectUsageException(sender.getTranslation(NEGATIVE, "Invalid argument for {input}: {}", param.getName(),
                                                                        sender.getTranslation(NONE, ex.getMessage(), ex.getMessageArgs())));
                // TODO move else where so context is not null when showing error
            }
        }
        return readParams;
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
