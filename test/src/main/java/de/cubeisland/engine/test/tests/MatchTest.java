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
package de.cubeisland.engine.test.tests;

import de.cubeisland.engine.core.util.matcher.Match;

public class MatchTest extends Test
{
    private final de.cubeisland.engine.test.Test module;

    public MatchTest(de.cubeisland.engine.test.Test module)
    {
        this.module = module;
    }
    
    @Override
    public void onEnable()
    {
        module.getLog().debug(String.valueOf(Match.enchant().enchantment("infinity")));
        module.getLog().debug(String.valueOf(Match.enchant().enchantment("infini")));
        module.getLog().debug(String.valueOf(Match.enchant().enchantment("hablablubb")) + " is null");
        module.getLog().debug(String.valueOf(Match.enchant().enchantment("protect")));
        module.getLog().debug(String.valueOf(Match.material().itemStack("stone").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("stoned").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("hablablubb")) + " is null");
        module.getLog().debug(String.valueOf(Match.material().itemStack("wool:red").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("35").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("35:15").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("35:red").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("wood:birch").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("leves:pine").serialize()));
        module.getLog().debug(String.valueOf(Match.material().itemStack("spawnegg:pig").serialize()));
        module.getLog().debug(String.valueOf(Match.entity().any("pig")));
        module.getLog().debug(String.valueOf(Match.entity().monster("zombi")));
        module.getLog().debug(String.valueOf(Match.entity().friendlyMob("shep")));
        module.getLog().debug(String.valueOf(Match.entity().friendlyMob("ghast")) + " is null");
        this.setSuccess(true);
    }
}
