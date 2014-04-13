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
package de.cubeisland.engine.core.command.parameterized.completer;

import java.util.ArrayList;
import java.util.List;

import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.command.parameterized.ParameterizedTabContext;
import de.cubeisland.engine.core.user.User;

public class PlayerListCompleter implements Completer
{
    @Override
    public List<String> complete(ParameterizedTabContext context, String token)
    {
        List<String> result = new ArrayList<>();
        String lastToken = token;
        String firstTokens = "";
        if (lastToken.contains(","))
        {
            firstTokens = lastToken.substring(0, lastToken.lastIndexOf(",")+1);
            lastToken = lastToken.substring(lastToken.lastIndexOf(",")+1,lastToken.length());
        }
        if (lastToken.startsWith("!"))
        {
            lastToken = lastToken.substring(1, lastToken.length());
            firstTokens += "!";
        }

        for (User user : context.getCore().getUserManager().getLoadedUsers())
        {
            if (user.getName().startsWith(lastToken))
            {
                if (!token.contains(user.getName()+","))
                {
                    result.add(firstTokens + user.getName());
                }
            }
        }
        return result;
    }
}
