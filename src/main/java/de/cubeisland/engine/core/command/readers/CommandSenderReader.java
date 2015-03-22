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
package de.cubeisland.engine.core.command.readers;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.ProviderManager;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.DefaultValue;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.user.User;

public class CommandSenderReader implements ArgumentReader<CommandSender>, DefaultValue<CommandSender>
{
    @Override
    public CommandSender read(ProviderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        if ("console".equalsIgnoreCase(invocation.currentToken()))
        {
            invocation.consume(1);
            return CubeEngine.getCore().getCommandManager().getConsoleSender();
        }
        return (User)manager.getReader(User.class).read(manager, type, invocation);
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
}
