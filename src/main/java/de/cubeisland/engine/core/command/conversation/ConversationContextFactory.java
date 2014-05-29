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
package de.cubeisland.engine.core.command.conversation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ContextParser;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.CubeContextFactory;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.CommandParameterIndexed;

import static de.cubeisland.engine.core.command.ContextParser.Type.*;

public class ConversationContextFactory extends CubeContextFactory
{
    public ConversationContextFactory()
    {
        this.addIndexed(CommandParameterIndexed.greedyIndex());
        // TODO ensure flags & named are all diff names or else named cannot be used
    }

    @Override
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
            else
            {
                rawArg = rawArg.toLowerCase();
                CommandFlag flag = this.flagMap.get(rawArg);
                if (flag != null)
                {
                    offset++;
                    flags.add(flag.getName());
                    type.last = NOTHING;
                }
                else
                {
                    type.last = FLAG_OR_PARAM;
                    CommandParameter param = this.namedMap.get(rawArg);
                    if (param != null)
                    {
                        StringBuilder paramValue = new StringBuilder();
                        offset++;
                        offset += readString(paramValue, rawArgs, offset);
                        //added named param
                        named.put(param.getName(), paramValue.toString());
                        type.last = PARAM_VALUE;
                    }
                    else
                    {
                        StringBuilder arg = new StringBuilder();
                        offset += readString(arg, rawArgs, offset);
                        flags.add(arg.toString()); // add as flag
                    }
                }
            }
        }
        return type.last;
    }

    @Override
    public CubeContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] rawArgs)
    {
        CubeContext parse = super.parse(command, sender, labels, rawArgs);
        this.readContext(parse, sender.getLocale()); // TODO catch exception and print instead of failing the command
        return parse;
    }
}
