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
package de.cubeisland.engine.core.command.reflected;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.cubeisland.engine.core.command.CubeContext;
import de.cubeisland.engine.core.command.parameterized.Completer;

import static de.cubeisland.engine.core.util.StringUtils.startsWithIgnoreCase;

public class IndexedParameterCompleter implements Completer
{
    private final Completer completer;
    private final Set<String> staticLabels;

    public IndexedParameterCompleter(Completer completer, Set<String> staticLabels)
    {
        this.completer = completer;
        this.staticLabels = staticLabels;
    }

    @Override
    public List<String> complete(CubeContext context, String token)
    {
        List<String> result = new ArrayList<>();
        if (this.completer != null)
        {
            result.addAll(this.completer.complete(context, token));
        }
        for (String staticLabel : staticLabels)
        {
            if (startsWithIgnoreCase(staticLabel, token))
            {
                result.add(staticLabel);
            }
        }
        return result;
    }
}
