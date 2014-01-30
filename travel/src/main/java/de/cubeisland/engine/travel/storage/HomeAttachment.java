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

public class HomeAttachment extends UserAttachment
{
    final Map<String, Home> homes = new HashMap<>();

    /**
     * Will try to find a home with that name among the homes the user can access
     * Different variations with the prefix is also tried
     */
    public Home getHome(String name)
    {
        if (name == null)
        {
            return null;
        }
        else if (homes.containsKey(name))
        {
            return homes.get(name);
        }
        else if (name.contains(":"))
        {
            return homes.get(name.substring(name.lastIndexOf(":") + 1, name.length()));
        }
        else
        {
            return null;
        }
    }

    /**
     * Will check if getHome(name) is not null
     */
    public boolean hasHome(String name)
    {
        return getHome(name) != null;
    }

    /**
     * Will find direct matches
     */
    public boolean containsHome(String name)
    {
        return homes.containsKey(name);
    }

    public Map<String, Home> allHomes()
    {
        return this.homes;
    }

    public void addHome(String name, Home home)
    {
        homes.put(name, home);
    }

    public void removeHome(String name)
    {
        homes.remove(name);
    }
}
