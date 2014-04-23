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
package de.cubeisland.engine.core.util.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Villager.Profession;

public class ProfessionMatcher
{
    private final List<String> professions;

    public ProfessionMatcher()
    {
        this.professions = new ArrayList<>();
        for (Profession profession : Profession.values())
        {
            this.professions.add(profession.toString());
        }
    }

    public Profession profession(String name)
    {
        String match = Match.string().matchString(name.toUpperCase(Locale.ENGLISH), this.professions);
        return Profession.valueOf(match);
    }

    public String[] professions()
    {
        return professions.toArray(new String[professions.size()]);
    }
}
