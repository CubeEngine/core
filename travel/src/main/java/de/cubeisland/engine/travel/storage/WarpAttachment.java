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
package de.cubeisland.engine.travel.storage;

import java.util.HashMap;
import java.util.Map;

import de.cubeisland.engine.core.user.UserAttachment;

public class WarpAttachment extends UserAttachment
{
    final Map<String, Warp> warps;

    public WarpAttachment()
    {
        warps = new HashMap<>();
    }

    /**
     * Will try to find a warp with that name among the warps the user can access
     * Different variations with the prefix is also tried
     *
     * @return the warp if found, else null
     */
    public Warp getWarp(String name)
    {
        if (name == null)
        {
            return null;
        }
        else if (warps.containsKey(name))
        {
            return warps.get(name);
        }
        else if (name.contains(":"))
        {
            return warps.get(name.substring(name.lastIndexOf(":") + 1, name.length()));
        }
        else
        {
            return null;
        }
    }

    /**
     * Will check if getWarp(name) is not null
     */
    public boolean hasWarp(String name)
    {
        return getWarp(name) != null;
    }

    /**
     * Will find direct matches
     */
    public boolean containsWarp(String name)
    {
        return warps.containsKey(name);
    }

    public Map<String, Warp> allWarps()
    {
        return this.warps;
    }

    public void addWarp(String name, Warp warp)
    {
        warps.put(name, warp);
    }

    public void removeWarp(String name)
    {
        warps.remove(name);
    }
}
