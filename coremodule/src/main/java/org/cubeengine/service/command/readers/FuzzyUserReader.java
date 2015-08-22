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
package org.cubeengine.service.command.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.parameter.reader.ArgumentReader;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;

import org.cubeengine.service.command.TranslatedReaderException;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.user.User;
import org.cubeengine.service.user.UserManager;

import static org.cubeengine.service.i18n.formatter.MessageType.NEGATIVE;
import static java.util.stream.Collectors.toList;

/**
 * Matches exact offline players and online players using * for wildcard
 */
public class FuzzyUserReader implements ArgumentReader<List<User>>
{

    private final UserManager um;
    private final I18n i18n;

    public FuzzyUserReader(UserManager um, I18n i18n)
    {
        this.um = um;
        this.i18n = i18n;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<User> read(Class type, CommandInvocation invocation) throws ReaderException
    {
        ArrayList<User> users = new ArrayList<>();
        if ("*".equals(invocation.currentToken()))
        {
            invocation.consume(1);
            users.addAll(um.getOnlineUsers());
            return users;
        }
        if (invocation.currentToken().contains(","))
        {
            ((List<List<User>>)invocation.getManager().getReader(List.class).read(FuzzyUserReader.class, invocation))
                .forEach(users::addAll);
            return users;
        }
        String token = invocation.currentToken();
        if (token.contains("*"))
        {
            Pattern pattern = Pattern.compile(token.replace("*", ".*"));
            users.addAll(um.getOnlineUsers().stream()
                           .filter(user -> pattern.matcher(user.getName()).matches())
                           .collect(toList()));
            if (users.isEmpty())
            {
                throw new TranslatedReaderException(i18n.translate(invocation.getLocale(), NEGATIVE, "Player {user} not found!", token));
            }
            invocation.consume(1);
        }
        else
        {
            users.add((User)invocation.getManager().read(User.class, User.class, invocation));
        }
        return users;
    }
}
