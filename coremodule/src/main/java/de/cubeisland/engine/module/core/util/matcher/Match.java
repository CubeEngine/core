/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 * <p/>
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.engine.module.core.util.matcher;

import de.cubeisland.engine.module.core.CubeEngine;
import org.spongepowered.api.Game;

public class Match
{
    private final MaterialDataMatcher materialDataMatcher;
    private final MaterialMatcher materialMatcher;
    private final EnchantMatcher enchantMatcher;
    private final ProfessionMatcher professionMatcher;
    private final EntityMatcher entityMatcher;
    private final StringMatcher stringMatcher = new StringMatcher();
    private final TimeMatcher timeMatcher = new TimeMatcher();
    private final WorldMatcher worldMatcher = new WorldMatcher();

    public Match(Game game)
    {
        materialDataMatcher = new MaterialDataMatcher(game);
        materialMatcher = new MaterialMatcher(game);
        enchantMatcher = new EnchantMatcher(game);
        professionMatcher = new ProfessionMatcher(game);
        entityMatcher = new EntityMatcher(game);
    }

    public static MaterialMatcher material()
    {
        return CubeEngine.getCore().getMatcherManager().materialMatcher;
    }

    public static MaterialDataMatcher materialData()
    {
        return CubeEngine.getCore().getMatcherManager().materialDataMatcher;
    }

    public static EnchantMatcher enchant()
    {
        return CubeEngine.getCore().getMatcherManager().enchantMatcher;
    }

    public static ProfessionMatcher profession()
    {
        return CubeEngine.getCore().getMatcherManager().professionMatcher;
    }

    public static EntityMatcher entity()
    {
        return CubeEngine.getCore().getMatcherManager().entityMatcher;
    }

    public static StringMatcher string()
    {
        return CubeEngine.getCore().getMatcherManager().stringMatcher;
    }

    public static TimeMatcher time()
    {
        return CubeEngine.getCore().getMatcherManager().timeMatcher;
    }

    public static WorldMatcher worlds()
    {
        return CubeEngine.getCore().getMatcherManager().worldMatcher;
    }
}
