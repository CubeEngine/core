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
import java.util.Arrays;
import java.util.List;

import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.parameterized.Completer;
import de.cubeisland.engine.core.util.StringUtils;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

public abstract class SimpleListCompleter implements Completer
{
    private final String[] strings;

    protected SimpleListCompleter(String... strings)
    {
        this.strings = strings;
    }

    @Override
    public List<String> complete(CubeContext context, String token)
    {
        List<String> tokens = Arrays.asList(StringUtils.explode(",", token));
        String lastToken = token.substring(token.lastIndexOf(",")+1,token.length()).toUpperCase();
        List<String> offers = new ArrayList<>();
        for (String string : this.strings)
        {
            if (startsWithIgnoreCase(string, lastToken) && !tokens.contains(string))
            {
                offers.add(token.substring(0, token.lastIndexOf(",") + 1) + string);
            }
        }
        return offers;
    }
}
