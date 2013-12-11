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
package de.cubeisland.engine.multiverse.config;

import java.io.File;

import org.bukkit.World;

import de.cubeisland.engine.configuration.YamlConfiguration;

public class UniverseConfig extends YamlConfiguration
{
    public World mainWorld;

    public boolean keepGameMode = false; // if false can use perm
    public boolean keepFlyMode = false; // if false can use perm

    @Override
    public void onLoaded(File loadedFrom)
    {
        // TODO search for unknown but loaded worlds matching pattern
        // main world is: world
        // matching pattern would be any world with
        // world_something_else

        // TODO when loading UniverseConfigs
        // check for missing universes
    }


}
