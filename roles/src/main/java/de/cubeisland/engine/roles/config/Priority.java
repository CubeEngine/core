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
package de.cubeisland.engine.roles.config;

import gnu.trove.map.hash.THashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class Priority
{
    private static final TIntObjectHashMap<Priority> prio = new TIntObjectHashMap<>();
    private static final THashMap<String, Priority> prioNames = new THashMap<>();
    public static final Priority ABSULTEZERO = new Priority(-273, "ABSULTEZERO");
    public static final Priority MINIMUM = new Priority(0, "MINIMUM");
    public static final Priority LOWEST = new Priority(125, "LOWEST");
    public static final Priority LOWER = new Priority(250, "LOWER");
    public static final Priority LOW = new Priority(375, "LOW");
    public static final Priority NORMAL = new Priority(500, "NORMAL");
    public static final Priority HIGH = new Priority(625, "HIGH");
    public static final Priority HIGHER = new Priority(750, "HIGHER");
    public static final Priority HIGHEST = new Priority(1000, "HIGHEST");
    public static final Priority OVER9000 = new Priority(9001, "OVER9000");
    public final int value;
    public final String name;

    private Priority(int value, String name)
    {
        this.value = value;
        this.name = name;
        if (name != null)
        {
            prioNames.put(name, this);
        }
        prio.put(value, this);
    }

    private Priority(int value)
    {
        this(value, null);
    }

    public static Priority getByValue(int value)
    {
        Priority p = prio.get(value);
        if (p == null)
        {
            p = new Priority(value);
        }
        return p;
    }

    @Override
    public String toString()
    {
        return this.name == null ? String.valueOf(this.value) : this.name;
    }

    public static Priority getByName(String name)
    {
        return prioNames.get(name);
    }
}
