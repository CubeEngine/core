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
package org.cubeengine.module.core.util.matcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import de.cubeisland.engine.modularity.asm.marker.ServiceProvider;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.type.Profession;

@ServiceProvider(ProfessionMatcher.class)
public class ProfessionMatcher
{
    private final Map<String, Profession> professions = new HashMap<>();
    @Inject private StringMatcher stringMatcher;

    @Inject
    public ProfessionMatcher(Game game)
    {
        for (Profession profession : game.getRegistry().getAllOf(Profession.class))
        {
            this.professions.put(profession.getName().toLowerCase(), profession);
        }
    }

    public Profession profession(String name)
    {
        return professions.get(stringMatcher.matchString(name.toLowerCase(Locale.ENGLISH), this.professions.keySet()));
    }

    public String[] professions()
    {
        return professions.keySet().toArray(new String[professions.size()]);
    }
}
