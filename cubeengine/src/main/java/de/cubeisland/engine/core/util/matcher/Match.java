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

    private MaterialMatcher materialMatcher;
    private MaterialDataMatcher materialDataMatcher;
    private EnchantMatcher enchantMatcher;
    private ProfessionMatcher professionMatcher;
    private EntityMatcher entityMatcher;
    private StringMatcher stringMatcher;
    private TimeMatcher timeMatcher;
    private WorldMatcher worldMatcher;

    public Match()
    {
        this.materialDataMatcher = new MaterialDataMatcher();
        this.materialMatcher = new MaterialMatcher(materialDataMatcher);
        this.enchantMatcher = new EnchantMatcher();
        this.professionMatcher = new ProfessionMatcher();
        this.entityMatcher = new EntityMatcher();
        this.stringMatcher = new StringMatcher();
        this.timeMatcher = new TimeMatcher();
        this.worldMatcher = new WorldMatcher();
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
