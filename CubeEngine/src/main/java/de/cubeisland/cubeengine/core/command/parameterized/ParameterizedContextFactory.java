package de.cubeisland.cubeengine.core.command.parameterized;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.ContextFactory;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.exception.IllegalParameterValue;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.command.exception.MissingParameterException;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.*;

public class ParameterizedContextFactory implements ContextFactory
{
    private final Map<String, CommandFlag>      flagMap;
    private final Map<String, CommandParameter> paramMap;

    public ParameterizedContextFactory()
    {
        this.flagMap = new THashMap<String, CommandFlag>();
        this.paramMap = new THashMap<String, CommandParameter>();
    }

    Map<String, CommandParameter> getParamMap()
    {
        return this.paramMap;
    }

    Map<String, CommandFlag> getFlagMap()
    {
        return this.flagMap;
    }

    public ParameterizedContextFactory(Collection<CommandFlag> flags, Collection<CommandParameter> params)
    {
        this();

        if (flags != null)
        {
            this.addFlags(flags);
        }

        if (params != null)
        {
            this.addParameters(params);
        }
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
            Iterator<Map.Entry<String, CommandParameter>> iter = this.paramMap.entrySet().iterator();
            while (iter.hasNext())
            {
                if (iter.next().getValue() == param)
                {
                    iter.remove();
                }
            }
        }
        return this;
    }

    public CommandParameter getParameter(String name)
    {
        return this.paramMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public Collection<CommandParameter> getParameters()
    {
        return this.paramMap.values();
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
            Iterator<Map.Entry<String, CommandFlag>> iter = this.flagMap.entrySet().iterator();
            while (iter.hasNext())
            {
                if (iter.next().getValue() == flag)
                {
                    iter.remove();
                }
            }
        }
        return this;
    }

    public CommandFlag getFlag(String name)
    {
        return this.flagMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    public Collection<CommandFlag> getFlags()
    {
        return this.flagMap.values();
    }

    @Override
    public ParameterizedContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        final LinkedList<String> args = new LinkedList<String>();
        final Set<String> flags = new THashSet<String>();
        final Map<String, Object> params = new THashMap<String, Object>();

        if (commandLine.length > 0)
        {
            String[] names;

            for (int offset = 0; offset < commandLine.length;)
            {
                if (commandLine[offset].isEmpty())
                {
                    offset++;
                    continue; // part is empty, ignoring...
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

                    if (this.flagMap.containsKey(flag)) // has flag ?
                    {
                        flags.add(flag); // added flag
                    }
                    else
                    {
                        args.add(commandLine[offset]); // flag not found, adding it as an indexed param
                    }
                    offset++;
                }
                else
                //else named param or indexed param
                {
                    String paramName = commandLine[offset].toLowerCase(Locale.ENGLISH);
                    // has alias named Param ?
                    CommandParameter param = paramMap.get(paramName);
                    // is named Param?
                    if (param != null && offset + 1 < commandLine.length)
                    {
                        try
                        {
                            offset++;
                            StringBuilder paramValue = new StringBuilder();
                            offset += readString(paramValue, commandLine, offset);
                            //added named param
                            params.put(paramName, ArgumentReader.read(param.getType(), paramValue.toString()));
                        }
                        catch (InvalidArgumentException ex)
                        {
                            IllegalParameterValue.illegalParameter(sender, "core", "", paramName);
                        }
                    }
                    else
                    // else is indexed param
                    {
                        StringBuilder arg = new StringBuilder();
                        offset += readString(arg, commandLine, offset);
                        args.add(arg.toString());// added indexed param
                    }
                }
            }
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
        if (args[offset] != null && !args[offset].isEmpty())
        {
            char quoteChar = args[offset].charAt(0);

            if (quoteChar == '"' || quoteChar == '\'')
            {
                if (args[offset].length() > 1 && args[offset].charAt(args[offset].length() - 1) == quoteChar)//ends with quotechar AND is not 1 long?
                {
                    sb.append(args[offset].substring(1, args[offset].length() - 1));
                    return 1;
                }

                int i = 1;
                sb.append(args[offset].substring(1));

                for (; offset < args.length;)
                {
                    ++i;
                    ++offset;
                    if (args[offset].charAt(args[offset].length() - 1) == quoteChar)
                    {
                        sb.append(' ').append(args[offset].substring(0, args[offset].length() - 1));
                        break;
                    }
                    sb.append(' ').append(args[offset]);
                }
                return i;
            }
        }
        sb.append(args[offset]);
        return 1;
    }
}
