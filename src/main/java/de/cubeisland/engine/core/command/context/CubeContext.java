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

public class CubeContext extends CommandContext<CubeCommand, CommandSender>
{
    private final Core core;

    public CubeContext(String[] rawArgs, List<String> rawIndexed, Map<String, String> rawNamed, Set<String> flags,
                       Type last, CubeCommand command, List<String> labels, CommandSender sender)
    {
        super(rawArgs, rawIndexed, rawNamed, flags, last, command, labels, sender);
        this.core = command.getModule().getCore();
    }

    public CubeContext(String[] rawArgs, List<String> rawIndexed, Map<String, String> rawNamed, Set<String> flags,
                       CubeCommand command, List<String> labels, CommandSender sender)
    {
        this(rawArgs, rawIndexed, rawNamed, flags, Type.ANY, command, labels, sender);
    }


    public CubeContext(String[] strings, CubeCommand command, Stack<String> labels, CommandSender sender)
    {
        this(strings, Collections.<String>emptyList(), Collections.<String, String>emptyMap(),
             Collections.<String>emptySet(), Type.ANY, command, labels, sender);
    }

    public Core getCore()
    {
        return this.core;
    }

    public void sendMessage(String message)
    {
        this.getSource().sendMessage(message);
    }

    public void sendTranslated(MessageType type, String message, Object... args)
    {
        this.getSource().sendTranslated(type, message, args);
    }

    public void sendTranslatedN(MessageType type, int count, String sMessage, String pMessage, Object... args)
    {
        this.getSource().sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    public void ensurePermission(Permission permission) throws PermissionDeniedException
    {
        if (!permission.isAuthorized(this.getSource()))
        {
            throw new PermissionDeniedException(permission);
        }
    }
}
