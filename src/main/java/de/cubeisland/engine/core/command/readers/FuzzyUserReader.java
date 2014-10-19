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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.parameter.reader.ArgumentReader;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;

import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

/**
 * Matches exact offline players and online players using * for wildcard
 */
public class FuzzyUserReader implements ArgumentReader<List<User>>
{
    private final Core core;

    public FuzzyUserReader(Core core)
    {
        this.core = core;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> read(ReaderManager manager, Class type, CommandInvocation invocation) throws ReaderException
    {
        ArrayList<User> users = new ArrayList<>();
        if ("*".equals(invocation.currentToken()))
        {
            invocation.consume(1);
            users.addAll(core.getUserManager().getOnlineUsers());
            return users;
        }
        if (invocation.currentToken().contains(","))
        {
            for (List<User> list : ((List<List<User>>)manager.getReader(List.class).read(manager, FuzzyUserReader.class, invocation)))
            {
                users.addAll(list);
            }
            return users;
        }
        String token = invocation.currentToken();
        if (token.contains("*"))
        {
            Pattern pattern = Pattern.compile(token.replace("*", ".*"));
            for (User user : core.getUserManager().getOnlineUsers())
            {
                if (pattern.matcher(user.getName()).matches())
                {
                    users.add(user);
                }
            }
            if (users.isEmpty())
            {
                throw new ReaderException(CubeEngine.getI18n().translate(invocation.getLocale(), NEGATIVE, "Player {user} not found!", token));
            }
            invocation.consume(1);
        }
        else
        {
            users.add((User)manager.read(User.class, User.class, invocation));
        }
        return users;
    }
}
