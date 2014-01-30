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

import de.cubeisland.engine.core.CubeEngine;

public class Match
{
    private final MaterialDataMatcher materialDataMatcher = new MaterialDataMatcher();
    private final MaterialMatcher materialMatcher = new MaterialMatcher(materialDataMatcher);
    private final EnchantMatcher enchantMatcher = new EnchantMatcher();
    private final ProfessionMatcher professionMatcher = new ProfessionMatcher();
    private final EntityMatcher entityMatcher = new EntityMatcher();
    private final StringMatcher stringMatcher = new StringMatcher();
    private final TimeMatcher timeMatcher = new TimeMatcher();
    private final WorldMatcher worldMatcher = new WorldMatcher();

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
