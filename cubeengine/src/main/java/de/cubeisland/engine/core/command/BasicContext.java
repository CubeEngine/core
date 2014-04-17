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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.bukkit.Bukkit;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class BasicContext implements CommandContext
{
    private final Core core;
    private final CubeCommand command;
    private final CommandSender sender;
    private final Stack<String> labels;
    private final List<String> args;
    private final int argCount;

    public BasicContext(CubeCommand command, CommandSender sender, Stack<String> labels, List<String> args)
    {
        this.core = command.getModule().getCore();
        this.command = command;
        this.sender = sender;
        this.labels = labels;
        this.args = args;
        this.argCount = args.size();
    }

    @Override
    public Core getCore()
    {
        return this.core;
    }

    @Override
    public CubeCommand getCommand()
    {
        return this.command;
    }

    @Override
    public boolean isSender(Class<? extends CommandSender> type)
    {
        return type.isAssignableFrom(this.sender.getClass());
    }

    @Override
    public CommandSender getSender()
    {
        return this.sender;
    }

    @Override
    public String getLabel()
    {
        return this.labels.peek();
    }

    @Override
    public Stack<String> getLabels()
    {
        Stack<String> newStack = new Stack<>();
        newStack.addAll(this.labels);
        return newStack;
    }

    @Override
    public void sendMessage(String message)
    {
        this.sender.sendMessage(message);
    }

    @Override
    public void sendTranslated(MessageType type, String message, Object... args)
    {
        this.sender.sendTranslated(type, message, args);
    }

    @Override
    public void sendTranslatedN(MessageType type, int count, String sMessage, String pMessage, Object... args)
    {
        this.sender.sendTranslatedN(type, count, sMessage, pMessage, args);
    }

    @Override
    public boolean hasArgs()
    {
        return this.argCount > 0;
    }

    public List<String> getArgs()
    {
        return new ArrayList<>(this.args);
    }

    @Override
    public boolean hasArg(int i)
    {
        return i >= 0 && i < this.argCount;
    }

    @Override
    public int getArgCount()
    {
        return this.argCount;
    }

    @Override
    public <T> T getArg(int index, Class<T> type)
    {
        try
        {
            return ArgumentReader.read(type, this.args.get(index), this.getSender());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public <T> T getArg(int index, Class<T> type, T def)
    {
        final T value = this.getArg(index, type);
        return value == null ? def : value;
    }

    @Override
    public String getString(int i)
    {
        return this.getArg(i, String.class);
    }

    @Override
    public String getStrings(int from)
    {
        if (!this.hasArg(from))
        {
            return null;
        }
        StringBuilder sb = new StringBuilder(this.getString(from));
        while (this.hasArg(++from))
        {
            sb.append(" ").append(this.getString(from));
        }
        return sb.toString();
    }

    @Override
    public String getString(int i, String def)
    {
        return this.getArg(i, String.class, def);
    }

    @Override
    public User getUser(int i)
    {
        return this.getArg(i, User.class);
    }

    @Override
    public void ensurePermission(Permission permission) throws PermissionDeniedException
    {
        if (!permission.isAuthorized(this.getSender()))
        {
            throw new PermissionDeniedException(permission);
        }
    }
}
