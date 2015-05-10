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
package de.cubeisland.engine.module.core.command.readers;

import java.util.ArrayList;
import java.util.List;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.completer.Completer;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.DefaultValue;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;
import de.cubeisland.engine.module.core.Core;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.user.User;

public class CommandSenderReader implements ArgumentReader<CommandSender>, DefaultValue<CommandSender>, Completer
{
    private final Core core;

    public CommandSenderReader(Core core)
    {
        this.core = core;
    }

    @Override
    public CommandSender read(Class type, CommandInvocation invocation) throws ReaderException
    {
        if ("console".equalsIgnoreCase(invocation.currentToken()))
        {
            invocation.consume(1);
            return core.getCommandManager().getConsoleSender();
        }
        return (User)invocation.getManager().getReader(User.class).read(type, invocation);
    }

    @Override
    public CommandSender getDefault(CommandInvocation invocation)
    {
        if (invocation.getCommandSource() instanceof User)
        {
            return (User)invocation.getCommandSource();
        }
        throw new ReaderException("You need to provide a player");
    }

    @Override
    public List<String> getSuggestions(CommandInvocation invocation)
    {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(invocation.getManager().getCompleter(User.class).getSuggestions(invocation));
        if ("console".startsWith(invocation.currentToken().toLowerCase()))
        {
            list.add("console");
        }
        return list;
    }
}
