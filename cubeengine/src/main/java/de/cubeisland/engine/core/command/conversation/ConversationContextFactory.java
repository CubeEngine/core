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

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.core.command.ArgBounds;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.command.parameterized.CommandFlag;
import de.cubeisland.engine.core.command.parameterized.CommandParameter;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.engine.core.util.formatter.MessageType;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.command.ArgBounds.NO_MAX;

public class ConversationContextFactory extends ParameterizedContextFactory
{
    private final LinkedList<String> args;

    public ConversationContextFactory()
    {
        super(new ArgBounds(0, NO_MAX));
        this.args = new LinkedList<>();
    }

    @Override
    public ParameterizedContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        final Set<String> flags = new THashSet<>();
        final Map<String, Object> params = new THashMap<>();
        if (commandLine.length > 0)
        {
            for (int offset = 0; offset < commandLine.length;)
            {
                if (commandLine[offset].isEmpty())
                {
                    offset++;
                    continue;
                }
                String flag = commandLine[offset].toLowerCase(Locale.ENGLISH); // lowercase flag
                CommandFlag cmdFlag = this.getFlag(flag);
                if (cmdFlag != null) // has flag ?
                {
                    flags.add(cmdFlag.getName()); // added flag
                    offset++;
                    continue;
                } //else named param
                String paramName = commandLine[offset].toLowerCase(Locale.ENGLISH);
                CommandParameter param = this.getParameter(paramName);
                if (param != null && offset + 1 < commandLine.length)
                {
                    StringBuilder paramValue = new StringBuilder();
                    try
                    {
                        offset++;
                        offset += readString(paramValue, commandLine, offset);
                        params.put(param.getName(), ArgumentReader.read(param.getType(), paramValue.toString(), sender));
                    }
                    catch (InvalidArgumentException ex)
                    {
                        sender.sendTranslated(MessageType.NEGATIVE, "Invalid argument for {input}: {}", param.getName(), sender.translate(MessageType.NONE, ex
                            .getMessage(), ex.getMessageArgs()));
                    }
                    continue;
                }
                offset++;
            }
        }
        return new ParameterizedContext(command, sender, labels, this.args, flags, params);
    }
}
