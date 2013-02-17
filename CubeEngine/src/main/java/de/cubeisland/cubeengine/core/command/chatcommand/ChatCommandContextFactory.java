package de.cubeisland.cubeengine.core.command.chatcommand;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.exception.IncorrectUsageException;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import de.cubeisland.cubeengine.core.command.parameterized.CommandFlag;
import de.cubeisland.cubeengine.core.command.parameterized.CommandParameter;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContextFactory;
import de.cubeisland.cubeengine.core.command.sender.CommandSender;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class ChatCommandContextFactory extends ParameterizedContextFactory
{
    @Override
    public ChatCommandContext parse(CubeCommand command, CommandSender sender, Stack<String> labels, String[] commandLine)
    {
        final Set<String> flags = new THashSet<String>();
        final Map<String, Object> params = new THashMap<String, Object>();
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
                    try
                    {
                        offset++;
                        StringBuilder paramValue = new StringBuilder();
                        offset += readString(paramValue, commandLine, offset);
                        params.put(param.getName(), ArgumentReader.read(param.getType(), paramValue.toString()));
                    }
                    catch (InvalidArgumentException ex)
                    {
                        throw new IncorrectUsageException(); // TODO message.
                    }
                    continue;
                }
                offset++;
            }
        }
        return new ChatCommandContext(command, sender, labels, flags, params);
    }
}
