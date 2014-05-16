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
import java.util.Locale;

import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.exception.InvalidArgumentException;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;

public class UserListReader extends ArgumentReader
{
    @Override
    public List<User> read(String arg, Locale locale) throws InvalidArgumentException
    {
        List<User> users = new ArrayList<>();
        String[] userStrings = StringUtils.explode(",", arg);
        for (String userString : userStrings)
        {
            users.add(ArgumentReader.read(User.class, userString, locale));
        }
        return users;
    }
}
