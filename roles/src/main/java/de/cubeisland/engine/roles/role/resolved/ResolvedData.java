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
package de.cubeisland.engine.roles.role.resolved;

import de.cubeisland.engine.roles.role.ResolvedDataHolder;
import de.cubeisland.engine.roles.role.Role;

public class ResolvedData
{
    private final ResolvedDataHolder origin;
    private String key;

    public ResolvedData(ResolvedDataHolder origin, String key)
    {
        this.origin = origin;
        this.key = key;
    }

    public ResolvedDataHolder getOrigin()
    {
        return origin;
    }

    public String getKey()
    {
        return key;
    }

    public int getPriorityValue()
    {
        if (origin instanceof Role)
        {
            return ((Role)origin).getPriorityValue();
        }
        return Integer.MAX_VALUE;
    }
}
