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
package de.cubeisland.engine.baumguard;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import de.cubeisland.engine.core.config.YamlConfiguration;
import de.cubeisland.engine.core.config.annotations.Option;

import static de.cubeisland.engine.baumguard.storage.GuardType.PRIVATE;

public class GuardConfig extends YamlConfiguration
{
    //@Option()
    //public int

    public boolean openIronDoorWithClick = false;

    public boolean protectEntityFromEnvironementalDamage = false;
    // List of DefaultGuardConfigs
    @Option("protections")
    public List<DefaultGuardConfiguration> protections;

    // TODO allow keybooks
    // TODO allow masterkeyBooks
    // limit protection count#
    // TODO globally disable protection from block destruction / left/right-click / Explosion / EntityBreak/Interact etc.
    // TODO protect only when online AND OR only when offline

    // TODO autoclose duration in sec
    @Override
    public void onLoaded(Path loadFrom)
    {
        if (protections == null || protections.isEmpty())
        {
            protections = new ArrayList<>();
            protections.add(new DefaultGuardConfiguration(Material.CHEST).autoProtect(PRIVATE));
            protections.add(new DefaultGuardConfiguration(Material.TRAPPED_CHEST).autoProtect(PRIVATE));
            protections.add(new DefaultGuardConfiguration(Material.FURNACE));
            protections.add(new DefaultGuardConfiguration(Material.DISPENSER));
            protections.add(new DefaultGuardConfiguration(Material.SIGN_POST));
            protections.add(new DefaultGuardConfiguration(Material.WALL_SIGN));
            protections.add(new DefaultGuardConfiguration(Material.WOODEN_DOOR));
            protections.add(new DefaultGuardConfiguration(Material.IRON_DOOR_BLOCK));
            protections.add(new DefaultGuardConfiguration(Material.TRAP_DOOR));
            protections.add(new DefaultGuardConfiguration(Material.FENCE_GATE));
            protections.add(new DefaultGuardConfiguration(EntityType.HORSE));
        }
    }
}

