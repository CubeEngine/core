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
package de.cubeisland.engine.core.command;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;

import static de.cubeisland.engine.core.command.ContextParser.Type.*;
import static de.cubeisland.engine.core.command.ContextParser.Type.INDEXED_OR_PARAM;
import static java.util.Locale.ENGLISH;

public class ContextParser extends ContextDescriptor
{
    public Type parse(String[] rawArgs, List<String> indexed, Map<String, String> named, Set<String> flags)
    {
        if (rawArgs.length < 1)
        {
            return ContextParser.Type.NOTHING;
        }
        ContextParser.LastType type = new ContextParser.LastType();
        for (int offset = 0; offset < rawArgs.length;)
        {
            String rawArg = rawArgs[offset];
            if (rawArg.isEmpty())
            {
                // ignore empty args except last
                if (offset == rawArgs.length -1)
                {
                    indexed.add(rawArg);
                }
                offset++;
                type.last = ANY;
            }
            else if (rawArg.length() >= 1 && rawArg.charAt(0) == '-')
            {
                // reads a flag or indexed param
                offset = readFlag(rawArg, indexed, flags, offset, type);
            }
            else
            {
                // reads a named param or indexed param
                offset = readRawParam(rawArgs, indexed, named, offset, type);
            }
        }
        return type.last;
    }

    protected int readFlag(String rawArg, List<String> indexed, Set<String> flags, int offset, ContextParser.LastType type)
    {
        String flag = rawArg;
        if (flag.charAt(0) == '-')
        {
            flag = flag.substring(1);
        }
        if (flag.isEmpty()) // is there still a name?
        {
            offset++;
            indexed.add(rawArg);
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
            indexed.add(rawArg); // flag not found, adding it as an indexed param
        }
        offset++;
        return offset;
    }

    protected int readRawParam(String[] rawArgs, List<String> indexed, Map<String, String> named, int offset, ContextParser.LastType type)
    {
        String paramName = rawArgs[offset].toLowerCase(ENGLISH);
        // has alias named Param ?
        CommandParameter param = this.namedMap.get(paramName);
        // is named Param?
        if (param != null && offset + 1 < rawArgs.length)
        {
            StringBuilder paramValue = new StringBuilder();
            offset++;
            offset += readString(paramValue, rawArgs, offset);
            //added named param
            named.put(param.getName(), paramValue.toString());
            type.last = PARAM_VALUE;
        }
        else // else is indexed param
        {
            StringBuilder arg = new StringBuilder();
            offset += readString(arg, rawArgs, offset);
            indexed.add(arg.toString());// added indexed param
            type.last = INDEXED_OR_PARAM;
        }
        return offset;
    }

    public static int readString(StringBuilder sb, String[] args, int offset)
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

    public static enum Type
    {
        ANY,
        NOTHING,
        FLAG_OR_INDEXED,
        INDEXED_OR_PARAM,
        PARAM_VALUE,

        FLAG_OR_PARAM
    }

    public static class LastType
    {
        public Type last;
    }
}
