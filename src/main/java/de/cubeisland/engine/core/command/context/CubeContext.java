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
package de.cubeisland.engine.core.command.context;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import de.cubeisland.engine.command.context.CommandContext;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.command.context.ContextParser.Type;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class CubeContext extends CommandContext<CubeCommand>
{
    private CommandSender sender;
    private final Core core;

    public CubeContext(String[] rawArgs, List<String> rawIndexed, Map<String, String> rawNamed, Set<String> flags,
                       Type last, CubeCommand command, List<String> labels)
    {
        super(rawArgs, rawIndexed, rawNamed, flags, last, command, labels);
        this.core = command.getModule().getCore();
    }

    public CubeContext(String[] rawArgs, List<String> rawIndexed, Map<String, String> rawNamed, Set<String> flags,
                       CubeCommand command, List<String> labels)
    {
        this(rawArgs, rawIndexed, rawNamed, flags, Type.ANY, command, labels);
    }


    public CubeContext(String[] strings, CubeCommand command, Stack<String> labels)
    {
        this(strings, Collections.<String>emptyList(), Collections.<String, String>emptyMap(),
             Collections.<String>emptySet(), Type.ANY, command, labels);
    }



    public boolean isSender(Class<? extends CommandSender> type)
    {
        return type.isAssignableFrom(this.sender.getClass());
    }

    public CommandSender getSender()
    {
        return this.sender;
    }




    public Core getCore()
    {
        return this.core;
    }

    public void sendMessage(String message)
    {
        this.sender.sendMessage(message);
    }

    public void sendTranslated(MessageType type, String message, Object... args)
    {
        this.sender.sendTranslated(type, message, args);
    }

    public void sendTranslatedN(MessageType type, int count, String sMessage, String pMessage, Object... args)
    {
        this.sender.sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    public void ensurePermission(Permission permission) throws PermissionDeniedException
    {
        if (!permission.isAuthorized(this.getSender()))
        {
            throw new PermissionDeniedException(permission);
        }
    }

    public void setSender(CommandSender sender) // TODO prevent this beeing public
    {
        this.sender = sender;
    }
}
